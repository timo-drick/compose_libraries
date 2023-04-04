uniform float3 iResolution;
uniform float density;

layout(color) uniform vec3 background;
layout(color) uniform vec3 primary;

float PI = 3.14159265359;

float dSine(vec2 uv, vec2 offset, float amplitude) {
    float a = 2.5;
    return uv.y + offset.y - cos((sin(uv.x*a*1.1)*a + PI + offset.x)*1.) * amplitude * 1.6;
}

float dMainLine(vec2 uv, vec2 offset, float amplitude) {
    float diff = dSine(uv, offset, amplitude);
    float d = min(smoothstep(0.15, 0.0, diff), smoothstep(-0.005, 0.0, diff));
    return clamp(d, 0.0, 1.0);
}

float dMaskLine(vec2 uv, vec2 offset, float amplitude) {
    float diff = dSine(uv, offset, amplitude);
    float d = min(smoothstep(.2, 0.0, diff), smoothstep(-.2, 0.0, diff));
    return clamp(d, 0.0, 1.0);
}

const float width = 0.01;
float fwidth(float value) {
    return value + width;
}
vec2 fwidth(vec2 value) {
    return vec2(value.x + width, value.y + width);
}

vec3 Grid(vec2 uv) {
    vec3 col = vec3(0);
    if(abs(uv.x)<fwidth(uv.x)) col.g = 1.;
    if(abs(uv.y)<fwidth(uv.y)) col.r = 1.;
    vec2 grid = 1.-abs(fract(uv)-.5)*2.;
    grid = smoothstep(fwidth(grid), vec2(0), grid);
    col += (grid.x+grid.y)*.5;
    return col*.5;
}

float Line(in vec2 p, in vec2 a, in vec2 b, float w) {
    vec2 pa = p - a, ba = b - a;
    float h = clamp(dot(pa,ba) / dot(ba,ba), 0., 1.);
    float d = length(pa - ba * h);
    return smoothstep(w, w-fwidth(d), d);
}

float Arrow(vec2 p, vec2 a, vec2 b, float w) {
    float m = Line(p, a, b, w);

    vec2 n = normalize(a-b)*.1;
    vec2 c = b + n;
    vec2 d = n*.5;
    d = vec2(-d.y, d.x);

    m = max(m, Line(p, b, c+d, w));
    m = max(m, Line(p, b, c-d, w));

    return m;
}

float mod(float a, float b) {
    return a - (b * floor(a/b));
}

half4 main(vec2 fragcoord) {
    vec2 uv = (fragcoord) / 400.;
    uv *= vec2(.25, -1.);
    uv *= 1.5;
    float freq = 2.;
    float id = floor(uv.y / freq);
    uv.y = mod(uv.y, freq)-.5*2.;

    float d = 0.0;
    float mask = 0.0;

    uv.x += 5.*id;

    d = max(d, dMainLine(uv, vec2(.0, .0), 0.3)*0.8);
    d = max(d, dMainLine(uv, vec2(-.03, .033), 0.33)*0.6);
    mask = max(mask, dMaskLine(uv, vec2(.0, .0), 0.3)*0.8);
    float bshift = .2;
    d = max(d, dMainLine(uv, vec2(-.2, .10), 0.3)*0.3);
    mask = max(mask, dMaskLine(uv, vec2(-.2, .10), 0.3)*0.3);
    d = max(d, dMainLine(uv, vec2(-.25, .15), 0.34)*0.2);

    //d = clamp(d, .0, 1.);

    vec3 colLine = mix(vec3(1.), primary, d);
    vec3 col = mix(background, colLine, mask);

    //vec3 col = Grid(uv);
    vec2 ori= vec2(0,0);	// origin
    vec2 x	= vec2(1,0);	// basis 1
    vec2 y	= vec2(0,1);	// basis 2
    //col = mix(col, vec3(1,0,0), Arrow(uv, ori, x+ori, .04));
    //col = mix(col, vec3(0,1,0), Arrow(uv, ori, y+ori, .04));
    return vec4(col, 1);
    //return vec4(vec3(uv.y),1);
}