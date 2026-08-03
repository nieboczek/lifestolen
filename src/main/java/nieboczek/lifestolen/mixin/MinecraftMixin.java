package nieboczek.lifestolen.mixin;

import net.minecraft.client.Minecraft;
import nieboczek.lifestolen.module.FreeCamModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
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
