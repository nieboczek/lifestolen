package nieboczek.lifestolen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class Renderer3d {
    public static MultiBufferSource.BufferSource bufferSource;
    public static PoseStack poseStack;
    public static float tickDelta;

    public static Vec3 computePartialTickPos(Vec3 oldPos, Vec3 pos, Vec3 cameraPos) {
        double newX = oldPos.x + (pos.x - oldPos.x) * tickDelta - cameraPos.x;
        double newY = oldPos.y + (pos.y - oldPos.y) * tickDelta - cameraPos.y;
        double newZ = oldPos.z + (pos.z - oldPos.z) * tickDelta - cameraPos.z;
        return new Vec3(newX, newY, newZ);
    }

    public static void renderCircleOutline(int segments, int color, float radius, Vec3 pos) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.lines());

        poseStack.translate(pos);

        Matrix4f positionMatrix = poseStack.last().pose();
        var normalMatrix = poseStack.last();

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2.0 * Math.PI * i / segments);
            float angle2 = (float) (2.0 * Math.PI * (i + 1) / segments);

            float x1 = (float) Math.cos(angle1) * radius;
            float z1 = (float) Math.sin(angle1) * radius;
            float x2 = (float) Math.cos(angle2) * radius;
            float z2 = (float) Math.sin(angle2) * radius;

            // Line segment: vertex 1
            consumer.addVertex(positionMatrix, x1, 0, z1)
                    .setColor(color)
                    .setLineWidth(10.0f)
                    .setNormal(normalMatrix, 0f, 1f, 0f);

            // Line segment: vertex 2
            consumer.addVertex(positionMatrix, x2, 0, z2)
                    .setColor(color)
                    .setLineWidth(10.0f)
                    .setNormal(normalMatrix, 0f, 1f, 0f);
        }

        poseStack.translate(-pos.x, -pos.y, -pos.z);

        bufferSource.endBatch(RenderTypes.lines());
    }
}
