package nieboczek.lifestolen.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.KeyboardInput;
import nieboczek.lifestolen.Lifestolen;
import nieboczek.lifestolen.module.InvMoveModule;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
    @Shadow
    @Final
    private Options options;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        InvMoveModule invMove = InvMoveModule.INSTANCE;
        if (!invMove.getEnabled() || Lifestolen.INSTANCE.getKillSwitch()) return;

        Screen screen = Minecraft.getInstance().gui.screen();
        if (screen == null || screen instanceof ChatScreen) return;

        KeyMapping.setAll();

        if (!invMove.getPassthroughSneak()) {
            options.keyShift.setDown(false);
        }
    }
}
