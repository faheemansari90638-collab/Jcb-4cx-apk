package com.example.engine3d

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.GameUiState

@SuppressLint("ClickableViewAccessibility")
@Composable
fun Jcb3DViewport(
    state: GameUiState,
    renderer: GlRenderer,
    modifier: Modifier = Modifier,
    onPartTouchDrag: ((dx: Float, dy: Float) -> Unit)? = null
) {
    val context = LocalContext.current
    val glView = remember(context) {
        JcbGlSurfaceView(context, renderer)
    }

    LaunchedEffect(state) {
        renderer.gameState = state
        glView.onPartDragCallback = onPartTouchDrag
    }

    DisposableEffect(glView) {
        glView.onResume()
        onDispose {
            glView.onPause()
        }
    }

    AndroidView(
        factory = { glView },
        modifier = modifier
    )
}

@SuppressLint("ViewConstructor")
class JcbGlSurfaceView(
    context: Context,
    private val renderer: GlRenderer
) : GLSurfaceView(context) {

    var onPartDragCallback: ((dx: Float, dy: Float) -> Unit)? = null
    private var prevX = 0f
    private var prevY = 0f
    private var isScaling = false

    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                isScaling = true
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val minZoom = if (renderer.gameState.route == com.example.model.ScreenRoute.MICRO_ASSEMBLY) 0.22f else 2.0f
                renderer.orbitDistance = (renderer.orbitDistance / detector.scaleFactor).coerceIn(minZoom, 16.0f)
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                isScaling = false
            }
        }
    )

    init {
        setEGLContextClientVersion(3)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                prevX = event.x
                prevY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isScaling && event.pointerCount == 1) {
                    val dx = event.x - prevX
                    val dy = event.y - prevY
                    if (event.y > height * 0.72f && onPartDragCallback != null) {
                        // Bottom touch zone or single-finger camera orbit
                        renderer.orbitYawDeg = (renderer.orbitYawDeg - dx * 0.35f) % 360f
                        renderer.orbitPitchDeg = (renderer.orbitPitchDeg + dy * 0.28f).coerceIn(4f, 75f)
                    } else {
                        renderer.orbitYawDeg = (renderer.orbitYawDeg - dx * 0.35f) % 360f
                        renderer.orbitPitchDeg = (renderer.orbitPitchDeg + dy * 0.28f).coerceIn(4f, 75f)
                    }
                    prevX = event.x
                    prevY = event.y
                }
            }
        }
        return true
    }
}
