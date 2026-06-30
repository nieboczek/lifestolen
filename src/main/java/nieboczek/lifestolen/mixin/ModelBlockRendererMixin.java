package nieboczek.lifestolen.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import nieboczek.lifestolen.module.FullBrightModule;
import nieboczek.lifestolen.module.XRayModule;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin {
    @ModifyExpressionValue(
        method = "tesselateBlock", // nice spelling Mojang
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;ambientOcclusion:Z", opcode = Opcodes.GETFIELD)
    )
    private boolean tesselateBlock(boolean original) {
        var xRay = XRayModule.INSTANCE;
        if ((!xRay.getEnabled() || !xRay.getFullBright()) && !FullBrightModule.INSTANCE.getEnabled()) {
            return original;
        }
        return false;
    }
}
