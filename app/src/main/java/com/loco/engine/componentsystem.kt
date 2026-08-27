package com.loco.engine

import org.json.JSONArray
import org.json.JSONObject

enum class ComponentType {
    TRANSFORM,
    MESH_RENDERER,
    LIGHT,
    CAMERA,
    COLLIDER
}

data class ComponentData(
    val type: ComponentType,
    val enabled: Boolean = true,
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val positionZ: Float = 0f,
    val rotationX: Float = 0f,
    val rotationY: Float = 0f,
    val rotationZ: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val scaleZ: Float = 1f,
    val mesh: String = "Cube",
    val material: String = "Default",
    val intensity: Float = 1f,
    val range: Float = 10f,
    val colorR: Float = 1f,
    val colorG: Float = 1f,
    val colorB: Float = 1f,
    val fieldOfView: Float = 60f,
    val nearClip: Float = 0.1f,
    val farClip: Float = 100f,
    val colliderShape: String = "Box"
)

data class ComponentSet(
    val components: MutableList<ComponentData> = mutableListOf()
) {
    fun has(type: ComponentType) = components.any { it.type == type }
    fun add(type: ComponentType) { if (!has(type)) components.add(defaultComponent(type)) }
    fun remove(type: ComponentType) { components.removeAll { it.type == type } }
    fun get(type: ComponentType): ComponentData? = components.firstOrNull { it.type == type }
    fun update(type: ComponentType, update: (ComponentData) -> ComponentData) {
        val index = components.indexOfFirst { it.type == type }
        if (index >= 0) components[index] = update(components[index])
    }
}

fun defaultComponent(type: ComponentType): ComponentData = when (type) {
    ComponentType.TRANSFORM -> ComponentData(type = type)
    ComponentType.MESH_RENDERER -> ComponentData(type = type, mesh = "Cube", material = "Default")
    ComponentType.LIGHT -> ComponentData(type = type, intensity = 1f, range = 10f)
    ComponentType.CAMERA -> ComponentData(type = type, fieldOfView = 60f, nearClip = 0.1f, farClip = 100f)
    ComponentType.COLLIDER -> ComponentData(type = type, colliderShape = "Box")
}

fun componentDisplayName(type: ComponentType, language: String): String = when (type) {
    ComponentType.TRANSFORM -> text(language, "Transform", "التحويل")
    ComponentType.MESH_RENDERER -> text(language, "Mesh Renderer", "عارض المجسم")
    ComponentType.LIGHT -> text(language, "Light", "ضوء")
    ComponentType.CAMERA -> text(language, "Camera", "كاميرا")
    ComponentType.COLLIDER -> text(language, "Collider", "مصادم")
}

fun componentsToJson(componentSet: ComponentSet): JSONArray {
    val array = JSONArray()
    componentSet.components.forEach { c ->
        array.put(JSONObject().apply {
            put("type", c.type.name); put("enabled", c.enabled)
            put("positionX", c.positionX); put("positionY", c.positionY); put("positionZ", c.positionZ)
            put("rotationX", c.rotationX); put("rotationY", c.rotationY); put("rotationZ", c.rotationZ)
            put("scaleX", c.scaleX); put("scaleY", c.scaleY); put("scaleZ", c.scaleZ)
            put("mesh", c.mesh); put("material", c.material)
            put("intensity", c.intensity); put("range", c.range)
            put("colorR", c.colorR); put("colorG", c.colorG); put("colorB", c.colorB)
            put("fieldOfView", c.fieldOfView); put("nearClip", c.nearClip); put("farClip", c.farClip)
            put("colliderShape", c.colliderShape)
        })
    }
    return array
}

fun componentsFromJson(array: JSONArray?): ComponentSet {
    val result = ComponentSet()
    if (array == null) return result
    for (i in 0 until array.length()) {
        try {
            val item = array.getJSONObject(i)
            val type = ComponentType.valueOf(item.getString("type"))
            result.components.add(ComponentData(
                type = type,
                enabled = item.optBoolean("enabled", true),
                positionX = item.optDouble("positionX", 0.0).toFloat(),
                positionY = item.optDouble("positionY", 0.0).toFloat(),
                positionZ = item.optDouble("positionZ", 0.0).toFloat(),
                rotationX = item.optDouble("rotationX", 0.0).toFloat(),
                rotationY = item.optDouble("rotationY", 0.0).toFloat(),
                rotationZ = item.optDouble("rotationZ", 0.0).toFloat(),
                scaleX = item.optDouble("scaleX", 1.0).toFloat(),
                scaleY = item.optDouble("scaleY", 1.0).toFloat(),
                scaleZ = item.optDouble("scaleZ", 1.0).toFloat(),
                mesh = item.optString("mesh", "Cube"),
                material = item.optString("material", "Default"),
                intensity = item.optDouble("intensity", 1.0).toFloat(),
                range = item.optDouble("range", 10.0).toFloat(),
                colorR = item.optDouble("colorR", 1.0).toFloat(),
                colorG = item.optDouble("colorG", 1.0).toFloat(),
                colorB = item.optDouble("colorB", 1.0).toFloat(),
                fieldOfView = item.optDouble("fieldOfView", 60.0).toFloat(),
                nearClip = item.optDouble("nearClip", 0.1).toFloat(),
                farClip = item.optDouble("farClip", 100.0).toFloat(),
                colliderShape = item.optString("colliderShape", "Box")
            ))
        } catch (_: Exception) { }
    }
    return result
}

fun defaultComponentsForObject(objectType: String): ComponentSet {
    val result = ComponentSet()
    result.add(ComponentType.TRANSFORM)
    when (objectType) {
        "Cube", "Sphere", "Plane", "Cylinder", "Cone", "Capsule" -> result.add(ComponentType.MESH_RENDERER)
        "Camera" -> result.add(ComponentType.CAMERA)
        "Light" -> result.add(ComponentType.LIGHT)
    }
    return result
}

/* =========================================================
   UNREAL-INSPIRED 50 FEATURE SYSTEM
   These are engine-side Loco Engine systems inspired by Unreal concepts.
   They are intentionally lightweight so they work on Android/OpenGL ES.
   ========================================================= */

enum class UnrealFeatureType(
    val english: String,
    val arabic: String,
    val category: String
) {
    WORLD_PARTITION("World Partition", "تقسيم العالم", "World"),
    LEVEL_STREAMING("Level Streaming", "بث المستويات", "World"),
    DATA_LAYERS("Data Layers", "طبقات البيانات", "World"),
    LANDSCAPE_TERRAIN("Landscape Terrain", "تضاريس Landscape", "World"),
    FOLIAGE("Foliage", "النباتات", "World"),
    PROCEDURAL_FOLIAGE("Procedural Foliage", "نباتات إجرائية", "World"),
    NANITE_LIKE_GEOMETRY("Virtualized Geometry", "هندسة افتراضية", "Rendering"),
    LUMEN_LIKE_GI("Dynamic Global Illumination", "إضاءة عالمية ديناميكية", "Rendering"),
    VIRTUAL_SHADOWS("Virtual Shadow Maps", "خرائط ظلال افتراضية", "Rendering"),
    SKY_ATMOSPHERE("Sky Atmosphere", "غلاف جوي للسماء", "Rendering"),
    HEIGHT_FOG("Height Fog", "ضباب الارتفاع", "Rendering"),
    VOLUMETRIC_CLOUDS("Volumetric Clouds", "غيوم حجمية", "Rendering"),
    POST_PROCESS("Post Process", "معالجة لاحقة", "Rendering"),
    COLOR_GRADING("Color Grading", "تدرج الألوان", "Rendering"),
    BLOOM("Bloom", "توهج Bloom", "Rendering"),
    DEPTH_OF_FIELD("Depth of Field", "عمق المجال", "Rendering"),
    MOTION_BLUR("Motion Blur", "ضباب الحركة", "Rendering"),
    SCREEN_SPACE_REFLECTIONS("Screen Space Reflections", "انعكاسات مساحة الشاشة", "Rendering"),
    DECALS("Decals", "ملصقات سطحية", "Rendering"),
    REFLECTION_CAPTURE("Reflection Capture", "التقاط الانعكاس", "Rendering"),
    NIAGARA_VFX("Niagara-style VFX", "مؤثرات VFX شبيهة بناياجرا", "VFX"),
    PARTICLE_COLLISION("Particle Collision", "تصادم الجسيمات", "VFX"),
    GPU_VFX_EMITTERS("GPU VFX Emitters", "مولدات VFX", "VFX"),
    PROCEDURAL_AUDIO("Procedural Audio", "صوت إجرائي", "Audio"),
    SOUND_ATTENUATION("Sound Attenuation", "توهين الصوت", "Audio"),
    REVERB_ZONES("Reverb Zones", "مناطق الصدى", "Audio"),
    AUDIO_SUBMIX("Audio Submix", "مزج الصوت", "Audio"),
    CONTROL_RIG("Control Rig", "هيكل التحكم", "Animation"),
    SEQUENCER("Sequencer Timeline", "خط زمني Sequencer", "Animation"),
    ANIMATION_MONTAGES("Animation Montages", "مقاطع الأنيميشن", "Animation"),
    BLEND_SPACES("Blend Spaces", "مساحات المزج", "Animation"),
    IK_RETARGETING("IK Retargeting", "إعادة توجيه IK", "Animation"),
    ANIMATION_STATE_MACHINE("Animation State Machine", "آلة حالات الأنيميشن", "Animation"),
    BEHAVIOR_TREE("Behavior Tree", "شجرة السلوك", "AI"),
    STATE_TREE("StateTree", "شجرة الحالات", "AI"),
    AI_PERCEPTION("AI Perception", "إدراك الذكاء الاصطناعي", "AI"),
    NAVIGATION_MESH("Navigation Mesh", "شبكة الملاحة", "AI"),
    ENVIRONMENT_QUERY("Environment Query System", "نظام استعلام البيئة", "AI"),
    SMART_OBJECTS("Smart Objects", "العناصر الذكية", "Gameplay"),
    GAMEPLAY_TAGS("Gameplay Tags", "وسوم اللعب", "Gameplay"),
    GAMEPLAY_EFFECTS("Gameplay Effects", "تأثيرات اللعب", "Gameplay"),
    ENHANCED_INPUT("Enhanced Input", "الإدخال المحسن", "Input"),
    BLUEPRINT_LOGIC("Blueprint-style Logic", "منطق بصري شبيه Blueprint", "Logic"),
    SAVE_GAME("Save Game System", "نظام حفظ اللعبة", "Runtime"),
    HLOD("Hierarchical LOD", "تفاصيل هرمية HLOD", "Optimization"),
    DEBUG_DRAW("Debug Draw", "الرسم التشخيصي", "Debug"),
    ENGINE_PROFILER("Engine Profiler", "محلل أداء المحرك", "Debug"),
    AUTOMATION_TESTS("Automation Tests", "اختبارات تلقائية", "Debug"),
    REPLICATION("Multiplayer Replication", "مزامنة اللعب الجماعي", "Networking"),
    DEDICATED_SERVER("Dedicated Server Mode", "وضع الخادم المخصص", "Networking")
}

data class UnrealProjectSettings(
    val enabled: Set<UnrealFeatureType> = UnrealFeatureType.values().toSet(),
    val fogDensity: Float = 0.025f,
    val fogHeight: Float = 0f,
    val bloomStrength: Float = 0.12f,
    val colorGrading: Float = 1f,
    val worldStreamingRadius: Float = 1000f,
    val navigationCellSize: Float = 50f,
    val masterAudioVolume: Float = 1f,
    val profilerEnabled: Boolean = true,
    val debugDrawEnabled: Boolean = false
) {
    fun isEnabled(feature: UnrealFeatureType): Boolean = feature in enabled
    fun toggle(feature: UnrealFeatureType): UnrealProjectSettings = copy(
        enabled = if (feature in enabled) enabled - feature else enabled + feature
    )
}

fun unrealFeatureDisplayName(feature: UnrealFeatureType, language: String): String =
    text(language, feature.english, feature.arabic)

fun unrealSettingsToJson(settings: UnrealProjectSettings): JSONObject = JSONObject().apply {
    put("enabled", JSONArray().apply { settings.enabled.forEach { put(it.name) } })
    put("fogDensity", settings.fogDensity); put("fogHeight", settings.fogHeight)
    put("bloomStrength", settings.bloomStrength); put("colorGrading", settings.colorGrading)
    put("worldStreamingRadius", settings.worldStreamingRadius)
    put("navigationCellSize", settings.navigationCellSize)
    put("masterAudioVolume", settings.masterAudioVolume)
    put("profilerEnabled", settings.profilerEnabled); put("debugDrawEnabled", settings.debugDrawEnabled)
}

fun unrealSettingsFromJson(obj: JSONObject?): UnrealProjectSettings {
    if (obj == null) return UnrealProjectSettings()
    val enabled = mutableSetOf<UnrealFeatureType>()
    val array = obj.optJSONArray("enabled")
    if (array == null) enabled.addAll(UnrealFeatureType.values()) else {
        for (i in 0 until array.length()) {
            try { enabled.add(UnrealFeatureType.valueOf(array.getString(i))) } catch (_: Exception) { }
        }
    }
    return UnrealProjectSettings(
        enabled = enabled,
        fogDensity = obj.optDouble("fogDensity", 0.025).toFloat(),
        fogHeight = obj.optDouble("fogHeight", 0.0).toFloat(),
        bloomStrength = obj.optDouble("bloomStrength", 0.12).toFloat(),
        colorGrading = obj.optDouble("colorGrading", 1.0).toFloat(),
        worldStreamingRadius = obj.optDouble("worldStreamingRadius", 1000.0).toFloat(),
        navigationCellSize = obj.optDouble("navigationCellSize", 50.0).toFloat(),
        masterAudioVolume = obj.optDouble("masterAudioVolume", 1.0).toFloat(),
        profilerEnabled = obj.optBoolean("profilerEnabled", true),
        debugDrawEnabled = obj.optBoolean("debugDrawEnabled", false)
    )
}
