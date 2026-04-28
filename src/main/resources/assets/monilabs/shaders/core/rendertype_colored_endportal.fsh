#version 330 core

#line 0 1
/*#version 150*/

mat2 mat2_rotate_z(float radians) {
    return mat2(
    cos(radians), -sin(radians),
    sin(radians), cos(radians)
    );
}
#line 3 0

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform float GameTime;
uniform int EndPortalLayers;

in vec4 texProj0;
flat in vec3 vBaseColor;
flat in vec4 vParticleSpeeds;
flat in uvec4 vColors;

const vec3[] COLORS = vec3[](
vec3(0.022087, 0.098399, 0.110818),
vec3(0.011892, 0.095924, 0.089485),
vec3(0.027636, 0.101689, 0.100326),
vec3(0.046564, 0.109883, 0.114838),
vec3(0.064901, 0.117696, 0.097189),
vec3(0.063761, 0.086895, 0.123646),
vec3(0.084817, 0.111994, 0.166380),
vec3(0.097489, 0.154120, 0.091064),
vec3(0.106152, 0.131144, 0.195191),
vec3(0.097721, 0.110188, 0.187229),
vec3(0.133516, 0.138278, 0.148582),
vec3(0.070006, 0.243332, 0.235792),
vec3(0.196766, 0.142899, 0.214696),
vec3(0.047281, 0.315338, 0.321970),
vec3(0.204675, 0.390010, 0.302066),
vec3(0.080955, 0.314821, 0.661491)
);

const mat4 SCALE_TRANSLATE = mat4(
0.5, 0.0, 0.0, 0.25,
0.0, 0.5, 0.0, 0.25,
0.0, 0.0, 1.0, 0.0,
0.0, 0.0, 0.0, 1.0
);

vec3 rgb2hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}
vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

mat4 end_portal_layer(float layer, float speedModifier) {
    mat4 translate = mat4(
    1.0, 0.0, 0.0, 17.0 / layer,
    0.0, 1.0, 0.0, (2.0 + layer / 1.5) * (GameTime * 1.5 * speedModifier),
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0
    );

    mat2 rotate = mat2_rotate_z(radians((layer * layer * 4321.0 + layer * 9.0) * 2.0));

    mat2 scale = mat2((4.5 - layer / 4.0) * 2.0);

    return mat4(scale * rotate) * translate * SCALE_TRANSLATE;
}

void unpackPalette(uvec4 packedColors, out vec4[4] palette) {
    for (int i = 0; i < 4; i++) {
        uint c = packedColors[i];
        palette[i] = vec4(
            float((c >> 16) & 0xFFu) / 255.0, //Red
            float((c >> 8) & 0xFFu) / 255.0,  //Green
            float((c >> 0) & 0xFFu) / 255.0,  //Blue
            1.0 //Alpha
        );
    }
}

void expandPalette(vec4[4] base4, out vec4[16] palette) {
    for (int i = 0; i < 4; i++) {
        vec3 rgb = base4[i].rgb;
        vec3 hsv = rgb2hsv(rgb);

        int baseIdx = i * 4;

        //Original
        palette[baseIdx] = base4[i];

        //Brighter
        palette[baseIdx + 1] = vec4(hsv2rgb(vec3(
        hsv.x,
        clamp(hsv.y - 0.05, 0.0, 1.0),
        clamp(hsv.z + 0.15, 0.0, 1.0)
        )), 1.0);

        //Darker
        palette[baseIdx + 2] = vec4(hsv2rgb(vec3(
        hsv.x,
        clamp(hsv.y + 0.1, 0.0, 1.0),
        clamp(hsv.z - 0.2, 0.0, 1.0)
        )), 1.0);

        //Hue shifted
        palette[baseIdx + 3] = vec4(hsv2rgb(vec3(
        fract(hsv.x + 0.08),
        hsv.y,
        hsv.z
        )), 1.0);
    }
}

out vec4 fragColor;

void main() {
    float speeds[4] = float[](vParticleSpeeds.x, vParticleSpeeds.y, vParticleSpeeds.z, vParticleSpeeds.w);
    vec4 palette[4];
    vec4 expandedPalette[16];
    unpackPalette(vColors, palette);
    expandPalette(palette, expandedPalette);

    vec3 color = textureProj(Sampler0, texProj0).rgb * vBaseColor.rgb;
    for (int i = 0; i < EndPortalLayers; i++) {
        vec3 textureColor = textureProj(Sampler1, texProj0 * end_portal_layer(float(i + 1), speeds[i / 4])).rgb;
        bool isStar = length(textureColor) > 0.1;
        bool isBlackish = length(expandedPalette[i].rgb) < 0.1;
        if (isBlackish && isStar)
            color = expandedPalette[i].rgb;
        else
            color += textureColor * expandedPalette[i].rgb;
    }
    fragColor = vec4(color, 1.0);
}
