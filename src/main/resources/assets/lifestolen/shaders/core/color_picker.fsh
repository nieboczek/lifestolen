#version 330 core

in vec2 texCoord0;
in vec4 vertexColor;
in vec2 halfSize;
in float cornerRadius;

out vec4 fragColor;

vec3 hsv2rgb(float h, float s, float v) {
    vec3 rgb = clamp(abs(mod(h * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0, 0.0, 1.0);
    return v * mix(vec3(1.0), rgb, s);
}

void main() {
    vec2 q = abs(texCoord0 - halfSize) - (halfSize - cornerRadius);
    float sdf = length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - cornerRadius;
    float aa = fwidth(sdf);
    float outerAlpha = 1.0 - smoothstep(0.0, aa, sdf);
    if (outerAlpha < 0.001) discard;

    vec2 norm = texCoord0 / (halfSize * 2.0);

    float type = vertexColor.a * 255.0;
    float hue = vertexColor.r;
    float sat = vertexColor.g;
    float val = vertexColor.b;

    vec3 rgb;
    float alpha;

    if (type < 0.5) {
        sat = norm.x;
        val = 1.0 - norm.y;
        rgb = hsv2rgb(hue, sat, val);
        alpha = 1.0;
    } else if (type < 1.5) {
        rgb = hsv2rgb(norm.y, 1.0, 1.0);
        alpha = 1.0;
    } else {
        ivec2 checkerCoord = ivec2(texCoord0) / 4;
        float check = float((checkerCoord.x + checkerCoord.y) & 1);
        vec3 checker = mix(vec3(1.0), vec3(0.6), check);
        rgb = hsv2rgb(hue, sat, val);
        alpha = 1.0 - norm.y;
        rgb = mix(checker, rgb, alpha);
    }

    fragColor = vec4(rgb, outerAlpha);
}
