package com.loco.engine

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

override fun onCreate(savedInstanceState: Bundle?) {  
    super.onCreate(savedInstanceState)  

    val savedProjects = loadProjects(this)  
    val savedLanguage = loadLanguage(this)  
    val openedProject = loadOpenedProject(this)  

    setContent {  

        LocoEngineApp(  
            savedProjects = savedProjects,  
            initialLanguage = savedLanguage,  
            initialOpenedProject = openedProject  
        )  
    }  
}

}

/* =========================
DATA
========================= */

data class Project(
val name: String,
val type: String
)

data class GameObject(
val id: Int,
val name: String,
val type: String,
val x: Float = 0f,
val y: Float = 0f,
val rotation: Float = 0f,
val scale: Float = 1f
)

/* =========================
PROJECT STORAGE
========================= */

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

/* =========================
LANGUAGE
========================= */

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

fun text(
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

/* =========================
OPENED PROJECT STORAGE
========================= */

fun saveOpenedProject(
context: Context,
project: Project?
) {

val editor = context  
    .getSharedPreferences(  
        "loco_engine",  
        Context.MODE_PRIVATE  
    )  
    .edit()  

if (project == null) {  

    editor.remove("opened_project_name")  
    editor.remove("opened_project_type")  

} else {  

    editor.putString(  
        "opened_project_name",  
        project.name  
    )  

    editor.putString(  
        "opened_project_type",  
        project.type  
    )  
}  

editor.apply()

}

fun loadOpenedProject(
context: Context
): Project? {

val preferences = context.getSharedPreferences(  
    "loco_engine",  
    Context.MODE_PRIVATE  
)  

val name = preferences.getString(  
    "opened_project_name",  
    null  
)  

val type = preferences.getString(  
    "opened_project_type",  
    null  
)  

if (name == null || type == null) {  
    return null  
}  

return Project(  
    name = name,  
    type = type  
)

}

/* =========================
OBJECT STORAGE
========================= */

fun objectStorageKey(
project: Project
): String {

return "objects_${project.name}_${project.type}"  
    .replace(" ", "_")

}

fun loadObjects(
context: Context,
project: Project
): List<GameObject> {

val preferences = context.getSharedPreferences(  
    "loco_engine",  
    Context.MODE_PRIVATE  
)  

val data = preferences.getString(  
    objectStorageKey(project),  
    null  
)  

if (data == null) {  

    return listOf(  

        GameObject(  
            id = 1,  
            name = "Main Camera",  
            type = "Camera",  
            x = 0f,  
            y = -80f  
        ),  

        GameObject(  
            id = 2,  
            name = "Directional Light",  
            type = "Light",  
            x = 80f,  
            y = -60f  
        )  
    )  
}  

return try {  

    val array = JSONArray(data)  
    val result = mutableListOf<GameObject>()  

    for (i in 0 until array.length()) {  

        val item = array.getJSONObject(i)  

        result.add(  
            GameObject(  
                id = item.getInt("id"),  
                name = item.getString("name"),  
                type = item.getString("type"),  
                x = item.optDouble("x", 0.0).toFloat(),  
                y = item.optDouble("y", 0.0).toFloat(),  
                rotation = item.optDouble(  
                    "rotation",  
                    0.0  
                ).toFloat(),  
                scale = item.optDouble(  
                    "scale",  
                    1.0  
                ).toFloat()  
            )  
        )  
    }  

    result  

} catch (e: Exception) {  

    emptyList()  
}

}

fun saveObjects(
context: Context,
project: Project,
objects: List<GameObject>
) {

val array = JSONArray()  

for (obj in objects) {  

    val item = JSONObject()  

    item.put("id", obj.id)  
    item.put("name", obj.name)  
    item.put("type", obj.type)  
    item.put("x", obj.x)  
    item.put("y", obj.y)  
    item.put("rotation", obj.rotation)  
    item.put("scale", obj.scale)  

    array.put(item)  
}  

context  
    .getSharedPreferences(  
        "loco_engine",  
        Context.MODE_PRIVATE  
    )  
    .edit()  
    .putString(  
        objectStorageKey(project),  
        array.toString()  
    )  
    .apply()

}

/* =========================
MAIN APP
========================= */

@Composable
fun LocoEngineApp(
savedProjects: List<Project>,
initialLanguage: String,
initialOpenedProject: Project?
) {

val context =  
    androidx.compose.ui.platform.LocalContext.current  

val projects = remember {  

    mutableStateListOf<Project>().apply {  
        addAll(savedProjects)  
    }  
}  

var language by rememberSaveable {  
    mutableStateOf(initialLanguage)  
}  

var openedProject by rememberSaveable(  
    saver = androidx.compose.runtime.saveable.Saver(  
        save = {  
            if (it == null) {  
                null  
            } else {  
                listOf(  
                    it.name,  
                    it.type  
                )  
            }  
        },  
        restore = {  
            if (it == null) {  
                null  
            } else {  
                Project(  
                    name = it[0],  
                    type = it[1]  
                )  
            }  
        }  
    )  
) {  
    mutableStateOf(initialOpenedProject)  
}  

if (openedProject != null) {  

    EditorScreen(  
        project = openedProject!!,  
        language = language,  

        onLanguageChange = {  

            language = it  

            saveLanguage(  
                context,  
                it  
            )  
        },  

        onBack = {  

            openedProject = null  

            saveOpenedProject(  
                context,  
                null  
            )  
        }  
    )  

    return  
}  


HomeScreen(  
    projects = projects,  
    language = language,  

    onLanguageChange = {  

        language = it  

        saveLanguage(  
            context,  
            it  
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

    onOpenProject = {  

        openedProject = it  

        saveOpenedProject(  
            context,  
            it  
        )  
    },  

    onDeleteProject = {  

        projects.remove(it)  

        saveProjects(  
            context,  
            projects.toList()  
        )  
    }  
)

}

/* =========================
HOME
========================= */

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
                    if (language == "en") {  
                        "العربية"  
                    } else {  
                        "English"  
                    }  
                )  
            }  
        }  


        Spacer(  
            modifier = Modifier.height(25.dp)  
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
            text = text(  
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
                text = text(  
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
            text = text(  
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
                text = text(  
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

/* =========================
PROJECT CARD
========================= */

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
        text = text(  
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
        horizontalArrangement =  
            Arrangement.spacedBy(8.dp)  
    ) {  

        Button(  
            onClick = onOpen  
        ) {  

            Text(  
                text = text(  
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
                text = text(  
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
                text = text(  
                    language,  
                    "Delete Project",  
                    "حذف المشروع"  
                )  
            )  
        },  

        text = {  

            Text(  
                text = text(  
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
                    text = text(  
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
                    text = text(  
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

/* =========================
CREATE PROJECT
========================= */

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
            text = text(  
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
                        text = text(  
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
                text = text(  
                    language,  
                    "Project Type",  
                    "نوع المشروع"  
                )  
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
                text = text(  
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
                text = text(  
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
                text = text(  
                    language,  
                    "CANCEL",  
                    "إلغاء"  
                )  
            )  
        }  
    }  
)

}

/* =========================
EDITOR
========================= */

@Composable
fun EditorScreen(
project: Project,
language: String,
onLanguageChange: (String) -> Unit,
onBack: () -> Unit
) {

val context =  
    androidx.compose.ui.platform.LocalContext.current  

val objects = remember(project.name) {  

    mutableStateListOf<GameObject>().apply {  

        addAll(  
            loadObjects(  
                context,  
                project  
            )  
        )  
    }  
}  


var selectedObjectId by rememberSaveable {  
    mutableStateOf<Int?>(null)  
}  


var selectedTool by rememberSaveable {  
    mutableStateOf("SELECT")  
}  


var showObjectDialog by remember {  
    mutableStateOf(false)  
}  


fun saveCurrentObjects() {  

    saveObjects(  
        context,  
        project,  
        objects.toList()  
    )  
}  


fun updateObject(  
    id: Int,  
    update: (GameObject) -> GameObject  
) {  

    val index = objects.indexOfFirst {  
        it.id == id  
    }  

    if (index >= 0) {  

        objects[index] =  
            update(objects[index])  

        saveCurrentObjects()  
    }  
}  


fun rotateSelected() {  

    val id = selectedObjectId  
        ?: return  

    updateObject(id) {  

        it.copy(  
            rotation =  
                (it.rotation + 15f) % 360f  
        )  
    }  
}  


fun scaleSelected(amount: Float) {  

    val id = selectedObjectId  
        ?: return  

    updateObject(id) {  

        it.copy(  
            scale = (  
                it.scale + amount  
            ).coerceIn(  
                0.4f,  
                3f  
            )  
        )  
    }  
}  


BoxWithConstraints(  
    modifier = Modifier.fillMaxSize()  
) {  

    Column(  
        modifier = Modifier  
            .fillMaxSize()  
            .background(Color(0xFF111827))  
    ) {  

        EditorTopBar(  
            project = project,  
            language = language,  

            onLanguageChange =  
                onLanguageChange,  

            onBack = onBack  
        )  


        EditorToolBar(  
            selectedTool = selectedTool,  
            language = language,  

            onToolSelected = {  
                selectedTool = it  
            },  

            onRotate = {  
                rotateSelected()  
            },  

            onScaleUp = {  
                scaleSelected(0.2f)  
            },  

            onScaleDown = {  
                scaleSelected(-0.2f)  
            }  
        )  


        if (maxWidth > 700.dp) {  

            /* LANDSCAPE */  

            Row(  
                modifier = Modifier  
                    .fillMaxSize()  
            ) {  

                SceneTree(  
                    objects = objects,  
                    selectedObjectId =  
                        selectedObjectId,  

                    language = language,  

                    onSelect = {  
                        selectedObjectId = it  
                    },  

                    onAddObject = {  
                        showObjectDialog = true  
                    }  
                )  


                EditorViewport(  
                    project = project,  
                    objects = objects,  
                    selectedObjectId =  
                        selectedObjectId,  

                    selectedTool = selectedTool,  
                    language = language,  

                    onSelect = {  
                        selectedObjectId = it  
                    },  

                    onMove = { id, dx, dy ->  

                        updateObject(id) {  

                            it.copy(  
                                x = it.x + dx,  
                                y = it.y + dy  
                            )  
                        }  
                    }  
                )  


                InspectorPanel(  
                    objects = objects,  
                    selectedObjectId =  
                        selectedObjectId,  

                    language = language,  

                    onDelete = { id ->  

                        objects.removeAll {  
                            it.id == id  
                        }  

                        selectedObjectId = null  

                        saveCurrentObjects()  
                    },  

                    onDuplicate = { id ->  

                        val original =  
                            objects.firstOrNull {  
                                it.id == id  
                            }  

                        if (original != null) {  

                            val newId =  
                                (  
                                    objects.maxOfOrNull {  
                                        it.id  
                                    } ?: 0  
                                ) + 1  

                            val copy =  
                                original.copy(  
                                    id = newId,  
                                    name =  
                                        "${original.name} Copy",  
                                    x =  
                                        original.x + 30f,  
                                    y =  
                                        original.y + 30f  
                                )  

                            objects.add(copy)  

                            selectedObjectId =  
                                newId  

                            saveCurrentObjects()  
                        }  
                    }  
                )  
            }  

        } else {  

            /* PORTRAIT */  

            Column(  
                modifier = Modifier  
                    .fillMaxSize()  
            ) {  

                SceneTree(  
                    objects = objects,  
                    selectedObjectId =  
                        selectedObjectId,  

                    language = language,  

                    onSelect = {  
                        selectedObjectId = it  
                    },  

                    onAddObject = {  
                        showObjectDialog = true  
                    },  

                    compact = true  
                )  


                EditorViewport(  
                    project = project,  
                    objects = objects,  
                    selectedObjectId =  
                        selectedObjectId,  

                    selectedTool = selectedTool,  
                    language = language,  

                    onSelect = {  
                        selectedObjectId = it  
                    },  

                    onMove = { id, dx, dy ->  

                        updateObject(id) {  

                            it.copy(  
                                x = it.x + dx,  
                                y = it.y + dy  
                            )  
                        },  

                    compact = true  
                )  


                InspectorPanel(  
                    objects = objects,  
                    selectedObjectId =  
                        selectedObjectId,  

                    language = language,  

                    onDelete = { id ->  

                        objects.removeAll {  
                            it.id == id  
                        }  

                        selectedObjectId = null  

                        saveCurrentObjects()  
                    },  

                    onDuplicate = { id ->  

                        val original =  
                            objects.firstOrNull {  
                                it.id == id  
                            }  

                        if (original != null) {  

                            val newId =  
                                (  
                                    objects.maxOfOrNull {  
                                        it.id  
                                    } ?: 0  
                                ) + 1  

                            objects.add(  
                                original.copy(  
                                    id = newId,  
                                    name =  
                                        "${original.name} Copy",  
                                    x =  
                                        original.x + 30f,  
                                    y =  
                                        original.y + 30f  
                                )  
                            )  

                            selectedObjectId =  
                                newId  

                            saveCurrentObjects()  
                        }  
                    },  

                    compact = true  
                )  
            }  
        }  
    }  


    if (showObjectDialog) {  

        AddObjectDialog(  
            language = language,  

            onCancel = {  
                showObjectDialog = false  
            },  

            onAdd = { objectType ->  

                val newId =  
                    (  
                        objects.maxOfOrNull {  
                            it.id  
                        } ?: 0  
                    ) + 1  

                val objectName =  
                    when (objectType) {  

                        "Cube" ->  
                            "Cube $newId"  

                        "Sphere" ->  
                            "Sphere $newId"  

                        "Camera" ->  
                            "Camera $newId"  

                        else ->  
                            "Light $newId"  
                    }  


                objects.add(  
                    GameObject(  
                        id = newId,  
                        name = objectName,  
                        type = objectType,  
                        x = 0f,  
                        y = 0f  
                    )  
                )  


                selectedObjectId =  
                    newId  

                showObjectDialog =  
                    false  

                saveCurrentObjects()  
            }  
        )  
    }  
}

}

/* =========================
TOP BAR
========================= */

@Composable
fun EditorTopBar(
project: Project,
language: String,
onLanguageChange: (String) -> Unit,
onBack: () -> Unit
) {

Row(  
    modifier = Modifier  
        .fillMaxWidth()  
        .background(Color(0xFF0F172A))  
        .padding(6.dp),  

    horizontalArrangement =  
        Arrangement.SpaceBetween,  

    verticalAlignment =  
        Alignment.CenterVertically  
) {  

    TextButton(  
        onClick = onBack  
    ) {  

        Text(  
            text = text(  
                language,  
                "← BACK",  
                "رجوع →"  
            )  
        )  
    }  


    Text(  
        text = project.name,  
        color = Color.White,  
        fontSize = 17.sp  
    )  


    Row(  
        horizontalArrangement =  
            Arrangement.spacedBy(4.dp)  
    ) {  

        Text(  
            text = project.type,  
            color = Color(0xFF00E5FF)  
        )  


        TextButton(  
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
                    "ع"  
                } else {  
                    "EN"  
                }  
            )  
        }  
    }  
}

}

/* =========================
TOOL BAR
========================= */

@Composable
fun EditorToolBar(
selectedTool: String,
language: String,
onToolSelected: (String) -> Unit,
onRotate: () -> Unit,
onScaleUp: () -> Unit,
onScaleDown: () -> Unit
) {

LazyColumn(  
    modifier = Modifier  
        .fillMaxWidth()  
        .height(105.dp)  
        .background(Color(0xFF1E293B))  
        .padding(5.dp)  
) {  

    item {  

        Row(  
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

                val translated =  
                    when (tool) {  

                        "SELECT" ->  
                            text(  
                                language,  
                                "SELECT",  
                                "تحديد"  
                            )  

                        "MOVE" ->  
                            text(  
                                language,  
                                "MOVE",  
                                "تحريك"  
                            )  

                        "ROTATE" ->  
                            text(  
                                language,  
                                "ROTATE",  
                                "تدوير"  
                            )  

                        else ->  
                            text(  
                                language,  
                                "SCALE",  
                                "تحجيم"  
                            )  
                    }  


                if (tool == selectedTool) {  

                    Button(  
                        onClick = {  
                            onToolSelected(tool)  
                        }  
                    ) {  

                        Text(translated)  
                    }  

                } else {  

                    OutlinedButton(  
                        onClick = {  
                            onToolSelected(tool)  
                        }  
                    ) {  

                        Text(translated)  
                    }  
                }  
            }  
        }  


        Spacer(  
            modifier = Modifier.height(5.dp)  
        )  


        Row(  
            horizontalArrangement =  
                Arrangement.spacedBy(5.dp)  
        ) {  

            Button(  
                onClick = onRotate  
            ) {  

                Text(  
                    text = text(  
                        language,  
                        "⟳ +15°",  
                        "⟳ +15°"  
                    )  
                )  
            }  


            Button(  
                onClick = onScaleUp  
            ) {  

                Text(  
                    text = text(  
                        language,  
                        "SCALE +",  
                        "تكبير +"  
                    )  
                )  
            }  


            Button(  
                onClick = onScaleDown  
            ) {  

                Text(  
                    text = text(  
                        language,  
                        "SCALE -",  
                        "تصغير -"  
                    )  
                )  
            }  
        }  
    }  
}

}

/* =========================
SCENE TREE
========================= */

@Composable
fun SceneTree(
objects: List<GameObject>,
selectedObjectId: Int?,
language: String,
onSelect: (Int) -> Unit,
onAddObject: () -> Unit,
compact: Boolean = false
) {

Column(  
    modifier = if (compact) {  

        Modifier  
            .fillMaxWidth()  
            .height(130.dp)  
            .background(Color(0xFF0B1220))  
            .padding(8.dp)  

    } else {  

        Modifier  
            .width(170.dp)  
            .fillMaxHeight()  
            .background(Color(0xFF0B1220))  
            .padding(8.dp)  
    }  
) {  

    Text(  
        text = text(  
            language,  
            "SCENE",  
            "المشهد"  
        ),  
        color = Color(0xFF00E5FF),  
        fontSize = 18.sp  
    )  


    Spacer(  
        modifier = Modifier.height(5.dp)  
    )  


    Button(  
        onClick = onAddObject,  
        modifier = Modifier.fillMaxWidth()  
    ) {  

        Text(  
            text = text(  
                language,  
                "+ OBJECT",  
                "+ عنصر"  
            )  
        )  
    }  


    Spacer(  
        modifier = Modifier.height(5.dp)  
    )  


    LazyColumn {  

        items(  
            items = objects,  
            key = {  
                it.id  
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
                    .clickable {  
                        onSelect(obj.id)  
                    }  
                    .padding(7.dp)  
            )  
        }  
    }  
}

}

/* =========================
OBJECT DIALOG
========================= */

@Composable
fun AddObjectDialog(
language: String,
onCancel: () -> Unit,
onAdd: (String) -> Unit
) {

AlertDialog(  

    onDismissRequest = onCancel,  

    title = {  

        Text(  
            text = text(  
                language,  
                "Add Object",  
                "إضافة عنصر"  
            )  
        )  
    },  

    text = {  

        Column {  

            ObjectButton(  
                "Cube",  
                "مكعب",  
                language  
            ) {  
                onAdd("Cube")  
            }  


            ObjectButton(  
                "Sphere",  
                "كرة",  
                language  
            ) {  
                onAdd("Sphere")  
            }  


            ObjectButton(  
                "Camera",  
                "كاميرا",  
                language  
            ) {  
                onAdd("Camera")  
            }  


            ObjectButton(  
                "Light",  
                "ضوء",  
                language  
            ) {  
                onAdd("Light")  
            }  
        }  
    },  

    confirmButton = {  

        TextButton(  
            onClick = onCancel  
        ) {  

            Text(  
                text = text(  
                    language,  
                    "CANCEL",  
                    "إلغاء"  
                )  
            )  
        }  
    }  
)

}

/* =========================
OBJECT BUTTON
========================= */

@Composable
fun ObjectButton(
name: String,
arabicName: String,
language: String,
onClick: () -> Unit
) {

Button(  
    onClick = onClick,  
    modifier = Modifier.fillMaxWidth()  
) {  

    Text(  
        text = text(  
            language,  
            name,  
            arabicName  
        )  
    )  
}  


Spacer(  
    modifier = Modifier.height(5.dp)  
)

}

/* =========================
VIEWPORT
========================= */

@Composable
fun EditorViewport(
project: Project,
objects: List<GameObject>,
selectedObjectId: Int?,
selectedTool: String,
language: String,
onSelect: (Int) -> Unit,
onMove: (Int, Float, Float) -> Unit,
compact: Boolean = false
) {

Box(  
    modifier = if (compact) {  

        Modifier  
            .fillMaxWidth()  
            .height(300.dp)  
            .background(Color(0xFF182233))  
            .border(  
                1.dp,  
                Color(0xFF334155)  
            )  

    } else {  

        Modifier  
            .fillMaxHeight()  
            .weight(1f)  
            .background(Color(0xFF182233))  
            .border(  
                1.dp,  
                Color(0xFF334155)  
            )  
    }  
) {  

    Text(  
        text = text(  
            language,  
            "${project.type} VIEWPORT",  
            "نافذة ${project.type}"  
        ),  
        color = Color(0xFF00E5FF),  
        modifier = Modifier  
            .align(Alignment.TopCenter)  
            .padding(8.dp)  
    )  


    Text(  
        text = "┼",  
        color = Color(0xFF334155),  
        fontSize = 70.sp,  
        modifier = Modifier.align(  
            Alignment.Center  
        )  
    )  


    objects.forEach { obj ->  

        val isSelected =  
            obj.id == selectedObjectId  


        val size =  
            (48f * obj.scale)  
                .coerceIn(25f, 130f)  


        Box(  
            modifier = Modifier  
                .offset {  

                    IntOffset(  
                        obj.x.roundToInt(),  
                        obj.y.roundToInt()  
                    )  
                }  
                .width(size.dp)  
                .height(size.dp)  
                .background(  
                    if (isSelected) {  
                        Color(0xFF00E5FF)  
                    } else {  
                        Color(0xFF475569)  
                    }  
                )  
                .border(  
                    width =  
                        if (isSelected) {  
                            3.dp  
                        } else {  
                            1.dp  
                        },  
                    color = Color.White  
                )  
                .clickable {  

                    onSelect(obj.id)  
                }  
                .pointerInput(  
                    obj.id,  
                    selectedTool  
                ) {  

                    if (  
                        selectedTool == "MOVE"  
                    ) {  

                        detectDragGestures {  

                            change,  
                            dragAmount ->  

                            change.consume()  

                            onMove(  
                                obj.id,  
                                dragAmount.x,  
                                dragAmount.y  
                            )  
                        }  
                    }  
                }  
                .align(Alignment.Center)  
        ) {  

            Column(  
                modifier =  
                    Modifier.fillMaxSize(),  

                horizontalAlignment =  
                    Alignment.CenterHorizontally,  

                verticalArrangement =  
                    Arrangement.Center  
            ) {  

                Text(  
                    text = when (obj.type) {  

                        "Cube" -> "⬛"  

                        "Sphere" -> "●"  

                        "Camera" -> "📷"  

                        else -> "💡"  
                    },  

                    fontSize = 22.sp,  
                    color = Color.White  
                )  


                Text(  
                    text = obj.name,  
                    fontSize = 8.sp,  
                    color = Color.White  
                )  
            }  
        }  
    }  
}

}

/* =========================
INSPECTOR
========================= */

@Composable
fun InspectorPanel(
objects: List<GameObject>,
selectedObjectId: Int?,
language: String,
onDelete: (Int) -> Unit,
onDuplicate: (Int) -> Unit,
compact: Boolean = false
) {

val selectedObject =  
    objects.firstOrNull {  
        it.id == selectedObjectId  
    }  


Column(  
    modifier = if (compact) {  

        Modifier  
            .fillMaxWidth()  
            .height(170.dp)  
            .background(Color(0xFF0B1220))  
            .padding(10.dp)  

    } else {  

        Modifier  
            .width(190.dp)  
            .fillMaxHeight()  
            .background(Color(0xFF0B1220))  
            .padding(10.dp)  
    }  
) {  

    Text(  
        text = text(  
            language,  
            "INSPECTOR",  
            "الخصائص"  
        ),  
        color = Color(0xFF00E5FF),  
        fontSize = 18.sp  
    )  


    Spacer(  
        modifier = Modifier.height(8.dp)  
    )  


    if (selectedObject == null) {  

        Text(  
            text = text(  
                language,  
                "Select an object",  
                "حدد عنصرًا"  
            ),  
            color = Color.Gray  
        )  

    } else {  

        Text(  
            text = selectedObject.name,  
            color = Color.White,  
            fontSize = 17.sp  
        )  


        Text(  
            text = text(  
                language,  
                "Type: ${selectedObject.type}",  
                "النوع: ${selectedObject.type}"  
            ),  
            color = Color.Gray  
        )  


        Spacer(  
            modifier = Modifier.height(7.dp)  
        )  


        Text(  
            text = text(  
                language,  
                "Position",  
                "الموقع"  
            ),  
            color = Color.White  
        )  


        Text(  
            "X: ${  
                selectedObject.x.roundToInt()  
            }"  
        )  


        Text(  
            "Y: ${  
                selectedObject.y.roundToInt()  
            }"  
        )  


        Text(  
            text = text(  
                language,  
                "Rotation: ${selectedObject.rotation.roundToInt()}°",  
                "الدوران: ${selectedObject.rotation.roundToInt()}°"  
            )  
        )  


        Text(  
            text = text(  
                language,  
                "Scale: %.1f".format(  
                    selectedObject.scale  
                ),  
                "الحجم: %.1f".format(  
                    selectedObject.scale  
                )  
            )  
        )  


        Spacer(  
            modifier = Modifier.height(8.dp)  
        )  


        Row(  
            horizontalArrangement =  
                Arrangement.spacedBy(5.dp)  
        ) {  

            Button(  
                onClick = {  
                    onDuplicate(  
                        selectedObject.id  
                    )  
                }  
            ) {  

                Text(  
                    text = text(  
                        language,  
                        "DUP",  
                        "نسخ"  
                    )  
                )  
            }  


            OutlinedButton(  
                onClick = {  
                    onDelete(  
                        selectedObject.id  
                    )  
                }  
            ) {  

                Text(  
                    text = text(  
                        language,  
                        "DELETE",  
                        "حذف"  
                    )  
                )  
            }  
        }  
    }  
}
