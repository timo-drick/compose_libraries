uniform float2 iResolution;
uniform float2 iOffset;
uniform shader background;
const float PI = 3.14159265359;
const float AA = 2.;
const float X_SHIFT_START = 0.2;

vec4 render(vec2 fragCoord) {
    vec2 uv = fragCoord / iResolution.x; // change coordinate system 0..1
    vec2 offset = iOffset / iResolution.x;
    float xShift = max(0, X_SHIFT_START - offset.x);
    uv.x += xShift * .5;
    //offset.x = max(MAX_FOLDING, offset.x); // maximum folding
    vec2 a = abs(uv - offset);
    a = a * a * .2;
    float dist = offset.x + (a.y * (2.-uv.x) * (1.0 - offset.x));
    float distI = 1.0 - dist;
    float freq = 8.0;
    float intensity = distI * .5;
    float angle = (uv.x / dist) * 2.0 * PI * freq;
    float light = tan(angle) * intensity + 1.0;
    float fold = -cos(angle) * intensity;

    vec2 pos = vec2(uv.x / dist, uv.y + fold * .03);
    pos *= iResolution.x;
    vec3 col = background.eval(pos).rgb * light;
    col = mix(col, vec3(0.1), smoothstep(dist, dist + .005, uv.x));
    float shadow = 0.02;
    float alpha = 1.0 - smoothstep(dist, dist + shadow, uv.x);
    return vec4(col * alpha, alpha);
}

vec4 main(vec2 fragCoord) {
    vec4 col = vec4(0);
    // Antialiasing by super sampling
    for (float i=0; i<AA; i++) {
        for (float j=0; j<AA; j++) {
            vec2 o = vec2(i,j) / AA - 0.5;
            vec2 uv = (fragCoord + o);
            col += render(uv);
        }
    }
    col /= AA * AA;
    return col;
}