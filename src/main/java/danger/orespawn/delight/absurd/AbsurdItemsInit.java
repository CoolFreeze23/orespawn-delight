package danger.orespawn.delight.absurd;

import danger.orespawn.delight.OreSpawnDelight;
import danger.orespawn.delight.absurd.food.AbsurdFoods;
import danger.orespawn.delight.absurd.food.DrinkItem;
import danger.orespawn.delight.absurd.food.IrukandjiShooterItem;
import danger.orespawn.delight.absurd.food.JumpyBugGummiesItem;
import danger.orespawn.delight.absurd.food.TntRockCandyItem;
import danger.orespawn.delight.absurd.food.VortexDangoItem;
import danger.orespawn.delight.creativetabs.ModTabs;
import danger.orespawn.delight.items.ModItems;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.common.item.ConsumableItem;

/**
 * ABSURD-21 item classes - self-contained registration unit (own DeferredRegister
 * + BuildCreativeModeTabContentsEvent append, wired by a single init(modEventBus)
 * call, per the FeastsInit/KnifeInit pattern). Spec of record: ABSURD-21.json.
 *
 * Only behavior contained in the item itself lives here. World-interaction verbs
 * (Ray Gun searing, Stinky feeding, T-Rex bribing, chocolate gifting, apple-cow
 * milking, the TNT burp explosion, the gummy hiccup ticks, the chum projectile)
 * belong to the interactions agent; the two persistent-data handoff tags are
 * documented on JumpyBugGummiesItem.HICCUP_UNTIL_KEY ("HiccupUntil") and
 * TntRockCandyItem.TNT_BURP_AT_KEY ("TntBurpAt").
 *
 * REGISTRATION ORDER NOTE: The Hammy's usingConvertsTo chain needs each stage's
 * successor to exist when the FoodProperties is built (usingConvertsTo eagerly
 * news an ItemStack - verified in the 1.21.1 sources), so the chain foods are
 * built inside the item suppliers and the fields are declared bottom-up
 * (hammy_1 -> hammy). DeferredRegister registers entries in declaration order,
 * so by the time hammy_2's supplier runs, hammy_1 is already registered.
 */
public class AbsurdItemsInit {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OreSpawnDelight.MODID);

    // === MOD ABSURDITY ===

    /** Steak cooked by shooting it with the Ray Gun (acquisition is the interactions agent's). */
    public static final DeferredItem<Item> RAY_SEARED_STEAK = ITEMS.register("ray_seared_steak",
            () -> new ConsumableItem(ModItems.foodItem(AbsurdFoods.RAY_SEARED_STEAK), true));

    /** Red Cow gives you wings. De-milkified for your convenience (does NOT cure effects). */
    public static final DeferredItem<Item> RED_COW_ENERGY_DRINK = ITEMS.register("red_cow_energy_drink",
            () -> new DrinkItem(ModItems.foodItem(AbsurdFoods.RED_COW_ENERGY_DRINK).stacksTo(16)));

    // The Hammy chain, declared bottom-up so each converts-to target registers first.
    // hammy_1 is the last bite; it was bone-in, so it converts to a bone.
    public static final DeferredItem<Item> HAMMY_1 = ITEMS.register("hammy_1",
            () -> new Item(hammyProperties(Items.BONE)));
    public static final DeferredItem<Item> HAMMY_2 = ITEMS.register("hammy_2",
            () -> new Item(hammyProperties(HAMMY_1.get())));
    public static final DeferredItem<Item> HAMMY_3 = ITEMS.register("hammy_3",
            () -> new Item(hammyProperties(HAMMY_2.get())));
    public static final DeferredItem<Item> HAMMY_4 = ITEMS.register("hammy_4",
            () -> new Item(hammyProperties(HAMMY_3.get())));
    /** A cured ham hock the size and shape of Big Bertha. It is dinner. It is technically also a weapon. */
    public static final DeferredItem<Item> HAMMY = ITEMS.register("hammy",
            () -> new Item(hammyProperties(HAMMY_4.get())));

    public static final DeferredItem<Item> ROCK_CANDY_BLUE = ITEMS.register("rock_candy_blue",
            () -> new ConsumableItem(ModItems.foodItem(AbsurdFoods.ROCK_CANDY), true));
    public static final DeferredItem<Item> ROCK_CANDY_RED = ITEMS.register("rock_candy_red",
            () -> new ConsumableItem(ModItems.foodItem(AbsurdFoods.ROCK_CANDY), true));
    public static final DeferredItem<Item> ROCK_CANDY_GREEN = ITEMS.register("rock_candy_green",
            () -> new ConsumableItem(ModItems.foodItem(AbsurdFoods.ROCK_CANDY), true));
    public static final DeferredItem<Item> ROCK_CANDY_PURPLE = ITEMS.register("rock_candy_purple",
            () -> new ConsumableItem(ModItems.foodItem(AbsurdFoods.ROCK_CANDY), true));

    /** Settles your stomach exactly once, explosively (sets the TntBurpAt tag). */
    public static final DeferredItem<Item> TNT_ROCK_CANDY = ITEMS.register("tnt_rock_candy",
            () -> new TntRockCandyItem(ModItems.foodItem(AbsurdFoods.TNT_ROCK_CANDY)));

    // === LIVING WORLD (item halves; verbs are the interactions agent's) ===

    /** The house chili of the Stinky House. Coal burp + the tamed-Stinky verb are interactions'. */
    public static final DeferredItem<Item> BURP_BACK_CHILI = ITEMS.register("burp_back_chili",
            () -> new ConsumableItem(ModItems.bowlFoodItem(AbsurdFoods.BURP_BACK_CHILI), true));

    public static final DeferredItem<Item> DINO_DOG_GRANDE = ITEMS.register("dino_dog_grande",
            () -> new Item(ModItems.foodItem(AbsurdFoods.DINO_DOG_GRANDE)));

    public static final DeferredItem<Item> HEART_BOX_CHOCOLATES = ITEMS.register("heart_box_chocolates",
            () -> new Item(ModItems.foodItem(AbsurdFoods.HEART_BOX_CHOCOLATES)));

    public static final DeferredItem<Item> CARAMEL_MASH = ITEMS.register("caramel_mash",
            () -> new Item(ModItems.foodItem(AbsurdFoods.CARAMEL_MASH)));

    // === DIMENSION TERROIR ===

    public static final DeferredItem<Item> LAVA_EEL_HOTPOT = ITEMS.register("lava_eel_hotpot",
            () -> new ConsumableItem(ModItems.bowlFoodItem(AbsurdFoods.LAVA_EEL_HOTPOT), true));

    public static final DeferredItem<Item> BASILISK_RAMEN = ITEMS.register("basilisk_ramen",
            () -> new ConsumableItem(ModItems.bowlFoodItem(AbsurdFoods.BASILISK_RAMEN), true));

    public static final DeferredItem<Item> VORTEX_DANGO = ITEMS.register("vortex_dango",
            () -> new VortexDangoItem(ModItems.foodItem(AbsurdFoods.VORTEX_DANGO)));

    public static final DeferredItem<Item> CLIFF_RACER_HOT_WINGS = ITEMS.register("cliff_racer_hot_wings",
            () -> new ConsumableItem(ModItems.foodItem(AbsurdFoods.CLIFF_RACER_HOT_WINGS), true));

    /** Serving item for the blocks agent's sunday_pot_roast feast (handed out bare, no container). */
    public static final DeferredItem<Item> POT_ROAST_CUT = ITEMS.register("pot_roast_cut",
            () -> new ConsumableItem(ModItems.foodItem(AbsurdFoods.POT_ROAST_CUT), true));

    // === POWER FANTASY ===

    public static final DeferredItem<Item> JUMPY_BUG_GUMMIES = ITEMS.register("jumpy_bug_gummies",
            () -> new JumpyBugGummiesItem(ModItems.foodItem(AbsurdFoods.JUMPY_BUG_GUMMIES)));

    public static final DeferredItem<Item> FAIRY_FLOSS = ITEMS.register("fairy_floss",
            () -> new ConsumableItem(ModItems.foodItem(AbsurdFoods.FAIRY_FLOSS), true));

    public static final DeferredItem<Item> IRUKANDJI_SHOOTER = ITEMS.register("irukandji_shooter",
            () -> new IrukandjiShooterItem(ModItems.foodItem(AbsurdFoods.IRUKANDJI_SHOOTER).stacksTo(16)));

    public static final DeferredItem<Item> LUNA_MOTH_MACARON = ITEMS.register("luna_moth_macaron",
            () -> new ConsumableItem(ModItems.foodItem(AbsurdFoods.LUNA_MOTH_MACARON), true));

    // === helpers ===

    /**
     * One stage of The Hammy: stacksTo(1) (it is a weapon), a full porkchop's
     * nutrition + strength I 10s per bite, +4 attack damage via the
     * minecraft:attribute_modifiers data component (The Hammy spec's approach),
     * and usingConvertsTo the next stage down.
     */
    private static Item.Properties hammyProperties(ItemLike convertsTo) {
        return new Item.Properties()
                .stacksTo(1)
                .food(AbsurdFoods.hammyBite(convertsTo))
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(OreSpawnDelight.prefix("hammy_attack"),
                                        4.0, AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .build());
    }

    public static void init(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(AbsurdItemsInit::addToCreativeTab);
    }

    /** Appends to the OreSpawn Delight tab, frozen-ID manifest order. Partial
     *  Hammy stages are deliberately left out of the tab (eaten into existence). */
    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(ModTabs.OSD_TAB.getKey())) {
            event.accept(RAY_SEARED_STEAK.get());
            event.accept(RED_COW_ENERGY_DRINK.get());
            event.accept(HAMMY.get());
            event.accept(ROCK_CANDY_BLUE.get());
            event.accept(ROCK_CANDY_RED.get());
            event.accept(ROCK_CANDY_GREEN.get());
            event.accept(ROCK_CANDY_PURPLE.get());
            event.accept(TNT_ROCK_CANDY.get());
            event.accept(BURP_BACK_CHILI.get());
            event.accept(DINO_DOG_GRANDE.get());
            event.accept(HEART_BOX_CHOCOLATES.get());
            event.accept(CARAMEL_MASH.get());
            event.accept(LAVA_EEL_HOTPOT.get());
            event.accept(BASILISK_RAMEN.get());
            event.accept(VORTEX_DANGO.get());
            event.accept(CLIFF_RACER_HOT_WINGS.get());
            event.accept(JUMPY_BUG_GUMMIES.get());
            event.accept(FAIRY_FLOSS.get());
            event.accept(IRUKANDJI_SHOOTER.get());
            event.accept(LUNA_MOTH_MACARON.get());
            event.accept(POT_ROAST_CUT.get());
        }
    }
}
