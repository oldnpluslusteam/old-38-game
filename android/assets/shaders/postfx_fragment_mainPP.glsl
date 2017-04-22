#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform mat4 u_projTrans;

uniform float u_grayscalePower;

const float c_pxx = 1.0f / 1366.0f;
const float c_pxy = 1.0f / 768.0f;
const float c_limit = 0.7f;

vec4 addSample(vec2 offset) {
    vec4 sample_ = texture2D(u_texture, vec2(v_texCoords.x, 1.0 - v_texCoords.y) + offset);

    sample_ = clamp(sample_ - c_limit, vec4(0,0,0,0), vec4(1,1,1,1));

    sample_ = (sample_ + vec4(c_limit)) * sign(sample_);

    return sample_;
}

void main() {
        vec4 color = v_color * texture2D(u_texture, vec2(v_texCoords.x, 1.0 - v_texCoords.y));
        vec4 grayscale = vec4(dot(color.rgb, vec3(.3, .3, .3)));

        color = mix(color, grayscale, vec4(u_grayscalePower));
//*
        color += 0.125 * addSample(vec2(c_pxx, -c_pxy));
        color += 0.125 * addSample(vec2(-c_pxx, -c_pxy));
        color += 0.125 * addSample(vec2(-c_pxx, c_pxy));
        color += 0.125 * addSample(vec2(c_pxx, c_pxy));
        color += 0.25 * addSample(vec2(c_pxx, 0));
        color += 0.25 * addSample(vec2(0, -c_pxy));
        color += 0.25 * addSample(vec2(0, c_pxy));
        color += 0.25 * addSample(vec2(-c_pxx, 0));
        color += 0.0625 * addSample(2.0f * vec2(c_pxx, 0));
        color += 0.0625 * addSample(2.0f * vec2(0, -c_pxy));
        color += 0.0625 * addSample(2.0f * vec2(0, c_pxy));
        color += 0.0625 * addSample(2.0f * vec2(-c_pxx, 0));
//*/

        gl_FragColor = color;
}
