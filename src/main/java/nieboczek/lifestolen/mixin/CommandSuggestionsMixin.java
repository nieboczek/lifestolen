package nieboczek.lifestolen.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;
import nieboczek.lifestolen.Lifestolen;
import nieboczek.lifestolen.command.CommandExecutor;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {
    @Shadow
    @Final
    private EditBox input;
    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow
    private ParseResults<SharedSuggestionProvider> currentParse;
    @Shadow
    public abstract void showSuggestions(boolean immediateNarration);
    @Shadow
    private CommandSuggestions.@Nullable SuggestionsList suggestions;

    @Inject(
            method = "updateCommandInfo",
            at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;canRead()Z"),
            cancellable = true
    )
    private void updateCommandInfo(CallbackInfo ci) {
        if (Lifestolen.INSTANCE.getKillSwitch()) return;
        if (input.getValue().startsWith(Lifestolen.INSTANCE.getCfg().getCommandPrefix())) {
            pendingSuggestions = CommandExecutor.INSTANCE.autocomplete(input.getValue(), input.getCursorPosition());
            pendingSuggestions.thenRun(() -> {
                if (suggestions == null) showSuggestions(false);
            });

            currentParse = null;
            ci.cancel();
        }
    }
}
