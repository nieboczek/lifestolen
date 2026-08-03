package nieboczek.lifestolen.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import nieboczek.lifestolen.Lifestolen;
import nieboczek.lifestolen.command.CommandExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow
    private int historyPos;

    @Inject(method = "handleChatInput", at = @At("HEAD"), cancellable = true)
    private void handleChatInput(String msg, boolean addToRecent, CallbackInfo ci) {
        Lifestolen mod = Lifestolen.INSTANCE;

        if (msg.startsWith(mod.getCfg().getCommandPrefix())) {
            String command = msg.substring(mod.getCfg().getCommandPrefix().length());
            if (mod.getKillSwitch() && !command.trim().equals("kys")) return;

            CommandExecutor.INSTANCE.tryExecute(command);
            Minecraft.getInstance().gui.hud.getChat().addRecentChat(msg);
            ci.cancel();
        }
    }

    /// If kill switch is active, suggest only non-client commands in history
    @ModifyExpressionValue(method = "moveInHistory", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I"))
    private int moveInHistory(int original, @Local(name = "dir", argsOnly = true) int dir) {
        Lifestolen mod = Lifestolen.INSTANCE;
        if (!mod.getKillSwitch()) return original;

        String prefix = mod.getCfg().getCommandPrefix();
        List<String> recent = Minecraft.getInstance().gui.hud.getChat().getRecentChat();
        int max = recent.size();

        int idx = historyPos + dir;
        while (true) {
            if (idx >= max || idx < 0) return max;

            String s = recent.get(idx);
            if (s != null && s.startsWith(prefix)) {
                idx += dir;
                continue;
            }

            return idx;
        }
    }
}
