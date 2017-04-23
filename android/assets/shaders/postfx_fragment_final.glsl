#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform mat4 u_projTrans;

uniform float u_timePassed;

void main() {
        vec4 color = texture2D(u_texture, vec2(v_texCoords.x, v_texCoords.y));

        color = mix(color, vec4(1) - color, exp(-u_timePassed));

        gl_FragColor = color;
}
