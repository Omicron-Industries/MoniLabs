#version 330 core

in vec3 Position;
in vec3 BaseColor;
in vec4 ParticleSpeeds;
in uvec4 Colors;
in vec4 Anchor;
in float Integrity;
in float RenderEffect;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 IViewRotMat;

out vec3 vViewRay;
flat out uvec4 vColors;
flat out vec3 vBaseColor;
flat out vec4 vParticleSpeeds;
flat out vec4 vAnchor;
flat out float vIntegrity;
flat out uint vRenderEffect;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vViewRay = IViewRotMat * Position;
    vBaseColor = BaseColor;
    vParticleSpeeds = ParticleSpeeds;
    vColors = Colors;
    vAnchor = vec4(IViewRotMat * Anchor.xyz, Anchor.w);
    vIntegrity = Integrity;
    vRenderEffect = uint(RenderEffect);
}
