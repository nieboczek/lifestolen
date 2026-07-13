package nieboczek.lifestolen.gui.widget

import nieboczek.lifestolen.Lifestolen

object ScreenState {
    const val OUTLINE_COLOR = 0x92888888.toInt()
    const val HOVERED_OUTLINE_COLOR = 0x92BBBBBB.toInt()
    const val OUTLINE_WIDTH = 2
    const val FONT_BIG_HEIGHT = 12
    const val FONT_HEIGHT = 8
    const val FONT_SMALL_HEIGHT = 6
    const val FONT_EXTRA_SMALL_HEIGHT = 4
    const val SETTING_GAP = 6
    const val MODULE_INSIDE_V_PADDING = 4

    val font = Lifestolen.font
    val fontBig = Lifestolen.fontBig
    val fontSmall = Lifestolen.fontSmall
    val fontExtraSmall = Lifestolen.fontExtraSmall

    var currentlyConfiguring: ModuleWidget? = null
    var debugMode = false
    var guiScale = 1f
    var rainbowColor = 0
    var darkRainbowColor = 0

    fun lerpColor(start: Int, target: Int, progress: Float): Int {
        val startA = (start shr 24) and 0xFF
        val startR = (start shr 16) and 0xFF
        val startG = (start shr 8) and 0xFF
        val startB = start and 0xFF
        val targetA = (target shr 24) and 0xFF
        val targetR = (target shr 16) and 0xFF
        val targetG = (target shr 8) and 0xFF
        val targetB = target and 0xFF
        val a = (startA + ((targetA - startA) * progress).toInt()) shl 24
        val r = (startR + ((targetR - startR) * progress).toInt()) shl 16
        val g = (startG + ((targetG - startG) * progress).toInt()) shl 8
        val b = startB + ((targetB - startB) * progress).toInt()
        return a or r or g or b
    }

    fun lerpOutlineColor(progress: Float): Int = lerpColor(OUTLINE_COLOR, HOVERED_OUTLINE_COLOR, progress)
}
