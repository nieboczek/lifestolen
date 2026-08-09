package nieboczek.lifestolen.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import nieboczek.lifestolen.module.util.RotationUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @ModifyExpressionValue(
            method = "jumpFromGround",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F")
    )
    private float jumpFromGround(float original) {
        if (Minecraft.getInstance().player != (Object) this) return original;
        Float correctedYaw = RotationUtil.INSTANCE.computeCorrectedYaw();
        return correctedYaw == null ? original : correctedYaw;
    }
}
