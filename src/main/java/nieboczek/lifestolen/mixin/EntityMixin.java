package nieboczek.lifestolen.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import nieboczek.lifestolen.module.FreeCamModule;
import nieboczek.lifestolen.module.util.RotationUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Unique
    private final Minecraft mc = Minecraft.getInstance();

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void turn(double xo, double yo, CallbackInfo ci) {
        if (mc.player != (Object) this) return;
        FreeCamModule freeCam = FreeCamModule.INSTANCE;
        if (freeCam.isEnabled()) {
            freeCam.turnCamera(xo, yo);
            ci.cancel();
        }
    }

    @ModifyExpressionValue(
            method = "moveRelative",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getYRot()F")
    )
    private float moveRelative(float original) {
        if (mc.player != (Object) this) return original;
        Float correctedYaw = RotationUtil.INSTANCE.computeCorrectedYaw();
        return correctedYaw == null ? original : correctedYaw;
    }
}
