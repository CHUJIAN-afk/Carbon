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
@Mixin(targets = "dev.tr7zw.entityculling.EntityCullingModBase", remap = false)
public abstract class EntityCullingCaptureMixin {
    @Inject(method = "prefetchEntityData", at = @At("HEAD"), cancellable = true, require = 0)
    private void carbon$skipBatchedItems(Entity entity, CallbackInfoReturnable<Object> cir) {
        if (entity instanceof ItemEntity item) {
            var renderer = Carbon.RENDERER;
            if (!renderer.entityCullingCaptureHook) renderer.entityCullingCaptureHook = true;
            if (renderer.skipsEntityCulling(item)) cir.setReturnValue(null);
        }
    }
}
