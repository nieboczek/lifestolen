package nieboczek.lifestolen.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;

public record RoundedBoxRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        int x0,
        int y0,
        int x1,
        int y1,
        float radius,
        int backgroundColor,
        int borderColor,
        ScreenRectangle scissorArea
) implements GuiElementRenderState {
    @Override
    public void buildVertices(VertexConsumer consumer) {
        float width = (float)(x1 - x0);
        float height = (float)(y1 - y0);
        float x = x0;
        float y = y0;
        int bg = backgroundColor;

        if (width <= 0f || height <= 0f) return;

        float maxRadius = Math.min(width, height) / 2f;
        float r = Math.max(0f, Math.min(radius, maxRadius));
        int segments = 6; // minimum that seems to look good

        // Central + side rects
        addQuad(consumer, x + r, y + r, x + width - r, y + height - r, bg);
        addQuad(consumer, x + r, y, x + width - r, y + r, bg); // top
        addQuad(consumer, x + r, y + height - r, x + width - r, y + height, bg); // bottom
        addQuad(consumer, x, y + r, x + r, y + height - r, bg); // left
        addQuad(consumer, x + width - r, y + r, x + width, y + height - r, bg); // right

        // Corners (simplified fan with CCW winding)
        float[] cx = {x + r, x + width - r, x + width - r, x + r};
        float[] cy = {y + r, y + r, y + height - r, y + height - r};
        float[] startAng = {Mth.PI, Mth.HALF_PI, 0f, Mth.PI + Mth.HALF_PI};
        float[] endAng = {Mth.HALF_PI, 0f, Mth.PI + Mth.HALF_PI, Mth.PI};

        for (int c = 0; c < 4; c++) {
            float sweep = endAng[c] - startAng[c];
            if (sweep < 0) sweep += Mth.TWO_PI;
            float step = sweep / segments;

            for (int i = 0; i < segments; i++) {
                float a1 = startAng[c] + i * step;
                float a2 = startAng[c] + (i + 1) * step;
                float p1x = cx[c] + r * Mth.cos(a1), p1y = cy[c] + r * Mth.sin(a1);
                float p2x = cx[c] + r * Mth.cos(a2), p2y = cy[c] + r * Mth.sin(a2);

                consumer.addVertexWith2DPose(pose, cx[c], cy[c]).setColor(bg);
                consumer.addVertexWith2DPose(pose, p2x, p2y).setColor(bg);
                consumer.addVertexWith2DPose(pose, p1x, p1y).setColor(bg);
                consumer.addVertexWith2DPose(pose, p1x, p1y).setColor(bg);
            }
        }
    }

    private void addQuad(VertexConsumer consumer, float x0, float y0, float x1, float y1, int bg) {
        consumer.addVertexWith2DPose(pose, x0, y0).setColor(bg);
        consumer.addVertexWith2DPose(pose, x0, y1).setColor(bg);
        consumer.addVertexWith2DPose(pose, x1, y1).setColor(bg);
        consumer.addVertexWith2DPose(pose, x1, y0).setColor(bg);
    }

    @Override
    public RenderPipeline pipeline() {
        return pipeline;
    }

    @Override
    public TextureSetup textureSetup() {
        return textureSetup;
    }

    @Override
    public ScreenRectangle scissorArea() {
        return scissorArea;
    }

    @Override
    public ScreenRectangle bounds() {
        return new ScreenRectangle(x0, y0, x1 - x0, y1 - y0);
    }
}
