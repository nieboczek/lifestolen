package nieboczek.lifestolen.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.gui.font.FontTexture;
import nieboczek.lifestolen.Lifestolen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Supplier;

@Mixin(FontTexture.class)
public class FontTextureMixin {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/SamplerCache;getRepeat(Lcom/mojang/blaze3d/textures/FilterMode;)Lcom/mojang/blaze3d/textures/GpuSampler;"))
    public FilterMode init(FilterMode filterMode, @Local(argsOnly = true) Supplier<String> identifierSupplier) {
        String identifier = identifierSupplier.get();
        if (identifier.startsWith(Lifestolen.MOD_ID)) {
            Lifestolen.LOG.info("[FontTextureMixin::init] Set filter mode of {} to FilterMode.LINEAR", identifier);
            return FilterMode.LINEAR;
        }
        return filterMode;
    }
}
