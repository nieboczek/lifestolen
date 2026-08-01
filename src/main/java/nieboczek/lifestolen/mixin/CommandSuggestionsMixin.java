package nieboczek.lifestolen.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import nieboczek.lifestolen.Lifestolen;
import nieboczek.lifestolen.command.CommandExecutor;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
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
    @Shadow
    @Final
    private Font font;
    @Final
    @Shadow
    private List<FormattedCharSequence> commandUsage;
    @Shadow
    private int commandUsagePosition;
    @Shadow
    private int commandUsageWidth;

    @Inject(
            method = "updateCommandInfo",
            at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;canRead()Z"),
            cancellable = true
    )
    private void updateCommandInfo(CallbackInfo ci) {
        if (Lifestolen.INSTANCE.getKillSwitch()) return;
        String text = input.getValue();

        if (text.startsWith(Lifestolen.INSTANCE.getCfg().getCommandPrefix())) {
            pendingSuggestions = CommandExecutor.INSTANCE.autocomplete(text, input.getCursorPosition());
            pendingSuggestions.thenRun(() -> {
                if (suggestions == null) showSuggestions(false);
            });

            CommandExecutor.UsageInfo usageInfo = CommandExecutor.INSTANCE.getUsageInfo(text);
            currentParse = null;
            commandUsage.clear();

            if (usageInfo != null && !text.endsWith("  ")) {
                commandUsage.add(FormattedCharSequence.forward(usageInfo.getLine(), CommandSuggestions.USAGE_FORMAT));
                commandUsageWidth = font.width(commandUsage.getFirst());
                commandUsagePosition = Mth.clamp(
                    input.getScreenX(usageInfo.getStartPos()),
                    0,
                    input.getScreenX(0) + input.getInnerWidth() - commandUsageWidth
                );
            }

            ci.cancel();
        }
    }
}
