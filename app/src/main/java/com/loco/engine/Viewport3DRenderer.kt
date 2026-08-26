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
    var distance = 35f

    @Volatile
    var ambientBrightness = 1f

    @Volatile
    var sceneLightContribution = 0f

    var lastAppliedResetTrigger = 0

    var gridSpacing = 20f

    var floorVisible = true

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

    private lateinit var floorVertexBuffer: FloatBuffer
    private lateinit var floorIndexBuffer: java.nio.ShortBuffer
    private var floorIndexCount = 0

    private lateinit var gridVertexBuffer: FloatBuffer
    private var gridVertexCount = 0

    private var aspect = 1f
    private val projMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val viewProjMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val tintColorLoc = -1

    private val floorY = -2f
    private val floorSize = 300f
    private var builtGridSpacing = -1f

    private fun rebuildGridBuffer() {

        val gridLines = mutableListOf<Float>()
        val gridShade = floatArrayOf(0.32f, 0.36f, 0.42f)
        val gridY = floorY + 0.01f
        var g = -floorSize

        while (g <= floorSize) {

            gridLines.addAll(
                listOf(
                    g, gridY, -floorSize, gridShade[0], gridShade[1], gridShade[2],
                    g, gridY, floorSize, gridShade[0], gridShade[1], gridShade[2]
                )
            )

            gridLines.addAll(
                listOf(
                    -floorSize, gridY, g, gridShade[0], gridShade[1], gridShade[2],
                    floorSize, gridY, g, gridShade[0], gridShade[1], gridShade[2]
                )
            )

            g += gridSpacing
        }

        val gridArray = gridLines.toFloatArray()
        gridVertexCount = gridArray.size / 6

        gridVertexBuffer = ByteBuffer
            .allocateDirect(gridArray.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(gridArray); position(0) }

        builtGridSpacing = gridSpacing
    }

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

        val floorShade = floatArrayOf(0.16f, 0.18f, 0.22f)

        val floorVerts = floatArrayOf(
            -floorSize, floorY, -floorSize, floorShade[0], floorShade[1], floorShade[2],
            floorSize, floorY, -floorSize, floorShade[0], floorShade[1], floorShade[2],
            floorSize, floorY, floorSize, floorShade[0], floorShade[1], floorShade[2],
            -floorSize, floorY, floorSize, floorShade[0], floorShade[1], floorShade[2]
        )
        val floorIdx = shortArrayOf(0, 1, 2, 0, 2, 3)
        floorIndexCount = floorIdx.size

        floorVertexBuffer = ByteBuffer
            .allocateDirect(floorVerts.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(floorVerts); position(0) }

        floorIndexBuffer = ByteBuffer
            .allocateDirect(floorIdx.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply { put(floorIdx); position(0) }

        rebuildGridBuffer()

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
        Matrix.perspectiveM(projMatrix, 0, 55f, aspect, 0.1f, 800f)
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

        if (builtGridSpacing != gridSpacing) {
            rebuildGridBuffer()
        }

        if (floorVisible) {

            // ---- draw floor plane ----
            floorVertexBuffer.position(0)
            GLES20.glVertexAttribPointer(
                positionHandle, 3, GLES20.GL_FLOAT, false, strideBytes, floorVertexBuffer
            )
            floorVertexBuffer.position(3)
            GLES20.glVertexAttribPointer(
                shadeHandle, 3, GLES20.GL_FLOAT, false, strideBytes, floorVertexBuffer
            )

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, viewProjMatrix, 0, modelMatrix, 0)
            GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)
            val floorBrightness = ambientBrightness + sceneLightContribution * 0.05f
            GLES20.glUniform3f(tintHandle, floorBrightness, floorBrightness, floorBrightness)

            floorIndexBuffer.position(0)
            GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, floorIndexCount, GLES20.GL_UNSIGNED_SHORT, floorIndexBuffer
            )

            // ---- draw grid lines ----
            gridVertexBuffer.position(0)
            GLES20.glVertexAttribPointer(
                positionHandle, 3, GLES20.GL_FLOAT, false, strideBytes, gridVertexBuffer
            )
            gridVertexBuffer.position(3)
            GLES20.glVertexAttribPointer(
                shadeHandle, 3, GLES20.GL_FLOAT, false, strideBytes, gridVertexBuffer
            )

            GLES20.glDrawArrays(GLES20.GL_LINES, 0, gridVertexCount)
        }

        // ---- draw scene objects ----
        for (obj in objectsSnapshot) {

            val worldX = obj.x / 40f
            val worldY = -obj.y / 40f
            val worldScale = (0.6f * obj.scale).coerceIn(0.2f, 2.5f)

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, worldX, worldY, 0f)
            Matrix.scaleM(modelMatrix, 0, worldScale, worldScale, worldScale)

            Matrix.multiplyMM(mvpMatrix, 0, viewProjMatrix, 0, modelMatrix, 0)

            GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)

            val boost = (if (obj.selected) 1.3f else 1f) *
                (ambientBrightness + sceneLightContribution * 0.05f)
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
        "Light" -> Triple(0.92f, 0.7f, 0.03f)
        "Plane" -> Triple(0.39f, 0.45f, 0.55f)
        "Cylinder" -> Triple(0.66f, 0.33f, 0.97f)
        "Cone" -> Triple(0.93f, 0.28f, 0.6f)
        "Capsule" -> Triple(0.08f, 0.72f, 0.65f)
        "Empty" -> Triple(0.58f, 0.64f, 0.72f)
        else -> Triple(0.92f, 0.7f, 0.03f)
    }
}

@Composable
fun Viewport3DView(
    objects: List<GameObject>,
    selectedObjectId: Int?,
    backgroundColor: Color = Color(0xFF182233),
    ambientBrightness: Float = 1f,
    resetCameraTrigger: Int = 0,
    recenterPanTrigger: Int = 0,
    gridSpacing: Float = 20f,
    floorVisible: Boolean = true,
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
                var lastPinchDistance = 0f
                var lastMidX = 0f
                var lastMidY = 0f

                fun pinchDistance(event: android.view.MotionEvent): Float {
                    val dx = event.getX(0) - event.getX(1)
                    val dy = event.getY(0) - event.getY(1)
                    return kotlin.math.sqrt(dx * dx + dy * dy)
                }

                fun midX(event: android.view.MotionEvent): Float {
                    return (event.getX(0) + event.getX(1)) / 2f
                }

                fun midY(event: android.view.MotionEvent): Float {
                    return (event.getY(0) + event.getY(1)) / 2f
                }

                setOnTouchListener { _, event ->

                    when (event.actionMasked) {

                        android.view.MotionEvent.ACTION_DOWN -> {
                            lastTouchX = event.x
                            lastTouchY = event.y
                        }

                        android.view.MotionEvent.ACTION_POINTER_DOWN -> {

                            if (event.pointerCount == 2) {
                                lastPinchDistance = pinchDistance(event)
                                lastMidX = midX(event)
                                lastMidY = midY(event)
                            }
                        }

                        android.view.MotionEvent.ACTION_MOVE -> {

                            if (event.pointerCount >= 2) {

                                val newPinchDistance = pinchDistance(event)
                                val newMidX = midX(event)
                                val newMidY = midY(event)

                                if (lastPinchDistance > 0f) {

                                    val zoomDelta =
                                        (lastPinchDistance - newPinchDistance) * 0.03f

                                    renderer.distance = (
                                        renderer.distance + zoomDelta
                                    ).coerceIn(3f, 250f)

                                    val panScale = renderer.distance * 0.0025f
                                    val panDx = (newMidX - lastMidX) * panScale
                                    val panDy = (newMidY - lastMidY) * panScale

                                    val yawRad = Math.toRadians(renderer.yawDeg.toDouble())
                                    val cosYaw = kotlin.math.cos(yawRad).toFloat()
                                    val sinYaw = kotlin.math.sin(yawRad).toFloat()

                                    renderer.targetX -= panDx * cosYaw
                                    renderer.targetY += panDx * sinYaw
                                    renderer.targetY += panDy * cosYaw
                                }

                                lastPinchDistance = newPinchDistance
                                lastMidX = newMidX
                                lastMidY = newMidY

                            } else {

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

            renderer.ambientBrightness = ambientBrightness
            renderer.gridSpacing = gridSpacing
            renderer.floorVisible = floorVisible

            renderer.sceneLightContribution = objects
                .filter { it.visible }
                .flatMap { it.components.components }
                .filter { it.type == ComponentType.LIGHT && it.enabled }
                .sumOf { it.intensity.toDouble() }
                .toFloat()

            if (renderer.lastAppliedResetTrigger != resetCameraTrigger) {
                renderer.yawDeg = 45f
                renderer.pitchDeg = 30f
                renderer.distance = 35f
                renderer.lastAppliedResetTrigger = resetCameraTrigger
            }

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
