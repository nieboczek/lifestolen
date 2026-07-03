package nieboczek.friedfabricsvg.texture

import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.Identifier
import java.util.function.Supplier

object SvgTextureAllocator {
    private val errorTexture by lazy { createErrorTexture() }

    fun getOrCreateErrorTexture(): Identifier = errorTexture

    fun upload(identifier: Identifier, label: Supplier<String>, nativeImage: NativeImage) {
        val texture = DynamicTexture(label, nativeImage)
        Minecraft.getInstance().textureManager.register(identifier, texture)
    }

    private fun createErrorTexture(): Identifier {
        val image = NativeImage(1, 1, true)
        val texture = DynamicTexture(Supplier { "friedfabricsvg/error" }, image)
        val id = Identifier.fromNamespaceAndPath("friedfabricsvg", "svg/error")
        Minecraft.getInstance().textureManager.register(id, texture)
        return id
    }
}
