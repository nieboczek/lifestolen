package nieboczek.lifestolen.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import nieboczek.lifestolen.gui.ConfigScreen;
import nieboczek.lifestolen.module.NoRenderModule;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    private void extractCrosshair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (minecraft.gui.screen() instanceof ConfigScreen) ci.cancel();
    }

    @ModifyExpressionValue(
            method = "extractCameraOverlays",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;")
    )
    // NOTE: This actually expects a boolean from us but Mixins don't like generics
    private Object extractCameraOverlays(Object original) {
        NoRenderModule noRender = NoRenderModule.INSTANCE;
        boolean canDisplay = noRender.isDisabled() || !noRender.getNoVignette();
        return ((Boolean) original) && canDisplay;
    }
}
