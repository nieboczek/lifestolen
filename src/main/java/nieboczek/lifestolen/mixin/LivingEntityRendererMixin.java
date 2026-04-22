package nieboczek.lifestolen.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import nieboczek.lifestolen.module.util.RotationUtil;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;isUpsideDown:Z", opcode = Opcodes.PUTFIELD)
    )
    public void extractRenderState(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        if (entity == Minecraft.getInstance().player) {
            RotationUtil.Rotation rot = RotationUtil.INSTANCE.getLerpedRotation();
            if (rot == null) return;

            float yRot = Mth.wrapDegrees(rot.getY());
            state.bodyRot = yRot;
            state.yRot = yRot;
            state.xRot = rot.getX();
        }
    }
}
