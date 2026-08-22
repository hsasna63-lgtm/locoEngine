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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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


/* ============================================================
   DATA
   ============================================================ */

data class Project(
    val name: String,
    val type: String
)


data class GameObject(
    val id: Int,
    val name: String,
    val type: String,

    val posX: Float = 0f,
    val posY: Float = 0f,
    val posZ: Float = 0f,

    val rotX: Float = 0f,
    val rotY: Float = 0f,
    val rotZ: Float = 0f,

    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val scaleZ: Float = 1f
)


/* ============================================================
   TEXT / LANGUAGE
   ============================================================ */

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


/* ============================================================
   PROJECT SAVE / LOAD
   ============================================================ */

fun loadProjects(context: Context): List<Project> {

    val preferences =
        context.getSharedPreferences(
            "loco_engine",
            Context.MODE_PRIVATE
        )

    val data =
        preferences.getString(
            "projects",
            null
        ) ?: return emptyList()

    return try {

        val array = JSONArray(data)

        val result = mutableListOf<Project>()

        for (i in 0 until array.length()) {

            val item =
                array.getJSONObject(i)

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

        item.put(
            "name",
            project.name
        )

        item.put(
            "type",
            project.type
        )

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


/* ============================================================
   LANGUAGE SAVE / LOAD
   ============================================================ */

fun loadLanguage(
    context: Context
): String {

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


/* ============================================================
   OBJECT SAVE / LOAD
   ============================================================ */

fun sceneKey(
    project: Project
): String {

    return "scene_" +
            project.name +
            "_" +
            project.type
}


fun defaultObjects(): List<GameObject> {

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


fun loadObjects(
    context: Context,
    project: Project
): List<GameObject> {

    val preferences =
        context.getSharedPreferences(
            "loco_engine",
            Context.MODE_PRIVATE
        )

    val data =
        preferences.getString(
            sceneKey(project),
            null
        )

    if (data == null) {
        return defaultObjects()
    }

    return try {

        val array = JSONArray(data)

        val result =
            mutableListOf<GameObject>()

        for (i in 0 until array.length()) {

            val item =
                array.getJSONObject(i)

            result.add(

                GameObject(

                    id = item.getInt("id"),

                    name = item.getString(
                        "name"
                    ),

                    type = item.getString(
                        "type"
                    ),

                    posX = item.optDouble(
                        "posX",
                        0.0
                    ).toFloat(),

                    posY = item.optDouble(
                        "posY",
                        0.0
                    ).toFloat(),

                    posZ = item.optDouble(
                        "posZ",
                        0.0
                    ).toFloat(),

                    rotX = item.optDouble(
                        "rotX",
                        0.0
                    ).toFloat(),

                    rotY = item.optDouble(
                        "rotY",
                        0.0
                    ).toFloat(),

                    rotZ = item.optDouble(
                        "rotZ",
                        0.0
                    ).toFloat(),

                    scaleX = item.optDouble(
                        "scaleX",
                        1.0
                    ).toFloat(),

                    scaleY = item.optDouble(
                        "scaleY",
                        1.0
                    ).toFloat(),

                    scaleZ = item.optDouble(
                        "scaleZ",
                        1.0
                    ).toFloat()
                )
            )
        }

        if (result.isEmpty()) {
            defaultObjects()
        } else {
            result
        }

    } catch (e: Exception) {

        defaultObjects()
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

        item.put(
            "id",
            obj.id
        )

        item.put(
            "name",
            obj.name
        )

        item.put(
            "type",
            obj.type
        )

        item.put(
            "posX",
            obj.posX
        )

        item.put(
            "posY",
            obj.posY
        )

        item.put(
            "posZ",
            obj.posZ
        )

        item.put(
            "rotX",
            obj.rotX
        )

        item.put(
            "rotY",
            obj.rotY
        )

        item.put(
            "rotZ",
            obj.rotZ
        )

        item.put(
            "scaleX",
            obj.scaleX
        )

        item.put(
            "scaleY",
            obj.scaleY
        )

        item.put(
            "scaleZ",
            obj.scaleZ
        )

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


/* ============================================================
   MAIN APP
   ============================================================ */

@Composable
fun LocoEngineApp(
    savedProjects: List<Project>,
    initialLanguage: String
) {

    val context =
        LocalContext.current

    val projects =
        remember {

            mutableStateListOf<Project>().apply {
                addAll(savedProjects)
            }
        }

    var language by rememberSaveable {
        mutableStateOf(
            initialLanguage
        )
    }

    /*
       مهم جدًا:
       نحفظ اسم ونوع المشروع فقط.
       rememberSaveable يعيدهما بعد تدوير الهاتف.
    */

    var openedProjectName by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var openedProjectType by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val openedProject =
        if (
            openedProjectName != null &&
            openedProjectType != null
        ) {

            projects.firstOrNull {

                it.name == openedProjectName &&
                        it.type == openedProjectType
            }

        } else {

            null
        }


    if (openedProject != null) {

        EditorScreen(

            project = openedProject,

            language = language,

            onLanguageChange = {

                language = it

                saveLanguage(
                    context,
                    it
                )
            },

            onBack = {

                openedProjectName = null
                openedProjectType = null
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

        onCreateProject = {
                name,
                type ->

            val project =
                Project(
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

            openedProjectName =
                it.name

            openedProjectType =
                it.type
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


/* ============================================================
   HOME
   ============================================================ */

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
                .background(
                    Color(0xFF0F172A)
                )
                .padding(24.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.End
            ) {

                OutlinedButton(

                    onClick = {

                        if (language == "en") {

                            onLanguageChange(
                                "ar"
                            )

                        } else {

                            onLanguageChange(
                                "en"
                            )
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
                modifier =
                    Modifier.height(25.dp)
            )


            Text(

                text = "LOCO ENGINE",

                color =
                    Color(0xFF00E5FF),

                fontSize = 32.sp
            )


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            Text(

                text =
                    text(
                        language,
                        "Mobile Game Engine",
                        "محرك ألعاب للهواتف"
                    ),

                color = Color.White,

                fontSize = 18.sp
            )


            Spacer(
                modifier =
                    Modifier.height(30.dp)
            )


            Button(

                onClick = {
                    showCreateDialog = true
                }
            ) {

                Text(

                    text =
                        text(
                            language,
                            "CREATE PROJECT",
                            "إنشاء مشروع"
                        )
                )
            }


            Spacer(
                modifier =
                    Modifier.height(30.dp)
            )


            Text(

                text =
                    text(
                        language,
                        "PROJECTS",
                        "المشاريع"
                    ),

                color =
                    Color(0xFF00E5FF),

                fontSize = 20.sp
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            if (projects.isEmpty()) {

                Text(

                    text =
                        text(
                            language,
                            "No projects yet",
                            "لا توجد مشاريع حتى الآن"
                        ),

                    color = Color.Gray
                )

            } else {

                LazyColumn(

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    items(
                        projects,
                        key = {
                            it.name + it.type
                        }
                    ) { project ->

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

                onCreate = {
                        name,
                        type ->

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


/* ============================================================
   PROJECT CARD
   ============================================================ */

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

        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFF1E293B)
                )
                .padding(14.dp)
    ) {

        Text(

            text = project.name,

            color = Color.White,

            fontSize = 20.sp
        )


        Spacer(
            modifier =
                Modifier.height(5.dp)
        )


        Text(

            text =
                text(
                    language,
                    "Type: ${project.type}",
                    "النوع: ${project.type}"
                ),

            color =
                Color.LightGray
        )


        Spacer(
            modifier =
                Modifier.height(10.dp)
        )


        Row(

            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = onOpen
            ) {

                Text(
                    text =
                        text(
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
                    text =
                        text(
                            language,
                            "DELETE",
                            "حذف"
                        )
                )
            }
        }
    }


    Spacer(
        modifier =
            Modifier.height(8.dp)
    )


    if (showDeleteDialog) {

        AlertDialog(

            onDismissRequest = {
                showDeleteDialog = false
            },

            title = {

                Text(
                    text =
                        text(
                            language,
                            "Delete Project",
                            "حذف المشروع"
                        )
                )
            },

            text = {

                Text(
                    text =
                        text(
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
                        text =
                            text(
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
                        text =
                            text(
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


/* ============================================================
   CREATE PROJECT
   ============================================================ */

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
                text =
                    text(
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
                            text =
                                text(
                                    language,
                                    "Project Name",
                                    "اسم المشروع"
                                )
                        )
                    }
                )


                Spacer(
                    modifier =
                        Modifier.height(15.dp)
                )


                Text(
                    text =
                        text(
                            language,
                            "Project Type",
                            "نوع المشروع"
                        )
                )


                Spacer(
                    modifier =
                        Modifier.height(8.dp)
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
                    modifier =
                        Modifier.height(8.dp)
                )


                Text(

                    text =
                        text(
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
                    text =
                        text(
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
                    text =
                        text(
                            language,
                            "CANCEL",
                            "إلغاء"
                        )
                )
            }
        }
    )
}


/* ============================================================
   EDITOR
   ============================================================ */

@Composable
fun EditorScreen(
    project: Project,
    language: String,
    onLanguageChange: (String) -> Unit,
    onBack: () -> Unit
) {

    val context =
        LocalContext.current


    /*
       يتم تحميل المشهد من SharedPreferences.
       لذلك عند تدوير الهاتف لن تضيع العناصر.
    */

    val objects =
        remember(
            project.name,
            project.type
        ) {

            mutableStateListOf<GameObject>().apply {

                addAll(
                    loadObjects(
                        context,
                        project
                    )
                )
            }
        }


    var selectedObjectId by rememberSaveable(
        project.name,
        project.type
    ) {

        mutableStateOf<Int?>(null)
    }


    var selectedTool by rememberSaveable(
        project.name,
        project.type
    ) {

        mutableStateOf("SELECT")
    }


    var showObjectDialog by remember {
        mutableStateOf(false)
    }


    fun saveScene() {

        saveObjects(
            context,
            project,
            objects.toList()
        )
    }


    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color(0xFF111827)
                )
    ) {

        EditorTopBar(

            project = project,

            language = language,

            onLanguageChange =
                onLanguageChange,

            onBack = onBack
        )


        EditorToolBar(

            selectedTool =
                selectedTool,

            language = language,

            onToolSelected = {
                selectedTool = it
            }
        )


        Row(

            modifier =
                Modifier.fillMaxSize()
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

                onDeleteObject = {

                    objects.removeAll {
                        it.id == selectedObjectId
                    }

                    selectedObjectId = null

                    saveScene()
                },

                onDuplicateObject = {

                    val selected =
                        objects.firstOrNull {
                            it.id ==
                                    selectedObjectId
                        }

                    if (selected != null) {

                        val newId =
                            (
                                objects.maxOfOrNull {
                                    it.id
                                } ?: 0
                            ) + 1

                        val copy =
                            selected.copy(
                                id = newId,
                                name =
                                    selected.name +
                                            " Copy"
                            )

                        objects.add(copy)

                        selectedObjectId =
                            newId

                        saveScene()
                    }
                }
            )


            EditorViewport(

                project = project,

                objects = objects,

                selectedObjectId =
                    selectedObjectId,

                selectedTool =
                    selectedTool,

                language = language,

                onSelectObject = {
                    selectedObjectId = it
                }
            )


            InspectorPanel(

                objects = objects,

                selectedObjectId =
                    selectedObjectId,

                language = language,

                selectedTool =
                    selectedTool,

                onObjectChanged = { changed ->

                    val index =
                        objects.indexOfFirst {
                            it.id == changed.id
                        }

                    if (index >= 0) {

                        objects[index] =
                            changed

                        saveScene()
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

            onAdd = {

                objectType ->

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

                        type = objectType
                    )
                )


                selectedObjectId =
                    newId


                saveScene()

                showObjectDialog =
                    false
            }
        )
    }
}


/* ============================================================
   TOP BAR
   ============================================================ */

@Composable
fun EditorTopBar(
    project: Project,
    language: String,
    onLanguageChange: (String) -> Unit,
    onBack: () -> Unit
) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFF0F172A)
                )
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
                text =
                    text(
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

                color =
                    Color(0xFF00E5FF)
            )


            TextButton(

                onClick = {

                    if (language == "en") {

                        onLanguageChange(
                            "ar"
                        )

                    } else {

                        onLanguageChange(
                            "en"
                        )
                    }
                }
            ) {

                Text(

                    text =
                        if (language == "en") {
                            "ع"
                        } else {
                            "EN"
                        }
                )
            }
        }
    }
}


/* ============================================================
   TOOL BAR
   ============================================================ */

@Composable
fun EditorToolBar(
    selectedTool: String,
    language: String,
    onToolSelected: (String) -> Unit
) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFF1E293B)
                )
                .padding(6.dp),

        horizontalArrangement =
            Arrangement.spacedBy(5.dp)
    ) {

        val tools =
            listOf(
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
}


/* ============================================================
   SCENE TREE
   ============================================================ */

@Composable
fun SceneTree(
    objects: List<GameObject>,
    selectedObjectId: Int?,
    language: String,
    onSelect: (Int) -> Unit,
    onAddObject: () -> Unit,
    onDeleteObject: () -> Unit,
    onDuplicateObject: () -> Unit
) {

    Column(

        modifier =
            Modifier
                .width(165.dp)
                .fillMaxSize()
                .background(
                    Color(0xFF0B1220)
                )
                .padding(8.dp)
    ) {

        Text(

            text =
                text(
                    language,
                    "SCENE",
                    "المشهد"
                ),

            color =
                Color(0xFF00E5FF),

            fontSize = 18.sp
        )


        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        Button(

            onClick = onAddObject,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(

                text =
                    text(
                        language,
                        "+ OBJECT",
                        "+ عنصر"
                    )
            )
        }


        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        LazyColumn(

            modifier =
                Modifier.weight(1f)
        ) {

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

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(obj.id)
                            }
                            .padding(10.dp)
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(5.dp)
        )


        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(4.dp)
        ) {

            OutlinedButton(

                onClick = onDuplicateObject,

                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        text(
                            language,
                            "COPY",
                            "نسخ"
                        ),

                    fontSize = 11.sp
                )
            }


            OutlinedButton(

                onClick = onDeleteObject,

                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        text(
                            language,
                            "DEL",
                            "حذف"
                        ),

                    fontSize = 11.sp
                )
            }
        }
    }
}


/* ============================================================
   ADD OBJECT DIALOG
   ============================================================ */

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

                text =
                    text(
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

                    text =
                        text(
                            language,
                            "CANCEL",
                            "إلغاء"
                        )
                )
            }
        }
    )
}


/* ============================================================
   OBJECT BUTTON
   ============================================================ */

@Composable
fun ObjectButton(
    name: String,
    arabicName: String,
    language: String,
    onClick: () -> Unit
) {

    Button(

        onClick = onClick,

        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(

            text =
                text(
                    language,
                    name,
                    arabicName
                )
        )
    }


    Spacer(
        modifier =
            Modifier.height(6.dp)
    )
}


/* ============================================================
   VIEWPORT
   ============================================================ */

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

        modifier =
            Modifier
                .weight(1f)
                .fillMaxSize()
                .background(
                    Color(0xFF182233)
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFF334155)
                )
    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(12.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(

                text =
                    text(
                        language,
                        "${project.type} VIEWPORT",
                        "نافذة ${project.type}"
                    ),

                color =
                    Color(0xFF00E5FF),

                fontSize = 20.sp
            )


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            Text(

                text =
                    text(
                        language,
                        "Scene Grid",
                        "شبكة المشهد"
                    ),

                color =
                    Color(0xFF64748B)
            )


            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )


            Text(

                text =
                    "X  ─────────────  Z",

                color =
                    Color(0xFF64748B),

                fontSize = 15.sp
            )


            Spacer(
                modifier =
                    Modifier.height(15.dp)
            )


            LazyColumn(

                modifier =
                    Modifier.fillMaxSize(),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                items(

                    items = objects,

                    key = {
                        it.id
                    }

                ) { obj ->

                    val isSelected =
                        obj.id ==
                                selectedObjectId


                    val objectText =
                        "${obj.name}\n" +
                                "P(${obj.posX}, ${obj.posY}, ${obj.posZ})"


                    Box(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .background(

                                    if (isSelected) {
                                        Color(0xFF263B55)
                                    } else {
                                        Color(0xFF1E293B)
                                    }
                                )
                                .border(

                                    width = 1.dp,

                                    color =
                                        if (isSelected) {
                                            Color(0xFF00E5FF)
                                        } else {
                                            Color(0xFF334155)
                                        }
                                )
                                .clickable {
                                    onSelectObject(obj.id)
                                }
                                .padding(10.dp)
                    ) {

                        Column {

                            Text(

                                text = objectText,

                                color =
                                    if (isSelected) {
                                        Color(0xFF00E5FF)
                                    } else {
                                        Color.White
                                    }
                            )


                            if (isSelected) {

                                Spacer(
                                    modifier =
                                        Modifier.height(4.dp)
                                )


                                Text(

                                    text =
                                        text(
                                            language,
                                            "Tool: $selectedTool",
                                            "الأداة: $selectedTool"
                                        ),

                                    color =
                                        Color.LightGray,

                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


/* ============================================================
   INSPECTOR
   ============================================================ */

@Composable
fun InspectorPanel(
    objects: List<GameObject>,
    selectedObjectId: Int?,
    language: String,
    selectedTool: String,
    onObjectChanged: (GameObject) -> Unit
) {

    val selectedObject =
        objects.firstOrNull {
            it.id == selectedObjectId
        }


    Column(

        modifier =
            Modifier
                .width(205.dp)
                .fillMaxSize()
                .background(
                    Color(0xFF0B1220)
                )
                .padding(10.dp)
    ) {

        Text(

            text =
                text(
                    language,
                    "INSPECTOR",
                    "الخصائص"
                ),

            color =
                Color(0xFF00E5FF),

            fontSize = 18.sp
        )


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        if (selectedObject == null) {

            Text(

                text =
                    text(
                        language,
                        "Select an object",
                        "حدد عنصرًا"
                    ),

                color = Color.Gray
            )

        } else {

            InspectorContent(

                object = selectedObject,

                language = language,

                selectedTool = selectedTool,

                onObjectChanged =
                    onObjectChanged
            )
        }
    }
}


/* ============================================================
   INSPECTOR CONTENT
   ============================================================ */

@Composable
fun InspectorContent(
    object: GameObject,
    language: String,
    selectedTool: String,
    onObjectChanged: (GameObject) -> Unit
) {

    var xText by remember(
        object.id,
        object.posX
    ) {

        mutableStateOf(
            object.posX.toString()
        )
    }


    var yText by remember(
        object.id,
        object.posY
    ) {

        mutableStateOf(
            object.posY.toString()
        )
    }


    var zText by remember(
        object.id,
        object.posZ
    ) {

        mutableStateOf(
            object.posZ.toString()
        )
    }


    var rxText by remember(
        object.id,
        object.rotX
    ) {

        mutableStateOf(
            object.rotX.toString()
        )
    }


    var ryText by remember(
        object.id,
        object.rotY
    ) {

        mutableStateOf(
            object.rotY.toString()
        )
    }


    var rzText by remember(
        object.id,
        object.rotZ
    ) {

        mutableStateOf(
            object.rotZ.toString()
        )
    }


    var sxText by remember(
        object.id,
        object.scaleX
    ) {

        mutableStateOf(
            object.scaleX.toString()
        )
    }


    var syText by remember(
        object.id,
        object.scaleY
    ) {

        mutableStateOf(
            object.scaleY.toString()
        )
    }


    var szText by remember(
        object.id,
        object.scaleZ
    ) {

        mutableStateOf(
            object.scaleZ.toString()
        )
    }


    fun updatePosition() {

        onObjectChanged(

            object.copy(

                posX =
                    xText.toFloatOrNull()
                        ?: object.posX,

                posY =
                    yText.toFloatOrNull()
                        ?: object.posY,

                posZ =
                    zText.toFloatOrNull()
                        ?: object.posZ
            )
        )
    }


    fun updateRotation() {

        onObjectChanged(

            object.copy(

                rotX =
                    rxText.toFloatOrNull()
                        ?: object.rotX,

                rotY =
                    ryText.toFloatOrNull()
                        ?: object.rotY,

                rotZ =
                    rzText.toFloatOrNull()
                        ?: object.rotZ
            )
        )
    }


    fun updateScale() {

        onObjectChanged(

            object.copy(

                scaleX =
                    sxText.toFloatOrNull()
                        ?: object.scaleX,

                scaleY =
                    syText.toFloatOrNull()
                        ?: object.scaleY,

                scaleZ =
                    szText.toFloatOrNull()
                        ?: object.scaleZ
            )
        )
    }


    fun resetTransform() {

        val reset =
            object.copy(

                posX = 0f,
                posY = 0f,
                posZ = 0f,

                rotX = 0f,
                rotY = 0f,
                rotZ = 0f,

                scaleX = 1f,
                scaleY = 1f,
                scaleZ = 1f
            )


        xText = "0.0"
        yText = "0.0"
        zText = "0.0"

        rxText = "0.0"
        ryText = "0.0"
        rzText = "0.0"

        sxText = "1.0"
        syText = "1.0"
        szText = "1.0"

        onObjectChanged(reset)
    }


    Text(

        text = object.name,

        color = Color.White,

        fontSize = 17.sp
    )


    Spacer(
        modifier =
            Modifier.height(5.dp)
    )


    Text(

        text =
            text(
                language,
                "Type: ${object.type}",
                "النوع: ${object.type}"
            ),

        color = Color.Gray
    )


    Spacer(
        modifier =
            Modifier.height(12.dp)
    )


    Text(

        text =
            text(
                language,
                "Tool: $selectedTool",
                "الأداة: $selectedTool"
            ),

        color =
            Color(0xFF00E5FF)
    )


    Spacer(
        modifier =
            Modifier.height(12.dp)
    )


    Text(

        text =
            text(
                language,
                "POSITION",
                "الموقع"
            ),

        color =
            Color(0xFF00E5FF)
    )


    TransformField(
        label = "X",
        value = xText,
        onValueChange = {
            xText = it
        }
    )


    TransformField(
        label = "Y",
        value = yText,
        onValueChange = {
            yText = it
        }
    )


    TransformField(
        label = "Z",
        value = zText,
        onValueChange = {
            zText = it
        }
    )


    Button(

        onClick = {
            updatePosition()
        },

        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(
            text =
                text(
                    language,
                    "APPLY MOVE",
                    "تطبيق التحريك"
                ),

            fontSize = 11.sp
        )
    }


    Spacer(
        modifier =
            Modifier.height(10.dp)
    )


    Text(

        text =
            text(
                language,
                "ROTATION",
                "الدوران"
            ),

        color =
            Color(0xFF00E5FF)
    )


    TransformField(
        label = "X",
        value = rxText,
        onValueChange = {
            rxText = it
        }
    )


    TransformField(
        label = "Y",
        value = ryText,
        onValueChange = {
            ryText = it
        }
    )


    TransformField(
        label = "Z",
        value = rzText,
        onValueChange = {
            rzText = it
        }
    )


    Button(

        onClick = {
            updateRotation()
        },

        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(

            text =
                text(
                    language,
                    "APPLY ROTATE",
                    "تطبيق الدوران"
                ),

            fontSize = 11.sp
        )
    }


    Spacer(
        modifier =
            Modifier.height(10.dp)
    )


    Text(

        text =
            text(
                language,
                "SCALE",
                "الحجم"
            ),

        color =
            Color(0xFF00E5FF)
    )


    TransformField(
        label = "X",
        value = sxText,
        onValueChange = {
            sxText = it
        }
    )


    TransformField(
        label = "Y",
        value = syText,
        onValueChange = {
            syText = it
        }
    )


    TransformField(
        label = "Z",
        value = szText,
        onValueChange = {
            szText = it
        }
    )


    Button(

        onClick = {
            updateScale()
        },

        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(

            text =
                text(
                    language,
                    "APPLY SCALE",
                    "تطبيق الحجم"
                ),

            fontSize = 11.sp
        )
    }


    Spacer(
        modifier =
            Modifier.height(10.dp)
    )


    OutlinedButton(

        onClick = {
            resetTransform()
        },

        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(

            text =
                text(
                    language,
                    "RESET TRANSFORM",
                    "إعادة التحويل"
                ),

            fontSize = 10.sp
        )
    }
}


/* ============================================================
   TRANSFORM FIELD
   ============================================================ */

@Composable
fun TransformField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {

    Row(

        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(

            text = label,

            color =
                Color.LightGray,

            modifier =
                Modifier.width(20.dp)
        )


        OutlinedTextField(

            value = value,

            onValueChange =
                onValueChange,

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true
        )
    }


    Spacer(
        modifier =
            Modifier.height(3.dp)
    )
}
