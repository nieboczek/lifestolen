package nieboczek.lifestolen.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import nieboczek.lifestolen.module.XRayModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Block.class)
public abstract class BlockMixin {
    @ModifyReturnValue(method = "shouldRenderFace", at = @At("RETURN"))
    private static boolean shouldRenderFace(boolean original, BlockState state, BlockState neighborState, Direction direction) {
        var xRay = XRayModule.INSTANCE;
        if (xRay.isEnabled()) {
            return xRay.shouldRender(state);
        }
        return original;
    }
}
