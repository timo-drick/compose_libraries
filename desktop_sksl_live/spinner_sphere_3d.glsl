uniform float2 iResolution;
uniform float density;
uniform float iTime;

layout(color) uniform vec3 background;
layout(color) uniform vec3 primary;

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
