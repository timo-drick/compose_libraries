uniform float2 iResolution;
uniform float iTime;

layout(color) uniform vec4 background;
layout(color) uniform vec4 primary;

const int MAX_STEPS = 8;

float sdBox(vec2 p, vec2 b) {
    vec2 d = abs(p)-b;
    return length(max(d,0.0)) + min(max(d.x,d.y),0.0);
}

half4 main(vec2 fragCoord) {
    fragCoord -= iResolution.xy / 2.0; // Move 0,0 to center
    fragCoord /= iResolution.x; // scale to device width
    vec3 col = vec3(0);
    vec2 p = fragCoord * float(MAX_STEPS);
    float step = 1.0 / float(MAX_STEPS - 1);
    float currentPos = iTime * 2.0;
    for (int i = 0; i < MAX_STEPS; i++) {
        float slot = float(i) * step;
        float midPoint = (.5 - abs(slot - .5)) * 2.0;
        float m = 0.0;
        if (i >= MAX_STEPS / 2) m = 1.0;
        float relPos = mod(currentPos - slot - 1.0 - midPoint * m, 2.0);
        float dLight = mod(relPos, 2.0 - midPoint) / 2.0;

        float intensity = 1.0 - smoothstep(0.0, .7, dLight);
        vec2 pb = p - vec2((slot - .5) * 6.0, 0);

        float d = sdBox(pb, vec2(0.41, 0.1));
        float value = 1. - smoothstep(0.0, 0.01, d);
        col += mix(vec3(.1,.1,.1), vec3(1,.3,0), intensity) * value;

        float dCircle = length(pb * vec2(.5,1));
        float blur = mix(0.03, 0.3, intensity * intensity);
        col += (1.0 - smoothstep(0.0, blur, dCircle)) * vec3(1,.7,.3) * 1.5; // over exposure

        intensity = 1.0 - smoothstep(0.0, .35, dLight);
        float dStar = (1.0 - abs(pb.x));
        col += smoothstep(0.0, 4., dStar) * vec3(.8,.2,1.) * intensity;
    }
    return vec4(col,1);
}
