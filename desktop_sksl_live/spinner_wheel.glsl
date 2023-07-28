uniform float2 iResolution;
uniform float iTime;

layout(color) uniform vec4 background;
layout(color) uniform vec4 primary;

const float PI = 3.14159265359;

mat2 rot(float a) {
    float s=sin(a), c=cos(a);
    return mat2(c, -s, s, c);
}

float sdBox(vec2 p, vec2 b) {
    vec2 d = abs(p)-b;
    return length(max(d,0.0)) + min(max(d.x,d.y),0.0);
}


const int MAX_STEPS = 24;
half4 main(vec2 fragCoord) {
    vec2 centerPos = iResolution.xy/2.0;
    fragCoord -= centerPos; // Move 0,0 to center
    if (iResolution.x - iResolution.y < 0) {
        fragCoord /= iResolution.x; // scale to device width
    } else {
        fragCoord /= iResolution.y; // scale to device height
    }
    fragCoord *= 2.0;
    fragCoord *= 1.0;
    float angle = -PI * 2.0 * iTime;
    //fragCoord *= rot(-angle/4.0);
    float pulse = (cos( iTime * PI * 5.0) + 1.1);
    vec3 col = vec3(0);
    for (int i = 0; i < MAX_STEPS; i++) {
        float stepAngle = PI * 2.0 / float(MAX_STEPS) * float(i);
        float angleDistance = mod(stepAngle - angle, 2.0 * PI);
        angleDistance /= PI * 2.0;
        float intensity = (1.0 - angleDistance);
        intensity = min(intensity, -intensity*16.0+16.0);
        vec2 p = fragCoord * rot(stepAngle); // rotate
        p += vec2(0.4,0); // move outside
        float d = sdBox(p, vec2(0.25 * (intensity + .7), 0.003));
        //float pulse2 = 0.8 + 0.2*cos(40.0*d - iTime * PI * 16.0);
        col += vec3(1.0-smoothstep(0.0,max(0.01, 0.1 * intensity), d) );// * intensity * d * 4.0;
    }
    return vec4(col,1);
}
