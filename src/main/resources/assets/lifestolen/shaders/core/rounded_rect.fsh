#version 330 core

in vec2 texCoord0;
in vec4 vertexColor;
in vec2 rectHalfSize;
in float cornerRadius;
in vec4 outlineColor;
in float outlineWidth;

out vec4 fragColor;

void main() {
    vec2 p = texCoord0;
    vec2 q = abs(p - rectHalfSize) - (rectHalfSize - cornerRadius);
    float sdf = length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - cornerRadius;
    float aa = fwidth(sdf);

    float outerAlpha = 1.0 - smoothstep(0.0, aa, sdf);
    if (outerAlpha < 0.001) discard;

    float outlineMix = outlineWidth > 0.0
        ? smoothstep(-outlineWidth - aa, -outlineWidth, sdf)
        : 0.0;

    vec4 color = mix(vertexColor, outlineColor, outlineMix);
    fragColor = vec4(color.rgb, color.a * outerAlpha);
}
