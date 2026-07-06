package nieboczek.lifestolen.mixin.iris;

import nieboczek.lifestolen.module.FreeCamModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.pathways.HandRenderer")
public class HandRendererMixin {
    @Inject(method = "canRender", at = @At("HEAD"), cancellable = true)
    private void canRender(CallbackInfoReturnable<Boolean> cir) {
        if (FreeCamModule.INSTANCE.isEnabled()) cir.setReturnValue(false);
    }
}
