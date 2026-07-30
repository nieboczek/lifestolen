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
        if (msg.startsWith(Lifestolen.INSTANCE.getCfg().getCommandPrefix())) {
            String command = msg.substring(Lifestolen.INSTANCE.getCfg().getCommandPrefix().length());
            if (Lifestolen.INSTANCE.getKillSwitch() && !command.trim().equals("kys")) return;

            try {
                CommandExecutor.INSTANCE.execute(command);
            } catch (Throwable t) {
                throw t; // TODO: handle it
            }

            Minecraft.getInstance().gui.hud.getChat().addRecentChat(msg); // TODO: clear the evidence
            ci.cancel();
        }
    }
}
