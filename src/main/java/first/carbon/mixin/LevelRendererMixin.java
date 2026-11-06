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
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;"))
    private Iterable<Entity> imo$entities(ClientLevel level, Operation<Iterable<Entity>> original) {
        Iterable<Entity> entities = original.call(level);
        return Carbon.CONFIG.optimizeDroppedItems.get() ? Carbon.Client.DROPPED_RENDERER.collect(entities) : entities;
    }

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void imo$shadows(com.mojang.blaze3d.vertex.PoseStack pose, float partialTick, long finishTime, boolean outline, Camera camera, GameRenderer renderer, LightTexture light, Matrix4f projection, CallbackInfo ci) {
        Carbon.Client.RENDERER.beginWorld(camera, pose.last().pose(), projection);
        Carbon.Client.DROPPED_RENDERER.beginFrame(camera, pose.last().pose(), projection, partialTick);
    }
}
