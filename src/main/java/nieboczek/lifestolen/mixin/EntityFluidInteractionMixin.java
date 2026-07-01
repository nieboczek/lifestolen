package nieboczek.lifestolen.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.phys.Vec3;
import nieboczek.lifestolen.Lifestolen;
import nieboczek.lifestolen.module.NoPushModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityFluidInteraction.class)
public class EntityFluidInteractionMixin {
    @ModifyExpressionValue(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;getFlow(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/Vec3;")
    )
    private Vec3 update(Vec3 original, @Local(argsOnly = true, name = "entity") Entity entity) {
        if (entity != Minecraft.getInstance().player || Lifestolen.INSTANCE.getKillSwitch())
            return original;
        return NoPushModule.INSTANCE.getNoPushByFluids() ? Vec3.ZERO : original;
    }
}
