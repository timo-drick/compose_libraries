package de.drick.compose.sample.ui.animation

import org.intellij.lang.annotations.Language

@Language("AGSL")
const val SHADER_WHEEL = """
uniform float2 iResolution;
uniform float iTime;

const float PI = 3.14159265359;

mat2 rot(float a) {
    float s = sin(a), c = cos(a);
    return mat2(c, -s, s, c);
}

float sdBox(vec2 p, vec2 b) {
    vec2 d = abs(p)-b;
    return length(max(d,0.0)) + min(max(d.x,d.y),0.0);
}

const float MAX_STEPS = 24.0;
half4 main(vec2 fragCoord) {
    fragCoord = fragCoord / iResolution * 2.0 - 1.0; // -1..0..1 coordinate system
    float aspect = iResolution.y / iResolution.x;      // aspect ratio
    if (aspect > 1.0) { // landscape mode?
        fragCoord.y *= aspect;
    } else {
        fragCoord.x /= aspect;
    }

    float angle = -PI * 2.0 * iTime;
    //float pulse = (cos( iTime * PI * 4.0) + .1) * .2 + .8;
    //fragCoord.y *= pulse;
    float l = 0.0; // luminance
    for (float i = .0; i < MAX_STEPS; i++) {
        float rectAngle = PI * 2.0 / MAX_STEPS * i;
        vec2 p = fragCoord * rot(rectAngle); // rotate
        p.x += 0.4; // move outside
        float angleDistance = mod(rectAngle - angle, 2.0 * PI);
        float intensity = 1.0 - (angleDistance / (PI * 2.0));
        float box = sdBox(p, vec2(0.2 * (intensity + .7), 0.003));
        box = smoothstep(0.0, max(0.03, 0.13 * intensity), box);
        //l = max(l, 1.0 - box);
        l += 1.0 - box;
    }
    l = clamp(0., 1., l);
    vec3 col = vec3(.1, 1., 0.) * l;
    //col = mix(vec3(1.), col, l);
    return vec4(col, l);
}
"""

@Language("AGSL")
const val SHADER_KITT = """
uniform float2 iResolution;
uniform float iTime;
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
    float alpha = (col.r + col.g + col.b) / 3.0;
    return vec4(col, alpha);
}
"""

@Language("AGSL")
const val SHADER_SPINNER_SPHERE_3D = """
uniform float2 iResolution;
uniform float iTime;

const float PI = 3.14159265359;

const int MAX_STEPS = 100;
const float MAX_DIST = 100.;
const float SURF_DIST = .001;

mat2 Rot(float a) {
    float s=sin(a), c=cos(a);
    return mat2(c, -s, s, c);
}
float smin(float a, float b, float k) {
    float h = clamp(0.5+0.5*(b-a)/k, 0., 1.);
    return mix(b, a, h) - k*h*(1.-h);
}

float GetDist(vec3 p) {
    p.xy *= Rot(PI*2.0*iTime);
    p.xz *= Rot(PI*1.0*iTime);

    float width = .03;
    float sphere = abs(length(p)-1.)-width;
    float dx = abs(p.x)-width;
    float dy = abs(p.y)-width;
    float dz = abs(p.z)-width;
    float smoothness = .03;
    float d = smin(dx,dy, smoothness);
    d = smin(d, dz, smoothness);
    d = smin(d, sphere, -smoothness);
    return d;
}

float RayMarch(vec3 ro, vec3 rd) {
    float dO=0.;
    for(int i=0; i<MAX_STEPS; i++) {
        vec3 p = ro + rd*dO;
        float dS = GetDist(p);
        dO += dS;
        if(dO>MAX_DIST || abs(dS)<SURF_DIST) break;
    }
    return dO;
}
vec3 GetNormal(vec3 p) {
    vec2 e = vec2(.001, 0);
    vec3 n = GetDist(p) - vec3(
        GetDist(p-e.xyy),

    GetDist(p-e.yxy),
        GetDist(p-e.yyx)
    );
    return normalize(n);
}

vec3 GetRayDir(vec2 uv, vec3 p, vec3 l, float z) {
    vec3
        f = normalize(l-p),
        r = normalize(cross(vec3(0,1,0), f)),
        u = cross(f,r),
        c = f*z,
        i = c + uv.x*r + uv.y*u;
    return normalize(i);
}

half4 main(vec2 fragCoord) {
    // Normalized pixel coordinates (from -1 to 1)
    vec2 uv = (fragCoord/iResolution.xy-.5)*2.;
    uv.y *= iResolution.y/iResolution.x;

    vec3 ro = vec3(0, 3, -1)*.6;
    vec3 rd = GetRayDir(uv, ro, vec3(0,0,0), 1.);
    float d = RayMarch(ro, rd);


    // Center light
    float cd = dot(uv, uv);
    float centerLight = 0.006 / cd;
    float light = centerLight * smoothstep(.0, .3, d-2.);
    float s = GetDist(normalize(ro));
    light += centerLight*smoothstep(.0, .1, s);

    vec3 col = vec3(0);

    if(d<MAX_DIST) {
        vec3 p = ro + rd * d;
        vec3 n = GetNormal(p);
        vec3 r = reflect(rd, n);
        vec3 lightDir = -normalize(p);
        float pl = dot(n, lightDir)*.5+.5;
        float dif = .2*(dot(n, normalize(vec3(1,2,3)))*.5+.5);
        float l = max(pl, dif);
        col = vec3(l)+light;
    } else {
        col = vec3(light);
    }
    float alpha = (col.r + col.g + col.b) / 3.0;
    return half4(col, alpha);
}
"""
