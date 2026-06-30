package nieboczek.lifestolen.mixin.sodium;

import net.caffeinemc.mods.sodium.client.model.light.LightMode;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext;
import net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.model.SodiumShadeMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import nieboczek.lifestolen.module.FullBrightModule;
import nieboczek.lifestolen.module.XRayModule;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(AbstractBlockRenderContext.class)
public abstract class AbstractBlockRenderContextMixin {
    @Shadow
    protected BlockState state;

    @Shadow
    protected BlockPos pos;

    @Shadow
    @Final
    protected QuadLightData quadLightData;

    @Unique
    private static final int FULL_BRIGHT_LIGHTMAP = 0x00F000F0;

    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
    private void shouldDrawSide(Direction facing, CallbackInfoReturnable<Boolean> cir) {
        var xRay = XRayModule.INSTANCE;
        if (!xRay.getEnabled() || state == null || pos == null) {
            return;
        }
        cir.setReturnValue(xRay.shouldRender(state));
    }

    @Inject(method = "shadeQuad", at = @At("RETURN"))
    private void shadeQuad(MutableQuadViewImpl quad, LightMode lightMode, boolean emissive, SodiumShadeMode shadeMode, CallbackInfo ci) {
        boolean fullBright;
        if (FullBrightModule.INSTANCE.getEnabled()) {
            fullBright = true;
        } else {
            var xRay = XRayModule.INSTANCE;
            if (!xRay.getEnabled() || !xRay.getFullBright() || state == null || pos == null) return;
            fullBright = xRay.shouldRender(state);
        }

        if (!fullBright) return;

        float[] brightnesses = quadLightData.br;
        for (int i = 0; i < 4; i++) {
            quad.setLight(i, FULL_BRIGHT_LIGHTMAP);
            brightnesses[i] = 1.0F;
        }
    }
}
