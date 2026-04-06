package nieboczek.lifestolen.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import nieboczek.lifestolen.Lifestolen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(at = @At("HEAD"), method = "render")
    public void render(GuiGraphics context, DeltaTracker deltaTracker, CallbackInfo ci) {
        Lifestolen.render2d(context);
    }
}
