package nieboczek.lifestolen.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import nieboczek.lifestolen.Lifestolen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleRespawn", at = @At("TAIL"))
    private void onPlayerRespawn(CallbackInfo ci) {
        Lifestolen.INSTANCE.reset();
    }
}
