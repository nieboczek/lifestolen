#version 330 core

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec2 UV1;
in ivec2 UV2;
in float LineWidth;
in vec4 Normal;

layout (std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

layout (std140) uniform Projection {
    mat4 ProjMat;
};

out vec2 texCoord0;
out vec4 vertexColor;
out vec2 rectHalfSize;
out float cornerRadius;
out vec4 outlineColor;
out float outlineWidth;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    texCoord0 = UV0;
    vertexColor = Color * ColorModulator;
    rectHalfSize = UV1;
    cornerRadius = LineWidth;
    outlineColor = Normal;
    outlineColor.a = float(UV2.y) / 255.0;
    outlineWidth = float(UV2.x) / 256.0;
}
