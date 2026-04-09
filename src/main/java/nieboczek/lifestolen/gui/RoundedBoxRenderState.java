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
        float x,
        float y,
        float width,
        float height,
        float radius,
        int backgroundColor,
        int borderColor,
        ScreenRectangle scissorArea
) implements GuiElementRenderState {
    @Override
    public void buildVertices(VertexConsumer consumer) {
        if (width <= 0f || height <= 0f) return;

        float maxRadius = Math.min(width, height) / 2f;
        float r = Math.max(0f, Math.min(radius, maxRadius));
        int segments = 6; // minimum that seems to look good
        int bg = backgroundColor;

        // Central + side rects
        addQuad(consumer, x + r, y + r, x + width - r, y + height - r, bg);
        addQuad(consumer, x + r, y, x + width - r, y + r, bg); // top
        addQuad(consumer, x + r, y + height - r, x + width - r, y + height, bg); // bottom
        addQuad(consumer, x, y + r, x + r, y + height - r, bg); // left
        addQuad(consumer, x + width - r, y + r, x + width, y + height - r, bg); // right

        // Corners (simplified fan with CCW winding)
        float[] cx = {x + r, x + width - r, x + width - r, x + r};
        float[] cy = {y + r, y + r, y + height - r, y + height - r};
        float[] startAng = {Mth.PI, -Mth.HALF_PI, 0f, Mth.HALF_PI};
        float[] endAng = {Mth.PI + Mth.HALF_PI, 0f, Mth.HALF_PI, Mth.PI};

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

        // ===== BORDER =====

        float borderWidth = 1f;
        int bc = borderColor;
        float bx0 = x;
        float by0 = y;
        float bx1 = x + width;
        float by1 = y + height;

        addQuad(consumer, bx0 + r, by0, bx1 - r, by0 + borderWidth, bc); // top
        addQuad(consumer, bx0 + r, by1 - borderWidth, bx1 - r, by1, bc); // bottom
        addQuad(consumer, bx0, by0 + r, bx0 + borderWidth, by1 - r, bc); // left
        addQuad(consumer, bx1 - borderWidth, by0 + r, bx1, by1 - r, bc); // right

        float[] bcx = {bx0 + r, bx1 - r, bx1 - r, bx0 + r};
        float[] bcy = {by0 + r, by0 + r, by1 - r, by1 - r};

        // Border patches
        float[] bpcx = {bx0 + r + borderWidth, bx1 - r - borderWidth, bx1 - r - borderWidth, bx0 + r + borderWidth};
        float[] bpcy = {by0 + r + borderWidth, by0 + r + borderWidth, by1 - r - borderWidth, by1 - r - borderWidth};

        for (int c = 0; c < 4; c++) {
            float sweep = endAng[c] - startAng[c];
            if (sweep < 0) sweep += Mth.TWO_PI;
            float step = sweep / segments;

            for (int i = 0; i < segments; i++) {
                float a1 = startAng[c] + i * step;
                float a2 = startAng[c] + (i + 1) * step;
                float p1x = bcx[c] + r * Mth.cos(a1), p1y = bcy[c] + r * Mth.sin(a1);
                float p2x = bcx[c] + r * Mth.cos(a2), p2y = bcy[c] + r * Mth.sin(a2);

                consumer.addVertexWith2DPose(pose, bcx[c], bcy[c]).setColor(bc);
                consumer.addVertexWith2DPose(pose, p2x, p2y).setColor(bc);
                consumer.addVertexWith2DPose(pose, p1x, p1y).setColor(bc);
                consumer.addVertexWith2DPose(pose, p1x, p1y).setColor(bc);

            }

            // Border patches
            for (int i = 0; i < segments; i++) {
                float a1 = startAng[c] + i * step;
                float a2 = startAng[c] + (i + 1) * step;
                float p1x = bpcx[c] + r * Mth.cos(a1), p1y = bpcy[c] + r * Mth.sin(a1);
                float p2x = bpcx[c] + r * Mth.cos(a2), p2y = bpcy[c] + r * Mth.sin(a2);

                consumer.addVertexWith2DPose(pose, bpcx[c], bpcy[c]).setColor(bg);
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
        return new ScreenRectangle((int) x, (int) y, (int) width, (int) height);
    }
}
