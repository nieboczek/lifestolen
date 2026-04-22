package nieboczek.lifestolen.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import nieboczek.lifestolen.module.util.RotationUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @ModifyExpressionValue(
            method = {"sendPosition", "tick"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getYRot()F")
    )
    public float getYRot(float original) {
        RotationUtil.Rotation rot = RotationUtil.INSTANCE.getLerpedRotation();
        if (rot == null) return original;
        return rot.getY();
    }

    @ModifyExpressionValue(
            method = {"sendPosition", "tick"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXRot()F")
    )
    public float getXRot(float original) {
        RotationUtil.Rotation rot = RotationUtil.INSTANCE.getLerpedRotation();
        if (rot == null) return original;
        return rot.getX();
    }

    @ModifyExpressionValue(
            method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;")
    )
    private static Vec3 pick(Vec3 original, Entity entity, double blockInteractionRange, double entityInteractionRange, float partialTick) {
        if (entity != Minecraft.getInstance().player) return original;
        var rotation = RotationUtil.INSTANCE.getLerpedRotation();
        return rotation != null ? Vec3.directionFromRotation(rotation.getX(), rotation.getY()) : original;
    }
}
