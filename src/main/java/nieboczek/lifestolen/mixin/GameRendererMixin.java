package nieboczek.lifestolen.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import nieboczek.lifestolen.Lifestolen;
import nieboczek.lifestolen.util.Renderer3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow
    @Final
    private Camera mainCamera;

    @Inject(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z"),
            method = "renderLevel"
    )
    void renderLevel(DeltaTracker deltaTracker, CallbackInfo ci) {
        Renderer3d.tickDelta = deltaTracker.getGameTimeDeltaPartialTick(false);
        Renderer3d.camera = mainCamera;
        Renderer3d.setViewMatrix(new Matrix4f().rotation(mainCamera.rotation().conjugate(new org.joml.Quaternionf())));
        Renderer3d.beginFrame(Minecraft.getInstance().getMainRenderTarget(), mainCamera);

        try {
            Lifestolen.render3d();
        } finally {
            Renderer3d.endFrame();
        }
    }

}
