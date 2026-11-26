package first.carbon.mixin;

import first.carbon.Carbon;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = {"dev.tr7zw.entityculling.util.NMSCullingHelper", "dev.tr7zw.entityculling.NMSCullingHelper"}, remap = false)
public abstract class EntityCullingVisibilityMixin {
    @Inject(method = "ignoresCulling", at = @At("HEAD"), cancellable = true, require = 0)
    private static void carbon$keepBatchedItemsTicking(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof ItemEntity item) {
            var renderer = Carbon.DROPPED_RENDERER;
            if (!renderer.entityCullingVisibilityHook) renderer.entityCullingVisibilityHook = true;
            if (renderer.skipsEntityCulling(item)) cir.setReturnValue(true);
        }
    }
}
