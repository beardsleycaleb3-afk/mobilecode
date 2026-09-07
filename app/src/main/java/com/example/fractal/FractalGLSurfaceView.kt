package com.example.fractal

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import kotlin.math.exp

@SuppressLint("ViewConstructor")
class FractalGLSurfaceView(
  context: Context,
  private val renderer: FractalRenderer,
  private val onPanChanged: (dx: Float, dy: Float) -> Unit,
  private val onZoomScale: (scaleFactor: Float) -> Unit
) : GLSurfaceView(context) {

  private var lastTouchX = 0f
  private var lastTouchY = 0f
  private var isDragging = false

  private val scaleDetector = ScaleGestureDetector(
    context,
    object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
      override fun onScale(detector: ScaleGestureDetector): Boolean {
        val factor = detector.scaleFactor
        if (factor > 0.01f && factor < 100f) {
          onZoomScale(factor)
        }
        return true
      }
    }
  )

  init {
    setEGLContextClientVersion(2)
    setRenderer(renderer)
    renderMode = RENDERMODE_CONTINUOUSLY
    preserveEGLContextOnPause = true
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
    scaleDetector.onTouchEvent(event)

    if (scaleDetector.isInProgress) {
      isDragging = false
      return true
    }

    when (event.actionMasked) {
      MotionEvent.ACTION_DOWN -> {
        lastTouchX = event.x
        lastTouchY = event.y
        isDragging = true
      }

      MotionEvent.ACTION_MOVE -> {
        if (isDragging && event.pointerCount == 1) {
          val dx = event.x - lastTouchX
          val dy = event.y - lastTouchY
          lastTouchX = event.x
          lastTouchY = event.y
          onPanChanged(dx, dy)
        }
      }

      MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
        isDragging = false
      }
    }

    return true
  }
}
