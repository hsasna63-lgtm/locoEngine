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

        val projects = loadProjects(this)

        setContent {
            LocoApp(projects)
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

    val prefs = context.getSharedPreferences(
        "loco_engine",
        Context.MODE_PRIVATE
    )

    val data = prefs.getString("projects", null)
        ?: return emptyList()

    return try {

        val array = JSONArray(data)
        val result = mutableListOf<Project>()

        for (i in 0 until array.length()) {

            val item = array.getJSONObject(i)

            result.add(
                Project(
                    item.getString("name"),
                    item.getString("type")
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

    projects.forEach { project ->

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
fun LocoApp(
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

        Editor(
            project = openedProject!!,
            onBack = {
                openedProject = null
            }
        )

        return
    }

    HomeScreen(
        projects = projects,

        onCreate = { name, type ->

            projects.add(
                Project(name, type)
            )

            saveProjects(
                context,
                projects
            )
        },

        onOpen = { project ->

            openedProject = project
        },

        onDelete = { project ->

            projects.remove(project)

            saveProjects(
                context,
                projects
            )
        }
    )
}

@Composable
fun HomeScreen(
    projects: MutableList<Project>,
    onCreate: (String, String) -> Unit,
    onOpen: (Project) -> Unit,
    onDelete: (Project) -> Unit
) {

    var createDialog by remember {
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
            modifier = Modifier.height(50.dp)
        )

        Text(
            text = "LOCO ENGINE",
            color = Color(0xFF00E5FF),
            fontSize = 32.sp
        )

        Text(
            text = "Mobile Game Engine",
            color = Color.White,
            fontSize = 18.sp
        )

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Button(
            onClick = {
                createDialog = true
            }
        ) {
            Text("CREATE PROJECT")
        }

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Text(
            text = "PROJECTS",
            color = Color(0xFF00E5FF),
            fontSize = 20.sp
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        if (projects.isEmpty()) {

            Text(
                text = "No projects yet",
                color = Color.Gray
            )

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {

                items(projects) { project ->

                    ProjectItem(
                        project = project,
                        onOpen = {
                            onOpen(project)
                        },
                        onDelete = {
                            onDelete(project)
                        }
                    )
                }
            }
        }
    }

    if (createDialog) {

        CreateDialog(

            onCancel = {
                createDialog = false
            },

            onCreate = { name, type ->

                onCreate(name, type)

                createDialog = false
            }
        )
    }
}

@Composable
fun ProjectItem(
    project: Project,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {

    var deleteDialog by remember {
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

        Text(
            text = "Type: ${project.type}",
            color = Color.Gray
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
                    deleteDialog = true
                }
            ) {
                Text("DELETE")
            }
        }
    }

    Spacer(
        modifier = Modifier.height(8.dp)
    )

    if (deleteDialog) {

        AlertDialog(

            onDismissRequest = {
                deleteDialog = false
            },

            title = {
                Text("Delete Project")
            },

            text = {
                Text("Delete ${project.name}?")
            },

            confirmButton = {

                Button(
                    onClick = {

                        deleteDialog = false

                        onDelete()
                    }
                ) {
                    Text("DELETE")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        deleteDialog = false
                    }
                ) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun CreateDialog(
    onCancel: () -> Unit,
    onCreate: (String, String) -> Unit
) {

    var name by remember {
        mutableStateOf("")
    }

    var type by remember {
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
                    value = name,

                    onValueChange = {
                        name = it
                    },

                    label = {
                        Text("Project Name")
                    }
                )

                Spacer(
                    modifier = Modifier.height(15.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Button(
                        onClick = {
                            type = "2D"
                        }
                    ) {
                        Text("2D")
                    }

                    Button(
                        onClick = {
                            type = "3D"
                        }
                    ) {
                        Text("3D")
                    }
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Selected: $type"
                )
            }
        },

        confirmButton = {

            Button(
                onClick = {

                    if (name.isNotBlank()) {

                        onCreate(
                            name.trim(),
                            type
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
fun Editor(
    project: Project,
    onBack: () -> Unit
) {

    val objects = remember {

        mutableStateListOf(
            GameObject(
                1,
                "Main Camera",
                "Camera"
            ),

            GameObject(
                2,
                "Directional Light",
                "Light"
            )
        )
    }

    var selectedId by remember {
        mutableStateOf<Int?>(null)
    }

    var tool by remember {
        mutableStateOf("SELECT")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827))
    ) {

        TopBar(
            project = project,
            onBack = onBack
        )

        ToolBar(
            selectedTool = tool,
            onToolSelected = {
                tool = it
            }
        )

        Row(
            modifier = Modifier.fillMaxSize()
        ) {

            ScenePanel(
                objects = objects,
                selectedId = selectedId,
                onSelect = {
                    selectedId = it
                },

                onAdd = {

                    val id =
                        (objects.maxOfOrNull { it.id } ?: 0) + 1

                    objects.add(
                        GameObject(
                            id,
                            "Cube $id",
                            "Mesh"
                        )
                    )
                }
            )

            Viewport(
                project = project,
                tool = tool
            )

            Inspector(
                objects = objects,
                selectedId = selectedId
            )
        }
    }
}

@Composable
fun TopBar(
    project: Project,
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
            Text("← BACK")
        }

        Text(
            text = project.name,
            color = Color.White
        )

        Text(
            text = project.type,
            color = Color(0xFF00E5FF)
        )
    }
}

@Composable
fun ToolBar(
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
fun ScenePanel(
    objects: List<GameObject>,
    selectedId: Int?,
    onSelect: (Int) -> Unit,
    onAdd: () -> Unit
) {

    Column(
        modifier = Modifier
            .width(160.dp)
            .fillMaxSize()
            .background(Color(0xFF0B1220))
            .padding(8.dp)
    ) {

        Text(
            text = "SCENE",
            color = Color(0xFF00E5FF),
            fontSize = 18.sp
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Button(
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ OBJECT")
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        LazyColumn {

            items(
                objects,
                key = {
                    it.id
                }
            ) { obj ->

                Text(
                    text = obj.name,

                    color = if (obj.id == selectedId) {
                        Color(0xFF00E5FF)
                    } else {
                        Color.White
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelect(obj.id)
                        }
                        .padding(10.dp)
                )
            }
        }
    }
}

@Composable
fun Viewport(
    project: Project,
    tool: String
) {

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxSize()
            .background(Color(0xFF182233)),

        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "${project.type} VIEWPORT",
                color = Color(0xFF00E5FF),
                fontSize = 22.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Tool: $tool",
                color = Color.White
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "WORKSPACE",
                color = Color.Gray
            )
        }
    }
}

@Composable
fun Inspector(
    objects: List<GameObject>,
    selectedId: Int?
) {

    val selected = objects.firstOrNull {
        it.id == selectedId
    }

    Column(
        modifier = Modifier
            .width(180.dp)
            .fillMaxSize()
            .background(Color(0xFF0B1220))
            .padding(10.dp)
    ) {

        Text(
            text = "INSPECTOR",
            color = Color(0xFF00E5FF),
            fontSize = 18.sp
        )

        Spacer(
            modifier = Modifier.height(15.dp)
        )

        if (selected == null) {

            Text(
                text = "Select an object",
                color = Color.Gray
            )

        } else {

            Text(
                text = selected.name,
                color = Color.White,
                fontSize = 18.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Type: ${selected.type}",
                color = Color.Gray
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "TRANSFORM",
                color = Color(0xFF00E5FF)
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text("Position")
            Text("X: 0.0")
            Text("Y: 0.0")
            Text("Z: 0.0")

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text("Rotation: 0°")

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text("Scale: 1.0")
        }
    }
}
