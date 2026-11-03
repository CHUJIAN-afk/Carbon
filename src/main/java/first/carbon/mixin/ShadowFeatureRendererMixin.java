package first.carbon.mixin;

import first.carbon.Carbon;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ShadowFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(ShadowFeatureRenderer.class)
public abstract class ShadowFeatureRendererMixin {
    @Redirect(method = "renderTranslucent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollection;getShadowSubmits()Ljava/util/List;"))
    private List<SubmitNodeStorage.ShadowSubmit> carbon$shadows(SubmitNodeCollection nodes) {
        return Carbon.SHADOWS.collect(nodes.getShadowSubmits());
    }

    @Inject(method = "renderTranslucent", at = @At("TAIL"))
    private void carbon$flush(CallbackInfo ci) { Carbon.SHADOWS.flush(); }
}
