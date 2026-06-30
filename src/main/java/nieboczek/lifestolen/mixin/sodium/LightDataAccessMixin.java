package nieboczek.lifestolen.mixin.sodium;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import nieboczek.lifestolen.module.FullBrightModule;
import nieboczek.lifestolen.module.XRayModule;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(LightDataAccess.class)
public abstract class LightDataAccessMixin {
    @Shadow
    @Final
    private BlockPos.MutableBlockPos pos;

    @Shadow
    protected BlockAndTintGetter level;

    @Unique
    private static final int MAX_LIGHT_LEVEL = 15 | 15 << 4 | 15 << 8;

    @ModifyReturnValue(method = "compute", at = @At("RETURN"))
    private int compute(int original) {
        if (FullBrightModule.INSTANCE.getEnabled()) {
            return original | MAX_LIGHT_LEVEL;
        }

        var xRay = XRayModule.INSTANCE;
        if (xRay.getEnabled() && xRay.getFullBright()) {
            var state = level.getBlockState(pos);
            if (xRay.shouldRender(state)) {
                return original | MAX_LIGHT_LEVEL;
            }
        }
        return original;
    }
}
