varying vec4 v_color;

uniform float u_time;

vec3 hue(float h){
    return clamp(abs(mod(h * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0, 0.0, 1.0);
}

void main(){
    float pos = gl_FragCoord.x + gl_FragCoord.y;
    float shine = sin(pos * 0.125 - u_time * 6.0) * 0.5 + 0.5;
    vec3 rainbow = hue(fract(pos * 0.002 + u_time * 0.1 + v_color.r));
    gl_FragColor = vec4(rainbow, 1.0f);
}
