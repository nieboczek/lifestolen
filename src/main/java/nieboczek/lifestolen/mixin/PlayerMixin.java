package nieboczek.lifestolen.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import nieboczek.lifestolen.module.util.RotationUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin {
    @ModifyExpressionValue(
            method = "travel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getLookAngle()Lnet/minecraft/world/phys/Vec3;")
    )
    private Vec3 travel(Vec3 original) {
        if (Minecraft.getInstance().player != (Object) this) return original;
        RotationUtil.Rotation spoofed = RotationUtil.INSTANCE.spoofedRotation();
        return spoofed == null ? original : Vec3.directionFromRotation(spoofed.getX(), spoofed.getY());
    }
}
