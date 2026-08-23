package com.loco.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// =================================================================
// 1. UNREAL ENGINE FEATURES (PBR Materials, Blueprints, Sequencer)
// =================================================================

/** خامات PBR الواقعية (Unreal Engine) */
data class PBRMaterial(
    val albedoColor: Color = Color.White,
    val metallic: Float = 0.0f,   // 0 = غير معدني، 1 = معدني بالكامل
    val roughness: Float = 0.5f   // 0 = أملس/لامع، 1 = خشن
)

/** عقَد البرمجة المرئية (Unreal Blueprints) */
enum class BlueprintNodeType { EVENT_START, EVENT_TOUCH, APPLY_FORCE, CONDITION }

data class BlueprintNode(
    val id: Int,
    val type: BlueprintNodeType,
    val title: String,
    val posX: Float = 0f,
    val posY: Float = 0f
)

/** شريط الزمان والمفاتيح السينمائية (Unreal Sequencer) */
data class Keyframe(val timeInSeconds: Float, val value: Float)

data class SequencerTrack(
    val propertyName: String, // مثل "x" أو "y" أو "scale"
    val keyframes: List<Keyframe> = emptyList()
)

/** إعدادات التأثيرات البصرية (Unreal Post-Processing) */
data class PostProcessingSettings(
    val enableBloom: Boolean = true,
    val bloomIntensity: Float = 0.8f,
    val enableVignette: Boolean = false
)

// =================================================================
// 2. UNITY ENGINE FEATURES (Component System, Prefabs, Play Mode)
// =================================================================

/** نظام المكونات المعياري (Unity Components) */
sealed class EngineComponent {
    data class MeshRenderer(val pbrMaterial: PBRMaterial = PBRMaterial()) : EngineComponent()
    data class RigidBody(val mass: Float = 1.0f, val useGravity: Boolean = true) : EngineComponent()
    data class BoxCollider(val sizeX: Float = 1f, val sizeY: Float = 1f, val sizeZ: Float = 1f) : EngineComponent()
    data class AnimatorComponent(val currentState: String = "Idle") : EngineComponent()
    data class BlueprintComponent(val nodes: List<BlueprintNode> = emptyList()) : EngineComponent()
}

/** نظام النماذج الجاهزة (Unity Prefabs) */
data class Prefab(
    val prefabId: String,
    val name: String,
    val templateObject: GameObject
)

/** وضع التجربة والتشغيل (Unity Play/Pause Mode) */
enum class EnginePlayState { EDIT, PLAY, PAUSE }

// =================================================================
// 3. CORE GAME OBJECT & ENGINE MANAGER
// =================================================================

/** 
 * نموذج GameObject الموحد
 * يحتوي ميزات Unity و Unreal ومتوافق مع Viewport3DRenderer.kt
 */
data class GameObject(
    val id: Int,
    val name: String,
    val type: String,
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val rotation: Float = 0f,
    val scale: Float = 1f,
    
    // Unity
    val components: List<EngineComponent> = emptyList(),
    val isPrefab: Boolean = false,
    
    // Unreal
    val layer: Int = 0,
    val tag: String = "Default",
    val tracks: List<SequencerTrack> = emptyList()
)

/** مدير حالة المحرك وإدارة العناصر */
class LocoEngineManager {
    var objects by mutableStateOf<List<GameObject>>(emptyList())
        private set

    var selectedObjectId by mutableStateOf<Int?>(null)
        private set

    var playState by mutableStateOf(EnginePlayState.EDIT)
        private set

    var prefabs by mutableStateOf<List<Prefab>>(emptyList())
        private set

    var postProcessing by mutableStateOf(PostProcessingSettings())
        private set

    private var editStateSnapshot: List<GameObject> = emptyList()

    fun addObject(type: String) {
        val newId = (objects.maxOfOrNull { it.id } ?: 0) + 1
        val newObj = GameObject(
            id = newId,
            name = "$type $newId",
            type = type,
            components = listOf(EngineComponent.MeshRenderer())
        )
        objects = objects + newObj
        selectedObjectId = newId
    }

    fun selectObject(id: Int?) {
        selectedObjectId = id
    }

    fun addComponentToSelected(component: EngineComponent) {
        selectedObjectId?.let { id ->
            objects = objects.map { obj ->
                if (obj.id == id) obj.copy(components = obj.components + component) else obj
            }
        }
    }

    fun saveSelectedAsPrefab() {
        val selected = objects.find { it.id == selectedObjectId } ?: return
        val newPrefab = Prefab(
            prefabId = "prefab_${selected.id}_${System.currentTimeMillis()}",
            name = "Prefab_${selected.name}",
            templateObject = selected.copy(isPrefab = true)
        )
        prefabs = prefabs + newPrefab
    }

    fun togglePlayMode() {
        when (playState) {
            EnginePlayState.EDIT -> {
                editStateSnapshot = objects
                playState = EnginePlayState.PLAY
            }
            EnginePlayState.PLAY -> {
                playState = EnginePlayState.PAUSE
            }
            EnginePlayState.PAUSE -> {
                objects = editStateSnapshot
                playState = EnginePlayState.EDIT
            }
        }
    }
}
