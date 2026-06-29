package nieboczek.lifestolen.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import nieboczek.lifestolen.Lifestolen;
import nieboczek.lifestolen.module.NoPushModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Inject(method = "getPushableEntities", at = @At("HEAD"), cancellable = true)
    private void getPushableEntities(Entity pusher, AABB boundingBox, CallbackInfoReturnable<List<Entity>> cir) {
        if (NoPushModule.INSTANCE.getNoPushByEntities().getValue() && !Lifestolen.Companion.getKillSwitch()) {
            cir.setReturnValue(List.of());
            cir.cancel();
        }
    }
}
