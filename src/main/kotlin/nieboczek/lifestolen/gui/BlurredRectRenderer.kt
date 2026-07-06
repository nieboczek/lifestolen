package nieboczek.lifestolen.gui

import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.resource.GraphicsResourceAllocator
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LevelTargetBundle
import net.minecraft.client.renderer.PostChain
import nieboczek.lifestolen.Lifestolen.identifier

object BlurredRectRenderer {
    var active = false
    var blurRadius = 0f
    var centerX = 0.5f
    var centerY = 0.5f
    var halfSizeX = 0.5f
    var halfSizeY = 0.5f
    var cornerRadius = 0.06f
    var feather = 0.03f

    fun renderOnConfigScreen(renderTarget: RenderTarget, resourcePool: GraphicsResourceAllocator) {
        val chain: PostChain? = Minecraft.getInstance().shaderManager.getPostChain(
            identifier("rounded_rect_blur"),
            LevelTargetBundle.MAIN_TARGETS
        )
        if (chain == null) {
            return
        }

        var blurPasses = 0
        for (pass in chain.passes) {
            val bcBuffer = pass.customUniforms["BlurConfig"]
            if (bcBuffer != null) {
                bcBuffer.map(false, true).use { view ->
                    val builder = Std140Builder.intoBuffer(view.data())
                    val dirX = if (blurPasses == 0) 1.0f else 0.0f
                    val dirY = if (blurPasses == 0) 0.0f else 1.0f
                    builder.putVec2(dirX, dirY)
                    builder.putFloat(blurRadius)
                }
                blurPasses++
            }

            val rbBuffer = pass.customUniforms["RoundedRectConfig"]
            rbBuffer?.map(false, true)?.use { view ->
                val builder = Std140Builder.intoBuffer(view.data())
                builder.putVec2(centerX, centerY)
                builder.putVec2(halfSizeX, halfSizeY)
                builder.putFloat(cornerRadius)
                builder.putFloat(feather)
            }
        }

        chain.process(renderTarget, resourcePool)
        active = false
    }
}
