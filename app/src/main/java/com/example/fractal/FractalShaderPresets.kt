package com.example.fractal

data class FractalPreset(
  val id: String,
  val name: String,
  val category: String,
  val description: String,
  val defaultP1: Float = 45f,
  val defaultP2: Float = 50f,
  val defaultP3: Float = 30f,
  val defaultPanX: Float = 0f,
  val defaultPanY: Float = 0f,
  val fragmentShader: String
)

object FractalShaderPresets {

  val VERTEX_SHADER = """
    attribute vec2 a_pos;
    void main() {
      gl_Position = vec4(a_pos, 0.0, 1.0);
    }
  """.trimIndent()

  val MANDELBROT = FractalPreset(
    id = "mandelbrot_smooth",
    name = "Mandelbrot Smooth",
    category = "Escape-Time",
    description = "Continuous potential coloring with orbit trap glow and dynamic Julia morphing.",
    defaultP1 = 45f,
    defaultP2 = 50f,
    defaultP3 = 30f,
    defaultPanX = 0f,
    defaultPanY = 0f,
    fragmentShader = """
      precision highp float;
      uniform vec2  u_res;
      uniform float u_time;
      uniform float u_p1;   // zoom power
      uniform float u_p2;   // color cycle
      uniform float u_p3;   // Julia morph
      uniform float u_audio; // audio reactive pulse
      uniform vec2  u_pan;

      vec3 palette(float t) {
        vec3 a = vec3(0.5, 0.5, 0.5);
        vec3 b = vec3(0.5, 0.5, 0.5);
        vec3 c = vec3(1.0, 1.0, 1.0);
        vec3 d = vec3(0.00, 0.33, 0.67);
        return a + b * cos(6.28318 * (c * t + d));
      }

      void main() {
        vec2 uv = (gl_FragCoord.xy - 0.5 * u_res) / min(u_res.x, u_res.y);
        
        float zoom = exp(-u_p1 * 0.06) * 1.8;
        vec2 center = vec2(-0.5, 0.0) + u_pan;
        vec2 c = uv * zoom + center;
        
        float juliaMix = u_p3 / 100.0;
        vec2 juliaC = vec2(-0.8 + 0.2 * sin(u_time * 0.3), 0.156 + 0.1 * cos(u_time * 0.27));
        
        vec2 z = mix(vec2(0.0), c, 1.0 - juliaMix);
        vec2 k = mix(c, juliaC, juliaMix);
        
        float iter = 0.0;
        float maxIter = 80.0 + u_p1 * 0.6;
        float trap = 1e20;
        
        for (float i = 0.0; i < 120.0; i++) {
          if (i >= maxIter) break;
          z = vec2(z.x * z.x - z.y * z.y, 2.0 * z.x * z.y) + k;
          trap = min(trap, length(z - vec2(0.0, 0.0)));
          if (dot(z, z) > 4.0) break;
          iter++;
        }
        
        float smoothIter = iter - log2(max(1.0, log2(dot(z, z)))) + 4.0;
        float t = smoothIter / maxIter;
        
        float pulse = u_audio * 0.15;
        vec3 col = palette(t * 2.0 + u_time * 0.05 + u_p2 * 0.01 + pulse);
        col += vec3(0.3, 0.6, 1.0) * exp(-trap * 8.0) * (0.6 + pulse * 1.5);
        
        if (iter >= maxIter) col = vec3(0.0);
        
        gl_FragColor = vec4(col, 1.0);
      }
    """.trimIndent()
  )

  val BURNING_SHIP = FractalPreset(
    id = "burning_ship",
    name = "Burning Ship",
    category = "Non-Analytic",
    description = "Absolute value polynomial producing blazing fiery masts and dynamic cosmic ridges.",
    defaultP1 = 35f,
    defaultP2 = 50f,
    defaultP3 = 25f,
    defaultPanX = 0f,
    defaultPanY = 0f,
    fragmentShader = """
      precision highp float;
      uniform vec2  u_res;
      uniform float u_time;
      uniform float u_p1;
      uniform float u_p2;
      uniform float u_p3;
      uniform float u_audio;
      uniform vec2  u_pan;

      vec3 fire(float t) {
        return vec3(
          smoothstep(0.0, 0.4, t),
          smoothstep(0.2, 0.7, t) * 0.6,
          smoothstep(0.5, 1.0, t) * 0.3
        );
      }

      void main() {
        vec2 uv = (gl_FragCoord.xy - 0.5 * u_res) / min(u_res.x, u_res.y);
        
        float zoom = exp(-u_p1 * 0.055) * 2.2;
        vec2 center = vec2(-0.45, -0.55) + u_pan;
        vec2 c = uv * zoom + center;
        
        vec2 z = vec2(0.0);
        float iter = 0.0;
        float maxIter = 70.0 + u_p1 * 0.5;
        
        for (float i = 0.0; i < 110.0; i++) {
          if (i >= maxIter) break;
          z = abs(z);
          z = vec2(z.x * z.x - z.y * z.y, 2.0 * z.x * z.y) + c;
          if (dot(z, z) > 4.0) break;
          iter++;
        }
        
        float t = iter / maxIter;
        t = pow(t, 0.7);
        
        vec3 col = fire(t);
        col = mix(col, col.bgr, u_p2 / 100.0);
        col *= 0.85 + 0.15 * sin(u_time * 1.5 + t * 10.0 + u_audio * 2.5);
        
        if (iter >= maxIter) col = vec3(0.0);
        
        gl_FragColor = vec4(col, 1.0);
      }
    """.trimIndent()
  )

  val JULIA_ORBIT = FractalPreset(
    id = "julia_orbit",
    name = "Julia Orbit",
    category = "Dynamical Systems",
    description = "Oscillating Julia constant with distance estimator trap and glowing cosine spectral waves.",
    defaultP1 = 45f,
    defaultP2 = 50f,
    defaultP3 = 30f,
    defaultPanX = 0f,
    defaultPanY = 0f,
    fragmentShader = """
      precision highp float;
      uniform vec2  u_res;
      uniform float u_time;
      uniform float u_p1;
      uniform float u_p2;
      uniform float u_p3;
      uniform float u_audio;
      uniform vec2  u_pan;

      void main() {
        vec2 uv = (gl_FragCoord.xy - 0.5 * u_res) / min(u_res.x, u_res.y);
        
        float zoom = 1.6 - (u_p1 / 100.0) * 0.9;
        vec2 z = uv * zoom + u_pan;
        
        vec2 c = vec2(
          -0.4 + 0.3 * sin(u_time * 0.23 + u_p3 * 0.02),
           0.6 + 0.2 * cos(u_time * 0.19)
        );
        
        float iter = 0.0;
        float maxIter = 90.0;
        float minDist = 1e20;
        vec2 trapPos = vec2(0.0);
        
        for (float i = 0.0; i < 120.0; i++) {
          if (i >= maxIter) break;
          z = vec2(z.x * z.x - z.y * z.y, 2.0 * z.x * z.y) + c;
          
          float d = length(z);
          if (d < minDist) {
            minDist = d;
            trapPos = z;
          }
          if (d > 4.0) break;
          iter++;
        }
        
        float t = iter / maxIter;
        float angle = atan(trapPos.y, trapPos.x);
        vec3 col = 0.5 + 0.5 * cos(6.2831 * (t * 1.5 + angle * 0.1 + u_time * 0.04 + vec3(0.0, 0.3, 0.6)));
        
        col += exp(-minDist * 6.0) * vec3(0.4, 0.8, 1.0) * (0.7 + u_audio * 0.5);
        col *= 0.7 + 0.3 * (u_p2 / 100.0);
        
        if (iter >= maxIter) col *= 0.15;
        
        gl_FragColor = vec4(col, 1.0);
      }
    """.trimIndent()
  )

  val KALEIDO_FOLD = FractalPreset(
    id = "kaleido_fold",
    name = "Kaleido Fold",
    category = "IFS Fractals",
    description = "Iterated fold symmetries with spherical inversion, rotation matrices, and harmonic crystals.",
    defaultP1 = 40f,
    defaultP2 = 50f,
    defaultP3 = 30f,
    defaultPanX = 0f,
    defaultPanY = 0f,
    fragmentShader = """
      precision highp float;
      uniform vec2  u_res;
      uniform float u_time;
      uniform float u_p1;
      uniform float u_p2;
      uniform float u_p3;
      uniform float u_audio;
      uniform vec2  u_pan;

      vec2 rot(vec2 p, float a) {
        float c = cos(a), s = sin(a);
        return mat2(c, -s, s, c) * p;
      }

      void main() {
        vec2 uv = (gl_FragCoord.xy - 0.5 * u_res) / min(u_res.x, u_res.y);
        
        float scale = 1.8 + (u_p1 / 100.0) * 1.4;
        vec2 p = uv + u_pan;
        float d = 1e10;
        float trap = 1e10;
        
        for (int i = 0; i < 12; i++) {
          p = clamp(p, -1.0, 1.0) * 2.0 - p;
          
          float r2 = dot(p, p);
          float k = max(1.0 / max(r2, 0.0001), 1.0);
          p *= k;
          
          p = rot(p, u_time * 0.11 + float(i) * 0.4 + u_p3 * 0.015);
          p = p * scale + vec2(-0.5, -0.3);
          
          d = min(d, length(p) - 0.15);
          trap = min(trap, abs(p.x) + abs(p.y));
        }
        
        float t = exp(-abs(d) * 3.0);
        float hue = fract(trap * 0.7 + u_time * 0.04 + u_p2 * 0.01 + u_audio * 0.1);
        
        vec3 col = 0.5 + 0.5 * cos(6.2831 * (hue + vec3(0.0, 0.33, 0.67)));
        col *= t * 1.4;
        col += exp(-length(uv) * 2.5) * vec3(0.1, 0.15, 0.3);
        
        gl_FragColor = vec4(col, 1.0);
      }
    """.trimIndent()
  )

  val ALL_PRESETS = listOf(
    MANDELBROT,
    BURNING_SHIP,
    JULIA_ORBIT,
    KALEIDO_FOLD
  )
}
