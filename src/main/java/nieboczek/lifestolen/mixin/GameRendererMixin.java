package nieboczek.lifestolen.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import nieboczek.lifestolen.Lifestolen;
import nieboczek.lifestolen.util.Renderer3d;
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
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(Axis.XP.rotationDegrees(mainCamera.xRot()));
        poseStack.mulPose(Axis.YP.rotationDegrees(mainCamera.yRot() + 180f));

        Renderer3d.poseStack = poseStack;
        Renderer3d.bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        Renderer3d.tickDelta = deltaTracker.getGameTimeDeltaPartialTick(false);

        Lifestolen.render3d();
    }
}
