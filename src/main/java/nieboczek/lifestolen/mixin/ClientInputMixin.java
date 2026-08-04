package nieboczek.lifestolen.mixin;

import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import nieboczek.lifestolen.module.util.RotationUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientInput.class)
public class ClientInputMixin {
    @Shadow
    public Input keyPresses;

    @Inject(method = "hasForwardImpulse", at = @At("HEAD"), cancellable = true)
    private void hasForwardImpulse(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(RotationUtil.INSTANCE.shouldKeepSprinting(this.keyPresses));
    }
}
