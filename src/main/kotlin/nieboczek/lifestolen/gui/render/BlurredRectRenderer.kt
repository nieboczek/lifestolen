package nieboczek.lifestolen.gui.render

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.resource.GraphicsResourceAllocator
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LevelTargetBundle
import nieboczek.lifestolen.Lifestolen.identifier

data class BlurRectData(
    val centerX: Float,
    val centerY: Float,
    val halfSizeX: Float,
    val halfSizeY: Float,
    val cornerRadius: Float,
    val feather: Float,
    val blurRadius: Float,
)

object BlurredRectRenderer {
    private const val MAX_RECTS = 8
    private val rects = mutableListOf<BlurRectData>()

    fun isActive() = rects.isNotEmpty()

    fun addRect(data: BlurRectData) {
        rects.add(data)
    }

    private fun writeAllRects(buffer: GpuBuffer) {
        buffer.map(false, true).use { view ->
            val b = Std140Builder.intoBuffer(view.data())
            for (i in 0 until MAX_RECTS) {
                if (i < rects.size) {
                    val r = rects[i]
                    b.putVec2(r.centerX, r.centerY)
                    b.putVec2(r.halfSizeX, r.halfSizeY)
                    b.putFloat(r.cornerRadius)
                    b.putFloat(r.feather)
                } else {
                    b.putVec2(0f, 0f)
                    b.putVec2(0f, 0f)
                    b.putFloat(0f)
                    b.putFloat(0f)
                }
            }
            b.putInt(rects.size)
        }
    }

    fun render(renderTarget: RenderTarget, resourcePool: GraphicsResourceAllocator) {
        val chain = Minecraft.getInstance().shaderManager.getPostChain(
            identifier("rounded_rect_blur"),
            LevelTargetBundle.MAIN_TARGETS
        ) ?: return

        val rectsUboSize = MAX_RECTS * (8 + 8 + 4 + 4) + 4
        val rectsUbo = RenderSystem.getDevice().createBuffer(
            { "RoundedRectConfig (multi)" },
            GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_MAP_WRITE,
            rectsUboSize.toLong()
        )
        writeAllRects(rectsUbo)

        var blurPasses = 0
        for (pass in chain.passes) {
            val bBuffer = pass.customUniforms["BlurConfig"]
            if (bBuffer != null) {
                val dirX = if (blurPasses == 0) 1.0f else 0.0f
                val dirY = if (blurPasses == 0) 0.0f else 1.0f
                bBuffer.map(false, true).use { view ->
                    Std140Builder.intoBuffer(view.data())
                        .putVec2(dirX, dirY)
                        .putFloat(rects.first().blurRadius)
                }
                blurPasses++
            }

            if (pass.customUniforms.containsKey("RoundedRectConfig")) {
                val old = pass.customUniforms.put("RoundedRectConfig", rectsUbo)
                old?.close()
            }
        }

        @Suppress("DEPRECATION")
        chain.process(renderTarget, resourcePool)
        rectsUbo.close()
        rects.clear()
    }
}
