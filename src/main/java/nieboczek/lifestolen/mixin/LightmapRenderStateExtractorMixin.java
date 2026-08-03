package nieboczek.lifestolen.mixin;

import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import nieboczek.lifestolen.module.FullBrightModule;
import nieboczek.lifestolen.module.XRayModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapRenderStateExtractorMixin {
    @ModifyVariable(method = "extract", at = @At(value = "STORE"), name = "brightnessOption")
    private float extract(float brightnessOption) {
        var xRay = XRayModule.INSTANCE;
        if ((xRay.isDisabled() || !xRay.getFullBright()) && FullBrightModule.INSTANCE.isDisabled()) {
            return brightnessOption;
        }
        return Float.MAX_VALUE;
    }
}
