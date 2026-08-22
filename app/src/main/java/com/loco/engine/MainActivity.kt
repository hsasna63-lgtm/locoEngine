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
    val type: String,
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val positionZ: Float = 0f,
    val rotationX: Float = 0f,
    val rotationY: Float = 0f,
    val rotationZ: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val scaleZ: Float = 1f
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


/* =========================================================
   PROJECT OBJECT SAVE / LOAD
   ========================================================= */

fun sceneKey(project: Project): String {

    return "scene_" +
            project.name +
            "_" +
            project.type
}


fun saveScene(
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

        item.put("positionX", obj.positionX)
        item.put("positionY", obj.positionY)
        item.put("positionZ", obj.positionZ)

        item.put("rotationX", obj.rotationX)
        item.put("rotationY", obj.rotationY)
        item.put("rotationZ", obj.rotationZ)

        item.put("scaleX", obj.scaleX)
        item.put("scaleY", obj.scaleY)
        item.put("scaleZ", obj.scaleZ)

        array.put(item)
    }

    context
        .getSharedPreferences(
            "loco_engine",
            Context.MODE_PRIVATE
        )
        .edit()
        .putString(
            sceneKey(project),
            array.toString()
        )
        .apply()
}


fun loadScene(
    context: Context,
    project: Project
): List<GameObject> {

    val data =
        context
            .getSharedPreferences(
                "loco_engine",
                Context.MODE_PRIVATE
            )
            .getString(
                sceneKey(project),
                null
            )

    if (data == null) {

        return listOf(

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

                    positionX =
                        item.optDouble(
                            "positionX",
                            0.0
                        ).toFloat(),

                    positionY =
                        item.optDouble(
                            "positionY",
                            0.0
                        ).toFloat(),

                    positionZ =
                        item.optDouble(
                            "positionZ",
                            0.0
                        ).toFloat(),

                    rotationX =
                        item.optDouble(
                            "rotationX",
                            0.0
                        ).toFloat(),

                    rotationY =
                        item.optDouble(
                            "rotationY",
                            0.0
                        ).toFloat(),

                    rotationZ =
                        item.optDouble(
                            "rotationZ",
                            0.0
                        ).toFloat(),

                    scaleX =
                        item.optDouble(
                            "scaleX",
                            1.0
                        ).toFloat(),

                    scaleY =
                        item.optDouble(
                            "scaleY",
                            1.0
                        ).toFloat(),

                    scaleZ =
                        item.optDouble(
                            "scaleZ",
                            1.0
                        ).toFloat()
                )
            )
        }

        result

    } catch (e: Exception) {

        listOf(

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
}


/* =========================================================
   MAIN APP
   ========================================================= */

@Composable
fun LocoEngineApp(
    savedProjects: List<Project>,
    initialLanguage: String
) {

    val context =
        androidx.compose.ui.platform.LocalContext.current

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

            onLanguageChange = { newLanguage ->

                language = newLanguage

                saveLanguage(
                    context,
                    newLanguage
                )
            },

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


/* =========================================================
   HOME
   ========================================================= */

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


/* =========================================================
   PROJECT CARD
   ========================================================= */

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


/* =========================================================
   CREATE PROJECT
   ========================================================= */

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


/* =========================================================
   EDITOR
   ========================================================= */

@Composable
fun EditorScreen(
    project: Project,
    language: String,
    onLanguageChange: (String) -> Unit,
    onBack: () -> Unit
) {

    val context =
        androidx.compose.ui.platform.LocalContext.current

    val objects = remember(project) {

        mutableStateListOf<GameObject>().apply {

            addAll(
                loadScene(
                    context,
                    project
                )
            )
        }
    }

    var selectedObjectId by remember {
        mutableStateOf<Int?>(null)
    }

    var selectedTool by remember {
        mutableStateOf("SELECT")
    }

    var showObjectDialog by remember {
        mutableStateOf(false)
    }

    var showRenameDialog by remember {
        mutableStateOf(false)
    }

    var objectToRenameId by remember {
        mutableStateOf<Int?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827))
    ) {

        EditorTopBar(
            project = project,
            language = language,
            onLanguageChange = onLanguageChange,

            onSave = {

                saveScene(
                    context,
                    project,
                    objects.toList()
                )
            },

            onBack = {

                saveScene(
                    context,
                    project,
                    objects.toList()
                )

                onBack()
            }
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
                    showObjectDialog = true
                },

                onRename = { id ->

                    objectToRenameId = id
                    showRenameDialog = true
                },

                onDelete = { id ->

                    objects.removeAll {
                        it.id == id
                    }

                    if (selectedObjectId == id) {
                        selectedObjectId = null
                    }
                }
            )

            EditorViewport(
                project = project,
                objects = objects,
                selectedObjectId = selectedObjectId,
                selectedTool = selectedTool,
                language = language,

                onSelectObject = {
                    selectedObjectId = it
                }
            )

            InspectorPanel(
                objects = objects,
                selectedObjectId = selectedObjectId,
                language = language,

                onObjectChange = { changedObject ->

                    val index =
                        objects.indexOfFirst {
                            it.id == changedObject.id
                        }

                    if (index >= 0) {
                        objects[index] = changedObject
                    }
                }
            )
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
                    (objects.maxOfOrNull {
                        it.id
                    } ?: 0) + 1

                val objectName = when (objectType) {

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
                        type = objectType
                    )
                )

                selectedObjectId = newId

                showObjectDialog = false
            }
        )
    }

    if (showRenameDialog) {

        val objectToRename =
            objects.firstOrNull {
                it.id == objectToRenameId
            }

        if (objectToRename != null) {

            RenameObjectDialog(
                language = language,
                currentName = objectToRename.name,

                onCancel = {
                    showRenameDialog = false
                    objectToRenameId = null
                },

                onRename = { newName ->

                    val index =
                        objects.indexOfFirst {
                            it.id == objectToRename.id
                        }

                    if (index >= 0) {

                        objects[index] =
                            objectToRename.copy(
                                name = newName
                            )
                    }

                    showRenameDialog = false
                    objectToRenameId = null
                }
            )
        }
    }
}


/* =========================================================
   TOP BAR
   ========================================================= */

@Composable
fun EditorTopBar(
    project: Project,
    language: String,
    onLanguageChange: (String) -> Unit,
    onSave: () -> Unit,
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
                Arrangement.spacedBy(3.dp)
        ) {

            TextButton(
                onClick = onSave
            ) {

                Text(
                    text = text(
                        language,
                        "SAVE",
                        "حفظ"
                    )
                )
            }

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


/* =========================================================
   TOOL BAR
   ========================================================= */

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

            val translated = when (tool) {

                "SELECT" -> text(
                    language,
                    "SELECT",
                    "تحديد"
                )

                "MOVE" -> text(
                    language,
                    "MOVE",
                    "تحريك"
                )

                "ROTATE" -> text(
                    language,
                    "ROTATE",
                    "تدوير"
                )

                else -> text(
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
}


/* =========================================================
   SCENE TREE
   ========================================================= */

@Composable
fun SceneTree(
    objects: List<GameObject>,
    selectedObjectId: Int?,
    language: String,
    onSelect: (Int) -> Unit,
    onAddObject: () -> Unit,
    onRename: (Int) -> Unit,
    onDelete: (Int) -> Unit
) {

    Column(
        modifier = Modifier
            .width(170.dp)
            .fillMaxSize()
            .background(Color(0xFF0B1220))
            .padding(8.dp)
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
            modifier = Modifier.height(8.dp)
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
            modifier = Modifier.height(8.dp)
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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (selected) {
                                Color(0xFF163B4A)
                            } else {
                                Color.Transparent
                            }
                        )
                        .clickable {
                            onSelect(obj.id)
                        }
                        .padding(7.dp)
                ) {

                    Text(
                        text = obj.name,

                        color =
                            if (selected) {
                                Color(0xFF00E5FF)
                            } else {
                                Color.White
                            }
                    )

                    Text(
                        text = obj.type,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(3.dp)
                    ) {

                        TextButton(
                            onClick = {
                                onRename(obj.id)
                            }
                        ) {

                            Text(
                                text = text(
                                    language,
                                    "Rename",
                                    "تسمية"
                                ),
                                fontSize = 10.sp
                            )
                        }

                        TextButton(
                            onClick = {
                                onDelete(obj.id)
                            }
                        ) {

                            Text(
                                text = text(
                                    language,
                                    "Delete",
                                    "حذف"
                                ),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


/* =========================================================
   ADD OBJECT
   ========================================================= */

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
                    name = "Cube",
                    arabicName = "مكعب",
                    language = language,
                    onClick = {
                        onAdd("Cube")
                    }
                )

                ObjectButton(
                    name = "Sphere",
                    arabicName = "كرة",
                    language = language,
                    onClick = {
                        onAdd("Sphere")
                    }
                )

                ObjectButton(
                    name = "Camera",
                    arabicName = "كاميرا",
                    language = language,
                    onClick = {
                        onAdd("Camera")
                    }
                )

                ObjectButton(
                    name = "Light",
                    arabicName = "ضوء",
                    language = language,
                    onClick = {
                        onAdd("Light")
                    }
                )
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
        modifier = Modifier.height(6.dp)
    )
}


/* =========================================================
   VIEWPORT
   ========================================================= */

@Composable
fun EditorViewport(
    project: Project,
    objects: List<GameObject>,
    selectedObjectId: Int?,
    selectedTool: String,
    language: String,
    onSelectObject: (Int) -> Unit
) {

    Box(
        modifier = Modifier
            .width(300.dp)
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
                .padding(10.dp)
        ) {

            Text(
                text = text(
                    language,
                    "${project.type} VIEWPORT",
                    "نافذة ${project.type}"
                ),
                color = Color(0xFF00E5FF),
                fontSize = 19.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = text(
                    language,
                    "GRID / WORKSPACE",
                    "شبكة / مساحة العمل"
                ),
                color = Color(0xFF64748B),
                fontSize = 12.sp
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color(0xFF111827))
                    .border(
                        width = 1.dp,
                        color = Color(0xFF334155)
                    )
            ) {

                Text(
                    text = "+",
                    modifier = Modifier
                        .align(Alignment.Center),
                    color = Color(0xFF00E5FF),
                    fontSize = 35.sp
                )

                Text(
                    text = "X  ───────── Z",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp),
                    color = Color(0xFF475569),
                    fontSize = 12.sp
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),

                    verticalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {

                    objects.forEach { obj ->

                        val isSelected =
                            obj.id == selectedObjectId

                        val objectColor =
                            if (isSelected) {
                                Color(0xFF00E5FF)
                            } else {
                                Color(0xFF334155)
                            }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    objectColor
                                )
                                .border(
                                    width = if (isSelected) {
                                        2.dp
                                    } else {
                                        1.dp
                                    },
                                    color = if (isSelected) {
                                        Color.White
                                    } else {
                                        Color(0xFF64748B)
                                    }
                                )
                                .clickable {
                                    onSelectObject(
                                        obj.id
                                    )
                                }
                                .padding(8.dp)
                        ) {

                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),

                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {

                                Text(
                                    text = obj.name,
                                    color = if (isSelected) {
                                        Color.Black
                                    } else {
                                        Color.White
                                    },
                                    fontSize = 13.sp
                                )

                                Text(
                                    text = obj.type,
                                    color = if (isSelected) {
                                        Color.DarkGray
                                    } else {
                                        Color.LightGray
                                    },
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = text(
                    language,
                    "Tool: $selectedTool",
                    "الأداة: $selectedTool"
                ),
                color = Color.White
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = text(
                    language,
                    "Tap an object to select it",
                    "اضغط على عنصر لتحديده"
                ),
                color = Color.Gray,
                fontSize = 12.sp
            )
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
    language: String,
    onObjectChange: (GameObject) -> Unit
) {

    val selectedObject =
        objects.firstOrNull {
            it.id == selectedObjectId
        }

    Column(
        modifier = Modifier
            .width(190.dp)
            .fillMaxSize()
            .background(Color(0xFF0B1220))
            .padding(10.dp)
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
            modifier = Modifier.height(12.dp)
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

            Spacer(
                modifier = Modifier.height(5.dp)
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
                modifier = Modifier.height(15.dp)
            )

            Text(
                text = text(
                    language,
                    "TRANSFORM",
                    "التحويل"
                ),
                color = Color(0xFF00E5FF)
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = text(
                    language,
                    "POSITION",
                    "الموقع"
                ),
                color = Color.White
            )

            NumberField(
                label = "X",
                value = selectedObject.positionX,

                onChange = {

                    onObjectChange(
                        selectedObject.copy(
                            positionX = it
                        )
                    )
                }
            )

            NumberField(
                label = "Y",
                value = selectedObject.positionY,

                onChange = {

                    onObjectChange(
                        selectedObject.copy(
                            positionY = it
                        )
                    )
                }
            )

            NumberField(
                label = "Z",
                value = selectedObject.positionZ,

                onChange = {

                    onObjectChange(
                        selectedObject.copy(
                            positionZ = it
                        )
                    )
                }
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = text(
                    language,
                    "ROTATION",
                    "الدوران"
                ),
                color = Color.White
            )

            NumberField(
                label = "X",
                value = selectedObject.rotationX,

                onChange = {

                    onObjectChange(
                        selectedObject.copy(
                            rotationX = it
                        )
                    )
                }
            )

            NumberField(
                label = "Y",
                value = selectedObject.rotationY,

                onChange = {

                    onObjectChange(
                        selectedObject.copy(
                            rotationY = it
                        )
                    )
                }
            )

            NumberField(
                label = "Z",
                value = selectedObject.rotationZ,

                onChange = {

                    onObjectChange(
                        selectedObject.copy(
                            rotationZ = it
                        )
                    )
                }
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = text(
                    language,
                    "SCALE",
                    "الحجم"
                ),
                color = Color.White
            )

            NumberField(
                label = "X",
                value = selectedObject.scaleX,

                onChange = {

                    onObjectChange(
                        selectedObject.copy(
                            scaleX = it
                        )
                    )
                }
            )

            NumberField(
                label = "Y",
                value = selectedObject.scaleY,

                onChange = {

                    onObjectChange(
                        selectedObject.copy(
                            scaleY = it
                        )
                    )
                }
            )

            NumberField(
                label = "Z",
                value = selectedObject.scaleZ,

                onChange = {

                    onObjectChange(
                        selectedObject.copy(
                            scaleZ = it
                        )
                    )
                }
            )
        }
    }
}


/* =========================================================
   NUMBER FIELD
   ========================================================= */

@Composable
fun NumberField(
    label: String,
    value: Float,
    onChange: (Float) -> Unit
) {

    var valueText by remember(
        value,
        label
    ) {

        mutableStateOf(
            value.toString()
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = label,
            color = Color.LightGray,
            modifier = Modifier.width(22.dp)
        )

        OutlinedTextField(
            value = valueText,

            onValueChange = { newText ->

                valueText = newText

                val number =
                    newText.toFloatOrNull()

                if (number != null) {
                    onChange(number)
                }
            },

            modifier = Modifier.fillMaxWidth(),

            singleLine = true
        )
    }
}


/* =========================================================
   RENAME
   ========================================================= */

@Composable
fun RenameObjectDialog(
    language: String,
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

            Text(
                text = text(
                    language,
                    "Rename Object",
                    "إعادة تسمية العنصر"
                )
            )
        },

        text = {

            OutlinedTextField(
                value = newName,

                onValueChange = {
                    newName = it
                },

                label = {

                    Text(
                        text = text(
                            language,
                            "Name",
                            "الاسم"
                        )
                    )
                },

                singleLine = true
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

                Text(
                    text = text(
                        language,
                        "RENAME",
                        "تسمية"
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
