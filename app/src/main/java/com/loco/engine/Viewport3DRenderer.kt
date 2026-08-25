package com.loco.engine

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

/**
 * A single object to draw in the 3D viewport. Position is in "editor units"
 * (the same x/y the 2D inspector shows), converted to world space in the
 * renderer.
 */
data class RenderObject3D(
    val id: Int,
    val x: Float,
    val y: Float,
    val scale: Float,
    val colorR: Float,
    val colorG: Float,
    val colorB: Float,
    val selected: Boolean
)

private fun cubeVertexData(): FloatArray {

    val faceColors = arrayOf(
        floatArrayOf(1.0f, 1.0f, 1.0f),
        floatArrayOf(0.85f, 0.85f, 0.85f),
        floatArrayOf(0.7f, 0.7f, 0.7f),
        floatArrayOf(0.55f, 0.55f, 0.55f),
        floatArrayOf(0.4f, 0.4f, 0.4f),
        floatArrayOf(0.25f, 0.25f, 0.25f)
    )

    val positions = arrayOf(
        // front
        arrayOf(
            floatArrayOf(-0.5f, -0.5f, 0.5f),
            floatArrayOf(0.5f, -0.5f, 0.5f),
            floatArrayOf(0.5f, 0.5f, 0.5f),
            floatArrayOf(-0.5f, 0.5f, 0.5f)
        ),
        // back
        arrayOf(
            floatArrayOf(-0.5f, -0.5f, -0.5f),
            floatArrayOf(0.5f, -0.5f, -0.5f),
            floatArrayOf(0.5f, 0.5f, -0.5f),
            floatArrayOf(-0.5f, 0.5f, -0.5f)
        ),
        // top
        arrayOf(
            floatArrayOf(-0.5f, 0.5f, -0.5f),
            floatArrayOf(0.5f, 0.5f, -0.5f),
            floatArrayOf(0.5f, 0.5f, 0.5f),
            floatArrayOf(-0.5f, 0.5f, 0.5f)
        ),
        // bottom
        arrayOf(
            floatArrayOf(-0.5f, -0.5f, -0.5f),
            floatArrayOf(0.5f, -0.5f, -0.5f),
            floatArrayOf(0.5f, -0.5f, 0.5f),
            floatArrayOf(-0.5f, -0.5f, 0.5f)
        ),
        // right
        arrayOf(
            floatArrayOf(0.5f, -0.5f, -0.5f),
            floatArrayOf(0.5f, 0.5f, -0.5f),
            floatArrayOf(0.5f, 0.5f, 0.5f),
            floatArrayOf(0.5f, -0.5f, 0.5f)
        ),
        // left
        arrayOf(
            floatArrayOf(-0.5f, -0.5f, -0.5f),
            floatArrayOf(-0.5f, 0.5f, -0.5f),
            floatArrayOf(-0.5f, 0.5f, 0.5f),
            floatArrayOf(-0.5f, -0.5f, 0.5f)
        )
    )

    val data = mutableListOf<Float>()

    for (face in positions.indices) {
        val shade = faceColors[face]
        for (vertex in positions[face]) {
            data.add(vertex[0])
            data.add(vertex[1])
            data.add(vertex[2])
            data.add(shade[0])
            data.add(shade[1])
            data.add(shade[2])
        }
    }

    return data.toFloatArray()
}

private fun cubeIndexData(): ShortArray {

    val indices = mutableListOf<Short>()

    for (face in 0 until 6) {
        val base = (face * 4).toShort()
        indices.add(base)
        indices.add((base + 1).toShort())
        indices.add((base + 2).toShort())
        indices.add(base)
        indices.add((base + 2).toShort())
        indices.add((base + 3).toShort())
    }

    return indices.toShortArray()
}

class Viewport3DRenderer : GLSurfaceView.Renderer {

    @Volatile
    var renderObjects: List<RenderObject3D> = emptyList()

    @Volatile
    var yawDeg = 45f

    @Volatile
    var pitchDeg = 30f

    @Volatile
    var distance = 11f

    @Volatile
    var targetX = 0f

    @Volatile
    var targetY = 0f

    @Volatile
    var clearR = 0.06f

    @Volatile
    var clearG = 0.07f

    @Volatile
    var clearB = 0.1f

    private var program = 0
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var indexBuffer: java.nio.ShortBuffer
    private var indexCount = 0
    private val strideBytes = 6 * 4

    private var aspect = 1f
    private val projMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val viewProjMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val tintColorLoc = -1

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        GLES20.glClearColor(0.06f, 0.07f, 0.1f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        val vertexData = cubeVertexData()
        val indexData = cubeIndexData()
        indexCount = indexData.size

        vertexBuffer = ByteBuffer
            .allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(vertexData); position(0) }

        indexBuffer = ByteBuffer
            .allocateDirect(indexData.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply { put(indexData); position(0) }

        val vertexSrc = listOf(
            "uniform mat4 uMVPMatrix;",
            "uniform vec3 uTintColor;",
            "attribute vec4 aPosition;",
            "attribute vec3 aShade;",
            "varying vec3 vColor;",
            "void main() {",
            "gl_Position = uMVPMatrix * aPosition;",
            "vColor = aShade * uTintColor;",
            "}"
        ).joinToString("\n")

        val fragmentSrc = listOf(
            "precision mediump float;",
            "varying vec3 vColor;",
            "void main() {",
            "gl_FragColor = vec4(vColor, 1.0);",
            "}"
        ).joinToString("\n")

        val vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vertexShader, vertexSrc)
        GLES20.glCompileShader(vertexShader)

        val fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fragmentShader, fragmentSrc)
        GLES20.glCompileShader(fragmentShader)

        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

        GLES20.glViewport(0, 0, width, height)
        aspect = if (height == 0) 1f else width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projMatrix, 0, 55f, aspect, 0.1f, 100f)
    }

    private fun updateViewMatrix() {

        val yawRad = Math.toRadians(yawDeg.toDouble())
        val pitchRad = Math.toRadians(pitchDeg.toDouble())

        val eyeX = targetX + (distance * cos(pitchRad) * sin(yawRad)).toFloat()
        val eyeY = targetY + (distance * sin(pitchRad)).toFloat()
        val eyeZ = (distance * cos(pitchRad) * cos(yawRad)).toFloat()

        Matrix.setLookAtM(
            viewMatrix, 0,
            eyeX, eyeY, eyeZ,
            targetX, targetY, 0f,
            0f, 1f, 0f
        )

        Matrix.multiplyMM(viewProjMatrix, 0, projMatrix, 0, viewMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {

        updateViewMatrix()

        GLES20.glClearColor(clearR, clearG, clearB, 1f)

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        val shadeHandle = GLES20.glGetAttribLocation(program, "aShade")
        val mvpHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        val tintHandle = GLES20.glGetUniformLocation(program, "uTintColor")

        vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(
            positionHandle, 3, GLES20.GL_FLOAT, false, strideBytes, vertexBuffer
        )

        vertexBuffer.position(3)
        GLES20.glEnableVertexAttribArray(shadeHandle)
        GLES20.glVertexAttribPointer(
            shadeHandle, 3, GLES20.GL_FLOAT, false, strideBytes, vertexBuffer
        )

        val objectsSnapshot = renderObjects

        for (obj in objectsSnapshot) {

            val worldX = obj.x / 40f
            val worldY = -obj.y / 40f
            val worldScale = (0.6f * obj.scale).coerceIn(0.2f, 2.5f)

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, worldX, worldY, 0f)
            Matrix.scaleM(modelMatrix, 0, worldScale, worldScale, worldScale)

            Matrix.multiplyMM(mvpMatrix, 0, viewProjMatrix, 0, modelMatrix, 0)

            GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)

            val boost = if (obj.selected) 1.3f else 1f
            GLES20.glUniform3f(
                tintHandle,
                (obj.colorR * boost).coerceAtMost(1f),
                (obj.colorG * boost).coerceAtMost(1f),
                (obj.colorB * boost).coerceAtMost(1f)
            )

            vertexBuffer.position(0)
            indexBuffer.position(0)
            GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer
            )
        }

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(shadeHandle)
    }
}

/** Maps a game-object type to an RGB tint so cubes match the 2D editor's color coding. */
fun objectTypeColorRgb(type: String): Triple<Float, Float, Float> {

    return when (type) {
        "Cube" -> Triple(0.23f, 0.51f, 0.96f)
        "Sphere" -> Triple(0.13f, 0.77f, 0.37f)
        "Camera" -> Triple(0.98f, 0.45f, 0.09f)
        else -> Triple(0.92f, 0.7f, 0.03f)
    }
}

@Composable
fun Viewport3DView(
    objects: List<GameObject>,
    selectedObjectId: Int?,
    backgroundColor: Color = Color(0xFF182233),
    modifier: Modifier = Modifier
) {

    AndroidView(
        modifier = modifier.fillMaxSize(),

        factory = { ctx: Context ->

            val renderer = Viewport3DRenderer()

            GLSurfaceView(ctx).apply {
                setEGLContextClientVersion(2)
                setRenderer(renderer)
                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                tag = renderer

                var lastTouchX = 0f
                var lastTouchY = 0f

                setOnTouchListener { _, event ->

                    when (event.action) {

                        android.view.MotionEvent.ACTION_DOWN -> {
                            lastTouchX = event.x
                            lastTouchY = event.y
                        }

                        android.view.MotionEvent.ACTION_MOVE -> {

                            val dx = event.x - lastTouchX
                            val dy = event.y - lastTouchY

                            renderer.yawDeg -= dx * 0.4f

                            renderer.pitchDeg = (
                                renderer.pitchDeg - dy * 0.4f
                            ).coerceIn(-85f, 85f)

                            lastTouchX = event.x
                            lastTouchY = event.y
                        }
                    }

                    true
                }
            }
        },

        update = { view ->

            val renderer = view.tag as Viewport3DRenderer

            renderer.clearR = backgroundColor.red
            renderer.clearG = backgroundColor.green
            renderer.clearB = backgroundColor.blue

            val focusedObject = objects.firstOrNull { it.id == selectedObjectId }

            if (focusedObject != null) {
                renderer.targetX = focusedObject.x / 40f
                renderer.targetY = -focusedObject.y / 40f
            } else {
                renderer.targetX = 0f
                renderer.targetY = 0f
            }

            renderer.renderObjects = objects
                .filter { it.visible }
                .map { obj ->

                val (r, g, b) = objectTypeColorRgb(obj.type)

                RenderObject3D(
                    id = obj.id,
                    x = obj.x,
                    y = obj.y,
                    scale = obj.scale,
                    colorR = r,
                    colorG = g,
                    colorB = b,
                    selected = obj.id == selectedObjectId
                )
            }
        }
    )
}
