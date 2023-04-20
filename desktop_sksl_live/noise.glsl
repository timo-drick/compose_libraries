uniform float3 iResolution;
uniform float iDensity;
uniform float iTime;

layout(color) uniform half4 background;
layout(color) uniform half4 primary;

float PI_2 = 6.283185307;

vec2 hash22(vec2 p) {
    p = p * mat2(113.5, 532.1, 269.9, 183.3);
	p = -1.0 + 2.0 * fract(sin(p) * 43758.5453123);
	return sin(p * PI_2 + iTime);
}

float perlin_noise(vec2 p) {
	vec2 id = floor(p);
    vec2 pos = fract(p);
    float topLeft = dot(hash22(id), pos);
    float topRight = dot(hash22(id + vec2(1.0, 0.0)), pos - vec2(1.0, 0.0));
    float bottomLeft = dot(hash22(id + vec2(0.0, 1.0)), pos - vec2(0.0, 1.0));
    float bottomRight = dot(hash22(id + vec2(1.0, 1.0)), pos - vec2(1.0, 1.0));

    vec2 w = pos * pos * (3. - 2. * pos); // Smoothstep
    float top = mix(topLeft, topRight, w.x);
    float bottom = mix(bottomLeft, bottomRight, w.x);

    return mix(top, bottom, w.y);
}

float N21(vec2 p) {
    float n = fract(sin(p.x*102.+p.y*3456.)*890.);
    return sin(n * PI_2 + iTime);
}

float smoothNoise(vec2 p) {
    vec2 id = floor(p);
    vec2 pos = fract(p);
    float topLeft = N21(id);
    float topRight = N21(id + vec2(1, 0));
    float bottomLeft = N21(id + vec2(0, 1));
    float bottomRight = N21(id + vec2(1, 1));
    vec2 w = pos * pos * (3. - 2. * pos); // Smoothstep
    float top = mix(topLeft, topRight, w.x);
    float bottom = mix(bottomLeft, bottomRight, w.x);

    return mix(top, bottom, w.y);
}

float noise(vec2 p){
    p *= 4.;
	float a = 1., r = 0., s = 0.;
    for (int i=0; i<4; i++) {
        r += a * smoothNoise(p); s += a; p *= 2.; a *= .5;
    }
    return r/s;
}


half4 main(vec2 fragCoord) {
    vec2 uv = fragCoord / (400. * iDensity) ;
    float f = noise(uv);
    return mix(background, primary, f);
}
