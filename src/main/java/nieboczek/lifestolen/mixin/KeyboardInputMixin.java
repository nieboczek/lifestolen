package nieboczek.lifestolen.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import nieboczek.lifestolen.Lifestolen;
import nieboczek.lifestolen.mixininterfaces.IKeyboardInput;
import nieboczek.lifestolen.module.FreeCamModule;
import nieboczek.lifestolen.module.InvMoveModule;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin implements IKeyboardInput {
    @Unique
    private static final Input NO_INPUT = new Input(false, false, false, false, false, false, false);

    @Shadow
    @Final
    private Options options;

    @Unique
    private Input lifestolen$unmodified = NO_INPUT;

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

    @ModifyExpressionValue(method = "tick", at = @At(value = "NEW", target = "(ZZZZZZZ)Lnet/minecraft/world/entity/player/Input;"))
    public Input tick2(Input original) {
        lifestolen$unmodified = original;
        if (FreeCamModule.INSTANCE.isEnabled()) return NO_INPUT;
        return original;
    }

    @Override
    public @NonNull Input lifestolen$getUnmodified() {
        return lifestolen$unmodified;
    }
}
