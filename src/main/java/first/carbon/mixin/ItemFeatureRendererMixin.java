package first.carbon.mixin;

import first.carbon.Carbon;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFeatureRenderer.class)
public abstract class ItemFeatureRendererMixin {
    @Inject(method = {"renderSolid", "renderTranslucent"}, at = @At("HEAD"))
    private void carbon$begin(CallbackInfo ci) { Carbon.GENERIC.begin(); }

    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    private void carbon$submit(MultiBufferSource.BufferSource buffers, OutlineBufferSource outline, SubmitNodeStorage.ItemSubmit submit, CallbackInfo ci) {
        if (Carbon.GENERIC.submit(submit)) ci.cancel();
    }

    @Inject(method = {"renderSolid", "renderTranslucent"}, at = @At("TAIL"))
    private void carbon$flush(CallbackInfo ci) { Carbon.GENERIC.flush(); }
}
