#version 330 core

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

uniform float GameTime;
uniform int StarLayers;

in vec3 vViewRay;
flat in vec3 vBaseColor;
flat in vec4 vParticleSpeeds;
flat in uvec4 vColors;
flat in vec4 vAnchor;
flat in float vIntegrity;
flat in uint vRenderEffect;

const int WEB_SHELLS = 3;
const float TWO_PI = 6.28318530718;
const uint RENDER_EFFECT_SHATTERED = 1u;
const uint RENDER_EFFECT_CORRUPTED = 2u;

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

// PCG hash - Jarzynski & Olano, "Hash Functions for GPU Rendering", JCGT 2020
uint pcg_hash(uint v)  {
    uint state = v * 747796405u + 2891336453u;
    uint word = ((state >> ((state >> 28u) + 4u)) ^ state) * 277803737u;
    return (word >> 22u) ^ word;
}

// Returns the hash in [0;1] range
float hash01(uint v) {
    return float(pcg_hash(v)) / 4294967295.0;
}

// Returns the hash in [0;1] range
vec3 hash01_vec3(vec3 p) {
    uvec3 q = uvec3(ivec3(p));
    uint h = pcg_hash(q.x ^ pcg_hash(q.y ^ pcg_hash(q.z)));
    return vec3(hash01(h), hash01(h ^ 0x9E3779B9u), hash01(h ^ 0x85EBCA6Bu));
}

vec4 sample_cosmic_web(vec3 viewRay, float webScale, float filamentWidth, float clusterSize) {
    vec3 sample1 = texture(Sampler2, vec2(3.1574, 4.8511) + viewRay.yz * webScale).rgb;
    vec3 sample2 = texture(Sampler2, vec2(7.9221, 6.3466) + viewRay.xz * webScale).rgb;
    vec3 sample3 = texture(Sampler2, vec2(1.3213, 9.7544) + viewRay.xy * webScale).rgb;

    float filamentMask1 = 1.0 - smoothstep(0.0, filamentWidth, sample1.r);
    float filamentMask2 = 1.0 - smoothstep(0.0, filamentWidth, sample2.r);
    float filamentMask3 = 1.0 - smoothstep(0.0, filamentWidth, sample3.r);

    float clusterMask1 = 1.0 - smoothstep(0.0, clusterSize, sample1.g);
    float clusterMask2 = 1.0 - smoothstep(0.0, clusterSize, sample2.g);
    float clusterMask3 = 1.0 - smoothstep(0.0, clusterSize, sample3.g);

    vec3 weight = pow(abs(viewRay), vec3(5.0));
    // Normalize to make the sum 1
    weight /= (weight.x + weight.y + weight.z);

    float filamentMask = filamentMask1 * weight.x + filamentMask2 * weight.y + filamentMask3 * weight.z;
    float clusterMask = clusterMask1 * weight.x + clusterMask2 * weight.y + clusterMask3 * weight.z;
    float shimmerMask = sample1.b * weight.x + sample2.b * weight.y + sample3.b * weight.z;
    float edgeDistance = sample1.r * weight.x + sample2.r * weight.y + sample3.r * weight.z;

    return vec4(filamentMask, clusterMask, shimmerMask, edgeDistance);
}

vec3 triplanar_blend(sampler2D textureSampler, vec3 viewRay, float biasScale, float textureScale) {
    vec3 sample1 = texture(textureSampler, vec2(3.1574, 4.8511) * biasScale + viewRay.yz * textureScale).rgb;
    vec3 sample2 = texture(textureSampler, vec2(7.9221, 6.3466) * biasScale + viewRay.xz * textureScale).rgb;
    vec3 sample3 = texture(textureSampler, vec2(1.3213, 9.7544) * biasScale + viewRay.xy * textureScale).rgb;

    vec3 weight = pow(abs(viewRay), vec3(5.0));
    // Normalize to make the sum 1
    weight /= (weight.x + weight.y + weight.z);
    return sample1 * weight.x + sample2 * weight.y + sample3 * weight.z;
}

vec3 rotate_around_axis(vec3 v, vec3 axis, float angle) {
    // Rodgrigues rotation formula
    return v * cos(angle) + cross(axis, v) * sin(angle) + axis * dot(axis, v) * (1.0 - cos(angle));
}

vec3 fibonacci_sphere_point(float num, float total) {
    const float GOLDEN_ANGLE = 2.39996323;
    float y = 1.0 - 2 * (num + 0.5) / total;
    float r = sqrt(1 - y * y);
    float phi = num * GOLDEN_ANGLE;
    return vec3(r * cos(phi), y, r * sin(phi));
}

vec3 calc_layer_view_ray(vec3 viewRayNormalized, float layer) {
    float layerRadius = vAnchor.w * pow(1.3, StarLayers - layer);
    float tca = dot(vAnchor.xyz, viewRayNormalized);
    float dSquared = dot(vAnchor.xyz, vAnchor.xyz) - tca * tca;
    float thc = sqrt(layerRadius * layerRadius - dSquared);
    float t1 = tca + thc;
    vec3 p1 = viewRayNormalized * t1;
    return normalize(p1 - vAnchor.xyz);
}

float calc_shimmer_effect(float shimmerMask) {
    float time = fract(GameTime * 150.0 - shimmerMask * 3.0);
    float pulseLength = 0.3;
    float pulse = smoothstep(0.0, pulseLength * 0.5, time) * (1.0 - smoothstep(pulseLength * 0.5, pulseLength, time));
    return 0.3 + pulse * 0.7;
}

vec3 shift_view_ray(vec4 shard, float shiftScale) {
    vec3 viewRayNormalized = normalize(vViewRay);
    float shattered = step(0.1, float(vRenderEffect & RENDER_EFFECT_SHATTERED));
    return normalize(viewRayNormalized + shard.xyz * shiftScale * shattered);
}

vec3 refract_layer_ray(vec3 layerViewRay, vec4 shard, float layer) {
    float depthScale = log2((float(StarLayers) - layer + 2.0) / 2.0);
    float shattered = step(0.1, float(vRenderEffect & RENDER_EFFECT_SHATTERED));
    vec3 tangentShift = shard.xyz - layerViewRay * dot(shard.xyz, layerViewRay);
    return normalize(layerViewRay + tangentShift * depthScale * shattered);
}

vec3 calc_web_shell_ray(vec4 shard, float layer) {
    vec3 shellViewRay = calc_layer_view_ray(normalize(vViewRay), layer);
    vec3 refractedRay = refract_layer_ray(shellViewRay, shard, layer);
    float rotationAngle = TWO_PI * GameTime * 0.054;
    return rotate_around_axis(refractedRay, vec3(0.0, 1.0, 0.0), rotationAngle);
}

float calc_web_shell_density(vec4 shard, float shell) {
    float shellLayer = shell * StarLayers / float(WEB_SHELLS);
    vec3 rayDir = calc_web_shell_ray(shard, shellLayer);
    float filamentWidth = 0.20 * pow(vIntegrity, 2.0);
    float clusterWidth = 0.20 * pow(vIntegrity, 2.0);
    vec4 webData = sample_cosmic_web(rayDir, 0.5, filamentWidth, clusterWidth);

    float shimmer = calc_shimmer_effect(webData.z);
    float webMask = max(webData.x, webData.y) * shimmer;

    float voidBand = mix(filamentWidth, 1.5, pow(vIntegrity, 2.0));
    float voidMask = 1.0 - smoothstep(voidBand * 0.7, voidBand, webData.w);
    float voidDensity = 0.2 * voidMask;
    float density = min(voidDensity + webMask, 1.0);
    return density;
}

// @ENDESGA
// splat-field forked from https://www.shadertoy.com/view/clGyWm
// The MIT License
// Copyright (c) 2023 Inigo Quilez
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
// https://www.youtube.com/c/InigoQuilez
// https://iquilezles.org
// An adapted version of shard noise
float shard_noise(vec3 point, float sharpness) {
    vec3 cell = floor(point);
    vec3 cellLocal = fract(point);

    float valueSum = 0.0;
    float weightSum = 0.0;
    for (int z = -1; z <= 1; z++) {
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                vec3 cellOffset = vec3(x, y, z);
                vec3 neighborCell = cell + cellOffset;
                vec3 delta = cellLocal - (cellOffset + hash01_vec3(neighborCell));

                float weight = exp2(-TWO_PI * dot(delta, delta));
                // tanh deconstruction and optimization by @Xor
                float slope = sharpness * dot(delta, hash01_vec3(neighborCell + vec3(11.0, 31.0, 47.0)) - 0.5);
                // Changed by @WithersChat from slope * inversesqrt(1.0 + slope * slope) for sharper lines
                float denominator = 1.0 + slope;
                denominator = denominator < 0.0 ? min(denominator, -0.001) : max(denominator, 0.001);
                valueSum += weight * slope / denominator;
                weightSum += weight;
            }
        }
    }
    return (valueSum / weightSum) * 0.5 + 0.5;
}

vec3 shard_offset(vec3 point, vec3 axis) {
    vec3 na = vec3(
        shard_noise(point, 4.0),
        shard_noise(point + vec3(50.0), 4.0),
        shard_noise(point + vec3(-50.0, 25.0, 75.0), 4.0)
    );
    na = clamp(floor(na * 4.0) / 4.0 - 0.5, -0.5, 0.5) * 0.3;
    return na - axis * dot(na, axis);
}

float shard_mask(vec3 point) {
    float nb = shard_noise(point + vec3(100.0), 4.0);
    nb = nb > 1.0 ? sqrt(nb) : nb;
    nb = clamp((nb + 0.5) / 3.0, 0.0, 1.0);
    return nb;
}

vec4 calc_shard_data() {
    // Use the point on the projector face as an input to shard noise,
    // this way it does not change with camera movement. We do not have a real normal here on non-iris path,
    // so use view ray derivatives to approximate it (a better path ahead would be to add normal to the vertex format)
    vec3 facePoint = (vViewRay - vAnchor.xyz) / vAnchor.w;
    vec3 faceNormal = normalize(cross(dFdx(vViewRay), dFdy(vViewRay)));
    float time = GameTime * 120.0;
    vec3 offsetPoint = facePoint * 0.6 + faceNormal * time * 0.125;
    vec3 maskPoint = facePoint * 1.8 + faceNormal * time * 0.2;
    return vec4(shard_offset(offsetPoint, normalize(vViewRay)), shard_mask(maskPoint));
}

float calc_shard_shading(vec4 shard) {
    float shattered = step(0.1, float(vRenderEffect & RENDER_EFFECT_SHATTERED));
    return mix(1.0, shard.w, shattered);
}

vec3 calc_star_layer_weight(float density, vec4 shard, float layer, float speedModifier) {
    vec3 baseAxis = fibonacci_sphere_point(layer - 1.0, float(StarLayers));
    vec3 rotationAxis = rotate_around_axis(baseAxis, vec3(0.0, 1.0, 0.0), TWO_PI * GameTime * 0.012 * layer);
    float rotationAngle = 5.7491 * layer + TWO_PI * GameTime * 0.097 * (3.0 + layer) / (4.5 - layer / 4.0) * speedModifier;
    vec3 layerViewRay = refract_layer_ray(calc_layer_view_ray(normalize(vViewRay), layer), shard, layer);
    vec3 rotatedViewRay = normalize(rotate_around_axis(layerViewRay, rotationAxis, rotationAngle));

    float textureScale = max(1.0, 5.8 - 0.32 * layer) * 0.2;
    vec3 layerWeight = triplanar_blend(Sampler1, rotatedViewRay, layer, textureScale);
    return layerWeight * density;
}

vec3 calc_base_color(vec4 shard) {
    vec3 viewRayNormalized = shift_view_ray(shard, 1.0);
    vec3 baseColor = triplanar_blend(Sampler0, viewRayNormalized, 0.0, 1.0) * vBaseColor.rgb;

    float luma = dot(baseColor, vec3(0.2126, 0.7152, 0.0722)); // sRGB luminance weights
    vec3 desaturated = mix(vec3(luma), baseColor, smoothstep(0.3, 1.0, vIntegrity));

    float dimming = mix(0.05, 1.0, pow(vIntegrity, 2.0));
    return desaturated * dimming;
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

    vec4 shard = calc_shard_data();
    float webDensities[WEB_SHELLS];
    for (int i = 0; i < WEB_SHELLS; i++) {
        webDensities[i] = calc_web_shell_density(shard, float(i));
    }

    vec3 color = calc_base_color(shard);
    for (int i = 0; i < StarLayers; i++) {
        int webShell = min(i * WEB_SHELLS / StarLayers, WEB_SHELLS - 1);
        float density = webDensities[webShell];
        vec3 layerWeight = calc_star_layer_weight(density, shard, float(i + 1), speeds[i / 4]);
        bool isStar = length(layerWeight) > 0.1;
        bool isBlackish = length(expandedPalette[i].rgb) < 0.1;
        if (isBlackish && isStar) {
            color = expandedPalette[i].rgb;
        } else {
            color += layerWeight * expandedPalette[i].rgb;
        }
    }
    color = clamp(color, 0.0, 1.0);
    fragColor = vec4(color * calc_shard_shading(shard), 1.0);
}
