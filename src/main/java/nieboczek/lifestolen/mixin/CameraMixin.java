package nieboczek.lifestolen.mixin;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import nieboczek.lifestolen.Lifestolen;
import nieboczek.lifestolen.module.FreeCamModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Shadow
    private boolean detached;

    @Inject(method = "alignWithEntity", at = @At("RETURN"))
    private void alignWithEntity(float partialTicks, CallbackInfo ci) {
        if (FreeCamModule.INSTANCE.getEnabled() && !Lifestolen.INSTANCE.getKillSwitch()) {
            setPosition(FreeCamModule.INSTANCE.computeLerpedPos(partialTicks));
            detached = true;
        }
    }
}
