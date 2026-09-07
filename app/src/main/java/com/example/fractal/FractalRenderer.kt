package com.example.fractal

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.SystemClock
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class FractalRenderer(
  initialFragmentShader: String,
  private val onCompileResult: (Boolean, String?) -> Unit
) : GLSurfaceView.Renderer {

  private val quadCoords = floatArrayOf(
    -1.0f, -1.0f,
     1.0f, -1.0f,
    -1.0f,  1.0f,
     1.0f,  1.0f
  )

  private var vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(quadCoords.size * 4)
    .order(ByteOrder.nativeOrder())
    .asFloatBuffer()
    .apply {
      put(quadCoords)
      position(0)
    }

  @Volatile private var programId = 0
  @Volatile private var pendingFragmentShader: String? = initialFragmentShader
  @Volatile private var isRunning = true
  @Volatile private var p1 = 45f
  @Volatile private var p2 = 50f
  @Volatile private var p3 = 30f
  @Volatile private var panX = 0f
  @Volatile private var panY = 0f
  @Volatile private var audioLevel = 0f

  private var viewportWidth = 1
  private var viewportHeight = 1
  private var startTime = SystemClock.uptimeMillis()
  private var accumulatedTime = 0f
  private var lastFrameTime = SystemClock.uptimeMillis()

  // Uniform locations
  private var uResLoc = -1
  private var uTimeLoc = -1
  private var uP1Loc = -1
  private var uP2Loc = -1
  private var uP3Loc = -1
  private var uPanLoc = -1
  private var uAudioLoc = -1
  private var aPosLoc = -1

  override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
    GLES20.glClearColor(0.02f, 0.03f, 0.05f, 1.0f)
    compilePendingShaderIfAny()
  }

  override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    viewportWidth = if (width > 0) width else 1
    viewportHeight = if (height > 0) height else 1
    GLES20.glViewport(0, 0, viewportWidth, viewportHeight)
  }

  override fun onDrawFrame(gl: GL10?) {
    compilePendingShaderIfAny()

    val now = SystemClock.uptimeMillis()
    if (isRunning) {
      val dt = ((now - lastFrameTime).coerceAtLeast(0L).toFloat()) / 1000f
      accumulatedTime += dt.coerceAtMost(0.1f)
    }
    lastFrameTime = now

    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

    if (programId == 0) return

    GLES20.glUseProgram(programId)

    if (aPosLoc != -1) {
      GLES20.glEnableVertexAttribArray(aPosLoc)
      GLES20.glVertexAttribPointer(aPosLoc, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
    }

    if (uResLoc != -1) {
      GLES20.glUniform2f(uResLoc, viewportWidth.toFloat(), viewportHeight.toFloat())
    }
    if (uTimeLoc != -1) {
      GLES20.glUniform1f(uTimeLoc, accumulatedTime)
    }
    if (uP1Loc != -1) {
      GLES20.glUniform1f(uP1Loc, p1)
    }
    if (uP2Loc != -1) {
      GLES20.glUniform1f(uP2Loc, p2)
    }
    if (uP3Loc != -1) {
      GLES20.glUniform1f(uP3Loc, p3)
    }
    if (uPanLoc != -1) {
      GLES20.glUniform2f(uPanLoc, panX, panY)
    }
    if (uAudioLoc != -1) {
      GLES20.glUniform1f(uAudioLoc, audioLevel)
    }

    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

    if (aPosLoc != -1) {
      GLES20.glDisableVertexAttribArray(aPosLoc)
    }
  }

  private fun compilePendingShaderIfAny() {
    val shaderSource = pendingFragmentShader ?: return
    pendingFragmentShader = null

    val vsId = loadShader(GLES20.GL_VERTEX_SHADER, FractalShaderPresets.VERTEX_SHADER)
    if (vsId == 0) {
      onCompileResult(false, "Vertex Shader compile failed")
      return
    }

    val fsId = loadShader(GLES20.GL_FRAGMENT_SHADER, shaderSource)
    if (fsId == 0) {
      val log = GLES20.glGetShaderInfoLog(fsId)
      onCompileResult(false, if (log.isNullOrBlank()) "Fragment Shader error" else log)
      GLES20.glDeleteShader(vsId)
      return
    }

    val newProgram = GLES20.glCreateProgram()
    GLES20.glAttachShader(newProgram, vsId)
    GLES20.glAttachShader(newProgram, fsId)
    GLES20.glLinkProgram(newProgram)

    val linkStatus = IntArray(1)
    GLES20.glGetProgramiv(newProgram, GLES20.GL_LINK_STATUS, linkStatus, 0)
    if (linkStatus[0] == 0) {
      val infoLog = GLES20.glGetProgramInfoLog(newProgram)
      GLES20.glDeleteProgram(newProgram)
      GLES20.glDeleteShader(vsId)
      GLES20.glDeleteShader(fsId)
      onCompileResult(false, "Program Link Error: $infoLog")
      return
    }

    // Success! Delete old program if exists
    if (programId != 0) {
      GLES20.glDeleteProgram(programId)
    }
    programId = newProgram

    // Cache uniform locations
    aPosLoc = GLES20.glGetAttribLocation(programId, "a_pos")
    uResLoc = GLES20.glGetUniformLocation(programId, "u_res")
    uTimeLoc = GLES20.glGetUniformLocation(programId, "u_time")
    uP1Loc = GLES20.glGetUniformLocation(programId, "u_p1")
    uP2Loc = GLES20.glGetUniformLocation(programId, "u_p2")
    uP3Loc = GLES20.glGetUniformLocation(programId, "u_p3")
    uPanLoc = GLES20.glGetUniformLocation(programId, "u_pan")
    uAudioLoc = GLES20.glGetUniformLocation(programId, "u_audio")

    onCompileResult(true, null)
  }

  private fun loadShader(type: Int, shaderCode: String): Int {
    val shader = GLES20.glCreateShader(type)
    GLES20.glShaderSource(shader, shaderCode)
    GLES20.glCompileShader(shader)

    val compiled = IntArray(1)
    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
    if (compiled[0] == 0) {
      val errorMsg = GLES20.glGetShaderInfoLog(shader)
      onCompileResult(false, errorMsg)
      GLES20.glDeleteShader(shader)
      return 0
    }
    return shader
  }

  fun updateShader(fragmentShader: String) {
    pendingFragmentShader = fragmentShader
  }

  fun updateParams(
    newP1: Float,
    newP2: Float,
    newP3: Float,
    newPanX: Float,
    newPanY: Float,
    newAudio: Float
  ) {
    p1 = newP1
    p2 = newP2
    p3 = newP3
    panX = newPanX
    panY = newPanY
    audioLevel = newAudio
  }

  fun setRunning(running: Boolean) {
    isRunning = running
    if (running) {
      lastFrameTime = SystemClock.uptimeMillis()
    }
  }

  fun resetTime() {
    accumulatedTime = 0f
    lastFrameTime = SystemClock.uptimeMillis()
  }
}
