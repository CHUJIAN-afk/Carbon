package first.carbon.mixin;

import first.carbon.Carbon;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;"))
    private Iterable<Entity> imo$entities(ClientLevel level) {
        var renderer = Carbon.Client.DROPPED_RENDERER;
        renderer.collecting = Carbon.CONFIG.optimizeDroppedItems.get();
        try {
            return level.entitiesForRendering();
        } finally {
            renderer.collecting = false;
        }
    }

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void imo$shadows(com.mojang.blaze3d.vertex.PoseStack pose, float partialTick, long finishTime, boolean outline, Camera camera, GameRenderer renderer, LightTexture light, Matrix4f projection, CallbackInfo ci) {
        Carbon.Client.RENDERER.beginWorld(camera, pose.last().pose(), projection);
        Carbon.Client.DROPPED_RENDERER.beginFrame(camera, pose.last().pose(), projection, partialTick);
    }
}
