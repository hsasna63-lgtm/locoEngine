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

    if (openedProject != null) {

        EditorScreen(
            project = openedProject!!,

            onBack = {
                openedProject = null
            },

            onProjectRenamed = { oldProject, newName ->

                val index = projects.indexOf(oldProject)

                if (index >= 0) {

                    val updatedProject = Project(
                        name = newName,
                        type = oldProject.type
                    )

                    projects[index] = updatedProject

                    saveProjects(
                        context,
                        projects.toList()
                    )

                    openedProject = updatedProject
                }
            }
        )

        return
    }

    HomeScreen(
        projects = projects,

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
    onCreateProject: (String, String) -> Unit,
    onOpenProject: (Project) -> Unit,
    onDeleteProject: (Project) -> Unit
) {

    var showCreateDialog by remember {
        mutableStateOf(false)
    }

    var searchText by remember {
        mutableStateOf("")
    }

    val filteredProjects = projects.filter {

        it.name.contains(
            searchText,
            ignoreCase = true
        )
    }

    MaterialTheme {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(24.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier = Modifier.height(45.dp)
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
                text = "Mobile Game Engine",
                color = Color.White,
                fontSize = 18.sp
            )

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            Button(
                onClick = {
                    showCreateDialog = true
                }
            ) {
                Text("CREATE PROJECT")
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            OutlinedTextField(
                value = searchText,

                onValueChange = {
                    searchText = it
                },

                label = {
                    Text("Search Projects")
                },

                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "PROJECTS",
                    color = Color(0xFF00E5FF),
                    fontSize = 20.sp
                )

                Text(
                    text = "${projects.size}",
                    color = Color.Gray
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            if (projects.isEmpty()) {

                Text(
                    text = "No projects yet",
                    color = Color.Gray
                )

            } else if (filteredProjects.isEmpty()) {

                Text(
                    text = "No matching projects",
                    color = Color.Gray
                )

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    items(filteredProjects) { project ->

                        ProjectCard(
                            project = project,

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
            text = "Type: ${project.type}",
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
                Text("OPEN")
            }

            OutlinedButton(
                onClick = {
                    showDeleteDialog = true
                }
            ) {
                Text("DELETE")
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
                Text("Delete Project")
            },

            text = {
                Text(
                    "Delete ${project.name}?"
                )
            },

            confirmButton = {

                Button(
                    onClick = {

                        showDeleteDialog = false

                        onDelete()
                    }
                ) {
                    Text("DELETE")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun CreateProjectDialog(
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
            Text("Create Project")
        },

        text = {

            Column {

                OutlinedTextField(
                    value = projectName,

                    onValueChange = {
                        projectName = it
                    },

                    label = {
                        Text("Project Name")
                    }
                )

                Spacer(
                    modifier = Modifier.height(15.dp)
                )

                Text(
                    text = "Project Type"
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
                    text = "Selected: $projectType"
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
                Text("CREATE")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onCancel
            ) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
fun EditorScreen(
    project: Project,
    onBack: () -> Unit,
    onProjectRenamed: (Project, String) -> Unit
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

    var showRenameDialog by remember {
        mutableStateOf(false)
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

            onBack = onBack,

            onRename = {
                showRenameDialog = true
            }
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
                objects = objects,

                selectedObjectId = selectedObjectId,

                onSelect = {
                    selectedObjectId = it
                },

                onAddObject = {
                    showAddObjectDialog = true
                },

                onDeleteObject = {

                    val id = selectedObjectId

                    if (id != null) {

                        objects.removeAll {
                            it.id == id
                        }

                        selectedObjectId = null
                    }
                }
            )

            EditorViewport(
                project = project,
                selectedTool = selectedTool
            )

            InspectorPanel(
                objects = objects,
                selectedObjectId = selectedObjectId
            )
        }
    }

    if (showRenameDialog) {

        RenameProjectDialog(

            currentName = project.name,

            onCancel = {
                showRenameDialog = false
            },

            onRename = { newName ->

                onProjectRenamed(
                    project,
                    newName
                )

                showRenameDialog = false
            }
        )
    }

    if (showAddObjectDialog) {

        AddObjectDialog(

            onCancel = {
                showAddObjectDialog = false
            },

            onAdd = { objectType ->

                val newId =
                    (objects.maxOfOrNull { it.id } ?: 0) + 1

                val objectName = when (objectType) {

                    "Cube" -> "Cube $newId"

                    "Sphere" -> "Sphere $newId"

                    "Cylinder" -> "Cylinder $newId"

                    "Camera" -> "Camera $newId"

                    "Light" -> "Light $newId"

                    else -> "Object $newId"
                }

                objects.add(
                    GameObject(
                        id = newId,
                        name = objectName,
                        type = objectType
                    )
                )

                showAddObjectDialog = false
            }
        )
    }
}

@Composable
fun EditorTopBar(
    project: Project,
    onBack: () -> Unit,
    onRename: () -> Unit
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
            Text("← BACK")
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = project.name,
                color = Color.White,
                fontSize = 18.sp
            )

            Text(
                text = project.type,
                color = Color(0xFF00E5FF),
                fontSize = 12.sp
            )
        }

        TextButton(
            onClick = onRename
        ) {
            Text("RENAME")
        }
    }
}

@Composable
fun RenameProjectDialog(
    currentName: String,
    onCancel: () -> Unit,
    onRename: (String) -> Unit
) {

    var newName by remember {
        mutableStateOf(currentName)
    }

    AlertDialog(

        onDismissRequest = onCancel,

        title = {
            Text("Rename Project")
        },

        text = {

            OutlinedTextField(
                value = newName,

                onValueChange = {
                    newName = it
                },

                label = {
                    Text("New Project Name")
                }
            )
        },

        confirmButton = {

            Button(
                onClick = {

                    if (newName.isNotBlank()) {

                        onRename(
                            newName.trim()
                        )
                    }
                }
            ) {
                Text("RENAME")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onCancel
            ) {
                Text("CANCEL")
            }
        }
    )
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
                    Text(tool)
                }

            } else {

                OutlinedButton(
                    onClick = {
                        onToolSelected(tool)
                    }
                ) {
                    Text(tool)
                }
            }
        }
    }
}

@Composable
fun SceneTree(
    objects:
