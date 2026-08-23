package com.loco.engine

// 1. نظام المكونات المستوحى من Unity
sealed class EngineComponent {
    data class RigidBody(val mass: Float = 1.0f) : EngineComponent()
    data class BoxCollider(val sizeX: Float = 1f) : EngineComponent()
}

// 2. كائن اللعبة الرئيسي للـ locoEngine
data class GameObject(
    val id: Int,
    val name: String,
    val type: String,
    val x: Float = 0f,
    val y: Float = 0f,
    val scale: Float = 1f,
    val components: List<EngineComponent> = emptyList()
)
