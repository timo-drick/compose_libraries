uniform float3 iResolution;
uniform float iDensity;
uniform float iTime;

layout(color) uniform vec4 background;
layout(color) uniform vec4 primary;

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


half4 main(vec2 fragCoord) {
    vec2 uv = fragCoord / (400. * iDensity) ;
    return vec4(triangle(uv*10.), 1);
}
