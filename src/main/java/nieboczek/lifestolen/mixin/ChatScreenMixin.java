package nieboczek.lifestolen.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import nieboczek.lifestolen.Lifestolen;
import nieboczek.lifestolen.command.CommandExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Inject(method = "handleChatInput", at = @At("HEAD"), cancellable = true)
    private void handleChatInput(String msg, boolean addToRecent, CallbackInfo ci) {
        Lifestolen mod = Lifestolen.INSTANCE;

        if (msg.startsWith(mod.getCfg().getCommandPrefix())) {
            String command = msg.substring(mod.getCfg().getCommandPrefix().length());
            if (mod.getKillSwitch() && !command.trim().equals("kys")) return;

            CommandExecutor.INSTANCE.tryExecute(command);
            Minecraft.getInstance().gui.hud.getChat().addRecentChat(msg); // TODO: clear the evidence
            ci.cancel();
        }
    }
}
