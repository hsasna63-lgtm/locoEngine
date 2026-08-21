package com.loco.engine

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedProjects = loadProjects(this)

        setContent {
            LocoEngineApp(savedProjects)
        }
    }
}

data class Project(
    val name: String,
    val type: String
)

// 🧱 تطوير نموذج GameObject ليدعم الخصائص ثلاثية الأبعاد
data class GameObject(
    val id: Int,
    var name: String,
    val type: String,
    var posX: Float = 0f,
    var posY: Float = 0f,
    var posZ: Float = 0f,
    var scale: Float = 1.0f
)

fun loadProjects(context: Context): List<Project> {
    val preferences = context.getSharedPreferences(
        "loco_engine",
        Context.MODE_PRIVATE
    )

    val data = preferences.getString(
        "projects",
        null
    ) ?: return emptyList()

    return try {
        val array = JSONArray(data)
        val result = mutableListOf<Project>()

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            result.add(
                Project(
                    name = item.getString("name"),
                    type = item.getString("type")
                )
            )
        }
        result
    } catch (e: Exception) {
        emptyList()
    }
}

fun saveProjects(
    context: Context,
    projects: List<Project>
) {
    val array = JSONArray()

    for (project in projects) {
        val item = JSONObject()
        item.put("name", project.name)
        item.put("type", project.type)
        array.put(item)
    }

    context
        .getSharedPreferences(
            "loco_engine",
            Context.MODE_PRIVATE
        )
        .edit()
        .putString(
            "projects",
            array.toString()
        )
        .apply()
}

@Composable
fun LocoEngineApp(
    savedProjects: List<Project>
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val projects = remember {
        mutableStateListOf<Project>().apply {
            addAll(savedProjects)
        }
    }

    var openedProject by remember {
        mutableStateOf<Project?>(null)
    }

    // 🌐 حالة التحكم باللغة (العربية / الإنجليزية)
    var isArabic by remember { mutableStateOf(true) }

    MaterialTheme {
        if (openedProject != null) {
            EditorScreen(
                project = openedProject!!,
                isArabic = isArabic,
                onToggleLanguage = { isArabic = !isArabic },
                onBack = {
                    openedProject = null
                }
            )
        } else {
            HomeScreen(
                projects = projects,
                isArabic = isArabic,
                onToggleLanguage = { isArabic = !isArabic },
                onCreateProject = { name, type ->
                    val project = Project(
                        name = name,
                        type = type
                    )
                    projects.add(project)
                    saveProjects(
                        context,
                        projects.toList()
                    )
                },
                onOpenProject = { project ->
                    openedProject = project
                },
                onDeleteProject = { project ->
                    projects.remove(project)
                    saveProjects(
                        context,
                        projects.toList()
                    )
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    projects: List<Project>,
    isArabic: Boolean,
    onToggleLanguage: () -> Unit,
    onCreateProject: (String, String) -> Unit,
    onOpenProject: (Project) -> Unit,
    onDeleteProject: (Project) -> Unit
) {
    var showCreateDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🌐 زر تغيير اللغة
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = onToggleLanguage) {
                Text(if (isArabic) "English 🌐" else "العربية 🌐", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "LOCO ENGINE",
            color = Color(0xFF00E5FF),
            fontSize = 32.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isArabic) "محرك ألعاب الهواتف" else "Mobile Game Engine",
            color = Color.White,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                showCreateDialog = true
            }
        ) {
            Text(if (isArabic) "+ إنشاء مشروع" else "CREATE PROJECT")
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = if (isArabic) "المشاريع" else "PROJECTS",
            color = Color(0xFF00E5FF),
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (projects.isEmpty()) {
            Text(
                text = if (isArabic) "لا توجد مشاريع حتى الآن" else "No projects yet",
                color = Color.Gray
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(projects) { project ->
                    ProjectCard(
                        project = project,
                        isArabic = isArabic,
                        onOpen = {
                            onOpenProject(project)
                        },
                        onDelete = {
                            onDeleteProject(project)
                        }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateProjectDialog(
            isArabic = isArabic,
            onCancel = {
                showCreateDialog = false
            },
            onCreate = { name, type ->
                onCreateProject(
                    name,
                    type
                )
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun ProjectCard(
    project: Project,
    isArabic: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B))
            .padding(14.dp)
    ) {
        Text(
            text = project.name,
            color = Color.White,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "${if (isArabic) "النوع" else "Type"}: ${project.type}",
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onOpen
            ) {
                Text(if (isArabic) "فتح" else "OPEN")
            }

            OutlinedButton(
                onClick = {
                    showDeleteDialog = true
                }
            ) {
                Text(if (isArabic) "حذف" else "DELETE")
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text(if (isArabic) "حذف المشروع" else "Delete Project")
            },
            text = {
                Text(
                    if (isArabic) "هل أنت تأكد من حذف ${project.name}؟" else "Delete ${project.name}?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(if (isArabic) "حذف" else "DELETE")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text(if (isArabic) "إلغاء" else "CANCEL")
                }
            }
        )
    }
}

@Composable
fun CreateProjectDialog(
    isArabic: Boolean,
    onCancel: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var projectName by remember {
        mutableStateOf("")
    }

    var projectType by remember {
        mutableStateOf("3D")
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(if (isArabic) "إنشاء مشروع جديد" else "Create Project")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = {
                        projectName = it
                    },
                    label = {
                        Text(if (isArabic) "اسم المشروع" else "Project Name")
                    }
                )

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    text = if (isArabic) "نوع المشروع" else "Project Type"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            projectType = "2D"
                        }
                    ) {
                        Text("2D")
                    }

                    Button(
                        onClick = {
                            projectType = "3D"
                        }
                    ) {
                        Text("3D")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${if (isArabic) "المحدد" else "Selected"}: $projectType"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (projectName.isNotBlank()) {
                        onCreate(
                            projectName.trim(),
                            projectType
                        )
                    }
                }
            ) {
                Text(if (isArabic) "إنشاء" else "CREATE")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel
            ) {
                Text(if (isArabic) "إلغاء" else "CANCEL")
            }
        }
    )
}

@Composable
fun EditorScreen(
    project: Project,
    isArabic: Boolean,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit
) {
    val objects = remember {
        mutableStateListOf(
            GameObject(
                id = 1,
                name = "Main Camera",
                type = "Camera"
            ),
            GameObject(
                id = 2,
                name = "Directional Light",
                type = "Light"
            )
        )
    }

    var selectedObjectId by remember {
        mutableStateOf<Int?>(1)
    }

    var selectedTool by remember {
        mutableStateOf("SELECT")
    }

    var showAddObjectDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827))
    ) {
        EditorTopBar(
            project = project,
            isArabic = isArabic,
            onToggleLanguage = onToggleLanguage,
            onBack = onBack
        )

        EditorToolBar(
            selectedTool = selectedTool,
            onToolSelected = {
                selectedTool = it
            }
        )

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            SceneTree(
                modifier = Modifier.weight(0.25f),
                objects = objects,
                selectedObjectId = selectedObjectId,
                isArabic = isArabic,
                onSelect = {
                    selectedObjectId = it
                },
                onAddObjectClick = {
                    showAddObjectDialog = true
                }
            )

            EditorViewport(
                modifier = Modifier.weight(0.50f),
                project = project,
                selectedTool = selectedTool
            )

            InspectorPanel(
                modifier = Modifier.weight(0.25f),
                objects = objects,
                selectedObjectId = selectedObjectId,
                isArabic = isArabic,
                onUpdateTransform = { id, posX, posY, posZ, scale ->
                    val index = objects.indexOfFirst { it.id == id }
                    if (index != -1) {
                        val item = objects[index]
                        objects[index] = item.copy(
                            posX = posX ?: item.posX,
                            posY = posY ?: item.posY,
                            posZ = posZ ?: item.posZ,
                            scale = scale ?: item.scale
                        )
                    }
                }
            )
        }
    }

    // 🧱 نافذة إضافة الكائنات الجديدة
    if (showAddObjectDialog) {
        AddObjectDialog(
            isArabic = isArabic,
            onDismiss = { showAddObjectDialog = false },
            onAdd = { type ->
                val newId = (objects.maxOfOrNull { it.id } ?: 0) + 1
                objects.add(
                    GameObject(
                        id = newId,
                        name = "$type $newId",
                        type = type
                    )
                )
                selectedObjectId = newId
                showAddObjectDialog = false
            }
        )
    }
}

@Composable
fun EditorTopBar(
    project: Project,
    isArabic: Boolean,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onBack
        ) {
            Text("← ${if (isArabic) "رجوع" else "BACK"}")
        }

        Text(
            text = project.name,
            color = Color.White,
            fontSize = 18.sp
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onToggleLanguage) {
                Text(if (isArabic) "EN" else "عربي", color = Color.White, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = project.type,
                color = Color(0xFF00E5FF)
            )
        }
    }
}

@Composable
fun EditorToolBar(
    selectedTool: String,
    onToolSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        val tools = listOf(
            "SELECT",
            "MOVE",
            "ROTATE",
            "SCALE"
        )

        tools.forEach { tool ->
            if (tool == selectedTool) {
                Button(
                    onClick = {
                        onToolSelected(tool)
                    }
                ) {
                    Text(tool, fontSize = 11.sp)
                }
            } else {
                OutlinedButton(
                    onClick = {
                        onToolSelected(tool)
                    }
                ) {
                    Text(tool, fontSize = 11.sp)
                }
            }
        }
    }
}

// 🧱 قائمة حقيقية لإضافة Objects
@Composable
fun AddObjectDialog(
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isArabic) "إضافة عنصر للمشهد" else "Add Object") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onAdd("Cube") }) {
                    Text("🧊 Cube")
                }
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onAdd("Sphere") }) {
                    Text("🔮 Sphere")
                }
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onAdd("Camera") }) {
                    Text("📷 Camera")
                }
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onAdd("Light") }) {
                    Text("💡 Light")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "CANCEL")
            }
        }
    )
}

@Composable
fun SceneTree(
    modifier: Modifier,
    objects: List<GameObject>,
    selectedObjectId: Int?,
    isArabic: Boolean,
    onSelect: (Int) -> Unit,
    onAddObjectClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(Color(0xFF0B1220))
            .padding(8.dp)
    ) {
        Text(
            text = if (isArabic) "المشهد" else "SCENE",
            color = Color(0xFF00E5FF),
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onAddObjectClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(4.dp)
        ) {
            Text(if (isArabic) "+ عنصر" else "+ OBJECT", fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(
                items = objects,
                key = { it.id }
            ) { obj ->
                val selected = obj.id == selectedObjectId

                val icon = when (obj.type) {
                    "Cube" -> "🧊 "
                    "Sphere" -> "🔮 "
                    "Camera" -> "📷 "
                    "Light" -> "💡 "
                    else -> "📦 "
                }

                Text(
                    text = icon + obj.name,
                    color = if (selected) Color(0xFF00E5FF) else Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(obj.id) }
                        .padding(8.dp)
                )
            }
        }
    }
}

// 🎮 تحسين Viewport ليظهر بمساحة عمل حقيقية 
