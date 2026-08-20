package danger.orespawn.delight.interact;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import danger.orespawn.ModSounds;
import danger.orespawn.entity.Alosaurus;
import danger.orespawn.entity.AppleCow;
import danger.orespawn.entity.Baryonyx;
import danger.orespawn.entity.Boyfriend;
import danger.orespawn.entity.EnchantedAppleCow;
import danger.orespawn.entity.EntityStinky;
import danger.orespawn.entity.Girlfriend;
import danger.orespawn.entity.GoldenAppleCow;
import danger.orespawn.entity.LaserBall;
import danger.orespawn.entity.TRex;
import danger.orespawn.delight.OreSpawnDelight;
import danger.orespawn.delight.items.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * All living-world gameplay handlers of the absurd tier (spec: ABSURD-21.json).
 * Every public handler body is wrapped in try/catch with log-once so a single
 * broken interaction can never crash a tick loop or spam the log.
 *
 * The absurd-tier food ITEMS this class reacts to are registered by the items
 * module; they are resolved from the item registry by frozen id at event time
 * (registries are frozen long before any gameplay event fires), so this unit
 * stays compile-independent of that module and degrades gracefully - a missing
 * id logs one warning and the handler goes inert.
 *
 * OreSpawn API notes (all javap-verified against orespawn-1.21.1-1.0.0-beta.3):
 * - LaserBall extends ThrowableProjectile and its tick() calls super.tick(),
 *   so NeoForge's ProjectileImpactEvent fires for it; laser kills carry
 *   damageSources().thrown(laserBall, owner), so getDirectEntity() is the ball.
 * - EntityStinky's burp/drop methods (dropItemFront etc.) are private, so the
 *   chili trigger SIMULATES his canon front-burp (fart-family sound + 1 coal
 *   dropped in front, canon ENT-S-033) instead of calling into his AI.
 * - Girlfriend/Boyfriend are TamableAnimals; their registered happy voice
 *   lines (O_HAPPY1-7, B_HAPPY1-8) are public DeferredHolders in ModSounds.
 * - AppleCow / GoldenAppleCow / EnchantedAppleCow are three separate Cow
 *   subclasses; matching is instanceof-based, which stays safe even if a
 *   config phase disables their registrations.
 */
public class InteractionsHandler {

    // --- entity persistent-data keys (namespaced; survive save/reload via NeoForgeData) ---
    private static final String TRUCE_UNTIL = "orespawn_delight:TruceUntil";
    private static final String TRUCE_FEEDER = "orespawn_delight:TruceFeeder";
    private static final String COOKBACK_AT = "orespawn_delight:CookbackAt";
    private static final String MASH_READY_AT = "orespawn_delight:MashReadyAt";
    /** Deliberately un-namespaced: "HiccupUntil" is the cross-module contract name from the build plan. */
    private static final String HICCUP_UNTIL = "HiccupUntil";
    private static final String HICCUP_NEXT = "orespawn_delight:NextHiccup";

    // --- frozen item ids this layer reacts to ---
    private static final String RAY_SEARED_STEAK = "ray_seared_steak";
    private static final String TNT_ROCK_CANDY = "tnt_rock_candy";
    private static final String BURP_BACK_CHILI = "burp_back_chili";
    private static final String DINO_DOG_GRANDE = "dino_dog_grande";
    private static final String HEART_BOX_CHOCOLATES = "heart_box_chocolates";
    private static final String CARAMEL_MASH = "caramel_mash";
    private static final String JUMPY_BUG_GUMMIES = "jumpy_bug_gummies";

    // --- timings (ticks), per ABSURD-21.json ---
    private static final int BURP_FUSE = 40;                  // ~2s after eating TNT rock candy
    private static final int WIND_STRENGTH_DURATION = 1200;   // wind_charged + strength I, 60s
    private static final int TREX_TRUCE = 1200;               // ~60s truce
    private static final int SMALL_DINO_TRUCE = 600;          // alosaurus/baryonyx: "shorter truce"
    private static final int REGEN_DURATION = 600;            // regeneration I 30s for the gift giver
    private static final int COOKBACK_MIN = 2400;             // 2 min
    private static final int COOKBACK_MAX = 6000;             // 5 min
    private static final int COOKBACK_RETRY = 200;            // owner away: try again in 10s
    private static final double COOKBACK_RANGE_SQ = 16.0 * 16.0;
    private static final int GOLDEN_MASH_COOLDOWN = 6000;     // 5 min
    private static final int ENCHANTED_MASH_COOLDOWN = 24000; // 20 min
    private static final int HICCUP_WINDOW = 1200;            // "short" hiccups tag: 60s
    private static final int HICCUP_GAP_MIN = 200;            // hiccup every 200-300t
    private static final int HICCUP_GAP_SPREAD = 100;
    private static final double HICCUP_POP = 0.35;
    private static final double LASER_SEAR_RADIUS = 1.75;     // beef item pickup range around a laser impact

    /** Stinky's registered burp/fart voice bank (jar-verified public holders). */
    private static final List<DeferredHolder<SoundEvent, SoundEvent>> FARTS = List.of(
            ModSounds.FART1, ModSounds.FART2, ModSounds.FART3, ModSounds.FART4, ModSounds.FART5,
            ModSounds.FART6, ModSounds.FART7, ModSounds.FART8, ModSounds.FART9);

    private static final List<DeferredHolder<SoundEvent, SoundEvent>> GIRLFRIEND_HAPPY = List.of(
            ModSounds.O_HAPPY1, ModSounds.O_HAPPY2, ModSounds.O_HAPPY3, ModSounds.O_HAPPY4,
            ModSounds.O_HAPPY5, ModSounds.O_HAPPY6, ModSounds.O_HAPPY7);

    private static final List<DeferredHolder<SoundEvent, SoundEvent>> BOYFRIEND_HAPPY = List.of(
            ModSounds.B_HAPPY1, ModSounds.B_HAPPY2, ModSounds.B_HAPPY3, ModSounds.B_HAPPY4,
            ModSounds.B_HAPPY5, ModSounds.B_HAPPY6, ModSounds.B_HAPPY7, ModSounds.B_HAPPY8);

    /** Tier 1-3 dishes a companion may cook back (all shipped in this addon's ModItems). */
    private static final List<DeferredItem<Item>> SHIPPED_DISHES = List.of(
            ModItems.GRILLED_CHEESE, ModItems.DINO_STEW, ModItems.MOBZILLA_BURGER,
            ModItems.CRAB_ROLL, ModItems.SHARK_SANDWICH, ModItems.PEACOCK_CLUB_SANDWICH,
            ModItems.STRAWBERRY_SHORTCAKE, ModItems.WHALE_CHOWDER, ModItems.OSTRICH_EGG_OMELETTE,
            ModItems.INK_BLACK_PASTA, ModItems.FROG_LEG_PLATTER, ModItems.CRABBY_PATTY_DELUXE);

    // --- defensive plumbing ---

    private static final Set<String> LOGGED = ConcurrentHashMap.newKeySet();

    /** Server-thread-only delayed task queue, drained by onServerTick. */
    private static final List<ScheduledTask> SCHEDULED = new ArrayList<>();

    private record ScheduledTask(long runAtTick, Runnable action) {}

    private static void logOnce(String key, Throwable t) {
        if (LOGGED.add(key)) {
            OreSpawnDelight.LOGGER.error(
                    "[interactions] handler '{}' failed; muting further reports of this failure", key, t);
        }
    }

    private static void logMissingOnce(String itemId) {
        if (LOGGED.add("missing_item:" + itemId)) {
            OreSpawnDelight.LOGGER.warn(
                    "[interactions] item '{}:{}' is not registered (items module not landed yet?); "
                            + "its interaction stays inert", OreSpawnDelight.MODID, itemId);
        }
    }

    static void schedule(MinecraftServer server, int delayTicks, Runnable action) {
        SCHEDULED.add(new ScheduledTask(server.getTickCount() + delayTicks, action));
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (SCHEDULED.isEmpty()) {
            return;
        }
        long now = event.getServer().getTickCount();
        // Collect due tasks first so actions may safely schedule follow-ups.
        List<ScheduledTask> due = null;
        Iterator<ScheduledTask> it = SCHEDULED.iterator();
        while (it.hasNext()) {
            ScheduledTask task = it.next();
            if (now >= task.runAtTick()) {
                it.remove();
                if (due == null) {
                    due = new ArrayList<>();
                }
                due.add(task);
            }
        }
        if (due != null) {
            for (ScheduledTask task : due) {
                try {
                    task.action().run();
                } catch (Throwable t) {
                    logOnce("scheduled_task", t);
                }
            }
        }
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        SCHEDULED.clear();
    }

    // --- frozen-id item helpers ---

    /** True when the stack is our mod's item with the given frozen id (no registry lookup needed). */
    private static boolean is(ItemStack stack, String id) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key.getNamespace().equals(OreSpawnDelight.MODID) && key.getPath().equals(id);
    }

    /** Resolves one of our frozen ids from the registry, or null (log-once) if absent. */
    private static Item osdItem(String id) {
        Item item = BuiltInRegistries.ITEM
                .getOptional(ResourceLocation.fromNamespaceAndPath(OreSpawnDelight.MODID, id))
                .orElse(null);
        if (item == null) {
            logMissingOnce(id);
        }
        return item;
    }

    // =====================================================================
    // (1) RAY-SEARED STEAK - cooked by shooting it with the Ray Gun
    // =====================================================================

    /**
     * A laser_ball impact sears any dropped raw beef near the hit point.
     * Projectiles cannot collide with ItemEntities (they are not pickable), so
     * the conversion sweeps a small radius around the impact instead - shoot
     * the ground next to your beef and dinner is served.
     */
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        try {
            if (!(event.getProjectile() instanceof LaserBall laser)) {
                return;
            }
            Level level = laser.level();
            if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
                return;
            }
            Vec3 hit = event.getRayTraceResult().getLocation();
            List<ItemEntity> beefDrops = serverLevel.getEntitiesOfClass(ItemEntity.class,
                    new AABB(hit, hit).inflate(LASER_SEAR_RADIUS),
                    drop -> drop.isAlive() && drop.getItem().is(Items.BEEF));
            if (beefDrops.isEmpty()) {
                return;
            }
            Item steak = osdItem(RAY_SEARED_STEAK);
            if (steak == null) {
                return;
            }
            for (ItemEntity drop : beefDrops) {
                drop.setItem(new ItemStack(steak, drop.getItem().getCount()));
                zap(serverLevel, drop.getX(), drop.getY() + 0.2, drop.getZ());
            }
        } catch (Throwable t) {
            logOnce("projectile_impact", t);
        }
    }

    /** A cow killed by a laser_ball drops its beef pre-seared. */
    public static void onLivingDrops(LivingDropsEvent event) {
        try {
            if (!(event.getEntity() instanceof Cow cow)) {
                return;
            }
            if (!(event.getSource().getDirectEntity() instanceof LaserBall)) {
                return;
            }
            if (!(cow.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            Item steak = null;
            for (ItemEntity drop : event.getDrops()) {
                if (drop.getItem().is(Items.BEEF)) {
                    if (steak == null) {
                        steak = osdItem(RAY_SEARED_STEAK);
                        if (steak == null) {
                            return;
                        }
                    }
                    drop.setItem(new ItemStack(steak, drop.getItem().getCount()));
                }
            }
            if (steak != null) {
                zap(serverLevel, cow.getX(), cow.getY(0.5), cow.getZ());
            }
        } catch (Throwable t) {
            logOnce("living_drops", t);
        }
    }

    private static void zap(ServerLevel level, double x, double y, double z) {
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 12, 0.25, 0.25, 0.25, 0.15);
        level.playSound(null, x, y, z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.NEUTRAL, 0.5F, 1.8F);
    }

    // =====================================================================
    // (2/3/8) finish-eat triggers: TNT rock candy, chili, gummies
    // =====================================================================

    public static void onFinishEat(LivingEntityUseItemEvent.Finish event) {
        try {
            LivingEntity eater = event.getEntity();
            if (eater.level().isClientSide) {
                return;
            }
            ItemStack stack = event.getItem();
            if (is(stack, TNT_ROCK_CANDY)) {
                tntCandyFuse(eater);
            } else if (is(stack, BURP_BACK_CHILI)) {
                chiliBurp(eater);
            } else if (is(stack, JUMPY_BUG_GUMMIES)) {
                startHiccups(eater);
            }
        } catch (Throwable t) {
            logOnce("finish_eat", t);
        }
    }

    /**
     * TNT ROCK CANDY: ~2s after eating, a harmless burp explosion (spec:
     * "sound + smoke + shove only"). Level.explode at power 1.5, fire=false,
     * ExplosionInteraction.NONE - with the wind charge's public damage
     * calculator so entities are shoved but take zero damage (a bare NONE
     * explosion would still hurt them). Then wind_charged + strength I, 60s.
     */
    private static void tntCandyFuse(LivingEntity eater) {
        MinecraftServer server = eater.getServer();
        if (server == null) {
            return;
        }
        schedule(server, BURP_FUSE, () -> {
            if (!eater.isAlive()) {
                return;
            }
            Level level = eater.level();
            level.playSound(null, eater.getX(), eater.getY(), eater.getZ(),
                    SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1.0F, 0.6F);
            level.explode(eater, null, AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR,
                    eater.getX(), eater.getY(0.5), eater.getZ(), 1.5F, false,
                    Level.ExplosionInteraction.NONE);
            eater.addEffect(new MobEffectInstance(MobEffects.WIND_CHARGED, WIND_STRENGTH_DURATION, 0));
            eater.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, WIND_STRENGTH_DURATION, 0));
        });
    }

    /**
     * BURP-BACK CHILI (eaten yourself): burp + smoke, and a 1-in-3 chance to
     * burp up one coal at your feet - exactly Stinky's canon front-burp.
     * (The fire_resistance food effect ships on the item itself.)
     */
    private static void chiliBurp(LivingEntity eater) {
        Level level = eater.level();
        level.playSound(null, eater.getX(), eater.getY(), eater.getZ(),
                SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1.0F, 0.8F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    eater.getX(), eater.getEyeY() - 0.2, eater.getZ(), 8, 0.2, 0.15, 0.2, 0.02);
        }
        if (eater.getRandom().nextInt(3) == 0) {
            level.playSound(null, eater.getX(), eater.getY(), eater.getZ(),
                    randomSound(FARTS, eater.getRandom()), SoundSource.PLAYERS, 0.8F, 1.2F);
            ItemEntity coal = new ItemEntity(level,
                    eater.getX(), eater.getY() + 0.2, eater.getZ(), new ItemStack(Items.COAL));
            coal.setDeltaMovement(0.0, 0.1, 0.0);
            coal.setDefaultPickUpDelay();
            level.addFreshEntity(coal);
        }
    }

    /** JUMPY BUG GUMMIES: arm the (shared-contract) HiccupUntil player tag. */
    private static void startHiccups(LivingEntity eater) {
        CompoundTag data = eater.getPersistentData();
        long until = eater.level().getGameTime() + HICCUP_WINDOW;
        data.putLong(HICCUP_UNTIL, Math.max(data.getLong(HICCUP_UNTIL), until));
    }

    // =====================================================================
    // (8) HICCUPS ticker
    // =====================================================================

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        try {
            Player player = event.getEntity();
            if (player.level().isClientSide) {
                return;
            }
            CompoundTag data = player.getPersistentData();
            if (!data.contains(HICCUP_UNTIL)) {
                return;
            }
            long now = player.level().getGameTime();
            if (now >= data.getLong(HICCUP_UNTIL)) {
                data.remove(HICCUP_UNTIL);
                data.remove(HICCUP_NEXT);
                return;
            }
            long next = data.getLong(HICCUP_NEXT);
            if (next == 0L || next > now + HICCUP_GAP_MIN + HICCUP_GAP_SPREAD) {
                // First hiccup (or stale schedule after e.g. a dimension hop).
                data.putLong(HICCUP_NEXT, now + HICCUP_GAP_MIN + player.getRandom().nextInt(HICCUP_GAP_SPREAD + 1));
                return;
            }
            if (now < next) {
                return;
            }
            data.putLong(HICCUP_NEXT, now + HICCUP_GAP_MIN + player.getRandom().nextInt(HICCUP_GAP_SPREAD + 1));
            // Hic! A short, high burp plus a 0.35-block upward pop.
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.4F, 1.8F);
            player.setDeltaMovement(player.getDeltaMovement().add(0.0, HICCUP_POP, 0.0));
            player.hurtMarked = true; // sync the server-side velocity kick to the client
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.POOF,
                        player.getX(), player.getEyeY() - 0.1, player.getZ(), 2, 0.1, 0.1, 0.1, 0.01);
            }
        } catch (Throwable t) {
            logOnce("player_tick", t);
        }
    }

    // =====================================================================
    // (3/5/6/7) right-click feeding: chili->Stinky, dino dog, chocolates, mash
    // =====================================================================

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        try {
            ItemStack stack = event.getItemStack();
            if (stack.isEmpty()) {
                return;
            }
            Entity target = event.getTarget();
            if (is(stack, BURP_BACK_CHILI)
                    && target instanceof EntityStinky stinky && stinky.isTame()) {
                cancelAndRun(event, () -> feedStinky(event, stinky));
            } else if (is(stack, DINO_DOG_GRANDE) && target instanceof Mob dino
                    && (dino instanceof TRex || dino instanceof Alosaurus || dino instanceof Baryonyx)) {
                cancelAndRun(event, () -> feedDino(event, dino));
            } else if (is(stack, HEART_BOX_CHOCOLATES) && target instanceof TamableAnimal companion
                    && (companion instanceof Girlfriend || companion instanceof Boyfriend)) {
                // Only YOUR tamed companion accepts the gift; anyone else's falls
                // through to the mod's own (canon jealousy) interaction.
                if (companion.isTame() && companion.isOwnedBy(event.getEntity())) {
                    cancelAndRun(event, () -> giftChocolates(event, companion));
                }
            } else if (is(stack, CARAMEL_MASH) && target instanceof Cow cow
                    && (cow instanceof AppleCow || cow instanceof GoldenAppleCow
                            || cow instanceof EnchantedAppleCow)) {
                cancelAndRun(event, () -> feedAppleCow(event, cow));
            }
        } catch (Throwable t) {
            logOnce("entity_interact", t);
        }
    }

    /** Cancels with the vanilla sided-success result; runs the action server-side only. */
    private static void cancelAndRun(PlayerInteractEvent.EntityInteract event, Runnable serverAction) {
        boolean clientSide = event.getLevel().isClientSide;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(clientSide));
        if (!clientSide) {
            serverAction.run();
        }
    }

    /**
     * (3) Chili served to a tamed Stinky: he devours it, then fires 2-4 burp
     * cycles. His drop methods are private (javap-verified), so each cycle
     * simulates the canon front-burp: fart-bank sound + one coal spat out in
     * front of him (ENT-S-033: front burp = coal).
     */
    private static void feedStinky(PlayerInteractEvent.EntityInteract event, EntityStinky stinky) {
        Player player = event.getEntity();
        Level level = stinky.level();
        event.getItemStack().consume(1, player);
        level.playSound(null, stinky.getX(), stinky.getY(), stinky.getZ(),
                SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 1.0F, 0.8F);
        MinecraftServer server = level.getServer();
        if (server == null) {
            return;
        }
        int cycles = 2 + stinky.getRandom().nextInt(3);
        for (int i = 0; i < cycles; i++) {
            schedule(server, 10 + i * 15, () -> stinkyBurpCycle(stinky));
        }
    }

    private static void stinkyBurpCycle(EntityStinky stinky) {
        if (!stinky.isAlive()) {
            return;
        }
        Level level = stinky.level();
        Vec3 look = stinky.getLookAngle();
        Vec3 front = stinky.position().add(look.x * 0.9, 0.35, look.z * 0.9);
        level.playSound(null, stinky.getX(), stinky.getY(), stinky.getZ(),
                randomSound(FARTS, stinky.getRandom()), SoundSource.NEUTRAL, 1.0F, 1.0F);
        ItemEntity coal = new ItemEntity(level, front.x, front.y, front.z, new ItemStack(Items.COAL));
        coal.setDeltaMovement(look.x * 0.15, 0.12, look.z * 0.15);
        coal.setDefaultPickUpDelay();
        level.addFreshEntity(coal);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, front.x, front.y, front.z, 6, 0.15, 0.15, 0.15, 0.02);
        }
    }

    /**
     * (5) DINO DOG GRANDE: bribe a trex (60s truce) or alosaurus/baryonyx
     * (shorter truce). Clears the current target and stamps a persistent-data
     * truce that {@link #onLivingChangeTarget} enforces against re-targeting
     * the feeder.
     */
    private static void feedDino(PlayerInteractEvent.EntityInteract event, Mob dino) {
        Player player = event.getEntity();
        Level level = dino.level();
        ItemStack crumbs = event.getItemStack().copyWithCount(1);
        event.getItemStack().consume(1, player);
        // One meter-long gulp.
        level.playSound(null, dino.getX(), dino.getY(), dino.getZ(),
                SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 1.4F, 0.55F);
        if (level instanceof ServerLevel serverLevel) {
            Vec3 mouth = dino.getEyePosition().add(dino.getLookAngle().scale(0.8));
            serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, crumbs),
                    mouth.x, mouth.y, mouth.z, 10, 0.25, 0.25, 0.25, 0.05);
        }
        CompoundTag data = dino.getPersistentData();
        int truce = dino instanceof TRex ? TREX_TRUCE : SMALL_DINO_TRUCE;
        data.putLong(TRUCE_UNTIL, level.getGameTime() + truce);
        data.putUUID(TRUCE_FEEDER, player.getUUID());
        dino.setTarget(null);
        dino.getNavigation().stop();
    }

    /** Truce enforcement: a bribed dino will not re-target its feeder while the clock runs. */
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        try {
            LivingEntity mob = event.getEntity();
            if (!(mob instanceof TRex || mob instanceof Alosaurus || mob instanceof Baryonyx)) {
                return;
            }
            LivingEntity newTarget = event.getNewAboutToBeSetTarget();
            if (newTarget == null) {
                return;
            }
            CompoundTag data = mob.getPersistentData();
            if (!data.hasUUID(TRUCE_FEEDER)) {
                return;
            }
            if (newTarget.getUUID().equals(data.getUUID(TRUCE_FEEDER))
                    && mob.level().getGameTime() < data.getLong(TRUCE_UNTIL)) {
                event.setCanceled(true);
            }
        } catch (Throwable t) {
            logOnce("living_change_target", t);
        }
    }

    /**
     * (6) HEART-BOX CHOCOLATES: hearts + happy voice line + a thank-you chat
     * message, regeneration I 30s for the giver, and a persist-safe 2-5 minute
     * cook-back deadline stamped into the companion's persistent data
     * (delivered by {@link #onEntityTick}, so it survives save/reload).
     */
    private static void giftChocolates(PlayerInteractEvent.EntityInteract event, TamableAnimal companion) {
        Player player = event.getEntity();
        Level level = companion.level();
        RandomSource random = companion.getRandom();
        event.getItemStack().consume(1, player);
        playHappySound(companion);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HEART,
                    companion.getX(), companion.getEyeY() + 0.4, companion.getZ(), 7, 0.5, 0.4, 0.5, 0.02);
        }
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGEN_DURATION, 0));
        player.displayClientMessage(Component.translatable(
                "message.orespawn_delight.companion_thanks." + (1 + random.nextInt(3)),
                companion.getDisplayName()), false);
        long due = level.getGameTime() + COOKBACK_MIN + random.nextInt(COOKBACK_MAX - COOKBACK_MIN + 1);
        companion.getPersistentData().putLong(COOKBACK_AT, due);
    }

    /**
     * Cook-back delivery scan. EntityTickEvent fires for every entity, so the
     * hot path bails on two instanceof checks and only reads NBT once a second
     * per companion. When due: if the owner is within 16 blocks the companion
     * tosses them a random shipped dish with a chat line; otherwise the
     * deadline slides 10s and it tries again.
     */
    public static void onEntityTick(EntityTickEvent.Post event) {
        try {
            Entity entity = event.getEntity();
            if (!(entity instanceof TamableAnimal companion)) {
                return;
            }
            if (!(entity instanceof Girlfriend) && !(entity instanceof Boyfriend)) {
                return;
            }
            if (companion.level().isClientSide || (companion.tickCount % 20) != 0) {
                return;
            }
            CompoundTag data = companion.getPersistentData();
            if (!data.contains(COOKBACK_AT)) {
                return;
            }
            long now = companion.level().getGameTime();
            if (now < data.getLong(COOKBACK_AT)) {
                return;
            }
            LivingEntity owner = companion.getOwner();
            if (owner == null || !owner.isAlive() || owner.level() != companion.level()
                    || owner.distanceToSqr(companion) > COOKBACK_RANGE_SQ) {
                data.putLong(COOKBACK_AT, now + COOKBACK_RETRY);
                return;
            }
            data.remove(COOKBACK_AT);
            deliverCookback(companion, owner);
        } catch (Throwable t) {
            logOnce("entity_tick", t);
        }
    }

    private static void deliverCookback(TamableAnimal companion, LivingEntity owner) {
        Level level = companion.level();
        RandomSource random = companion.getRandom();
        ItemStack dish = new ItemStack(SHIPPED_DISHES.get(random.nextInt(SHIPPED_DISHES.size())).get());
        Vec3 toward = owner.position().subtract(companion.position());
        toward = toward.lengthSqr() > 1.0E-4 ? toward.normalize() : Vec3.ZERO;
        ItemEntity gift = new ItemEntity(level,
                companion.getX(), companion.getEyeY(), companion.getZ(), dish);
        gift.setDeltaMovement(toward.x * 0.25, 0.18, toward.z * 0.25);
        gift.setDefaultPickUpDelay();
        level.addFreshEntity(gift);
        playHappySound(companion);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HEART,
                    companion.getX(), companion.getEyeY() + 0.4, companion.getZ(), 5, 0.4, 0.3, 0.4, 0.02);
        }
        if (owner instanceof Player player) {
            player.displayClientMessage(Component.translatable(
                    "message.orespawn_delight.companion_cookback." + (1 + random.nextInt(3)),
                    companion.getDisplayName()), false);
        }
    }

    private static void playHappySound(TamableAnimal companion) {
        List<DeferredHolder<SoundEvent, SoundEvent>> bank =
                companion instanceof Girlfriend ? GIRLFRIEND_HAPPY : BOYFRIEND_HAPPY;
        companion.level().playSound(null, companion.getX(), companion.getY(), companion.getZ(),
                randomSound(bank, companion.getRandom()), SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    /**
     * (7) CARAMEL MASH: apple_cow drops an apple instantly; golden_apple_cow a
     * golden apple on a 5-min cooldown; enchanted_apple_cow an enchanted golden
     * apple on a 20-min cooldown with fanfare. Cooldowns live in the cow's
     * persistent data; a cow still on cooldown just chews smoke and the mash is
     * NOT consumed.
     */
    private static void feedAppleCow(PlayerInteractEvent.EntityInteract event, Cow cow) {
        Player player = event.getEntity();
        Level level = cow.level();
        CompoundTag data = cow.getPersistentData();
        long now = level.getGameTime();
        if (now < data.getLong(MASH_READY_AT)) {
            // Still ruminating - unhappy low moo, keep your mash.
            level.playSound(null, cow.getX(), cow.getY(), cow.getZ(),
                    SoundEvents.COW_AMBIENT, SoundSource.NEUTRAL, 0.8F, 0.7F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        cow.getX(), cow.getEyeY(), cow.getZ(), 5, 0.3, 0.2, 0.3, 0.01);
            }
            return;
        }
        Item reward;
        int cooldown;
        boolean fanfare = false;
        if (cow instanceof EnchantedAppleCow) {
            reward = Items.ENCHANTED_GOLDEN_APPLE;
            cooldown = ENCHANTED_MASH_COOLDOWN;
            fanfare = true;
        } else if (cow instanceof GoldenAppleCow) {
            reward = Items.GOLDEN_APPLE;
            cooldown = GOLDEN_MASH_COOLDOWN;
        } else {
            reward = Items.APPLE;
            cooldown = 0;
        }
        event.getItemStack().consume(1, player);
        if (cooldown > 0) {
            data.putLong(MASH_READY_AT, now + cooldown);
        }
        cow.spawnAtLocation(new ItemStack(reward));
        // Happy moo + hearts.
        level.playSound(null, cow.getX(), cow.getY(), cow.getZ(),
                SoundEvents.COW_AMBIENT, SoundSource.NEUTRAL, 1.0F, 1.3F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HEART,
                    cow.getX(), cow.getEyeY() + 0.3, cow.getZ(), 5, 0.5, 0.3, 0.5, 0.02);
            if (fanfare) {
                level.playSound(null, cow.getX(), cow.getY(), cow.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0F, 1.0F);
                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                        cow.getX(), cow.getY(0.5), cow.getZ(), 24, 0.6, 0.6, 0.6, 0.25);
                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        cow.getX(), cow.getEyeY(), cow.getZ(), 30, 0.8, 0.6, 0.8, 0.5);
            }
        }
    }

    private static SoundEvent randomSound(List<DeferredHolder<SoundEvent, SoundEvent>> bank, RandomSource random) {
        return bank.get(random.nextInt(bank.size())).get();
    }
}
