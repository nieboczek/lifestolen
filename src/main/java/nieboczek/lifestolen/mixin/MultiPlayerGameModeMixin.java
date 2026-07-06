package nieboczek.lifestolen.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import nieboczek.lifestolen.module.FreeCamModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void useItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult blockHit, CallbackInfoReturnable<InteractionResult> cir) {
        if (FreeCamModule.INSTANCE.isEnabled()) cir.setReturnValue(InteractionResult.PASS);
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void interact(Player player, Entity entity, EntityHitResult hitResult, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (FreeCamModule.INSTANCE.isEnabled()) cir.setReturnValue(InteractionResult.PASS);
    }
}
