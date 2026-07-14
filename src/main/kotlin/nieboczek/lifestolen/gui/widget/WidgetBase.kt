package nieboczek.lifestolen.gui.widget

import net.minecraft.client.input.MouseButtonEvent

abstract class Widget {
    var bounds: Bounds = Bounds()

    open fun getVisibleChildren() = listOf<Widget>()
    open fun tick(dt: Float) {}
}

interface Hoverable {
    var hovered: Boolean
    var hoverProgress: Float
}

interface Draggable {
    var dragging: Boolean
    var dragProgress: Float

    fun drag(x: Double, y: Double)
}

interface Clickable {
    fun click(button: Int): Action

    enum class Action {
        NONE, CAPTURE_KEY;
    }
}

interface KeyCapturer {
    fun captureKey(key: Int): Action

    enum class Action {
        NONE, STOP_CAPTURING;
    }
}

class ClickableWidget(
    private val onClick: (Int) -> Clickable.Action
) : Widget(), Clickable {
    override fun click(button: Int) = onClick(button)
}

class Bounds(val x: Int, val y: Int, width: Int, height: Int) {
    val x2: Int = x + width
    val y2: Int = y + height

    constructor() : this(0, 0, 0, 0)

    fun isInBounds(event: MouseButtonEvent) = isInBounds(event.x, event.y)
    fun isInBounds(cx: Double, cy: Double) = cx >= x && cy >= y && cx <= x2 && cy <= y2
}
