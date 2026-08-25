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

/* =========================
   COMPONENT DATA
   ========================= */

data class ComponentData(
    val type: ComponentType,
    val enabled: Boolean = true,

    /* Transform */
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val positionZ: Float = 0f,

    val rotationX: Float = 0f,
    val rotationY: Float = 0f,
    val rotationZ: Float = 0f,

    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val scaleZ: Float = 1f,

    /* Mesh Renderer */
    val mesh: String = "Cube",
    val material: String = "Default",

    /* Light */
    val intensity: Float = 1f,
    val range: Float = 10f,
    val colorR: Float = 1f,
    val colorG: Float = 1f,
    val colorB: Float = 1f,

    /* Camera */
    val fieldOfView: Float = 60f,
    val nearClip: Float = 0.1f,
    val farClip: Float = 100f,

    /* Collider */
    val colliderShape: String = "Box"
)


/* =========================
   COMPONENT SET
   ========================= */

data class ComponentSet(
    val components: MutableList<ComponentData> =
        mutableListOf()
) {

    fun has(type: ComponentType): Boolean {

        return components.any {
            it.type == type
        }
    }


    fun add(type: ComponentType) {

        if (!has(type)) {

            components.add(
                defaultComponent(type)
            )
        }
    }


    fun remove(type: ComponentType) {

        components.removeAll {
            it.type == type
        }
    }


    fun get(type: ComponentType): ComponentData? {

        return components.firstOrNull {
            it.type == type
        }
    }


    fun update(
        type: ComponentType,
        update: (ComponentData) -> ComponentData
    ) {

        val index =
            components.indexOfFirst {
                it.type == type
            }

        if (index >= 0) {

            components[index] =
                update(
                    components[index]
                )
        }
    }
}


/* =========================
   DEFAULT COMPONENT
   ========================= */

fun defaultComponent(
    type: ComponentType
): ComponentData {

    return when (type) {

        ComponentType.TRANSFORM -> {

            ComponentData(
                type = type,

                positionX = 0f,
                positionY = 0f,
                positionZ = 0f,

                rotationX = 0f,
                rotationY = 0f,
                rotationZ = 0f,

                scaleX = 1f,
                scaleY = 1f,
                scaleZ = 1f
            )
        }


        ComponentType.MESH_RENDERER -> {

            ComponentData(
                type = type,

                mesh = "Cube",
                material = "Default"
            )
        }


        ComponentType.LIGHT -> {

            ComponentData(
                type = type,

                intensity = 1f,
                range = 10f,

                colorR = 1f,
                colorG = 1f,
                colorB = 1f
            )
        }


        ComponentType.CAMERA -> {

            ComponentData(
                type = type,

                fieldOfView = 60f,
                nearClip = 0.1f,
                farClip = 100f
            )
        }


        ComponentType.COLLIDER -> {

            ComponentData(
                type = type,

                colliderShape = "Box"
            )
        }
    }
}


/* =========================
   DISPLAY NAME
   ========================= */

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


/* =========================
   SAVE COMPONENTS
   ========================= */

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


        /* Transform */

        item.put(
            "positionX",
            component.positionX
        )

        item.put(
            "positionY",
            component.positionY
        )

        item.put(
            "positionZ",
            component.positionZ
        )


        item.put(
            "rotationX",
            component.rotationX
        )

        item.put(
            "rotationY",
            component.rotationY
        )

        item.put(
            "rotationZ",
            component.rotationZ
        )


        item.put(
            "scaleX",
            component.scaleX
        )

        item.put(
            "scaleY",
            component.scaleY
        )

        item.put(
            "scaleZ",
            component.scaleZ
        )


        /* Mesh Renderer */

        item.put(
            "mesh",
            component.mesh
        )

        item.put(
            "material",
            component.material
        )


        /* Light */

        item.put(
            "intensity",
            component.intensity
        )

        item.put(
            "range",
            component.range
        )

        item.put(
            "colorR",
            component.colorR
        )

        item.put(
            "colorG",
            component.colorG
        )

        item.put(
            "colorB",
            component.colorB
        )


        /* Camera */

        item.put(
            "fieldOfView",
            component.fieldOfView
        )

        item.put(
            "nearClip",
            component.nearClip
        )

        item.put(
            "farClip",
            component.farClip
        )


        /* Collider */

        item.put(
            "colliderShape",
            component.colliderShape
        )


        array.put(item)
    }


    return array
}


/* =========================
   LOAD COMPONENTS
   ========================= */

fun componentsFromJson(
    array: JSONArray?
): ComponentSet {

    val result =
        ComponentSet()


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


            val component =
                ComponentData(

                    type = type,

                    enabled = enabled,


                    /* Transform */

                    positionX =
                        item.optDouble(
                            "positionX",
                            0.0
                        ).toFloat(),

                    positionY =
                        item.optDouble(
                            "positionY",
                            0.0
                        ).toFloat(),

                    positionZ =
                        item.optDouble(
                            "positionZ",
                            0.0
                        ).toFloat(),


                    rotationX =
                        item.optDouble(
                            "rotationX",
                            0.0
                        ).toFloat(),

                    rotationY =
                        item.optDouble(
                            "rotationY",
                            0.0
                        ).toFloat(),

                    rotationZ =
                        item.optDouble(
                            "rotationZ",
                            0.0
                        ).toFloat(),


                    scaleX =
                        item.optDouble(
                            "scaleX",
                            1.0
                        ).toFloat(),

                    scaleY =
                        item.optDouble(
                            "scaleY",
                            1.0
                        ).toFloat(),

                    scaleZ =
                        item.optDouble(
                            "scaleZ",
                            1.0
                        ).toFloat(),


                    /* Mesh */

                    mesh =
                        item.optString(
                            "mesh",
                            "Cube"
                        ),

                    material =
                        item.optString(
                            "material",
                            "Default"
                        ),


                    /* Light */

                    intensity =
                        item.optDouble(
                            "intensity",
                            1.0
                        ).toFloat(),

                    range =
                        item.optDouble(
                            "range",
                            10.0
                        ).toFloat(),

                    colorR =
                        item.optDouble(
                            "colorR",
                            1.0
                        ).toFloat(),

                    colorG =
                        item.optDouble(
                            "colorG",
                            1.0
                        ).toFloat(),

                    colorB =
                        item.optDouble(
                            "colorB",
                            1.0
                        ).toFloat(),


                    /* Camera */

                    fieldOfView =
                        item.optDouble(
                            "fieldOfView",
                            60.0
                        ).toFloat(),

                    nearClip =
                        item.optDouble(
                            "nearClip",
                            0.1
                        ).toFloat(),

                    farClip =
                        item.optDouble(
                            "farClip",
                            100.0
                        ).toFloat(),


                    /* Collider */

                    colliderShape =
                        item.optString(
                            "colliderShape",
                            "Box"
                        )
                )


            result.components.add(
                component
            )

        } catch (_: Exception) {

            // Ignore invalid component
        }
    }


    return result
}


/* =========================
   DEFAULT OBJECT COMPONENTS
   ========================= */

fun defaultComponentsForObject(
    objectType: String
): ComponentSet {

    val result =
        ComponentSet()


    /* Every GameObject has Transform */

    result.add(
        ComponentType.TRANSFORM
    )


    when (objectType) {

        "Cube",
        "Sphere",
        "Plane",
        "Cylinder",
        "Cone",
        "Capsule" -> {

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
