package nieboczek.lifestolen.module

object NoRenderModule : Module("No Render", Category.VISUALS) {
    val noVignette by boolean("No Vignette", true)
}
