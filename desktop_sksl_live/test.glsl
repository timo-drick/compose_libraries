uniform float3 iResolution;
uniform float iDensity;
uniform float iTime;

layout(color) uniform vec4 background;
layout(color) uniform vec4 primary;

const float PI = 3.14159265359;

mat2 rot(float a) {
    float s=sin(a), c=cos(a);
    return mat2(c, -s, s, c);
}

vec3 checkBoard(vec2 uv) {
    vec2 id = floor(uv);
    float w = fract((id.x + id.y)/2.) * 2.;
    vec3 col = mix(primary.rgb, background.rgb, clamp(0., 1., w));
    return col;
}

vec3 triangle(vec2 uv) {
    uv.y /= 1.732050808; //sqrt(3.);
    float idy = floor(uv.y);
    float yodd = mod(idy, 2);
    float y = mod(floor(uv.x+ yodd), 2) - fract(uv.y);
    float idx = floor(uv.x-abs(y));
    vec2 id = vec2(idx, idy);
    float w = mod((id.x + id.y), 2.) / 2.;
    vec3 col = mix(primary.rgb, background.rgb, clamp(0., 1., w));
    return col;
}

float sdBox(vec2 p, vec2 b) {
    vec2 d = abs(p)-b;
    return length(max(d,0.0)) + min(max(d.x,d.y),0.0);
}

half4 main(vec2 fragCoord) {
    vec2 centerPos = vec2(100,100);
    fragCoord -= centerPos; // Move 0,0 to center
    float angleRad = PI * 2.0 * iTime;
    fragCoord *= rot(angleRad); // rotate 45 degree
    fragCoord += centerPos; // Move 0,0 to left, top
    float d = sdBox(fragCoord - centerPos, vec2(100, 30));
    return vec4(vec3(d),1);
}
