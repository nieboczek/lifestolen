package nieboczek.lifestolen.mixin;

import net.minecraft.client.gui.screens.Screen;
import nieboczek.lifestolen.CursedWebView;
import org.spongepowered.asm.mixin.*;
import tytoo.grapheneui.api.widget.GrapheneWebViewWidget;

@Mixin(GrapheneWebViewWidget.class)
public class GrapheneWebViewWidgetMixin implements CursedWebView {
    @Mutable
    @Shadow
    @Final
    private Screen screen;

    @Override
    public void lifestolen$setScreen(Screen screen) {
        this.screen = screen;
    }
}
