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
        val savedLanguage = loadLanguage(this)

        setContent {
            LocoEngineApp(
                savedProjects = savedProjects,
                initialLanguage = savedLanguage
            )
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

fun loadLanguage(context: Context): String {

    return context
        .getSharedPreferences(
            "loco_engine",
            Context.MODE_PRIVATE
        )
        .getString(
            "language",
            "en"
        ) ?: "en"
}

fun saveLanguage(
    context: Context,
    language: String
) {

    context
        .getSharedPreferences(
            "loco_engine",
            Context.MODE_PRIVATE
        )
        .edit()
        .putString(
            "language",
            language
        )
        .apply()
}

fun tr(
    language: String,
    english: String,
    arabic: String
): String {

    return if (language == "ar") {
        arabic
    } else {
        english
    }
}

@Composable
fun LocoEngineApp(
    savedProjects: List<Project>,
    initialLanguage: String
) {

    val context = androidx.compose.ui.platform.LocalContext.current

    val projects = remember {

        mutableStateListOf<Project>().apply {
            addAll(savedProjects)
        }
    }

    var language by remember {
        mutableStateOf(initialLanguage)
    }

    var openedProject by remember {
        mutableStateOf<Project?>(null)
    }

    if (openedProject != null) {

        EditorScreen(
            project = openedProject!!,
            language = language,
            onBack = {
                openedProject = null
            }
        )

        return
    }

    HomeScreen(
        projects = projects,
        language = language,

        onLanguageChange = { newLanguage ->

            language = newLanguage

            saveLanguage(
                context,
                newLanguage
            )
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

@Composable
fun HomeScreen(
    projects: List<Project>,
    language: String,
    onLanguageChange: (String) -> Unit,
    onCreateProject: (String, String) -> Unit,
    onOpenProject: (Project) -> Unit,
    onDeleteProject: (Project) -> Unit
) {

    var showCreateDialog by remember {
        mutableStateOf(false)
    }

    MaterialTheme {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(24.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                OutlinedButton(
                    onClick = {

                        if (language == "en") {
                            onLanguageChange("ar")
                        } else {
                            onLanguageChange("en")
                        }
                    }
                ) {

                    Text(
                        text = if (language == "en") {
                            "العربية"
                        } else {
                            "English"
                        }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
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
                text = tr(
                    language,
                    "Mobile Game Engine",
                    "محرك ألعاب للهواتف"
                ),
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
                    tr(
                        language,
                        "CREATE PROJECT",
                        "إنشاء مشروع"
                    )
                )
            }

            Spacer(
                modifier = Modifier.height(30.dp)
            )

            Text(
                text = tr(
                    language,
                    "PROJECTS",
                    "المشاريع"
                ),
                color = Color(0xFF00E5FF),
                fontSize = 20.sp
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            if (projects.isEmpty()) {

                Text(
                    text = tr(
                        language,
                        "No projects yet",
                        "لا توجد مشاريع حتى الآن"
                    ),
                    color = Color.Gray
                )

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    items(projects) { project ->

                        ProjectCard(
                            project = project,
                            language = language,

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

                language = language,

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
}

@Composable
fun ProjectCard(
    project: Project,
    language: String,
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
            text = tr(
                language,
                "Type: ${project.type}",
                "النوع: ${project.type}"
            ),
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
                    tr(
                        language,
                        "OPEN",
                        "فتح"
                    )
                )
            }

            OutlinedButton(
                onClick = {
                    showDeleteDialog = true
                }
            ) {

                Text(
                    tr(
                        language,
                        "DELETE",
                        "حذف"
                    )
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
                    tr(
                        language,
                        "Delete Project",
                        "حذف المشروع"
                    )
                )
            },

            text = {

                Text(
                    tr(
                        language,
                        "Delete ${project.name}?",
                        "هل تريد حذف ${project.name}؟"
                    )
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
                        tr(
                            language,
                            "DELETE",
                            "حذف"
                        )
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
                        tr(
                            language,
                            "CANCEL",
                            "إلغاء"
                        )
                    )
                }
            }
        )
    }
}

@Composable
fun CreateProjectDialog(
    language: String,
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
                tr(
                    language,
                    "Create Project",
                    "إنشاء مشروع"
                )
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
                            tr(
                                language,
                                "Project Name",
                                "اسم المشروع"
                            )
                        )
                    }
                )

                Spacer(
                    modifier = Modifier.height(15.dp)
                )

                Text(
                    text = tr(
                        language,
                        "Project Type",
                        "نوع المشروع"
                    )
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

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

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = tr(
                        language,
                        "Selected: $projectType",
                        "المحدد: $projectType"
                    )
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
                    tr(
                        language,
                        "CREATE",
                        "إنشاء"
                    )
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onCancel
            ) {

                Text(
                    tr(
                        language,
                        "CANCEL",
                        "إلغاء"
                    )
                )
            }
        }
    )
}

@Composable
fun EditorScreen(
    project: Project,
    language: String,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827))
    ) {

        EditorTopBar(
            project = project,
            language = language,
            onBack = onBack
        )

        EditorToolBar(
            selectedTool = selectedTool,
            language = language,

            onToolSelected = {
                selectedTool = it
            }
        )

        Row(
            modifier = Modifier.fillMaxSize()
        ) {

            SceneTree(
                objects = objects,
                selectedObjectId = selectedObjectId,
                language = language,

                onSelect = {
                    selectedObjectId = it
                },

                onAddObject = {

                    val newId =
                        (objects.maxOfOrNull { it.id } ?: 0) + 1

                    objects.add(
                        GameObject(
                            id = newId,
                            name = "Cube $newId",
                            type = "Mesh"
                        )
                    )
                }
            )

            EditorViewport(
                project = project,
                selectedTool = selectedTool,
                language = language
            )

            InspectorPanel(
                objects = objects,
                selectedObjectId = selectedObjectId,
                language = language
            )
        }
    }
}

@Composable
fun EditorTopBar(
    project: Project,
    language: String,
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

            Text(
                tr(
                    language,
                    "← BACK",
                    "← رجوع"
                )
            )
        }

        Text(
            text = project.name,
            color = Color.White,
            fontSize = 18.sp
        )

        Text(
            text = project.type,
            color = Color(0xFF00E5FF)
        )
    }
}

@Composable
fun EditorToolBar(
    selectedTool: String,
    language: String,
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

            val toolText = when (tool) {

                "SELECT" -> tr(
                    language,
                    "SELECT",
                    "تحديد"
                )

                "MOVE" -> tr(
                    language,
                    "MOVE",
                    "تحريك"
                )

               
