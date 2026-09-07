package com.example.fractal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

data class FractalUiState(
  val activePreset: FractalPreset = FractalShaderPresets.MANDELBROT,
  val isRunning: Boolean = true,
  val p1: Float = 45f,
  val p2: Float = 50f,
  val p3: Float = 30f,
  val panX: Float = 0f,
  val panY: Float = 0f,
  val showCode: Boolean = false,
  val activeCodeTab: String = "draw", // init, update, draw
  val editableCode: String = FractalShaderPresets.MANDELBROT.fragmentShader,
  val isCodeModified: Boolean = false,
  val audioEnabled: Boolean = false,
  val audioLevel: Float = 0f,
  val error: String? = null,
  val showInfoModal: Boolean = false
)

class FractalViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(FractalUiState())
  val uiState: StateFlow<FractalUiState> = _uiState.asStateFlow()

  var renderer: FractalRenderer? = null
    private set

  private var audioJob: Job? = null

  init {
    renderer = FractalRenderer(
      initialFragmentShader = _uiState.value.activePreset.fragmentShader,
      onCompileResult = { success, errorMsg ->
        _uiState.update { current ->
          current.copy(error = if (success) null else errorMsg)
        }
      }
    )
    syncRendererParams()
  }

  fun setPreset(preset: FractalPreset) {
    _uiState.update {
      it.copy(
        activePreset = preset,
        p1 = preset.defaultP1,
        p2 = preset.defaultP2,
        p3 = preset.defaultP3,
        panX = preset.defaultPanX,
        panY = preset.defaultPanY,
        editableCode = preset.fragmentShader,
        isCodeModified = false,
        error = null
      )
    }
    renderer?.updateShader(preset.fragmentShader)
    syncRendererParams()
  }

  fun toggleRunning() {
    _uiState.update { current ->
      val newRunning = !current.isRunning
      renderer?.setRunning(newRunning)
      current.copy(isRunning = newRunning)
    }
  }

  fun resetParams() {
    val currentPreset = _uiState.value.activePreset
    _uiState.update {
      it.copy(
        p1 = currentPreset.defaultP1,
        p2 = currentPreset.defaultP2,
        p3 = currentPreset.defaultP3,
        panX = currentPreset.defaultPanX,
        panY = currentPreset.defaultPanY,
        editableCode = currentPreset.fragmentShader,
        isCodeModified = false,
        error = null
      )
    }
    renderer?.resetTime()
    renderer?.updateShader(currentPreset.fragmentShader)
    syncRendererParams()
  }

  fun updateP1(newP1: Float) {
    _uiState.update { it.copy(p1 = newP1) }
    syncRendererParams()
  }

  fun updateP2(newP2: Float) {
    _uiState.update { it.copy(p2 = newP2) }
    syncRendererParams()
  }

  fun updateP3(newP3: Float) {
    _uiState.update { it.copy(p3 = newP3) }
    syncRendererParams()
  }

  fun onPan(dx: Float, dy: Float, viewportMinDim: Float) {
    val minDim = if (viewportMinDim > 10f) viewportMinDim else 800f
    val zoomFactor = exp(-_uiState.value.p1 * 0.06f) * 1.8f
    val dPanX = -(dx / minDim) * zoomFactor
    val dPanY = (dy / minDim) * zoomFactor

    _uiState.update {
      it.copy(
        panX = it.panX + dPanX,
        panY = it.panY + dPanY
      )
    }
    syncRendererParams()
  }

  fun onZoomGesture(scaleFactor: Float) {
    val delta = (scaleFactor - 1.0f) * 20f
    val newP1 = (_uiState.value.p1 + delta).coerceIn(0f, 100f)
    updateP1(newP1)
  }

  fun toggleShowCode() {
    _uiState.update { it.copy(showCode = !it.showCode) }
  }

  fun setActiveCodeTab(tab: String) {
    _uiState.update { it.copy(activeCodeTab = tab) }
  }

  fun onCodeChanged(newCode: String) {
    _uiState.update {
      it.copy(
        editableCode = newCode,
        isCodeModified = true
      )
    }
  }

  fun applyShaderCode() {
    val code = _uiState.value.editableCode
    renderer?.updateShader(code)
  }

  fun revertToPresetCode() {
    val presetCode = _uiState.value.activePreset.fragmentShader
    _uiState.update {
      it.copy(
        editableCode = presetCode,
        isCodeModified = false,
        error = null
      )
    }
    renderer?.updateShader(presetCode)
  }

  fun toggleAudio() {
    val nextAudio = !_uiState.value.audioEnabled
    _uiState.update { it.copy(audioEnabled = nextAudio) }

    if (nextAudio) {
      startAudioSynthLoop()
    } else {
      audioJob?.cancel()
      audioJob = null
      _uiState.update { it.copy(audioLevel = 0f) }
      syncRendererParams()
    }
  }

  private fun startAudioSynthLoop() {
    audioJob?.cancel()
    audioJob = viewModelScope.launch {
      var step = 0f
      while (isActive) {
        step += 0.1f
        // Dynamic rhythmic envelope: kick hit every ~1s with fast decay + harmonic sub-bass
        val beat = (sin(step * 4.0).toFloat().coerceAtLeast(0f) * 1.5f) +
                   (cos(step * 1.8).toFloat().coerceAtLeast(0f) * 0.8f)
        val level = (beat * 0.7f).coerceIn(0f, 1.2f)

        _uiState.update { it.copy(audioLevel = level) }
        syncRendererParams()
        delay(33) // ~30fps audio pulse updates
      }
    }
  }

  fun dismissError() {
    _uiState.update { it.copy(error = null) }
  }

  fun showInfo(show: Boolean) {
    _uiState.update { it.copy(showInfoModal = show) }
  }

  private fun syncRendererParams() {
    val s = _uiState.value
    renderer?.updateParams(
      newP1 = s.p1,
      newP2 = s.p2,
      newP3 = s.p3,
      newPanX = s.panX,
      newPanY = s.panY,
      newAudio = if (s.audioEnabled) s.audioLevel else 0f
    )
  }

  override fun onCleared() {
    super.onCleared()
    audioJob?.cancel()
  }
}
