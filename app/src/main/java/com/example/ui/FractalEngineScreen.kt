package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fractal.FractalGLSurfaceView
import com.example.fractal.FractalPreset
import com.example.fractal.FractalShaderPresets
import com.example.fractal.FractalUiState
import com.example.fractal.FractalViewModel
import com.example.ui.theme.EngineAmber
import com.example.ui.theme.EngineAmberBg
import com.example.ui.theme.EngineAmberBorder
import com.example.ui.theme.EngineBorder
import com.example.ui.theme.EngineBorderSubtle
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.EngineCodeGreen
import com.example.ui.theme.EngineControlBg
import com.example.ui.theme.EngineCyan
import com.example.ui.theme.EngineDarkBg
import com.example.ui.theme.EngineEmerald
import com.example.ui.theme.EngineEmeraldBg
import com.example.ui.theme.EngineEmeraldBorder
import com.example.ui.theme.EngineErrorRed
import com.example.ui.theme.EngineErrorRedBg
import com.example.ui.theme.EngineFuchsia
import com.example.ui.theme.EngineFuchsiaDark
import com.example.ui.theme.EngineFuchsiaLight
import com.example.ui.theme.EngineHeaderBg
import com.example.ui.theme.EnginePurple
import com.example.ui.theme.EnginePurpleBg
import com.example.ui.theme.EnginePurpleBorder
import com.example.ui.theme.EngineTextMuted
import com.example.ui.theme.EngineTextPrimary
import com.example.ui.theme.EngineTextSecondary
import java.util.Locale
import kotlin.math.min

@Composable
fun FractalEngineScreen(
  viewModel: FractalViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var viewportWidth by remember { mutableStateOf(1080) }
  var viewportHeight by remember { mutableStateOf(1920) }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(800),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dotPulse"
  )

  Surface(
    modifier = modifier
      .fillMaxSize()
      .statusBarsPadding()
      .navigationBarsPadding(),
    color = EngineDarkBg
  ) {
    Column(
      modifier = Modifier.fillMaxSize()
    ) {
      FractalHeader(
        uiState = uiState,
        pulseAlpha = pulseAlpha,
        onPresetSelected = { viewModel.setPreset(it) },
        onInfoClicked = { viewModel.showInfo(true) },
        onToggleCode = { viewModel.toggleShowCode() }
      )

      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .background(EngineDarkBg)
          .onSizeChanged {
            viewportWidth = it.width
            viewportHeight = it.height
          }
      ) {
        if (uiState.showCode) {
          FractalCodeEditor(
            uiState = uiState,
            onTabSelected = { viewModel.setActiveCodeTab(it) },
            onCodeChanged = { viewModel.onCodeChanged(it) },
            onApply = { viewModel.applyShaderCode() },
            onRevert = { viewModel.revertToPresetCode() }
          )
        } else {
          viewModel.renderer?.let { renderer ->
            AndroidView(
              factory = { context ->
                FractalGLSurfaceView(
                  context = context,
                  renderer = renderer,
                  onPanChanged = { dx, dy ->
                    val minDim = min(viewportWidth, viewportHeight).toFloat()
                    viewModel.onPan(dx, dy, minDim)
                  },
                  onZoomScale = { factor ->
                    viewModel.onZoomGesture(factor)
                  }
                )
              },
              modifier = Modifier
                .fillMaxSize()
                .testTag("fractal_canvas")
            )
          }

          Box(
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(12.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(Color.Black.copy(alpha = 0.65f))
              .border(1.dp, EngineBorderSubtle, RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "Drag to pan · Pinch to zoom",
              color = EngineTextSecondary,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp
            )
          }

          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(12.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(Color.Black.copy(alpha = 0.65f))
              .border(1.dp, EngineBorderSubtle, RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = String.format(Locale.US, "X:%.2f Y:%.2f Z:%.0f", uiState.panX, uiState.panY, uiState.p1),
              color = EngineCyan,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium
            )
          }

          if (!uiState.isRunning) {
            Box(
              modifier = Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.75f))
                .border(1.dp, EngineBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
              Text(
                text = "FRACTAL PAUSED · PRESS START",
                color = EngineTextMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )
            }
          }
        }

        if (uiState.error != null) {
          uiState.error?.let { err ->
            Row(
              modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(EngineErrorRedBg)
                .border(1.dp, EngineErrorRed.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = err,
                color = Color(0xFFFFCDD2),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
              )
              IconButton(
                onClick = { viewModel.dismissError() },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Dismiss error",
                  tint = EngineErrorRed,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }

      FractalControlDeck(
        uiState = uiState,
        onToggleRunning = { viewModel.toggleRunning() },
        onReset = { viewModel.resetParams() },
        onToggleAudio = { viewModel.toggleAudio() },
        onP1Change = { viewModel.updateP1(it) },
        onP2Change = { viewModel.updateP2(it) },
        onP3Change = { viewModel.updateP3(it) }
      )
    }

    if (uiState.showInfoModal) {
      FractalInfoDialog(onDismiss = { viewModel.showInfo(false) })
    }
  }
}

@Composable
private fun FractalHeader(
  uiState: FractalUiState,
  pulseAlpha: Float,
  onPresetSelected: (FractalPreset) -> Unit,
  onInfoClicked: () -> Unit,
  onToggleCode: () -> Unit
) {
  var presetMenuExpanded by remember { mutableStateOf(false) }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(EngineHeaderBg)
      .border(1.dp, EngineBorder)
      .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Box(
        modifier = Modifier
          .size(10.dp)
          .clip(CircleShape)
          .background(EngineFuchsia.copy(alpha = pulseAlpha))
      )
      Text(
        text = "0oo0 · FRACTAL",
        color = EngineFuchsiaLight,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        fontFamily = FontFamily.Monospace
      )
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Box {
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(EngineCardBg)
            .border(1.dp, EngineBorderSubtle, RoundedCornerShape(6.dp))
            .clickable { presetMenuExpanded = true }
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .testTag("preset_dropdown"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = uiState.activePreset.name,
            color = EngineFuchsiaLight,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Choose Preset",
            tint = EngineFuchsiaLight,
            modifier = Modifier.size(16.dp)
          )
        }

        DropdownMenu(
          expanded = presetMenuExpanded,
          onDismissRequest = { presetMenuExpanded = false },
          modifier = Modifier.background(EngineHeaderBg)
        ) {
          FractalShaderPresets.ALL_PRESETS.forEach { preset ->
            DropdownMenuItem(
              text = {
                Column {
                  Text(
                    text = preset.name,
                    color = if (preset.id == uiState.activePreset.id) EngineFuchsiaLight else EngineTextPrimary,
                    fontWeight = if (preset.id == uiState.activePreset.id) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                  )
                  Text(
                    text = preset.category,
                    color = EngineTextMuted,
                    fontSize = 10.sp
                  )
                }
              },
              onClick = {
                presetMenuExpanded = false
                onPresetSelected(preset)
              }
            )
          }
        }
      }

      IconButton(
        onClick = onInfoClicked,
        modifier = Modifier
          .size(32.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(EngineCardBg)
          .border(1.dp, EngineBorderSubtle, RoundedCornerShape(6.dp))
          .testTag("info_button")
      ) {
        Icon(
          imageVector = Icons.Default.Info,
          contentDescription = "Engine Info",
          tint = EngineTextSecondary,
          modifier = Modifier.size(16.dp)
        )
      }

      Button(
        onClick = onToggleCode,
        modifier = Modifier
          .height(32.dp)
          .testTag("toggle_code_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (uiState.showCode) EngineFuchsiaDark else EngineCardBg,
          contentColor = if (uiState.showCode) Color.White else EngineTextSecondary
        ),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (uiState.showCode) EngineFuchsia else EngineBorderSubtle
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
      ) {
        Icon(
          imageVector = if (uiState.showCode) Icons.Default.Visibility else Icons.Default.Code,
          contentDescription = if (uiState.showCode) "Visual Mode" else "Code Mode",
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (uiState.showCode) "VISUAL" else "CODE",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp
        )
      }
    }
  }
}

@Composable
private fun FractalCodeEditor(
  uiState: FractalUiState,
  onTabSelected: (String) -> Unit,
  onCodeChanged: (String) -> Unit,
  onApply: () -> Unit,
  onRevert: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(EngineDarkBg)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(EngineHeaderBg)
        .border(1.dp, EngineBorder),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        modifier = Modifier.horizontalScroll(rememberScrollState())
      ) {
        listOf("draw", "init", "update").forEach { tab ->
          val isSelected = uiState.activeCodeTab == tab
          Box(
            modifier = Modifier
              .clickable { onTabSelected(tab) }
              .background(if (isSelected) EngineDarkBg else Color.Transparent)
              .border(
                1.dp,
                if (isSelected) EngineFuchsia else Color.Transparent,
                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
              )
              .padding(horizontal = 14.dp, vertical = 9.dp)
          ) {
            Text(
              text = if (tab == "draw") "draw() [GLSL]" else "$tab()",
              color = if (isSelected) EngineFuchsiaLight else EngineTextMuted,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
          }
        }
      }

      Row(
        modifier = Modifier.padding(end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        if (uiState.isCodeModified) {
          OutlinedButton(
            onClick = onRevert,
            modifier = Modifier.height(28.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, EngineBorderSubtle),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = EngineTextSecondary)
          ) {
            Text("Revert", fontSize = 10.sp)
          }
        }

        Button(
          onClick = onApply,
          modifier = Modifier
            .height(28.dp)
            .testTag("apply_shader_button"),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
          shape = RoundedCornerShape(4.dp),
          colors = ButtonDefaults.buttonColors(containerColor = EngineFuchsia)
        ) {
          Text("Apply", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
      }
    }

    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(8.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(EngineHeaderBg)
        .border(1.dp, EngineBorder, RoundedCornerShape(8.dp))
        .padding(8.dp)
    ) {
      val displayCode = when (uiState.activeCodeTab) {
        "draw" -> uiState.editableCode
        "init" -> "// Vertex Shader Source\n${FractalShaderPresets.VERTEX_SHADER}\n\n// Uniform Configuration:\n// u_res: Screen Resolution\n// u_time: Elapsed Seconds\n// u_p1: Zoom Power\n// u_p2: Color Cycling\n// u_p3: Julia Morph\n// u_pan: 2D Coordinates\n// u_audio: Beat Reactivity"
        "update" -> "// Animation loop execution:\nstate.time += dt;\nu_p1 = params.p1;\nu_p2 = params.p2;\nu_p3 = params.p3;\nu_pan = params.pan;\nu_audio = audioReactivePulse;"
        else -> uiState.editableCode
      }

      val isEditable = uiState.activeCodeTab == "draw"

      BasicTextField(
        value = displayCode,
        onValueChange = { if (isEditable) onCodeChanged(it) },
        readOnly = !isEditable,
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .testTag("code_editor_input"),
        textStyle = TextStyle(
          color = EngineCodeGreen,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          lineHeight = 16.sp
        ),
        cursorBrush = SolidColor(EngineFuchsiaLight)
      )

      Box(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(4.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(Color.Black.copy(alpha = 0.7f))
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = if (isEditable) "Live GLSL Editor" else "Read-Only Spec",
          color = EngineTextMuted,
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}

@Composable
private fun FractalControlDeck(
  uiState: FractalUiState,
  onToggleRunning: () -> Unit,
  onReset: () -> Unit,
  onToggleAudio: () -> Unit,
  onP1Change: (Float) -> Unit,
  onP2Change: (Float) -> Unit,
  onP3Change: (Float) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(EngineControlBg)
      .border(1.dp, EngineBorder)
      .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Button(
        onClick = onToggleRunning,
        modifier = Modifier
          .weight(1.3f)
          .height(40.dp)
          .testTag("play_pause_button"),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (uiState.isRunning) EngineAmberBg else EngineEmeraldBg,
          contentColor = if (uiState.isRunning) EngineAmber else EngineEmerald
        ),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (uiState.isRunning) EngineAmberBorder else EngineEmeraldBorder
        ),
        contentPadding = PaddingValues(horizontal = 8.dp)
      ) {
        Icon(
          imageVector = if (uiState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
          contentDescription = if (uiState.isRunning) "Pause Simulation" else "Start Simulation",
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (uiState.isRunning) "PAUSE" else "START",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp
        )
      }

      Button(
        onClick = onReset,
        modifier = Modifier
          .weight(1f)
          .height(40.dp)
          .testTag("reset_button"),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = EngineCardBg,
          contentColor = EngineTextSecondary
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, EngineBorderSubtle),
        contentPadding = PaddingValues(horizontal = 8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = "Reset Parameters",
          modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "RESET",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Button(
        onClick = onToggleAudio,
        modifier = Modifier
          .weight(1.1f)
          .height(40.dp)
          .testTag("audio_toggle_button"),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (uiState.audioEnabled) EnginePurpleBg else EngineCardBg,
          contentColor = if (uiState.audioEnabled) EnginePurple else EngineTextSecondary
        ),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (uiState.audioEnabled) EnginePurpleBorder else EngineBorderSubtle
        ),
        contentPadding = PaddingValues(horizontal = 8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.GraphicEq,
          contentDescription = "Toggle Audio Reactivity",
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (uiState.audioEnabled) "BEAT ON" else "AUDIO",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(EngineCardBg)
        .border(1.dp, EngineBorder, RoundedCornerShape(8.dp))
        .padding(horizontal = 10.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      FractalSliderColumn(
        label = "ZOOM",
        value = uiState.p1,
        onValueChange = onP1Change,
        modifier = Modifier.weight(1f)
      )

      Spacer(modifier = Modifier.width(12.dp))

      FractalSliderColumn(
        label = "COLOR",
        value = uiState.p2,
        onValueChange = onP2Change,
        modifier = Modifier.weight(1f)
      )

      Spacer(modifier = Modifier.width(12.dp))

      FractalSliderColumn(
        label = "MORPH",
        value = uiState.p3,
        onValueChange = onP3Change,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun FractalSliderColumn(
  label: String,
  value: Float,
  onValueChange: (Float) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = label,
        color = EngineTextSecondary,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = value.toInt().toString(),
        color = EngineFuchsiaLight,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold
      )
    }

    Slider(
      value = value,
      onValueChange = onValueChange,
      valueRange = 0f..100f,
      colors = SliderDefaults.colors(
        thumbColor = EngineFuchsia,
        activeTrackColor = EngineFuchsia,
        inactiveTrackColor = EngineBorderSubtle
      ),
      modifier = Modifier
        .height(26.dp)
        .testTag("slider_${label.lowercase()}")
    )
  }
}

@Composable
private fun FractalInfoDialog(
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = EngineFuchsia),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("GOT IT", fontWeight = FontWeight.Bold, color = Color.White)
      }
    },
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "About 0oo0 Fractal Engine",
          color = EngineFuchsiaLight,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "Welcome to the 0oo0 GLSL Fractal Sandbox. Explore complex mathematical sets rendered in real-time via hardware-accelerated OpenGL fragment shaders.",
          color = EngineTextPrimary,
          fontSize = 12.sp,
          lineHeight = 17.sp
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          InfoBullet(
            title = "Presets",
            description = "Explore Mandelbrot Smooth, Burning Ship, Julia Orbit, and Kaleidoscopic IFS folds."
          )
          InfoBullet(
            title = "Interactive Pan & Zoom",
            description = "Touch and drag to pan coordinates smoothly. Pinch or use the ZOOM slider to dive deep into fractal boundaries."
          )
          InfoBullet(
            title = "Live Code Editor",
            description = "Switch to CODE mode to view and edit GLSL fragment shaders directly on your device with instant live shader recompilation."
          )
          InfoBullet(
            title = "Audio Reactivity",
            description = "Toggle the AUDIO beat synthesizer to pulsate fractal palettes and orbit traps to rhythmic audio waves."
          )
        }
      }
    },
    containerColor = EngineCardBg,
    shape = RoundedCornerShape(16.dp)
  )
}

@Composable
private fun InfoBullet(
  title: String,
  description: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Text("•", color = EngineFuchsia, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    Text(
      text = "$title: $description",
      color = EngineTextSecondary,
      fontSize = 11.sp,
      lineHeight = 16.sp
    )
  }
}
