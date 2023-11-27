uniform float2 iScaling;

float3 color1 = float3(0.1);
float3 color2 = float3(0.9);

float3 checkerBoard(float2 uv) {
    float2 id = floor(uv);
    float w = fract((id.x + id.y)/2.) * 2.;
    return mix(color1.rgb, color2.rgb, w);
}

float4 main(float2 fragCoord) {
    return float4(checkerBoard(fragCoord * iScaling), 1);
}


const float MAX_STEPS = 24.0;
half4 main(vec2 fragCoord) {
    fragCoord = fragCoord / iResolution * 2.0 - 1.0;
    float angle = -PI * 2.0 * iTime;
    float l = 0.0; // luminance
    for (float i = .0; i < MAX_STEPS; i++) {
        float rectAngle = PI * 2.0 / MAX_STEPS * i;
        vec2 p = fragCoord * rot(rectAngle); // rotate
        p.x += 0.4; // move outside
        float angleDistance = mod(rectAngle - angle, 2.0 * PI);
        float intensity = 1.0 - (angleDistance / (PI * 2.0));
        float box = sdBox(p, vec2(0.2 * (intensity + .7), 0.003));
        box = smoothstep(0.0, max(0.04, 0.13 * intensity), box);
        l += 1.0 - box; // using + instead of min
    }
    l = clamp(l, 0., 1.); // keep luminance in 0..1 range
    vec3 col = vec3(.1, 1., .0) * l;
    return vec4(col, l);
}