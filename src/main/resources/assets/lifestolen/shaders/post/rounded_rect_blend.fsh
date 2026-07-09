#version 330

uniform sampler2D InSampler;
uniform sampler2D BlurSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
    vec2 BlurSize;
};

layout(std140) uniform RoundedRectConfig {
    vec2 RectCenter;
    vec2 RectHalfSize;
    float CornerRadius;
    float Feather;
};

in vec2 texCoord;

out vec4 fragColor;

float roundedRectSDF(vec2 p, vec2 halfSize, float r) {
    vec2 d = abs(p) - halfSize + vec2(r);
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - r;
}

void main() {
    vec4 original = texture(InSampler, texCoord);
    vec4 blurred = texture(BlurSampler, texCoord);

    vec2 pos = (texCoord - RectCenter) * OutSize;
    vec2 halfSize = RectHalfSize * OutSize;
    float sdf = roundedRectSDF(pos, halfSize, CornerRadius);
    float mask = 1.0 - smoothstep(0.0, Feather, sdf);

    fragColor = mix(original, blurred, mask);
}
