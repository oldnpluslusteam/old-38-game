#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform mat4 u_projTrans;

void main() {
        vec4 color = texture2D(u_texture, vec2(v_texCoords.x, 1.0 - v_texCoords.y)) * v_color;
        gl_FragColor = color;
}
