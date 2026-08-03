package nieboczek.lifestolen.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import nieboczek.lifestolen.Lifestolen;
import nieboczek.lifestolen.gui.render.BlurredRectRenderer;
import nieboczek.lifestolen.module.FreeCamModule;
import nieboczek.lifestolen.module.TracersModule;
import nieboczek.lifestolen.module.util.RotationUtil;
import nieboczek.lifestolen.util.Renderer3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow
    @Final
    private Camera mainCamera;
    @Shadow
    @Final
    private RenderTarget mainRenderTarget;
    @Shadow
    @Final
    public CrossFrameResourcePool resourcePool;

    @Inject(method = "update", at = @At("HEAD"))
    private void update(DeltaTracker deltaTracker, CallbackInfo ci) {
        RotationUtil.INSTANCE.lerpRotation(deltaTracker.getGameTimeDeltaPartialTick(false));
    }

    @Inject(
            method = "renderLevel",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/CameraEntityRenderState;isSleeping:Z", opcode = Opcodes.GETFIELD)
    )
    void renderLevel(DeltaTracker deltaTracker, CallbackInfo ci) {
        Renderer3d renderer = Renderer3d.INSTANCE;

        renderer.setTickDelta(deltaTracker.getGameTimeDeltaPartialTick(false));
        renderer.setCamera(mainCamera);
        renderer.setViewMatrix(new Matrix4f().rotation(mainCamera.rotation().conjugate(new Quaternionf())));
        renderer.beginFrame(mainRenderTarget, mainCamera);

        try {
            Lifestolen.INSTANCE.render3d();
        } finally {
            renderer.endFrame();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void bobView(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
        if (TracersModule.INSTANCE.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void renderItemInHand(CallbackInfo ci) {
        if (FreeCamModule.INSTANCE.isEnabled()) ci.cancel();
    }

    @Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
    private void shouldRenderBlockOutline(CallbackInfoReturnable<Boolean> cir) {
        if (FreeCamModule.INSTANCE.isEnabled()) cir.setReturnValue(false);
    }

    @Inject(method = "processBlurEffect", at = @At("HEAD"), cancellable = true)
    private void processBlurEffect(CallbackInfo ci) {
        BlurredRectRenderer renderer = BlurredRectRenderer.INSTANCE;
        if (renderer.isActive()) {
            renderer.render(mainRenderTarget, resourcePool);
            ci.cancel();
        }
    }
}
