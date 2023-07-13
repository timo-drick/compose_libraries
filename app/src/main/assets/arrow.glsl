uniform vec2 iResolution;
uniform vec3 iLightDir;
layout(color) uniform vec4 background;

const int MAX_STEPS = 100;
const float MAX_DIST = 100.;
const float SURF_DIST = .001;

const float TAU = 6.283185;
const float PI = 3.141592;

struct Material {
    float dist; // distance to eye
    vec3 color; // color
};

mat2 Rot(float a) {
    float s=sin(a), c=cos(a);
    return mat2(c, -s, s, c);
}

Material mixMaterial(Material a, Material b) {
    if (a.dist > b.dist) return b;
    else return a;
}


float arrow(vec3 position, vec3 start, vec3 end) {
    float baseRadius = 0.2;
    float tipRadius = 0.5;
    float tipHeight = 0.6;
    float cornerRadius = 0.01;

    vec3 t = start - end;
    float l = length(t);
    t /= l;
    l = max(l, tipHeight);

    position -= end;
    if (t.y + 1.0 < 0.0001) {
        position.y = -position.y;
    } else {
        float k = 1.0 / (1.0 + t.y);
        vec3 column1 = vec3(t.z * t.z * k + t.y, t.x, t.z * -t.x * k);
        vec3 column2 = vec3(-t.x, t.y, -t.z);
        vec3 column3 = vec3(-t.x * t.z * k, t.z, t.x * t.x * k + t.y);
        position = mat3(column1, column2, column3) * position;
    }

    vec2 q = vec2(length(position.xz), position.y);
    q.x = abs(q.x);

    // tip
    vec2 e = vec2(tipRadius, tipHeight);
    float h = clamp(dot(q, e) / dot(e, e), 0.0, 1.0);
    vec2 d1 = q - e * h;
    vec2 d2 = q - vec2(tipRadius, tipHeight);
    d2.x -= clamp(d2.x, baseRadius - tipRadius, 0.0);

    // base
    vec2 d3 = q - vec2(baseRadius, tipHeight);
    d3.y -= clamp(d3.y, 0.0, l - tipHeight);
    vec2 d4 = vec2(q.y - l, max(q.x - baseRadius, 0.0));

    float s = max(max(max(d1.x, -d1.y), d4.x), min(d2.y, d3.x));
    return sqrt(min(min(min(dot(d1, d1), dot(d2, d2)), dot(d3, d3)), dot(d4, d4))) * sign(s);
}


Material GetDist(vec3 p) {
    vec3 start = vec3(0.0, 0.0, 0.0);
    Material x = Material(arrow(p, start, vec3(1,0,0)), vec3(1,0,0));
    Material y = Material(arrow(p, start, vec3(0,1,0)), vec3(0,1,0));
    Material z = Material(arrow(p, start, vec3(0,0,1)), vec3(0,0,1));
    Material m = mixMaterial(x, y);
    m = mixMaterial(m, z);
    return m;
}

Material RayMarch(vec3 ro, vec3 rd) {
    float dO=0.;
    Material m = Material(0., vec3(0));
    for(int i=0; i<MAX_STEPS; i++) {
        vec3 p = ro + rd*dO;
        m = GetDist(p);
        float dS = m.dist;
        dO += dS;
        if(dO>MAX_DIST) return Material(MAX_DIST, vec3(0));
        m.dist = dO;
        if(abs(dS)<SURF_DIST) break;
    }

    return m;
}

vec3 GetNormal(vec3 p) {
    vec2 e = vec2(.001, 0);
    vec3 n = GetDist(p).dist - vec3(GetDist(p-e.xyy).dist, GetDist(p-e.yxy).dist,GetDist(p-e.yyx).dist);
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

float calcShadow(vec3 p, vec3 lightPos, float sharpness) {
    vec3 rd = normalize(lightPos - p);

    float h;
    float minH = 1.0;
    float d = 0.1;
    for (int i = 0; i < 16; i++) {
        h = GetDist(p + rd * d).dist;
        minH = abs(h / d);
        if (minH < 0.01)
        return 0.0;
        d += h;
    }
    return minH * sharpness;
}

float calcOcc(vec3 p, vec3 n, float strength) {
    const float dist = 0.2;
    return 1.0 - (dist - GetDist(p + n * dist).dist) * strength;
}


// https://iquilezles.org/articles/rmshadows
float calcSoftshadow( in vec3 ro, in vec3 rd, in float mint, in float tmax ) {
    // bounding volume
    float tp = (0.8-ro.y)/rd.y; if( tp>0.0 ) tmax = min( tmax, tp );

    float res = 1.0;
    float t = mint;
    for( int i=0; i<24; i++ ) {
        float h = GetDist( ro + rd*t ).dist;
        float s = clamp(8.0*h/t,0.0,1.0);
        res = min( res, s );
        t += clamp( h, 0.01, 0.2 );
        if( res<0.004 || t>tmax ) break;
    }
    res = clamp( res, 0.0, 1.0 );
    return res*res*(3.0-2.0*res);
}

// https://iquilezles.org/articles/nvscene2008/rwwtt.pdf
float calcAO( in vec3 pos, in vec3 nor ) {
    float occ = 0.0;
    float sca = 1.0;
    for( int i=0; i<5; i++ )
    {
        float h = 0.01 + 0.12*float(i)/4.0;
        float d = GetDist( pos + h*nor ).dist;
        occ += (h-d)*sca;
        sca *= 0.95;
        if( occ>0.35 ) break;
    }
    return clamp( 1.0 - 3.0*occ, 0.0, 1.0 ) * (0.5+0.5*nor.y);
}

vec4 renderScene(vec2 uv) {
    //vec3 ro = vec3(0, 3, -3)*.7;
    vec3 ro = vec3(0, 8, -3)*.5;

    vec3 rd = GetRayDir(uv, ro, vec3(0, 0, 0), 1.);
    vec3 col = vec3(0);

    Material mat = RayMarch(ro, rd);
    float d = mat.dist;
    float alpha;
    if(d<MAX_DIST) {
        alpha = 1.;
        vec3 p = ro + rd * d;
        vec3 n = GetNormal(p);

        //vec3 lightPos = vec3(-1.0, 1.0, 2.0)*20.;
        vec3 lightPos = iLightDir*30.;
        vec3 lightCol = vec3(1.0, 0.9, 0.8);
        vec3 lightToPoint = normalize(lightPos - p);
        vec3 skyCol = background.rgb;
        float sha = calcShadow(p, lightPos, 5.0);
        //float sha = calcSoftshadow(p, normalize(lightPos - p), 0.02, 2.5);
        float occ = calcOcc(p, n, 4.0);
        //float occ = calcAO(p, n);
        float spe = pow(max(0.0, dot(rd, reflect(lightToPoint, n))), 5.0);
        float mainLight = max(0.0, dot(n, lightToPoint));
        float backLight = clamp(dot(n, -rd), 0.01, 1.0) * 0.1;
        vec3 skyLight = clamp(dot(n, vec3(0.0, 1.0, 0.0)), 0.01, 1.0) * 0.4 * skyCol;
        float fog = 1.0 - exp(-d * 0.03);


        col = (mainLight * sha + (spe + backLight) * occ) * lightCol;
        col += skyLight * occ;
        col *= mat.color;
        col = mix(col, skyCol, fog);
        //float t = 2.;
        //col = mix( col, vec3(0.7,0.7,0.9), 1.0-exp( -0.0001*t*t*t ) );
    } else {
        alpha = 0.;
    }

    col = pow(col, vec3(.45));    // gamma correction

    return vec4(col, alpha);
}

vec4 main(vec2 fragcoord) {
    vec2 uv = fragcoord / iResolution;
    uv = (uv-.5)*2.;
    uv.y *= -iResolution.y/iResolution.x;
    vec4 col = renderScene(uv);
    return col;//vec4(col.rgb * col.a,  col.a); // premultiply alpha
}
