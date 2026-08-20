package danger.orespawn.delight.farming;

import java.util.Map;
import java.util.Set;

import danger.orespawn.ModBlocks;
import danger.orespawn.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

/**
 * Plants OreSpawn's inert seed items when they are used on soil.
 *
 * Event choice (verified against neoforge-21.1.223 bytecode): the patched
 * {@code ItemStack.useOn} posts {@link UseItemOnBlockEvent} with
 * {@code UsePhase.ITEM_AFTER_BLOCK} on the game bus right before
 * {@code Item.useOn} runs, on BOTH logical sides, and only after the block's
 * own interaction PASSed. That makes it strictly better here than
 * {@code PlayerInteractEvent.RightClickBlock} (which fires before block
 * interaction and would need manual chest/door/etc. exclusions): clicking a
 * container while holding a seed still opens it, and our handler slots in
 * exactly where a real ItemNameBlockItem's useOn would have run.
 * Cancelling with {@code cancelWithResult(sidedSuccess(...))} returns SUCCESS
 * client side (arm swing animation) and CONSUME server side, the vanilla
 * seed-planting result pattern.
 *
 * Placement validity is delegated to each plant block's own
 * {@code canSurvive}/{@code mayPlaceOn} (verified via javap against
 * orespawn-1.21.1-1.0.0-beta.3.jar):
 * - strawberry/radish/rice/butterfly/moth/mosquito/firefly plants extend
 *   vanilla CropBlock -&gt; farmland only, need light.
 * - corn_0/quinoa_0/tomato_0/lettuce_0 are custom BushBlocks whose
 *   mayPlaceOn accepts GRASS_BLOCK, DIRT, FARMLAND (and their own stalk for
 *   corn stacking), matching how the port's worldgen plants them.
 * All placed states default to AGE=0 and random-tick onward from there.
 */
public class GreenThumbHandler {

    /** seed item -> initial plant block; built lazily (registries are frozen long before gameplay events fire). */
    private static Map<Item, Block> seedToPlant;
    /** Seeds whose plants accept grass/dirt as well as farmland (the four custom BushBlock stalk lines). */
    private static Set<Item> anySoilSeeds;

    private static Map<Item, Block> seedToPlant() {
        if (seedToPlant == null) {
            seedToPlant = Map.ofEntries(
                    // 4-stage BushBlock lines: _0 is the freshly planted stage the port's worldgen uses
                    Map.entry(ModItems.CORN_SEED.get(), ModBlocks.CORN_0.get()),
                    Map.entry(ModItems.QUINOA_SEED.get(), ModBlocks.QUINOA_0.get()),
                    Map.entry(ModItems.TOMATO_SEED.get(), ModBlocks.TOMATO_0.get()),
                    Map.entry(ModItems.LETTUCE_SEED.get(), ModBlocks.LETTUCE_0.get()),
                    // vanilla CropBlock lines (age 0-7), farmland only
                    Map.entry(ModItems.STRAWBERRY_SEED.get(), ModBlocks.STRAWBERRY_PLANT.get()),
                    Map.entry(ModItems.RADISH_SEED.get(), ModBlocks.RADISH_PLANT.get()),
                    Map.entry(ModItems.RICE_SEED.get(), ModBlocks.RICE_PLANT.get()),
                    // insect plants, also CropBlocks (they spawn their bug when ripe)
                    Map.entry(ModItems.BUTTERFLY_SEED.get(), ModBlocks.BUTTERFLY_PLANT.get()),
                    Map.entry(ModItems.MOTH_SEED.get(), ModBlocks.MOTH_PLANT.get()),
                    Map.entry(ModItems.MOSQUITO_SEED.get(), ModBlocks.MOSQUITO_PLANT.get()),
                    Map.entry(ModItems.FIREFLY_SEED.get(), ModBlocks.FIREFLY_PLANT.get()));
            // apple_tree_seed / cherry_tree_seed / peach_tree_seed are NOT mapped:
            // the port ships no sapling blocks for them (1.7.10 grew an instant
            // tree from code), and this pass adds no new blocks.
        }
        return seedToPlant;
    }

    private static Set<Item> anySoilSeeds() {
        if (anySoilSeeds == null) {
            anySoilSeeds = Set.of(
                    ModItems.CORN_SEED.get(),
                    ModItems.QUINOA_SEED.get(),
                    ModItems.TOMATO_SEED.get(),
                    ModItems.LETTUCE_SEED.get());
        }
        return anySoilSeeds;
    }

    public static void onUseSeedOnBlock(UseItemOnBlockEvent event) {
        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.ITEM_AFTER_BLOCK) {
            return;
        }
        ItemStack stack = event.getItemStack();
        Block plant = seedToPlant().get(stack.getItem());
        if (plant == null) {
            return;
        }

        UseOnContext context = event.getUseOnContext();
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        // Same target selection as BlockItem: plant into the clicked block if it
        // is replaceable (short grass etc.), otherwise against the clicked face.
        BlockPos plantPos = level.getBlockState(clickedPos).canBeReplaced()
                ? clickedPos
                : clickedPos.relative(context.getClickedFace());

        BlockState plantState = plant.defaultBlockState();
        if (!level.getBlockState(plantPos).canBeReplaced()
                || !level.getFluidState(plantPos).isEmpty()
                || !plantState.canSurvive(level, plantPos)) {
            return; // leave the event alone -> normal PASS behavior
        }

        Player player = context.getPlayer();
        if (!level.isClientSide) {
            level.setBlock(plantPos, plantState, Block.UPDATE_ALL);
            SoundType soundType = plantState.getSoundType();
            level.playSound(null, plantPos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
            level.gameEvent(GameEvent.BLOCK_PLACE, plantPos, new GameEvent.Context(player, plantState));
            // Respects creative mode (no-op when the holder has infinite materials).
            if (player != null) {
                stack.consume(1, player);
            } else {
                stack.shrink(1);
            }
        }
        if (player != null) {
            player.swing(context.getHand());
        }
        event.cancelWithResult(ItemInteractionResult.sidedSuccess(level.isClientSide));
    }

    /** Client-side hint so players can discover that the formerly dead seeds now plant. */
    public static void onSeedTooltip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        if (!seedToPlant().containsKey(item)) {
            return;
        }
        String key = anySoilSeeds().contains(item)
                ? "tooltip.orespawn_delight.plantable.any_soil"
                : "tooltip.orespawn_delight.plantable.farmland";
        event.getToolTip().add(Component.translatable(key).withStyle(ChatFormatting.DARK_GREEN));
    }
}
