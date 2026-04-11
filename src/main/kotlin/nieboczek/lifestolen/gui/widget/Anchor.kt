package nieboczek.lifestolen.gui.widget

data class Anchor(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val alignment: Alignment,
    val guiWidth: Int,
    val guiHeight: Int
) {
    fun calculatePosition(): Pair<Int, Int> {
        val (cx, cy) = alignment.calculatePosition(width, height, guiWidth, guiHeight)
        return Pair(cx + x, cy + y)
    }

    enum class Alignment {
        TOP_LEFT,
        TOP_RIGHT,
        TOP_CENTER,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        BOTTOM_CENTER,
        CENTER_LEFT,
        CENTER_RIGHT,
        CENTER;

        fun calculatePosition(
            widgetWidth: Int,
            widgetHeight: Int,
            guiWidth: Int,
            guiHeight: Int
        ): Pair<Int, Int> {
            return when (this) {
                TOP_LEFT -> Pair(0, 0)
                TOP_RIGHT -> Pair(guiWidth - widgetWidth, 0)
                TOP_CENTER -> Pair(guiWidth / 2 - widgetWidth / 2, 0)
                BOTTOM_LEFT -> Pair(0, guiHeight - widgetHeight)
                BOTTOM_RIGHT -> Pair(guiWidth - widgetWidth, guiHeight - widgetHeight)
                BOTTOM_CENTER -> Pair(guiWidth / 2 - widgetWidth / 2, guiHeight - widgetHeight)
                CENTER_LEFT -> Pair(0, guiHeight / 2 - widgetHeight / 2)
                CENTER_RIGHT -> Pair(guiWidth - widgetWidth, guiHeight / 2 - widgetHeight / 2)
                CENTER -> Pair(guiWidth / 2 - widgetWidth / 2, guiHeight / 2 - widgetHeight / 2)
            }
        }
    }
}
