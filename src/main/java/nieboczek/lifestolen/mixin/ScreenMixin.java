package nieboczek.lifestolen.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import nieboczek.lifestolen.Lifestolen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Screen.class)
public class ScreenMixin {

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (minecraft.options.keySocialInteractions.matches(keyEvent)) {
            Lifestolen.Companion.setKillSwitch(!Lifestolen.Companion.getKillSwitch());
            return true;
        }
        return super.keyPressed(keyEvent);
    }
}
