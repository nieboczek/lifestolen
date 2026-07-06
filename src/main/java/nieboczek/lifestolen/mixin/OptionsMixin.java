package nieboczek.lifestolen.mixin;

import net.minecraft.client.Options;
import nieboczek.lifestolen.module.FreeCamModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class OptionsMixin {
    @Inject(method = "setCameraType", at = @At("HEAD"), cancellable = true)
    private void setCameraType(CallbackInfo ci) {
        if (FreeCamModule.INSTANCE.isEnabled()) ci.cancel();
    }
}
