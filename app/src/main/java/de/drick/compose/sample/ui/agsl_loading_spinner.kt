package de.drick.compose.sample.ui

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.unit.dp
import org.intellij.lang.annotations.Language

@Language("AGSL")
private const val shaderSrc = """
uniform float3 iResolution;
uniform float density;
uniform float iTime;

layout(color) uniform vec3 background;
layout(color) uniform vec3 primary;

const float PI = 3.14159265359;

const int MAX_STEPS = 500;
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
    p.xy *= Rot(-PI*iTime*0.5);
    p.xz *= Rot(PI*iTime*0.2);

    float width = .06;
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
    //uv.x *= iResolution.x/iResolution.y;

    vec3 ro = vec3(0, 3, -3)*.6;
    vec3 rd = GetRayDir(uv, ro, vec3(0,0,0), 1.);
    float d = RayMarch(ro, rd);

    // Center light
    float cd = dot(uv, uv);
    float centerLight = 0.006 / cd;
    float light = centerLight * smoothstep(.0, .3, d-2.);
    float s = GetDist(normalize(ro));
    light += centerLight * smoothstep(.0, .1, s);

    vec3 col = vec3(0);
    float alphaMask = 0.;

    if(d<MAX_DIST) {
        vec3 p = ro + rd * d;
        vec3 n = GetNormal(p);
        vec3 r = reflect(rd, n);

        vec3 lightDir = -normalize(p);

        float pl = dot(n, lightDir)*.5+.5;
        float dif = .2*(dot(n, normalize(vec3(1,2,3)))*.5+.5);
        float l = max(pl, dif);
        col = vec3(l)+light;

        alphaMask = 1.;
    } else {
        col = vec3(1.);
        alphaMask = light;
    }

    col = pow(col, vec3(.4545));    // gamma correction
    return half4(col * alphaMask, alphaMask);
}

"""

@Composable
fun LoadingSpinner() {
    if (Build.VERSION.SDK_INT >= 33) {
        val time by produceState(0f) {
            val startNanos = System.nanoTime()
            while (true) {
                withInfiniteAnimationFrameNanos {
                    value = ((startNanos - it) / 1000000L) / 1000f // convert to seconds
                }
            }
        }
        Box(
            Modifier
                .size(300.dp)
                .drawWithCache {
                    val spinnerShader = RuntimeShader(shaderSrc)
                    spinnerShader.setFloatUniform("iResolution", size.width, size.height, 1f)
                    spinnerShader.setFloatUniform("iTime", time)
                    val brush = ShaderBrush(spinnerShader)
                    onDrawBehind {
                        drawRect(
                            brush = brush,
                            topLeft = Offset.Zero,
                            size = size
                        )
                    }
                }
        )
    }
}
