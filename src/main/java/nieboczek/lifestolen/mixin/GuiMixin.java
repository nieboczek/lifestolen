package nieboczek.lifestolen.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import nieboczek.lifestolen.Lifestolen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public class GuiMixin {
    @ModifyExpressionValue(
            at = @At(value = "NEW", target = "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/state/gui/GuiRenderState;II)Lnet/minecraft/client/gui/GuiGraphicsExtractor;"),
            method = "extractRenderState"
    )
    public GuiGraphicsExtractor render(GuiGraphicsExtractor original) {
        Lifestolen.INSTANCE.render2d(original);
        return original;
    }

    @WrapOperation(
            method = "handleKeybinds",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 2)
    )
    public boolean handleKeybinds(KeyMapping instance, Operation<Boolean> original) {
        // nuke social interactions key when not kill switched
        return Lifestolen.INSTANCE.getKillSwitch() ? original.call(instance) : false;
    }
}
