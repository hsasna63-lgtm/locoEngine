package com.loco.engine

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

/* =========================================================
   DATA
   ========================================================= */

data class Project(
    val name: String,
    val type: String
)

data class GameObject(
    val id: Int,
    val name: String,
    val type: String
)

/* =========================================================
   PROJECT STORAGE
   ========================================================= */

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

/* =========================================================
   MAIN APP
   ========================================================= */

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

/* =========================================================
   HOME
   ========================================================= */

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
            .padding(20.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "LOCO ENGINE",
                color = Color(0xFF00E5FF),
                fontSize = 28.sp
            )

            OutlinedButton(
                onClick = onLanguageChange
            ) {
                Text(
                    if (arabic) {
                        "EN"
                    } else {
                        "ع"
                    }
                )
            }
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = if (arabic) {
                "محرك ألعاب للموبايل"
            } else {
                "Mobile Game Engine"
            },
            color = Color.White,
            fontSize = 17.sp
        )

        Spacer(
            modifier = Modifier.height(25.dp)
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
            modifier = Modifier.height(25.dp)
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
            modifier = Modifier.height(10.dp)
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

                    Spacer(
                        modifier = Modifier.height(8.dp)
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

/* =========================================================
   PROJECT CARD
   ========================================================= */

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
            fontSize = 19.sp
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

/* =========================================================
   CREATE PROJECT
   ========================================================= */

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

/* =========================================================
   EDITOR
   ========================================================= */

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

    var showAddDialog by remember {
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
                    showAddDialog = true
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

    if (showAddDialog) {

        AddObjectDialog(
            arabic = arabic,

            onCancel = {
                showAddDialog = false
            },

            onObjectSelected = { type ->

                val newId =
                    (objects.maxOfOrNull { obj ->
                        obj.id
                    } ?: 0) + 1

                val newObject = GameObject(
                    id = newId,
                    name = "$type $newId",
                    type = type
                )

                objects.add(newObject)

                selectedObjectId = newId

                showAddDialog = false
            }
        )
    }
}

/* =========================================================
   EDITOR TOP BAR
   ========================================================= */

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

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = project.name,
                color = Color.White,
                fontSize = 17.sp
            )

            Text(
                text = project.type,
                color = Color(0xFF00E5FF),
                fontSize = 12.sp
            )
        }

        OutlinedButton(
            onClick = onLanguageChange
        ) {
            Text(
                if (arabic) {
                    "EN"
                } else {
                    "ع"
                }
            )
        }
    }
}

/* =========================================================
   TOOL BAR
   ========================================================= */

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
                    if (arabic) "تدوير" else "ROTATE"

                "SCALE" ->
                    if (arabic) "تحجيم" else "SCALE"

                else ->
                    tool
            }

            if (tool == selectedTool) {

                Button(
                    onClick = {
                        onToolSelected(tool)
                    }
                ) {
                    Text(label)
                }

            } else {

                OutlinedButton(
                    onClick = {
                        onToolSelected(tool)
                    }
                ) {
                    Text(label)
                }
            }
        }
    }
}

/* =========================================================
   SCENE TREE
   ========================================================= */

@Composable
fun SceneTree(
    objects: List<GameObject>,
    selectedObjectId: Int?,
    arabic: Boolean,
    onSelect: (Int) -> Unit,
    onAddObject: () -> Unit
) {

    Column(
        modifier = Modifier
            .width(165.dp)
            .fillMaxSize()
            .background(Color(0xFF0B1220))
            .padding(8.dp)
    ) {

        Text(
            text = if (arabic) {
                "المشهد"
            } else {
                "SCENE"
            },
            color = Color(0xFF00E5FF),
            fontSize = 18.sp
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Button(
            onClick = onAddObject,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (arabic) {
                    "+ كائن"
                } else {
                    "+ OBJECT"
                }
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        LazyColumn {

            items(
                items = objects,
                key = { obj ->
                    obj.id
                }
            ) { obj ->

                val selected =
                    obj.id == selectedObjectId

                Text(
                    text = obj.name,

                    color =
                        if (selected) {
                            Color(0xFF00E5FF)
                        } else {
                            Color.White
                        },

                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (selected) {
                                Color(0xFF172554)
                            } else {
                                Color.Transparent
                            }
                        )
                        .clickable {
                            onSelect(obj.id)
                        }
                        .padding(10.dp)
                )
            }
        }
    }
}

/* =========================================================
   ADD OBJECT DIALOG
   ========================================================= */

@Composable
fun AddObjectDialog(
    arabic: Boolean,
    onCancel: () -> Unit,
    onObjectSelected: (String) -> Unit
) {

    AlertDialog(

        onDismissRequest = onCancel,

        title = {
            Text(
                if (arabic) {
                    "إضافة كائن"
                } else {
                    "Add Object"
                }
            )
        },

        text = {

            Column {

                ObjectButton(
                    text = "Cube",
                    onClick = {
                        onObjectSelected("Cube")
                    }
                )

                ObjectButton(
                    text = "Sphere",
                    onClick = {
                        onObjectSelected("Sphere")
                    }
                )

                ObjectButton(
                    text = "Camera",
                    onClick = {
                        onObjectSelected("Camera")
                    }
                )

                ObjectButton(
                    text = "Light",
                    onClick = {
                        onObjectSelected("Light")
                    }
                )
            }
        },

        confirmButton = {},

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
fun ObjectButton(
    text: String,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }

    Spacer(
        modifier = Modifier.height(5.dp)
    )
}

/* =========================================================
   VIEWPORT
   ========================================================= */

@Composable
fun EditorViewport(
    project: Project,
    selectedTool: String,
    arabic: Boolean
) {

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxSize()
            .background(Color(0xFF182233))
            .border(
                width = 1.dp,
                color = Color(0xFF334155)
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
        ) {

            Text(
                text = if (arabic) {
                    "مساحة العمل ${project.type}"
                } else {
                    "${project.type} VIEWPORT"
                },
                color = Color(0xFF00E5FF),
                fontSize = 20.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = if (arabic) {
                    "الأداة: $selectedTool"
                } else {
                    "Tool: $selectedTool"
                },
                color = Color.White
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            /* Grid */

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF141D2B)),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.Center
            ) {

                Text(
                    text = "┼",
                    color = Color(0xFF00E5FF),
                    fontSize = 42.sp
                )

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = if (arabic) {
                        "مركز المشهد"
                    } else {
                        "SCENE CENTER"
                    },
                    color = Color.Gray
                )

                Spacer(
                    modifier = Modifier.height(15.dp)
                )

                Text(
                    text = "──────────────",
                    color = Color(0xFF334155)
                )

                Text(
                    text = "│  │  │  │  │",
                    color = Color(0xFF334155)
                )

                Text(
                    text = "──────────────",
                    color = Color(0xFF334155)
                )
            }
        }
    }
}

/* =========================================================
   INSPECTOR
   ========================================================= */

@Composable
fun InspectorPanel(
    objects: List<GameObject>,
    selectedObjectId: Int?,
    arabic: Boolean
) {

    val selectedObject =
        objects.firstOrNull { obj ->
            obj.id == selectedObjectId
        }

    Column(
        modifier = Modifier
            .width(190.dp)
            .fillMaxSize()
            .background(Color(0xFF0B1220))
            .padding(10.dp)
    ) {

        Text(
            text = if (arabic) {
                "الخصائص"
            } else {
                "INSPECTOR"
            },
            color = Color(0xFF00E5FF),
            fontSize = 18.sp
        )

        Spacer(
            modifier = Modifier.height(15.dp)
        )

        if (selectedObject == null) {

            Text(
                text = if (arabic) {
                    "اختر كائنًا من المشهد"
                } else {
                    "Select an object"
                },
                color = Color.Gray
            )

        } else {

            Text(
                text = selectedObject.name,
                color = Color.White,
                fontSize = 18.sp
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = if (arabic) {
                    "النوع: ${selectedObject.type}"
                } else {
                    "Type: ${selectedObject.type}"
                },
                color = Color.Gray
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = if (arabic) {
                    "التحويل"
                } else {
                    "TRANSFORM"
                },
                color = Color(0xFF00E5FF),
                fontSize = 16.sp
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = if (arabic) {
                    "الموقع"
                } else {
                    "POSITION"
                },
                color = Color.White
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = "X: 0.0",
                color = Color.LightGray
            )

            Text(
                text = "Y: 0.0",
                color = Color.LightGray
            )

            Text(
                text = "Z: 0.0",
                color = Color.LightGray
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = if (arabic) {
                    "الدوران"
                } else {
                    "ROTATION"
                },
                color = Color.White
            )

            Text(
                text = "X: 0°",
                color = Color.LightGray
            )

            Text(
                text = "Y: 0°",
                color = Color.LightGray
            )

            Text(
                text = "Z: 0°",
                color = Color.LightGray
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = if (arabic) {
                    "المقياس"
                } else {
                    "SCALE"
                },
                color = Color.White
            )

            Text(
                text = "X: 1.0",
                color = Color.LightGray
            )

            Text(
                text = "Y: 1.0",
                color = Color.LightGray
            )

            Text(
                text = "Z: 1.0",
                color = Color.LightGray
            )
        }
    }
}
