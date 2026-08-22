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
                    text = projectType,
                    color = Color(0xFF00E5FF)
                )
            }
        },

        confirmButton = {

            Button(
                onClick = {

                    if (projectName.isNotEmpty()) {

                        onCreate(
                            projectName,
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
   EDITOR SCREEN
   ========================= */

@Composable
fun EditorScreen(
    project: Project,
    language: String,
    onLanguageChange: (String) -> Unit,
    onBack: () -> Unit
) {

    val context = androidx.compose.ui.platform.LocalContext.current
    var objects by remember { mutableStateOf(loadObjects(context, project)) }
    var selectedObject by remember { mutableStateOf<GameObject?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Button(onClick = onBack) {
                Text(text(language, "BACK", "رجوع"))
            }

            Text(
                text = project.name,
                color = Color.White,
                fontSize = 20.sp
            )

            OutlinedButton(
                onClick = {
                    onLanguageChange(if (language == "en") "ar" else "en")
                }
            ) {
                Text(if (language == "en") "العربية" else "English")
            }
        }

        Row(modifier = Modifier.fillMaxSize()) {

            // Canvas/Viewport
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF00E5FF))
                    .padding(16.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                        }
                    }
            ) {

                Text(
                    text = text(language, "Game Viewport", "منطقة اللعبة"),
                    color = Color.Gray,
                    fontSize = 16.sp
                )

                // Display game objects
                objects.forEach { obj ->
                    Box(
                        modifier = Modifier
                            .offset(obj.x.dp, obj.y.dp)
                            .width(50.dp)
                            .height(50.dp)
                            .background(
                                if (selectedObject?.id == obj.id)
                                    Color(0xFF00E5FF)
                                else
                                    Color(0xFF3B82F6)
                            )
                            .clickable { selectedObject = obj }
                    ) {
                        Text(
                            text = obj.name.take(3),
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }

            // Inspector Panel
            InspectorPanel(
                language = language,
                selectedObject = selectedObject,
                onObjectUpdate = { updated ->
                    objects = objects.map { if (it.id == updated.id) updated else it }
                    saveObjects(context, project, objects)
                },
                onAddObject = { showAddDialog = true }
            )
        }

        if (showAddDialog) {
            AddObjectDialog(
                language = language,
                nextId = (objects.maxByOrNull { it.id }?.id ?: 0) + 1,
                onCancel = { showAddDialog = false },
                onAdd = { name, type ->
                    val newObject = GameObject(
                        id = (objects.maxByOrNull { it.id }?.id ?: 0) + 1,
                        name = name,
                        type = type
                    )
                    objects = objects + newObject
                    saveObjects(context, project, objects)
                    showAddDialog = false
                }
            )
        }
    }
}


/* =========================
   INSPECTOR PANEL
   ========================= */

@Composable
fun InspectorPanel(
    language: String,
    selectedObject: GameObject?,
    onObjectUpdate: (GameObject) -> Unit,
    onAddObject: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(Color(0xFF1E293B))
            .padding(16.dp)
    ) {

        Text(
            text = text(language, "PROPERTIES", "الخصائص"),
            color = Color(0xFF00E5FF),
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedObject != null) {

            var name by remember { mutableStateOf(selectedObject.name) }
            var x by remember { mutableStateOf(selectedObject.x.toString()) }
            var y by remember { mutableStateOf(selectedObject.y.toString()) }
            var rotation by remember { mutableStateOf(selectedObject.rotation.toString()) }
            var scale by remember { mutableStateOf(selectedObject.scale.toString()) }

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = x,
                        onValueChange = { x = it },
                        label = { Text("X") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = y,
                        onValueChange = { y = it },
                        label = { Text("Y") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = rotation,
                        onValueChange = { rotation = it },
                        label = { Text("Rotation") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = scale,
                        onValueChange = { scale = it },
                        label = { Text("Scale") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Button(
                        onClick = {
                            onObjectUpdate(
                                selectedObject.copy(
                                    name = name,
                                    x = x.toFloatOrNull() ?: selectedObject.x,
                                    y = y.toFloatOrNull() ?: selectedObject.y,
                                    rotation = rotation.toFloatOrNull() ?: selectedObject.rotation,
                                    scale = scale.toFloatOrNull() ?: selectedObject.scale
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text(language, "UPDATE", "تحديث"))
                    }
                }
            }

        } else {

            Text(
                text = text(language, "No object selected", "لم يتم اختيار كائن"),
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onAddObject,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text(language, "ADD OBJECT", "إضافة كائن"))
        }
    }
}


/* =========================
   ADD OBJECT DIALOG
   ========================= */

@Composable
fun AddObjectDialog(
    language: String,
    nextId: Int,
    onCancel: () -> Unit,
    onAdd: (String, String) -> Unit
) {

    var objectName by remember { mutableStateOf("") }
    var objectType by remember { mutableStateOf("Sprite") }

    AlertDialog(

        onDismissRequest = onCancel,

        title = {
            Text(text(language, "Add Object", "إضافة كائن"))
        },

        text = {
            Column {

                OutlinedTextField(
                    value = objectName,
                    onValueChange = { objectName = it },
                    label = { Text(text(language, "Object Name", "اسم الكائن")) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text(language, "Object Type", "نوع الكائن"))

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    Button(
                        onClick = { objectType = "Sprite" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sprite")
                    }

                    Button(
                        onClick = { objectType = "Camera" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Camera")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    Button(
                        onClick = { objectType = "Light" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Light")
                    }

                    Button(
                        onClick = { objectType = "Empty" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Empty")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = objectType,
                    color = Color(0xFF00E5FF)
                )
            }
        },

        confirmButton = {

            Button(
                onClick = {
                    if (objectName.isNotEmpty()) {
                        onAdd(objectName, objectType)
                    }
                }
            ) {
                Text(text(language, "ADD", "إضافة"))
            }
        },

        dismissButton = {

            TextButton(onClick = onCancel) {
                Text(text(language, "CANCEL", "إلغاء"))
            }
        }
    )
}
