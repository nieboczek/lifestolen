package nieboczek.lifestolen.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import nieboczek.lifestolen.module.FreeCamModule;
import nieboczek.lifestolen.module.KillAuraModule;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    @Final
    public Gui gui;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;tick()V"))
    private void tick(CallbackInfo ci) {
        // If handleKeybinds won't be called, call it instead of it
        if (gui.screen() != null || gui.overlay() != null) {
            KillAuraModule.INSTANCE.callQueuedAttack();
        }
    }

    @Inject(
            method = "handleKeybinds",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z")
    )
    private void handleKeybinds(CallbackInfo ci) {
        KillAuraModule.INSTANCE.callQueuedAttack();
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void startAttack(CallbackInfoReturnable<Boolean> ci) {
        if (FreeCamModule.INSTANCE.isEnabled()) ci.cancel();
    }

    @Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true)
    private void pickBlockOrEntity(CallbackInfo ci) {
        if (FreeCamModule.INSTANCE.isEnabled()) ci.cancel();
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void continueAttack(CallbackInfo ci) {
        if (FreeCamModule.INSTANCE.isEnabled()) ci.cancel();
    }
}
