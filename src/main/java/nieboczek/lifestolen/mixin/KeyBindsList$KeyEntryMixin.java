package nieboczek.lifestolen.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import nieboczek.lifestolen.Lifestolen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyBindsList.KeyEntry.class)
public class KeyBindsList$KeyEntryMixin {
    @WrapOperation(method = "refreshEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;getTranslatedKeyMessage()Lnet/minecraft/network/chat/Component;"))
    public Component getTranslatedKeyMessage(KeyMapping instance, Operation<Component> original) {
        boolean isSocialInteractions = instance.getDefaultKey() == InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_P);
        return isSocialInteractions && Lifestolen.INSTANCE.getKillSwitch() ? instance.getDefaultKey().getDisplayName() : original.call(instance);
    }

    @WrapOperation(method = "refreshEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDefault()Z", ordinal = 0))
    public boolean isDefault(KeyMapping instance, Operation<Boolean> original) {
        boolean isSocialInteractions = instance.getDefaultKey() == InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_P);
        return (isSocialInteractions && Lifestolen.INSTANCE.getKillSwitch()) || original.call(instance);
    }
}

