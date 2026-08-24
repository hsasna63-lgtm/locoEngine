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
    val enabled: Boolean = true
)

data class ComponentSet(
    val components: MutableList<ComponentData> = mutableListOf()
) {

    fun has(type: ComponentType): Boolean {
        return components.any {
            it.type == type
        }
    }

    fun add(type: ComponentType) {
        if (!has(type)) {
            components.add(
                ComponentData(type)
            )
        }
    }

    fun remove(type: ComponentType) {
        components.removeAll {
            it.type == type
        }
    }
}

fun componentDisplayName(
    type: ComponentType,
    language: String
): String {

    return when (type) {

        ComponentType.TRANSFORM ->
            text(
                language,
                "Transform",
                "التحويل"
            )

        ComponentType.MESH_RENDERER ->
            text(
                language,
                "Mesh Renderer",
                "عارض المجسم"
            )

        ComponentType.LIGHT ->
            text(
                language,
                "Light",
                "ضوء"
            )

        ComponentType.CAMERA ->
            text(
                language,
                "Camera",
                "كاميرا"
            )

        ComponentType.COLLIDER ->
            text(
                language,
                "Collider",
                "مصادم"
            )
    }
}

fun componentsToJson(
    componentSet: ComponentSet
): JSONArray {

    val array = JSONArray()

    componentSet.components.forEach { component ->

        val item = JSONObject()

        item.put(
            "type",
            component.type.name
        )

        item.put(
            "enabled",
            component.enabled
        )

        array.put(item)
    }

    return array
}

fun componentsFromJson(
    array: JSONArray?
): ComponentSet {

    val result = ComponentSet()

    if (array == null) {
        return result
    }

    for (i in 0 until array.length()) {

        try {

            val item =
                array.getJSONObject(i)

            val type =
                ComponentType.valueOf(
                    item.getString("type")
                )

            val enabled =
                item.optBoolean(
                    "enabled",
                    true
                )

            result.components.add(
                ComponentData(
                    type = type,
                    enabled = enabled
                )
            )

        } catch (_: Exception) {
            // Ignore invalid component
        }
    }

    return result
}

fun defaultComponentsForObject(
    objectType: String
): ComponentSet {

    val result = ComponentSet()

    result.add(
        ComponentType.TRANSFORM
    )

    when (objectType) {

        "Cube",
        "Sphere" -> {

            result.add(
                ComponentType.MESH_RENDERER
            )
        }

        "Camera" -> {

            result.add(
                ComponentType.CAMERA
            )
        }

        "Light" -> {

            result.add(
                ComponentType.LIGHT
            )
        }
    }

    return result
}
