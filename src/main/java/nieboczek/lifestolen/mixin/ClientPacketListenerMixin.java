package nieboczek.lifestolen.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import nieboczek.lifestolen.Lifestolen;
import nieboczek.lifestolen.gui.notification.AntiCheatDetector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleRespawn", at = @At("TAIL"))
    private void handleRespawn(CallbackInfo ci) {
        Lifestolen.INSTANCE.reset();
    }

    @Inject(method = "handleCommands", at = @At("TAIL"))
    private void handleCommands(CallbackInfo ci) {
        AntiCheatDetector.INSTANCE.sendPluginsCommand();
    }
}
