package nieboczek.lifestolen.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import nieboczek.lifestolen.module.FreeCamModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void turn(double xo, double yo, CallbackInfo ci) {
        if (Minecraft.getInstance().player != (Object) this) return;
        FreeCamModule freeCam = FreeCamModule.INSTANCE;
        if (freeCam.isEnabled()) {
            freeCam.turnCamera(xo, yo);
            ci.cancel();
        }
    }
}
