#version 330 core

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec2 UV1;
in float LineWidth;

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
out vec2 halfSize;
out float cornerRadius;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    texCoord0 = UV0;
    vertexColor = Color;
    halfSize = UV1;
    cornerRadius = LineWidth;
}
