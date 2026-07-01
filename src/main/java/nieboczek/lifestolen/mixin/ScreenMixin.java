package nieboczek.lifestolen.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.KeyEvent;
import nieboczek.lifestolen.Lifestolen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin {
    @Shadow
    @Final
    protected Minecraft minecraft;

    @SuppressWarnings("ConstantValue")
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (((Object) this) instanceof TitleScreen && minecraft.options.keySocialInteractions.matches(event)) {
            Lifestolen mod = Lifestolen.INSTANCE;
            mod.toggleKillSwitch();
            mod.getLog().info("Set kill switch in title screen to {}", mod.getKillSwitch());
            cir.setReturnValue(true);
        }
    }
}
