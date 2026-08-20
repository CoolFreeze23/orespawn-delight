package danger.orespawn.delight.interact;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client-only wiring for the interactions layer. Only ever classloaded behind
 * the {@code FMLEnvironment.dist.isClient()} check in {@link InteractionsInit}.
 *
 * The chum projectile renders with the vanilla {@link ThrownItemRenderer}
 * (ChumEntity extends ThrowableItemProjectile and therefore supplies its item
 * stack) - the same renderer snowballs and eggs use.
 */
public class InteractionsClient {

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(InteractionsInit.CHUM.get(), ThrownItemRenderer::new);
    }
}
