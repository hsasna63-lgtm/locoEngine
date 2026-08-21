package com.loco.engine

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

data class GameObject(
    val id: Int,
    val name: String,
    val type: String
)

fun loadProjects(context: Context): List<Project> {

    val preferences = context.getSharedPreferences(
        "loco_engine",
        Context.MODE_PRIVATE
    )

    val data = preferences.getString("projects", null)
        ?: return emptyList()

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
        .putString("projects", array.toString())
        .apply()
}

@Composable
fun LocoEngineApp(
    savedProjects: List<Project>
) {

    val context =
        androidx.compose.ui.platform.LocalContext.current

    val projects = remember {

        mutableStateListOf<Project>().apply {
            addAll(savedProjects)
        }
    }

    var openedProject by remember {
        mutableStateOf<Project?>(null)
    }

    var arabic by remember {
        mutableStateOf(false)
    }

    MaterialTheme {

        if (openedProject != null) {

            EditorScreen(
                project = openedProject!!,
                arabic = arabic,
                onLanguageChange = {
                    arabic = !arabic
                },
                onBack = {
                    openedProject = null
                }
            )

        } else {

            HomeScreen(
                projects = projects,
                arabic = arabic,

                onLanguageChange = {
                    arabic = !arabic
                },

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
    arabic: Boolean,
    onLanguageChange: () -> Unit,
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

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {

            OutlinedButton(
                onClick = onLanguageChange
            ) {

                Text(
                    if (arabic) {
                        "English"
                    } else {
                        "العربية"
                    }
                )
            }
        }

        Spacer(
            modifier = Modifier.height(15.dp)
        )

        Text(
            text = "LOCO ENGINE",
            color = Color(0xFF00E5FF),
            fontSize = 32.sp
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = if (arabic) {
                "محرك ألعاب للهواتف"
            } else {
                "Mobile Game Engine"
            },
            color = Color.White,
            fontSize = 18.sp
        )

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Button(
            onClick = {
                showCreateDialog = true
            }
        ) {

            Text(
                if (arabic) {
                    "إنشاء مشروع"
                } else {
                    "CREATE PROJECT"
                }
            )
        }

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Text(
            text = if (arabic) {
                "المشاريع"
            } else {
                "PROJECTS"
            },
            color = Color(0xFF00E5FF),
            fontSize = 20.sp
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        if (projects.isEmpty()) {

            Text(
                text = if (arabic) {
                    "لا توجد مشاريع"
                } else {
                    "No projects yet"
                },
                color = Color.Gray
            )

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {

                items(projects) { project ->

                    ProjectCard(
                        project = project,
                        arabic = arabic,

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
            arabic = arabic,

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
    arabic: Boolean,
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

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = if (arabic) {
                "النوع: ${project.type}"
            } else {
                "Type: ${project.type}"
            },
            color = Color.LightGray
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = onOpen
            ) {

                Text(
                    if (arabic) {
                        "فتح"
                    } else {
                        "OPEN"
                    }
                )
            }

            OutlinedButton(
                onClick = {
                    showDeleteDialog = true
                }
            ) {

                Text(
                    if (arabic) {
                        "حذف"
                    } else {
                        "DELETE"
                    }
                )
            }
        }
    }

    Spacer(
        modifier = Modifier.height(8.dp)
    )

    if (showDeleteDialog) {

        AlertDialog(

            onDismissRequest = {
                showDeleteDialog = false
            },

            title = {
                Text(
                    if (arabic) {
                        "حذف المشروع"
                    } else {
                        "Delete Project"
                    }
                )
            },

            text = {
                Text(
                    if (arabic) {
                        "هل تريد حذف ${project.name}؟"
                    } else {
                        "Delete ${project.name}?"
                    }
                )
            },

            confirmButton = {

                Button(
                    onClick = {

                        showDeleteDialog = false
                        onDelete()
                    }
                ) {

                    Text(
                        if (arabic) {
                            "حذف"
                        } else {
                            "DELETE"
                        }
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {

                    Text(
                        if (arabic) {
                            "إلغاء"
                        } else {
                            "CANCEL"
                        }
                    )
                }
            }
        )
    }
}

@Composable
fun CreateProjectDialog(
    arabic: Boolean,
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
            Text(
                if (arabic) {
                    "إنشاء مشروع"
                } else {
                    "Create Project"
                }
            )
        },

        text = {

            Column {

                OutlinedTextField(
                    value = projectName,

                    onValueChange = {
                        projectName = it
                    },

                    label = {
                        Text(
                            if (arabic) {
                                "اسم المشروع"
                            } else {
                                "Project Name"
                            }
                        )
                    }
                )

                Spacer(
                    modifier = Modifier.height(15.dp)
                )

                Text(
                    if (arabic) {
                        "نوع المشروع"
                    } else {
                        "Project Type"
                    }
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
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

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    if (arabic) {
                        "المحدد: $projectType"
                    } else {
                        "Selected: $projectType"
                    }
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

                Text(
                    if (arabic) {
                        "إنشاء"
                    } else {
                        "CREATE"
                    }
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onCancel
            ) {

                Text(
                    if (arabic) {
                        "إلغاء"
                    } else {
                        "CANCEL"
                    }
                )
            }
        }
    )
}

@Composable
fun EditorScreen(
    project: Project,
    arabic: Boolean,
    onLanguageChange: () -> Unit,
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
        mutableStateOf<Int?>(null)
    }

    var selectedTool by remember {
        mutableStateOf("SELECT")
    }

    var showAddObject by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827))
    ) {

        EditorTopBar(
            project = project,
            arabic = arabic,
            onLanguageChange = onLanguageChange,
            onBack = onBack
        )

        EditorToolBar(
            selectedTool = selectedTool,
            arabic = arabic,

            onToolSelected = { tool ->
                selectedTool = tool
            }
        )

        Row(
            modifier = Modifier.fillMaxSize()
        ) {

            SceneTree(
                objects = objects,
                selectedObjectId = selectedObjectId,
                arabic = arabic,

                onSelect = { id ->
                    selectedObjectId = id
                },

                onAddObject = {
                    showAddObject = true
                }
            )

            EditorViewport(
                project = project,
                selectedTool = selectedTool,
                arabic = arabic
            )

            InspectorPanel(
                objects = objects,
                selectedObjectId = selectedObjectId,
                arabic = arabic
            )
        }
    }

    if (showAddObject) {

        AddObjectDialog(
            arabic = arabic,

            onCancel = {
                showAddObject = false
            },

            onObjectSelected = { type ->

                val newId =
                    (objects.maxOfOrNull { obj -> obj.id } ?: 0) + 1

                val objectName = when (type) {

                    "Cube" -> "Cube $newId"

                    "Sphere" -> "Sphere $newId"

                    "Camera" -> "Camera $newId"

                    "Light" -> "Light $newId"

                    else -> "Object $newId"
                }

                objects.add(
                    GameObject(
                        id = newId,
                        name = objectName,
                        type = type
                    )
                )

                selectedObjectId = newId
                showAddObject = false
            }
        )
    }
}

@Composable
fun EditorTopBar(
    project: Project,
    arabic: Boolean,
    onLanguageChange: () -> Unit,
    onBack: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .padding(8.dp),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        TextButton(
            onClick = onBack
        ) {

            Text(
                if (arabic) {
                    "← رجوع"
                } else {
                    "← BACK"
                }
            )
        }

        Text(
            text = project.name,
            color = Color.White,
            fontSize = 18.sp
        )

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = project.type,
                color = Color(0xFF00E5FF)
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            OutlinedButton(
                onClick = onLanguageChange
            ) {

                Text(
                    if (arabic) {
                        "EN"
                    } else {
                        "AR"
                    }
                )
            }
        }
    }
}

@Composable
fun EditorToolBar(
    selectedTool: String,
    arabic: Boolean,
    onToolSelected: (String) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B))
            .padding(6.dp),

        horizontalArrangement =
            Arrangement.spacedBy(5.dp)
    ) {

        val tools = listOf(
            "SELECT",
            "MOVE",
            "ROTATE",
            "SCALE"
        )

        tools.forEach { tool ->

            val label = when (tool) {

                "SELECT" ->
                    if (arabic) "تحديد" else "SELECT"

                "MOVE" ->
                    if (arabic) "تحريك" else "MOVE"

                "ROTATE" ->
                    if (arabic) "تدوير" else 
