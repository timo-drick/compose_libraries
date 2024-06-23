uniform float2 iResolution;
uniform float iTime;

const float4 fg = float4(0.6392156862745098,0.7686274509803922,0.2235294117647059,1.0);
const float4 bg = float4(0);

const float PI = 3.14;

float sdHalfCircle(float2 p, float r) {
    float cd = length(p) - r;
    return (p.y > 0) ? cd : max(cd, -p.y);
}

float sdOriginLine(float2 p, float angle, float l, float l2) {
    float2 parc = float2(cos(angle), sin(angle));
    p -= parc * l;
    float t = dot(p, parc) / l2;
    if (t < 0) return length(p);
    float2 p2 = parc * l2;
    if (t > 1) return length(p - p2);
    return length(p - p2 * t);
}

float sdAndroidLogo(float2 p) {
    p.y *= 1.12;
    float r = 0.455;
    p.y += r / 2.0 + .05;
    float d = sdHalfCircle(p, r);
    float2 eyePos = float2(abs(p.x) - 0.21, p.y - 0.18);
    d = max(d, -(length(eyePos) - 0.06));
    float2 antennaPos = float2(abs(p.x), p.y);
    float antennaD = sdOriginLine(antennaPos, PI * .347, r, 0.18) - .02;
    return min(antennaD, d);
}

float4 main(float2 fragCoord) {
    fragCoord -= iResolution.xy / 2.0; // Move 0,0 to center
    fragCoord /= -iResolution.x; // scale to device width
    float d = sdAndroidLogo(fragCoord);
    float intensity = smoothstep(0.0, 0.01, d);

    return mix(fg, bg, intensity);
}
