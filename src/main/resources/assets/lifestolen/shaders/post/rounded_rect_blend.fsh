#version 330

uniform sampler2D InSampler;
uniform sampler2D BlurSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
    vec2 BlurSize;
};

#define MAX_RECTS 8

layout(std140) uniform RoundedRectConfig {
    vec2 RectCenter0; vec2 RectHalfSize0; float CornerRadius0; float Feather0;
    vec2 RectCenter1; vec2 RectHalfSize1; float CornerRadius1; float Feather1;
    vec2 RectCenter2; vec2 RectHalfSize2; float CornerRadius2; float Feather2;
    vec2 RectCenter3; vec2 RectHalfSize3; float CornerRadius3; float Feather3;
    vec2 RectCenter4; vec2 RectHalfSize4; float CornerRadius4; float Feather4;
    vec2 RectCenter5; vec2 RectHalfSize5; float CornerRadius5; float Feather5;
    vec2 RectCenter6; vec2 RectHalfSize6; float CornerRadius6; float Feather6;
    vec2 RectCenter7; vec2 RectHalfSize7; float CornerRadius7; float Feather7;
    int RectCount;
};

in vec2 texCoord;

out vec4 fragColor;

float roundedRectSDF(vec2 p, vec2 halfSize, float r) {
    vec2 d = abs(p) - halfSize + vec2(r);
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - r;
}

float rectMask(vec2 uv, vec2 center, vec2 halfSize, float radius, float feather) {
    vec2 pos = (uv - center) * OutSize;
    vec2 hs = halfSize * OutSize;
    float sdf = roundedRectSDF(pos, hs, radius);
    return 1.0 - smoothstep(0.0, feather, sdf);
}

void main() {
    vec4 original = texture(InSampler, texCoord);
    vec4 blurred = texture(BlurSampler, texCoord);

    float mask = rectMask(texCoord, RectCenter0, RectHalfSize0, CornerRadius0, Feather0);
    if (RectCount > 1) mask = max(mask, rectMask(texCoord, RectCenter1, RectHalfSize1, CornerRadius1, Feather1));
    if (RectCount > 2) mask = max(mask, rectMask(texCoord, RectCenter2, RectHalfSize2, CornerRadius2, Feather2));
    if (RectCount > 3) mask = max(mask, rectMask(texCoord, RectCenter3, RectHalfSize3, CornerRadius3, Feather3));
    if (RectCount > 4) mask = max(mask, rectMask(texCoord, RectCenter4, RectHalfSize4, CornerRadius4, Feather4));
    if (RectCount > 5) mask = max(mask, rectMask(texCoord, RectCenter5, RectHalfSize5, CornerRadius5, Feather5));
    if (RectCount > 6) mask = max(mask, rectMask(texCoord, RectCenter6, RectHalfSize6, CornerRadius6, Feather6));
    if (RectCount > 7) mask = max(mask, rectMask(texCoord, RectCenter7, RectHalfSize7, CornerRadius7, Feather7));

    fragColor = mix(original, blurred, mask);
}
