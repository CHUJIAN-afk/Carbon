package first.carbon.mixin;

import first.carbon.Carbon;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Inject(method = {"addEntity", "removeEntity", "sendBlockUpdated", "onChunkLoaded", "unload"}, at = @At("HEAD"))
    private void imo$changed(CallbackInfo ci) {
        Carbon.DROPPED_RENDERER.sceneDirty = true;
    }
    @Inject(method = {"sendBlockUpdated", "onChunkLoaded", "unload"}, at = @At("HEAD"))
    private void imo$blockChanged(CallbackInfo ci) {
        Carbon.DROPPED_RENDERER.blockCacheDirty = true;
    }
    @Inject(method = "entitiesForRendering", at = @At("RETURN"), cancellable = true)
    private void imo$collect(CallbackInfoReturnable<Iterable<Entity>> ci) {
        if (Carbon.DROPPED_RENDERER.collecting) {
            Carbon.DROPPED_RENDERER.collecting = false;
            ci.setReturnValue(Carbon.DROPPED_RENDERER.collect(ci.getReturnValue()));
        }
    }
}
