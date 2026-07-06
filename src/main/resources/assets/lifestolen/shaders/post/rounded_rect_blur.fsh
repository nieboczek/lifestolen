#version 330

#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform BlurConfig {
    vec2 BlurDir;
    float Radius;
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
    vec2 oneTexel = 1.0 / InSize;
    vec2 sampleStep = oneTexel * BlurDir;

    vec4 blurred = vec4(0.0);
    float actualRadius = Radius >= 0.5 ? round(Radius) : float(MenuBlurRadius);
    for (float a = -actualRadius + 0.5; a <= actualRadius; a += 2.0) {
        blurred += texture(InSampler, texCoord + sampleStep * a);
    }
    blurred += texture(InSampler, texCoord + sampleStep * actualRadius) / 2.0;
    blurred /= (actualRadius + 0.5);

    vec4 original = texture(InSampler, texCoord);

    vec2 pos = texCoord - RectCenter;
    float sdf = roundedRectSDF(pos, RectHalfSize, CornerRadius);
    float mask = 1.0 - smoothstep(0.0, Feather, sdf);

    fragColor = mix(original, blurred, mask);
}
