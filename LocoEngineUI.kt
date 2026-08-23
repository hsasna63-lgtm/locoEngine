package com.loco.engine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LocoEngineMainEditor(
    engineManager: LocoEngineManager = remember { LocoEngineManager() }
) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF111318))) {
        
        // شريط الأدوات العلوي (Play / Pause / Add Objects)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF1E222D))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { engineManager.togglePlayMode() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (engineManager.playState == EnginePlayState.PLAY) Color.Red else Color(0xFF238636)
                )
            ) {
                Text(if (engineManager.playState == EnginePlayState.PLAY) "Pause ⏸" else "Play ▶")
            }

            Row {
                Button(onClick = { engineManager.addObject("Cube") }) { Text("+ Cube") }
                Spacer(modifier = Modifier.width(4.dp))
                Button(onClick = { engineManager.addObject("Sphere") }) { Text("+ Sphere") }
            }
        }

        // منطقة العرض ولوحة الخصائص (Inspector)
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            
            Text(
                text = "عدد العناصر في المشهد: ${engineManager.objects.size}",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )

            // لوحة Inspector عند تحديد عنصر
            val selectedObj = engineManager.objects.find { it.id == engineManager.selectedObjectId }
            if (selectedObj != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .width(210.dp)
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xDD1E222D))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(selectedObj.name, color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text("Tag: ${selectedObj.tag}", color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text("المكونات (${selectedObj.components.size}):", color = Color.LightGray)
                        selectedObj.components.forEach { comp ->
                            Text("- ${comp::class.simpleName}", color = Color(0xFF58A6FF))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { engineManager.addComponentToSelected(EngineComponent.RigidBody()) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("+ إضافة فيزياء")
                        }
                        Button(
                            onClick = { engineManager.saveSelectedAsPrefab() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("حفظ كـ Prefab")
                        }
                    }
                }
            }
        }
    }
}
