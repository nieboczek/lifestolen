package nieboczek.lifestolen.gui.notification

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.FormattedText
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.command.Commands
import nieboczek.lifestolen.gui.render.blurredRoundedRect
import nieboczek.lifestolen.gui.widget.ScreenState
import nieboczek.lifestolen.module.Module
import kotlin.math.min

object Notifications {
    private const val TOTAL_TICKS = 20f * 5f
    private const val SLIDE_TICKS = 10f

    private val mc = Minecraft.getInstance()
    private val fontSmall = Lifestolen.fontSmall
    private val notifications = mutableListOf<Notification>()

    fun render(graphics: GuiGraphicsExtractor, dt: Float) {
        if (notifications.isEmpty()) return

        // This requires that nothing else calls this function this frame
        graphics.blurBeforeThisStratum()

        val width = mc.window.guiScaledWidth
        val height = mc.window.guiScaledHeight

        val notifWidth = 128
        val notifHeight = 24
        val notifGap = 4
        val edgePadding = 8
        val textPadding = 3
        val targetX = width - notifWidth - edgePadding
        val bottomY = height - notifHeight - edgePadding

        notifications.retainAll {
            it.visibilityTimer -= dt
            it.visibilityTimer > 0f
        }

        for (i in notifications.indices) {
            val notification = notifications[i]
            val targetY = bottomY - (notifications.size - 1 - i) * (notifHeight + notifGap)
            notification.animateY(targetY.toFloat(), dt)
        }

        for (notification in notifications.asReversed()) {
            val slideProgress = min(
                1f, min(TOTAL_TICKS - notification.visibilityTimer, notification.visibilityTimer) / SLIDE_TICKS
            )
            val x = (targetX + (width - targetX) * (1f - slideProgress)).toInt()
            val y = notification.y.toInt()

            graphics.blurredRoundedRect(
                x,
                y,
                notifWidth,
                notifHeight,
                withAlpha(0x92000000.toInt(), slideProgress),
                withAlpha(ScreenState.OUTLINE_COLOR, slideProgress),
                ScreenState.OUTLINE_WIDTH,
                4f,
                16f,
            )

            graphics.textWithWordWrap(
                fontSmall,
                notification.message,
                x + textPadding,
                y + textPadding,
                notifWidth - (textPadding * 2),
                withAlpha(-1, slideProgress),
                false
            )
        }
    }

    fun add(message: FormattedText) {
        notifications.add(Notification(message))
        if (notifications.size > 6) {
            val notif = notifications.first()
            notif.visibilityTimer = min(notif.visibilityTimer, SLIDE_TICKS)
        }
    }

    fun clear() = notifications.clear()

    fun addModuleToggleNotification(module: Module) = add(
        FormattedText.composite(
            FormattedText.of("${module.name} "),
            Commands.formattedBoolean(module.enabled),
        )
    )

    private fun withAlpha(color: Int, alpha: Float): Int {
        val a = ((color ushr 24) and 0xFF) * alpha
        return (color and 0xFFFFFF) or (a.toInt() shl 24)
    }

    private class Notification(val message: FormattedText) {
        var visibilityTimer = TOTAL_TICKS
        var y = 0f
        var yFrom = 0f
        var yTo = Float.NaN
        var yTimer = 0f

        fun animateY(targetY: Float, dt: Float) {
            if (targetY != yTo) {
                if (!yTo.isNaN()) {
                    yFrom = y
                    yTimer = SLIDE_TICKS
                }
                yTo = targetY
            }

            if (yTimer > 0f) {
                yTimer -= dt
                val t = if (yTimer <= 0f) 1f else 1f - yTimer / SLIDE_TICKS
                y = yFrom + (yTo - yFrom) * t
            } else {
                y = yTo
            }
        }
    }
}