#version 330 core

#line 0 1
/*#version 150*/

vec4 projection_from_position(vec4 position) {
    vec4 projection = position * 0.5;
    projection.xy = vec2(projection.x + projection.w, projection.y + projection.w);
    projection.zw = position.zw;
    return projection;
}
#line 3 0

in vec3 Position;
in vec3 BaseColor;
in vec4 ParticleSpeeds;
in uvec4 Colors;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 texProj0;
flat out uvec4 vColors;
flat out vec3 vBaseColor;
flat out vec4 vParticleSpeeds;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texProj0 = projection_from_position(gl_Position);
    vBaseColor = BaseColor;
    vParticleSpeeds = ParticleSpeeds;
    vColors = Colors;
}
