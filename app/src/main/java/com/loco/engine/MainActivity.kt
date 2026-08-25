package com.loco.engine

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt


/* =========================================================
   MAIN ACTIVITY
   ========================================================= */

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
         * إجبار التطبيق على الوضع الأفقي.
         * هذا يبقي Loco Engine في Landscape.
         */
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val savedProjects =
            loadProjects(this)

        val savedLanguage =
            loadLanguage(this)

        val openedProject =
            loadOpenedProject(this)

        setContent {

            LocoEngineApp(
                savedProjects = savedProjects,
                initialLanguage = savedLanguage,
                initialOpenedProject = openedProject
            )
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
    val type: String,
    val x: Float = 0f,
    val y: Float = 0f,
    val rotation: Float = 0f,
    val scale: Float = 1f,
    val visible: Boolean = true,
    val locked: Boolean = false,

    val components: ComponentSet =
        defaultComponentsForObject(type)
)


/* =========================================================
   PROJECT STORAGE
   ========================================================= */

fun loadProjects(
    context: Context
): List<Project> {

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

        val array =
            JSONArray(data)

        val result =
            mutableListOf<Project>()

        for (i in 0 until array.length()) {

            val item =
                array.getJSONObject(i)

            result.add(
                Project(
                    name =
                        item.getString("name"),

                    type =
                        item.getString("type")
                )
            )
        }

        result

    } catch (_: Exception) {

        emptyList()
    }
}


fun saveProjects(
    context: Context,
    projects: List<Project>
) {

    val array =
        JSONArray()

    for (project in projects) {

        val item =
            JSONObject()

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


/* =========================================================
   LANGUAGE
   ========================================================= */

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
   OBJECT COLORS
   ========================================================= */

fun objectTypeColor(
    type: String
): Color {

    return when (type) {

        "Cube" ->
            Color(0xFF3B82F6)

        "Sphere" ->
            Color(0xFF22C55E)

        "Camera" ->
            Color(0xFFF97316)

        else ->
            Color(0xFFEAB308)
    }
}


/* =========================================================
   OPENED PROJECT STORAGE
   ========================================================= */

fun saveOpenedProject(
    context: Context,
    project: Project?
) {

    val editor =
        context
            .getSharedPreferences(
                "loco_engine",
                Context.MODE_PRIVATE
            )
            .edit()

    if (project == null) {

        editor.remove(
            "opened_project_name"
        )

        editor.remove(
            "opened_project_type"
        )

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

    val preferences =
        context.getSharedPreferences(
            "loco_engine",
            Context.MODE_PRIVATE
        )

    val name =
        preferences.getString(
            "opened_project_name",
            null
        )

    val type =
        preferences.getString(
            "opened_project_type",
            null
        )

    if (
        name == null ||
        type == null
    ) {

        return null
    }

    return Project(
        name = name,
        type = type
    )
}


/* =========================================================
   OBJECT STORAGE
   ========================================================= */

fun objectStorageKey(
    project: Project
): String {

    return "objects_${project.name}_${project.type}"
        .replace(
            " ",
            "_"
        )
}


fun worldBackgroundStorageKey(
    project: Project
): String {

    return "world_bg_${project.name}_${project.type}"
        .replace(
            " ",
            "_"
        )
}


fun saveWorldBackgroundColor(
    context: Context,
    project: Project,
    color: Color
) {

    context
        .getSharedPreferences(
            "loco_engine",
            Context.MODE_PRIVATE
        )
        .edit()
        .putInt(
            worldBackgroundStorageKey(
                project
            ),
            color.toArgb()
        )
        .apply()
}


fun loadWorldBackgroundColor(
    context: Context,
    project: Project
): Color {

    val defaultArgb =
        Color(0xFF182233)
            .toArgb()

    val stored =
        context
            .getSharedPreferences(
                "loco_engine",
                Context.MODE_PRIVATE
            )
            .getInt(
                worldBackgroundStorageKey(
                    project
                ),
                defaultArgb
            )

    return Color(stored)
}


/* =========================================================
   LOAD OBJECTS
   ========================================================= */

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

        val array =
            JSONArray(data)

        val result =
            mutableListOf<GameObject>()

        for (i in 0 until array.length()) {

            val item =
                array.getJSONObject(i)

            val type =
                item.getString("type")

            val components =
                if (
                    item.has("components")
                ) {

                    componentsFromJson(
                        item.optJSONArray(
                            "components"
                        )
                    )

                } else {

                    defaultComponentsForObject(
                        type
                    )
                }

            result.add(

                GameObject(

                    id =
                        item.getInt("id"),

                    name =
                        item.getString(
                            "name"
                        ),

                    type =
                        type,

                    x =
                        item.optDouble(
                            "x",
                            0.0
                        ).toFloat(),

                    y =
                        item.optDouble(
                            "y",
                            0.0
                        ).toFloat(),

                    rotation =
                        item.optDouble(
                            "rotation",
                            0.0
                        ).toFloat(),

                    scale =
                        item.optDouble(
                            "scale",
                            1.0
                        ).toFloat(),

                    visible =
                        item.optBoolean(
                            "visible",
                            true
                        ),

                    locked =
                        item.optBoolean(
                            "locked",
                            false
                        ),

                    components =
                        components
                )
            )
        }

        result

    } catch (_: Exception) {

        emptyList()
    }
}


/* =========================================================
   SAVE OBJECTS
   ========================================================= */

fun saveObjects(
    context: Context,
    project: Project,
    objects: List<GameObject>
) {

    val array =
        JSONArray()

    for (obj in objects) {

        val item =
            JSONObject()

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
            "x",
            obj.x
        )

        item.put(
            "y",
            obj.y
        )

        item.put(
            "rotation",
            obj.rotation
        )

        item.put(
            "scale",
            obj.scale
        )

        item.put(
            "visible",
            obj.visible
        )

        item.put(
            "locked",
            obj.locked
        )

        item.put(
            "components",
            componentsToJson(
                obj.components
            )
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
            objectStorageKey(project),
            array.toString()
        )
        .apply()
}


/* =========================================================
   MAIN APP
   ========================================================= */

@Composable
fun LocoEngineApp(
    savedProjects: List<Project>,
    initialLanguage: String,
    initialOpenedProject: Project?
) {

    val context =
        androidx.compose.ui.platform.LocalContext.current

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

    var openedProjectName by
        rememberSaveable {

            mutableStateOf(
                initialOpenedProject?.name
            )
        }

    var openedProjectType by
        rememberSaveable {

            mutableStateOf(
                initialOpenedProject?.type
            )
        }

    val openedProject =
        if (
            openedProjectName != null &&
            openedProjectType != null
        ) {

            Project(
                name =
                    openedProjectName!!,

                type =
                    openedProjectType!!
            )

        } else {

            null
        }

    fun setOpenedProject(
        project: Project?
    ) {

        openedProjectName =
            project?.name

        openedProjectType =
            project?.type
    }

    if (openedProject != null) {

        EditorScreen(
            project =
                openedProject,

            language =
                language,

            onLanguageChange = {

                language = it

                saveLanguage(
                    context,
                    it
                )
            },

            onBack = {

                setOpenedProject(null)

                saveOpenedProject(
                    context,
                    null
                )
            }
        )

        return
    }

    HomeScreen(
        projects =
            projects,

        language =
            language,

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

            setOpenedProject(it)

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


/* =========================================================
   HOME SCREEN - LANDSCAPE FRIENDLY
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

    var showCreateDialog by
        remember {
            mutableStateOf(false)
        }

    MaterialTheme {

        BoxWithConstraints(
            modifier =
                Modifier.fillMaxSize()
        ) {

            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Color(0xFF0F172A)
                        )
                        .padding(16.dp),

                horizontalArrangement =
                    Arrangement.spacedBy(18.dp)
            ) {

                /* =====================
                   LEFT HOME PANEL
                   ===================== */

                Column(
                    modifier =
                        Modifier
                            .width(
                                if (
                                    this@BoxWithConstraints.maxWidth > 700.dp
                                ) {
                                    300.dp
                                } else {
                                    250.dp
                                }
                            )
                            .fillMaxHeight(),

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

                                if (
                                    language ==
                                    "en"
                                ) {

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
                                if (
                                    language ==
                                    "en"
                                ) {
                                    "العربية"
                                } else {
                                    "English"
                                }
                            )
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.height(
                                20.dp
                            )
                    )

                    Text(
                        text =
                            "LOCO ENGINE",

                        color =
                            Color(0xFF00E5FF),

                        fontSize =
                            30.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                8.dp
                            )
                    )

                    Text(
                        text =
                            text(
                                language,
                                "Mobile Game Engine",
                                "محرك ألعاب للهواتف"
                            ),

                        color =
                            Color.White,

                        fontSize =
                            17.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                30.dp
                            )
                    )

                    Button(
                        onClick = {
                            showCreateDialog =
                                true
                        },

                        modifier =
                            Modifier.fillMaxWidth()
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
                }


                /* =====================
                   PROJECT PANEL
                   ===================== */

                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                Color(0xFF111C2E)
                            )
                            .padding(12.dp)
                ) {

                    Text(
                        text =
                            text(
                                language,
                                "PROJECTS",
                                "المشاريع"
                            ),

                        color =
                            Color(0xFF00E5FF),

                        fontSize =
                            21.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                10.dp
                            )
                    )

                    if (projects.isEmpty()) {

                        Box(
                            modifier =
                                Modifier.fillMaxSize(),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                text =
                                    text(
                                        language,
                                        "No projects yet",
                                        "لا توجد مشاريع حتى الآن"
                                    ),

                                color =
                                    Color.Gray
                            )
                        }

                    } else {

                        LazyColumn(
                            modifier =
                                Modifier.fillMaxSize(),

                            verticalArrangement =
                                Arrangement.spacedBy(
                                    8.dp
                                )
                        ) {

                            items(
                                items =
                                    projects,

                                key = {
                                    "${it.name}_${it.type}"
                                }
                            ) {

                                project ->

                                ProjectCard(
                                    project =
                                        project,

                                    language =
                                        language,

                                    onOpen = {
                                        onOpenProject(
                                            project
                                        )
                                    },

                                    onDelete = {
                                        onDeleteProject(
                                            project
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {

            CreateProjectDialog(
                language =
                    language,

                onCancel = {
                    showCreateDialog =
                        false
                },

                onCreate = {
                        name,
                        type ->

                    onCreateProject(
                        name,
                        type
                    )

                    showCreateDialog =
                        false
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

    var showDeleteDialog by
        remember {
            mutableStateOf(false)
        }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFF1E293B)
                )
                .padding(12.dp)
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        project.name,

                    color =
                        Color.White,

                    fontSize =
                        18.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
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
            }

            Button(
                onClick =
                    onOpen
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

            Spacer(
                modifier =
                    Modifier.width(6.dp)
            )

            OutlinedButton(
                onClick = {
                    showDeleteDialog =
                        true
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

    if (showDeleteDialog) {

        AlertDialog(

            onDismissRequest = {
                showDeleteDialog =
                    false
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

                        showDeleteDialog =
                            false

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
                        showDeleteDialog =
                            false
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


/* =========================================================
   CREATE PROJECT
   ========================================================= */

@Composable
fun CreateProjectDialog(
    language: String,
    onCancel: () -> Unit,
    onCreate: (String, String) -> Unit
) {

    var projectName by
        remember {
            mutableStateOf("")
        }

    var projectType by
        remember {
            mutableStateOf("3D")
        }

    AlertDialog(

        onDismissRequest =
            onCancel,

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
                    value =
                        projectName,

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
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
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
                        Modifier.height(6.dp)
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    Button(
                        onClick = {
                            projectType =
                                "2D"
                        }
                    ) {

                        Text("2D")
                    }

                    Button(
                        onClick = {
                            projectType =
                                "3D"
                        }
                    ) {

                        Text("3D")
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
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

                    if (
                        projectName
                            .isNotBlank()
                    ) {

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
                onClick =
                    onCancel
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

    val objects =
        remember(project.name) {

            mutableStateListOf<GameObject>().apply {

                addAll(
                    loadObjects(
                        context,
                        project
                    )
                )
            }
        }

    var selectedObjectId by
        rememberSaveable {
            mutableStateOf<Int?>(null)
        }

    var selectedTool by
        rememberSaveable {
            mutableStateOf("SELECT")
        }

    var showObjectDialog by
        remember {
            mutableStateOf(false)
        }

    var searchQuery by
        remember {
            mutableStateOf("")
        }

    var snapEnabled by
        remember {
            mutableStateOf(false)
        }

    var rotationStep by
        remember {
            mutableStateOf(15f)
        }

    var isPlaying by
        remember {
            mutableStateOf(false)
        }

    var undoSnapshot by
        remember {
            mutableStateOf<List<GameObject>?>(null)
        }

    var multiSelectedIds by
        remember {
            mutableStateOf(setOf<Int>())
        }

    var snapGridSize by
        remember {
            mutableStateOf(20f)
        }

    var worldBackgroundColor by
        remember(project.name) {

            mutableStateOf(
                loadWorldBackgroundColor(
                    context,
                    project
                )
            )
        }

    fun changeWorldBackgroundColor(
        color: Color
    ) {

        worldBackgroundColor =
            color

        saveWorldBackgroundColor(
            context,
            project,
            color
        )
    }

    fun saveCurrentObjects() {

        saveObjects(
            context,
            project,
            objects.toList()
        )
    }

    fun pushUndoSnapshot() {

        undoSnapshot = objects.toList()
    }

    fun performUndo() {

        val snapshot =
            undoSnapshot ?: return

        objects.clear()
        objects.addAll(snapshot)

        saveCurrentObjects()

        undoSnapshot = null
    }

    fun updateObject(
        id: Int,
        update:
            (GameObject) -> GameObject
    ) {

        val index =
            objects.indexOfFirst {
                it.id == id
            }

        if (index >= 0) {

            pushUndoSnapshot()

            objects[index] =
                update(
                    objects[index]
                )

            saveCurrentObjects()
        }
    }

    fun toggleVisible(
        id: Int
    ) {

        updateObject(id) {

            it.copy(
                visible =
                    !it.visible
            )
        }
    }

    fun toggleLock(
        id: Int
    ) {

        updateObject(id) {

            it.copy(
                locked =
                    !it.locked
            )
        }
    }

    fun renameObject(
        id: Int,
        newName: String
    ) {

        if (
            newName.isNotBlank()
        ) {

            val nameTaken =
                objects.any {
                    it.id != id &&
                        it.name == newName
                }

            val finalName =
                if (nameTaken) {

                    var counter = 2
                    var candidate = "$newName ($counter)"

                    while (
                        objects.any {
                            it.id != id &&
                                it.name == candidate
                        }
                    ) {
                        counter++
                        candidate = "$newName ($counter)"
                    }

                    candidate

                } else {
                    newName
                }

            updateObject(id) {

                it.copy(
                    name =
                        finalName
                )
            }
        }
    }

    fun resetTransform(
        id: Int
    ) {

        updateObject(id) {

            it.copy(
                x = 0f,
                y = 0f,
                rotation = 0f,
                scale = 1f
            )
        }
    }

    fun moveSelected(
        id: Int,
        dx: Float,
        dy: Float
    ) {

        val target =
            objects.firstOrNull {
                it.id == id
            } ?: return

        if (target.locked) {
            return
        }

        updateObject(id) {

            val newX =
                it.x + dx

            val newY =
                it.y + dy

            val finalX =
                if (snapEnabled) {

                    (
                        newX /
                            snapGridSize
                        )
                        .roundToInt() *
                            snapGridSize

                } else {
                    newX
                }

            val finalY =
                if (snapEnabled) {

                    (
                        newY /
                            snapGridSize
                        )
                        .roundToInt() *
                            snapGridSize

                } else {
                    newY
                }

            val syncedComponents =
                if (it.components.has(ComponentType.TRANSFORM)) {

                    val newSet =
                        ComponentSet(
                            it.components
                                .components
                                .toMutableList()
                        )

                    newSet.update(ComponentType.TRANSFORM) { c ->
                        c.copy(
                            positionX = finalX,
                            positionY = finalY
                        )
                    }

                    newSet

                } else {
                    it.components
                }

            it.copy(
                x = finalX,
                y = finalY,
                components = syncedComponents
            )
        }
    }

    fun rotateSelected() {

        if (isPlaying) {
            return
        }

        val id =
            selectedObjectId
                ?: return

        updateObject(id) {

            it.copy(
                rotation =
                    (
                        it.rotation +
                            rotationStep
                        ) % 360f
            )
        }
    }

    fun scaleSelected(
        amount: Float
    ) {

        if (isPlaying) {
            return
        }

        val id =
            selectedObjectId
                ?: return

        updateObject(id) {

            it.copy(

                scale =
                    (
                        it.scale +
                            amount
                        ).coerceIn(
                            0.4f,
                            3f
                        )
            )
        }
    }

    fun addComponent(
        id: Int,
        type: ComponentType
    ) {

        if (isPlaying) {
            return
        }

        updateObject(id) {

            val newSet =
                ComponentSet(
                    it.components
                        .components
                        .toMutableList()
                )

            newSet.add(type)

            it.copy(
                components =
                    newSet
            )
        }
    }


    fun updateComponentField(
        id: Int,
        type: ComponentType,
        update: (ComponentData) -> ComponentData
    ) {

        if (isPlaying) {
            return
        }

        updateObject(id) {

            val newSet =
                ComponentSet(
                    it.components
                        .components
                        .toMutableList()
                )

            newSet.update(type, update)

            it.copy(
                components = newSet
            )
        }
    }


    fun removeComponentFromObject(
        id: Int,
        type: ComponentType
    ) {

        if (isPlaying) {
            return
        }

        updateObject(id) {

            val newSet =
                ComponentSet(
                    it.components
                        .components
                        .toMutableList()
                )

            newSet.remove(type)

            it.copy(
                components = newSet
            )
        }
    }

    fun resetAllComponents(id: Int) {

        if (isPlaying) {
            return
        }

        updateObject(id) {

            val attachedTypes =
                it.components.components.map { c -> c.type }

            val newSet =
                ComponentSet(
                    attachedTypes
                        .map { t -> defaultComponent(t) }
                        .toMutableList()
                )

            it.copy(
                components = newSet
            )
        }
    }


    /* =====================================================
       EDITOR LAYOUT
       ===================================================== */

    BoxWithConstraints(
        modifier =
            Modifier.fillMaxSize()
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Color(0xFF111827)
                    )
        ) {

            /*
             * Top bar أصغر حتى لا يأخذ مساحة كبيرة
             */

            EditorTopBar(
                project =
                    project,

                language =
                    language,

                onLanguageChange =
                    onLanguageChange,

                isPlaying =
                    isPlaying,

                onTogglePlay = {
                    isPlaying =
                        !isPlaying
                },

                canUndo =
                    undoSnapshot != null,

                onUndo = {
                    performUndo()
                },

                onBack =
                    onBack
            )


            /*
             * Toolbar أصغر في الوضع الأفقي
             */

            EditorToolBar(
                selectedTool =
                    selectedTool,

                language =
                    language,

                onToolSelected = {
                    selectedTool = it
                },

                onRotate = {
                    rotateSelected()
                },

                onScaleUp = {
                    scaleSelected(
                        0.2f
                    )
                },

                onScaleDown = {
                    scaleSelected(
                        -0.2f
                    )
                },

                snapEnabled =
                    snapEnabled,

                onToggleSnap = {
                    snapEnabled =
                        !snapEnabled
                },

                snapGridSize =
                    snapGridSize,

                onSnapGridSizeChange = {
                    snapGridSize = it
                },

                rotationStep =
                    rotationStep,

                onRotationStepChange = {
                    rotationStep = it
                }
            )


            /*
             * أهم تغيير:
             *
             * في Landscape نستخدم Row دائمًا
             * حتى تبقى Scene / Viewport / Inspector
             * معروضة معًا.
             */

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
            ) {

                /* =====================
                   SCENE TREE
                   ===================== */

                SceneTree(
                    objects =
                        objects,

                    selectedObjectId =
                        selectedObjectId,

                    language =
                        language,

                    onSelect = {
                        selectedObjectId =
                            it
                    },

                    onAddObject = {

                        if (!isPlaying) {
                            showObjectDialog =
                                true
                        }
                    },

                    searchQuery =
                        searchQuery,

                    onSearchChange = {
                        searchQuery =
                            it
                    },

                    onToggleVisible = {
                        toggleVisible(it)
                    },

                    onToggleLock = {
                        toggleLock(it)
                    },

                    multiSelectedIds =
                        multiSelectedIds,

                    onToggleMultiSelect = { id ->
                        multiSelectedIds =
                            if (id in multiSelectedIds) {
                                multiSelectedIds - id
                            } else {
                                multiSelectedIds + id
                            }
                    },

                    onSelectAll = { ids ->
                        multiSelectedIds = ids
                    },

                    onDeleteMultiSelected = {

                        if (!isPlaying && multiSelectedIds.isNotEmpty()) {

                            pushUndoSnapshot()

                            objects.removeAll {
                                it.id in multiSelectedIds
                            }

                            multiSelectedIds = emptySet()
                            selectedObjectId = null

                            saveCurrentObjects()
                        }
                    }
                )


                /* =====================
                   VIEWPORT
                   ===================== */

                EditorViewport(
                    project =
                        project,

                    objects =
                        objects,

                    selectedObjectId =
                        selectedObjectId,

                    selectedTool =
                        selectedTool,

                    language =
                        language,

                    onSelect = {
                        selectedObjectId =
                            it
                    },

                    onMove = {
                            id,
                            dx,
                            dy ->

                        if (!isPlaying) {

                            moveSelected(
                                id,
                                dx,
                                dy
                            )
                        }
                    },

                    backgroundColor =
                        worldBackgroundColor,

                    modifier =
                        Modifier.weight(
                            1f
                        )
                )


                /* =====================
                   INSPECTOR
                   ===================== */

                InspectorPanel(
                    objects =
                        objects,

                    selectedObjectId =
                        selectedObjectId,

                    language =
                        language,

                    onDelete = { id ->

                        if (!isPlaying) {

                            pushUndoSnapshot()

                            objects.removeAll {
                                it.id == id
                            }

                            selectedObjectId =
                                null

                            saveCurrentObjects()
                        }
                    },

                    onRename = {
                            id,
                            newName ->

                        renameObject(
                            id,
                            newName
                        )
                    },

                    onResetTransform = {
                            id ->

                        if (!isPlaying) {

                            resetTransform(
                                id
                            )
                        }
                    },

                    worldBackgroundColor =
                        worldBackgroundColor,

                    onWorldBackgroundColorChange = {
                        changeWorldBackgroundColor(
                            it
                        )
                    },

                    onDuplicate = { id ->

                        val original =
                            objects.firstOrNull {
                                it.id == id
                            }

                        if (
                            original != null &&
                            !isPlaying
                        ) {

                            pushUndoSnapshot()

                            val newId =
                                (
                                    objects.maxOfOrNull {
                                        it.id
                                    } ?: 0
                                ) + 1

                            val newComponents =
                                ComponentSet(
                                    original
                                        .components
                                        .components
                                        .toMutableList()
                                )

                            val copy =
                                original.copy(

                                    id =
                                        newId,

                                    name =
                                        "${original.name} Copy",

                                    x =
                                        original.x +
                                            30f,

                                    y =
                                        original.y +
                                            30f,

                                    components =
                                        newComponents
                                )

                            objects.add(
                                copy
                            )

                            selectedObjectId =
                                newId

                            saveCurrentObjects()
                        }
                    },

                    onAddComponent = {
                            id,
                            type ->

                        addComponent(
                            id,
                            type
                        )
                    },

                    onUpdateComponent = {
                            id,
                            type,
                            update ->

                        updateComponentField(
                            id,
                            type,
                            update
                        )
                    },

                    onRemoveComponent = {
                            id,
                            type ->

                        removeComponentFromObject(
                            id,
                            type
                        )
                    },

                    onResetAllComponents = { id ->
                        resetAllComponents(id)
                    }
                )
            }
        }


        /* =================================================
           ADD OBJECT DIALOG
           ================================================= */

        if (showObjectDialog) {

            AddObjectDialog(
                language =
                    language,

                onCancel = {
                    showObjectDialog =
                        false
                },

                onAdd = {
                        objectType ->

                    pushUndoSnapshot()

                    val newId =
                        (
                            objects.maxOfOrNull {
                                it.id
                            } ?: 0
                        ) + 1

                    val objectName =
                        when (
                            objectType
                        ) {

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
                            id =
                                newId,

                            name =
                                objectName,

                            type =
                                objectType,

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


/* =========================================================
   TOP BAR
   ========================================================= */

@Composable
fun EditorTopBar(
    project: Project,
    language: String,
    onLanguageChange:
        (String) -> Unit,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    canUndo: Boolean,
    onUndo: () -> Unit,
    onBack: () -> Unit
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(
                    Color(0xFF0F172A)
                )
                .padding(
                    horizontal = 5.dp
                ),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        TextButton(
            onClick =
                onBack
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
            text =
                project.name,

            color =
                Color.White,

            fontSize =
                16.sp
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(2.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            if (canUndo) {

                TextButton(
                    onClick = onUndo
                ) {

                    Text(
                        text = text(
                            language,
                            "↶ UNDO",
                            "↶ تراجع"
                        ),
                        color = Color(0xFFFACC15)
                    )
                }
            }

            TextButton(
                onClick =
                    onTogglePlay
            ) {

                Text(
                    text =
                        if (
                            isPlaying
                        ) {

                            text(
                                language,
                                "⏸ STOP",
                                "⏸ إيقاف"
                            )

                        } else {

                            text(
                                language,
                                "▶ PLAY",
                                "▶ تشغيل"
                            )
                        },

                    color =
                        if (
                            isPlaying
                        ) {
                            Color(0xFF22C55E)
                        } else {
                            Color.White
                        }
                )
            }

            Text(
                text =
                    project.type,

                color =
                    Color(0xFF00E5FF)
            )

            TextButton(
                onClick = {

                    if (
                        language ==
                        "en"
                    ) {

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
                        if (
                            language ==
                            "en"
                        ) {
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
    onToolSelected:
        (String) -> Unit,
    onRotate: () -> Unit,
    onScaleUp: () -> Unit,
    onScaleDown: () -> Unit,
    snapEnabled: Boolean,
    onToggleSnap: () -> Unit,
    snapGridSize: Float,
    onSnapGridSizeChange: (Float) -> Unit,
    rotationStep: Float,
    onRotationStepChange: (Float) -> Unit
) {

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(82.dp)
                .background(
                    Color(0xFF1E293B)
                )
                .padding(4.dp)
    ) {

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(4.dp)
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

                if (
                    tool ==
                    selectedTool
                ) {

                    Button(
                        onClick = {
                            onToolSelected(
                                tool
                            )
                        }
                    ) {

                        Text(
                            translated
                        )
                    }

                } else {

                    OutlinedButton(
                        onClick = {
                            onToolSelected(
                                tool
                            )
                        }
                    ) {

                        Text(
                            translated
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(4.dp)
        ) {

            Button(
                onClick =
                    onRotate
            ) {

                Text(
                    "⟳ +${rotationStep.roundToInt()}°"
                )
            }

            var stepText by remember(rotationStep) {
                mutableStateOf(rotationStep.roundToInt().toString())
            }

            OutlinedTextField(
                value = stepText,
                onValueChange = { newText ->
                    stepText = newText
                    newText.toFloatOrNull()?.let {
                        onRotationStepChange(it)
                    }
                },
                label = {
                    Text(
                        text = text(
                            language,
                            "Step°",
                            "الخطوة°"
                        )
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.width(90.dp)
            )

            Button(
                onClick =
                    onScaleUp
            ) {

                Text(
                    text =
                        text(
                            language,
                            "SCALE +",
                            "تكبير +"
                        )
                )
            }

            Button(
                onClick =
                    onScaleDown
            ) {

                Text(
                    text =
                        text(
                            language,
                            "SCALE -",
                            "تصغير -"
                        )
                )
            }

            if (snapEnabled) {

                Button(
                    onClick =
                        onToggleSnap
                ) {

                    Text(
                        text =
                            text(
                                language,
                                "🧲 SNAP ON",
                                "🧲 تشغيل"
                            )
                    )
                }

            } else {

                OutlinedButton(
                    onClick =
                        onToggleSnap
                ) {

                    Text(
                        text =
                            text(
                                language,
                                "🧲 SNAP OFF",
                                "🧲 إيقاف"
                            )
                    )
                }
            }

            var gridText by remember(snapGridSize) {
                mutableStateOf(snapGridSize.roundToInt().toString())
            }

            OutlinedTextField(
                value = gridText,
                onValueChange = { newText ->
                    gridText = newText
                    newText.toFloatOrNull()?.let {
                        if (it > 0f) {
                            onSnapGridSizeChange(it)
                        }
                    }
                },
                label = {
                    Text(
                        text = text(
                            language,
                            "Grid",
                            "الشبكة"
                        )
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.width(80.dp)
            )
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
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleVisible: (Int) -> Unit,
    onToggleLock: (Int) -> Unit,
    multiSelectedIds: Set<Int>,
    onToggleMultiSelect: (Int) -> Unit,
    onSelectAll: (Set<Int>) -> Unit,
    onDeleteMultiSelected: () -> Unit,
    compact: Boolean = false
) {

    Column(
        modifier =
            Modifier
                .width(150.dp)
                .fillMaxHeight()
                .background(
                    Color(0xFF0B1220)
                )
                .padding(7.dp)
    ) {

        Text(
            text =
                text(
                    language,
                    "SCENE (${objects.size})",
                    "المشهد (${objects.size})"
                ),

            color =
                Color(0xFF00E5FF),

            fontSize =
                16.sp
        )

        Spacer(
            modifier =
                Modifier.height(3.dp)
        )

        OutlinedTextField(
            value =
                searchQuery,

            onValueChange =
                onSearchChange,

            label = {

                Text(
                    text =
                        text(
                            language,
                            "Search",
                            "بحث"
                        )
                )
            },

            trailingIcon = {

                if (searchQuery.isNotEmpty()) {

                    Text(
                        text = "✕",
                        color = Color.Gray,
                        modifier = Modifier
                            .clickable {
                                onSearchChange("")
                            }
                            .padding(6.dp)
                    )
                }
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(58.dp)
        )

        Spacer(
            modifier =
                Modifier.height(3.dp)
        )

        Button(
            onClick =
                onAddObject,

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
                Modifier.height(3.dp)
        )

        Spacer(
            modifier =
                Modifier.height(3.dp)
        )

        var typeFilter by remember {
            mutableStateOf("All")
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {

            listOf("All", "Cube", "Sphere", "Camera", "Light").forEach { filterOption ->

                val active = typeFilter == filterOption

                Text(
                    text = filterOption,
                    fontSize = 10.sp,
                    color = if (active) Color(0xFF00E5FF) else Color.Gray,
                    modifier = Modifier
                        .clickable { typeFilter = filterOption }
                        .padding(3.dp)
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(3.dp)
        )

        val filteredObjects =
            objects.filter {

                (
                    typeFilter == "All" ||
                        it.type == typeFilter
                ) &&
                    it.name.contains(
                        searchQuery,
                        ignoreCase = true
                    )
            }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Text(
                text = text(language, "All", "الكل"),
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier
                    .clickable {
                        onSelectAll(filteredObjects.map { it.id }.toSet())
                    }
                    .padding(2.dp)
            )

            Text(
                text = text(language, "None", "لا شيء"),
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier
                    .clickable { onSelectAll(emptySet()) }
                    .padding(2.dp)
            )

            if (multiSelectedIds.isNotEmpty()) {

                Text(
                    text = text(
                        language,
                        "Delete (${multiSelectedIds.size})",
                        "حذف (${multiSelectedIds.size})"
                    ),
                    fontSize = 10.sp,
                    color = Color(0xFFEF4444),
                    modifier = Modifier
                        .clickable { onDeleteMultiSelected() }
                        .padding(2.dp)
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(3.dp)
        )

        LazyColumn(
            modifier =
                Modifier.weight(1f)
        ) {

            items(
                items =
                    filteredObjects,

                key = {
                    it.id
                }
            ) { obj ->

                val selected =
                    obj.id ==
                        selectedObjectId

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(
                                    obj.id
                                )
                            }
                            .padding(5.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text = if (obj.id in multiSelectedIds) "☑" else "☐",
                        color = Color.Gray,
                        modifier = Modifier
                            .clickable { onToggleMultiSelect(obj.id) }
                            .padding(horizontal = 3.dp)
                    )

                    Text(
                        text =
                            "${obj.name} (${obj.components.components.size})",

                        color =
                            if (
                                selected
                            ) {

                                Color(
                                    0xFF00E5FF
                                )

                            } else if (
                                !obj.visible
                            ) {

                                Color.Gray

                            } else {

                                Color.White
                            },

                        modifier =
                            Modifier.weight(1f)
                    )

                    Text(
                        text =
                            if (
                                obj.visible
                            ) {
                                "👁"
                            } else {
                                "🚫"
                            },

                        modifier =
                            Modifier
                                .clickable {
                                    onToggleVisible(
                                        obj.id
                                    )
                                }
                                .padding(
                                    horizontal = 2.dp
                                )
                    )

                    Text(
                        text =
                            if (
                                obj.locked
                            ) {
                                "🔒"
                            } else {
                                "🔓"
                            },

                        modifier =
                            Modifier
                                .clickable {
                                    onToggleLock(
                                        obj.id
                                    )
                                }
                                .padding(
                                    horizontal = 2.dp
                                )
                    )
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

        onDismissRequest =
            onCancel,

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
                onClick =
                    onCancel
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


/* =========================================================
   OBJECT BUTTON
   ========================================================= */

@Composable
fun ObjectButton(
    name: String,
    arabicName: String,
    language: String,
    onClick: () -> Unit
) {

    Button(
        onClick =
            onClick,

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
            Modifier.height(4.dp)
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
    onSelect: (Int) -> Unit,
    onMove:
        (Int, Float, Float) -> Unit,
    modifier: Modifier =
        Modifier,
    backgroundColor: Color =
        Color(0xFF182233),
    compact: Boolean = false
) {

    Box(
        modifier =
            modifier
                .fillMaxHeight()
                .background(
                    backgroundColor
                )
                .border(
                    1.dp,
                    Color(0xFF334155)
                )
    ) {

        if (
            project.type ==
            "3D"
        ) {

            Viewport3DView(
                objects =
                    objects,

                selectedObjectId =
                    selectedObjectId,

                backgroundColor =
                    backgroundColor,

                modifier =
                    Modifier.fillMaxSize()
            )

            objects.forEach { obj ->

                if (
                    obj.id ==
                    selectedObjectId
                ) {

                    Text(
                        text =
                            obj.name,

                        color =
                            Color(0xFF00E5FF),

                        modifier =
                            Modifier
                                .align(
                                    Alignment
                                        .BottomCenter
                                )
                                .padding(
                                    6.dp
                                )
                    )
                }
            }

            return@Box
        }


        Text(
            text =
                text(
                    language,
                    "${project.type} VIEWPORT",
                    "نافذة ${project.type}"
                ),

            color =
                Color(0xFF00E5FF),

            modifier =
                Modifier
                    .align(
                        Alignment.TopCenter
                    )
                    .padding(6.dp)
        )


        Text(
            text =
                "┼",

            color =
                Color(0xFF334155),

            fontSize =
                60.sp,

            modifier =
                Modifier.align(
                    Alignment.Center
                )
        )


        objects.forEach { obj ->

            if (
                !obj.visible
            ) {
                return@forEach
            }

            val isSelected =
                obj.id ==
                    selectedObjectId

            val size =
                (
                    48f *
                        obj.scale
                    ).coerceIn(
                        25f,
                        130f
                    )

            Box(
                modifier =
                    Modifier
                        .offset {

                            IntOffset(
                                obj.x
                                    .roundToInt(),

                                obj.y
                                    .roundToInt()
                            )
                        }

                        .width(
                            size.dp
                        )

                        .height(
                            size.dp
                        )

                        .background(

                            if (
                                isSelected
                            ) {

                                Color(
                                    0xFF00E5FF
                                )

                            } else {

                                objectTypeColor(
                                    obj.type
                                )
                            }
                        )

                        .border(

                            width =
                                if (
                                    isSelected
                                ) {
                                    3.dp
                                } else {
                                    1.dp
                                },

                            color =
                                Color.White
                        )

                        .clickable {
                            onSelect(
                                obj.id
                            )
                        }

                        .pointerInput(
                            obj.id,
                            selectedTool
                        ) {

                            if (
                                selectedTool ==
                                "MOVE"
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

                        .align(
                            Alignment.Center
                        )
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
                        text =
                            when (obj.type) {
                                "Cube" -> "⬛"
                                "Sphere" -> "●"
                                "Camera" -> "📷"
                                else -> "💡"
                            },

                        fontSize = 20.sp,
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


/* =========================================================
   INSPECTOR
   ========================================================= */

@Composable
fun InspectorPanel(
    objects: List<GameObject>,
    selectedObjectId: Int?,
    language: String,
    onDelete: (Int) -> Unit,
    onDuplicate: (Int) -> Unit,
    onRename: (Int, String) -> Unit,
    onResetTransform: (Int) -> Unit,
    worldBackgroundColor: Color,
    onWorldBackgroundColorChange: (Color) -> Unit,
    onAddComponent: (Int, ComponentType) -> Unit,
    onUpdateComponent: (Int, ComponentType, (ComponentData) -> ComponentData) -> Unit,
    onRemoveComponent: (Int, ComponentType) -> Unit,
    onResetAllComponents: (Int) -> Unit,
    compact: Boolean = false
) {

    val selectedObject =
        objects.firstOrNull {
            it.id == selectedObjectId
        }

    Column(
        modifier =
            Modifier
                .width(280.dp)
                .fillMaxHeight()
                .background(Color(0xFF0B1220))
                .padding(10.dp)
                .verticalScroll(rememberScrollState())
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

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = text(
                language,
                "World Background",
                "خلفية العالم"
            ),
            color = Color.Gray
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            val presets = listOf(
                Color(0xFF182233),
                Color(0xFF1B1B1B),
                Color(0xFF223311),
                Color(0xFF2B1730)
            )

            presets.forEach { presetColor ->

                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height(26.dp)
                        .background(presetColor)
                        .border(
                            width = if (presetColor == worldBackgroundColor) {
                                2.dp
                            } else {
                                1.dp
                            },
                            color = Color.White
                        )
                        .clickable {
                            onWorldBackgroundColorChange(presetColor)
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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

            var renameText by remember(selectedObject.id) {
                mutableStateOf(selectedObject.name)
            }

            OutlinedTextField(
                value = renameText,
                onValueChange = {
                    renameText = it
                    onRename(selectedObject.id, it)
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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = text(
                    language,
                    "Type: ${selectedObject.type}",
                    "النوع: ${selectedObject.type}"
                ),
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = "X: ${selectedObject.x.roundToInt()}  Y: ${selectedObject.y.roundToInt()}",
                color = Color.White
            )

            Text(
                text = text(
                    language,
                    "Rotation: ${selectedObject.rotation.roundToInt()}°",
                    "الدوران: ${selectedObject.rotation.roundToInt()}°"
                ),
                color = Color.White
            )

            Text(
                text = text(
                    language,
                    "Scale: %.1f".format(selectedObject.scale),
                    "الحجم: %.1f".format(selectedObject.scale)
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {

                Button(
                    onClick = { onDuplicate(selectedObject.id) }
                ) {
                    Text(text = text(language, "DUP", "نسخ"))
                }

                OutlinedButton(
                    onClick = { onResetTransform(selectedObject.id) }
                ) {
                    Text(text = text(language, "RESET", "إعادة ضبط"))
                }

                OutlinedButton(
                    onClick = { onDelete(selectedObject.id) }
                ) {
                    Text(text = text(language, "DELETE", "حذف"))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            var expandedTypes by remember(selectedObject.id) {
                mutableStateOf(setOf<ComponentType>())
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = text(
                        language,
                        "COMPONENTS",
                        "المكونات"
                    ),
                    color = Color(0xFF00E5FF),
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = text(language, "Expand All", "توسيع الكل"),
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .clickable {
                            expandedTypes = selectedObject.components.components
                                .map { it.type }
                                .toSet()
                        }
                        .padding(horizontal = 4.dp)
                )

                Text(
                    text = text(language, "Collapse", "طي الكل"),
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .clickable { expandedTypes = emptySet() }
                        .padding(horizontal = 4.dp)
                )

                Text(
                    text = "↺",
                    color = Color(0xFFFACC15),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable { onResetAllComponents(selectedObject.id) }
                        .padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            selectedObject.components.components.forEachIndexed { index, component ->

                ComponentCard(
                    objectId = selectedObject.id,
                    component = component,
                    language = language,
                    expanded = component.type in expandedTypes,
                    onToggleExpand = {
                        expandedTypes = if (component.type in expandedTypes) {
                            expandedTypes - component.type
                        } else {
                            expandedTypes + component.type
                        }
                    },
                    onUpdate = onUpdateComponent,
                    onRemove = onRemoveComponent
                )

                if (index < selectedObject.components.components.size - 1) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFF243044))
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))

            val missingTypes = ComponentType.values().filter {
                !selectedObject.components.has(it)
            }

            if (missingTypes.isNotEmpty()) {

                Text(
                    text = text(
                        language,
                        "+ Add Component",
                        "+ إضافة مكوّن"
                    ),
                    color = Color.Gray
                )

                missingTypes.forEach { type ->

                    OutlinedButton(
                        onClick = {
                            onAddComponent(selectedObject.id, type)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            componentIcon(type) + " " + componentDisplayName(type, language)
                        )
                    }
                }
            }
        }
    }
}


/* =========================================================
   COMPONENT ICON
   ========================================================= */

fun componentIcon(type: ComponentType): String {

    return when (type) {
        ComponentType.TRANSFORM -> "📐"
        ComponentType.MESH_RENDERER -> "🧱"
        ComponentType.LIGHT -> "💡"
        ComponentType.CAMERA -> "🎥"
        ComponentType.COLLIDER -> "📦"
    }
}


fun componentAccentColor(type: ComponentType): Color {

    return when (type) {
        ComponentType.TRANSFORM -> Color(0xFF64748B)
        ComponentType.MESH_RENDERER -> Color(0xFF3B82F6)
        ComponentType.LIGHT -> Color(0xFFEAB308)
        ComponentType.CAMERA -> Color(0xFFF97316)
        ComponentType.COLLIDER -> Color(0xFF22C55E)
    }
}


fun componentSummary(component: ComponentData): String {

    return when (component.type) {

        ComponentType.TRANSFORM ->
            "pos ${component.positionX.roundToInt()}," +
                "${component.positionY.roundToInt()}," +
                "${component.positionZ.roundToInt()}"

        ComponentType.MESH_RENDERER ->
            component.mesh

        ComponentType.LIGHT ->
            "I:${component.intensity}"

        ComponentType.CAMERA ->
            "FOV:${component.fieldOfView.roundToInt()}"

        ComponentType.COLLIDER ->
            component.colliderShape
    }
}


/* =========================================================
   AXIS FIELD (Godot-style colored X/Y/Z)
   ========================================================= */

@Composable
fun AxisField(
    axisLabel: String,
    axisColor: Color,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {

    var text by remember(value) {
        mutableStateOf(
            if (value == value.toInt().toFloat()) {
                value.toInt().toString()
            } else {
                value.toString()
            }
        )
    }

    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            newText.toFloatOrNull()?.let { parsed ->
                onValueChange(parsed)
            }
        },
        label = {
            Text(axisLabel, color = axisColor)
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        modifier = modifier
    )
}


/* =========================================================
   VECTOR3 ROW (Godot-style compact XYZ with reset)
   ========================================================= */

@Composable
fun Vector3Row(
    label: String,
    language: String,
    x: Float,
    y: Float,
    z: Float,
    defaultValue: Float,
    onXChange: (Float) -> Unit,
    onYChange: (Float) -> Unit,
    onZChange: (Float) -> Unit,
    onResetAll: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "↺",
            color = Color.Gray,
            modifier = Modifier
                .clickable { onResetAll() }
                .padding(4.dp)
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        AxisField(
            axisLabel = "X",
            axisColor = Color(0xFFEF4444),
            value = x,
            onValueChange = onXChange,
            modifier = Modifier.weight(1f)
        )

        AxisField(
            axisLabel = "Y",
            axisColor = Color(0xFF22C55E),
            value = y,
            onValueChange = onYChange,
            modifier = Modifier.weight(1f)
        )

        AxisField(
            axisLabel = "Z",
            axisColor = Color(0xFF3B82F6),
            value = z,
            onValueChange = onZChange,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(4.dp))
}


/* =========================================================
   COMPONENT NUMBER FIELD
   ========================================================= */

@Composable
fun ComponentNumberField(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {

    var text by remember(value) {
        mutableStateOf(
            if (value == value.toInt().toFloat()) {
                value.toInt().toString()
            } else {
                value.toString()
            }
        )
    }

    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            newText.toFloatOrNull()?.let { parsed ->
                onValueChange(parsed)
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        modifier = Modifier.fillMaxWidth()
    )
}


/* =========================================================
   COMPONENT CARD
   ========================================================= */

@Composable
fun ComponentCard(
    objectId: Int,
    component: ComponentData,
    language: String,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onUpdate: (Int, ComponentType, (ComponentData) -> ComponentData) -> Unit,
    onRemove: (Int, ComponentType) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (component.enabled) 1f else 0.45f)
    ) {

        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(componentAccentColor(component.type))
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFF162032))
                .padding(6.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = componentIcon(component.type) + " " +
                        componentDisplayName(component.type, language) +
                        if (!expanded) {
                            "  (" + componentSummary(component) + ")"
                        } else {
                            ""
                        },
                    color = Color.White,
                    fontSize = if (expanded) 15.sp else 13.sp,
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = component.enabled,
                    onCheckedChange = { checked ->
                        onUpdate(objectId, component.type) {
                            it.copy(enabled = checked)
                        }
                    }
                )

            Text(
                text = "✕",
                color = Color(0xFFEF4444),
                modifier = Modifier
                    .clickable {
                        onRemove(objectId, component.type)
                    }
                    .padding(horizontal = 6.dp)
            )
        }

        if (expanded) {

            Spacer(modifier = Modifier.height(4.dp))

            when (component.type) {

                ComponentType.TRANSFORM -> {

                    Vector3Row(
                        label = text(language, "Position", "الموقع"),
                        language = language,
                        x = component.positionX,
                        y = component.positionY,
                        z = component.positionZ,
                        defaultValue = 0f,
                        onXChange = { onUpdate(objectId, component.type) { c -> c.copy(positionX = it) } },
                        onYChange = { onUpdate(objectId, component.type) { c -> c.copy(positionY = it) } },
                        onZChange = { onUpdate(objectId, component.type) { c -> c.copy(positionZ = it) } },
                        onResetAll = {
                            onUpdate(objectId, component.type) { c ->
                                c.copy(positionX = 0f, positionY = 0f, positionZ = 0f)
                            }
                        }
                    )

                    Vector3Row(
                        label = text(language, "Rotation", "الدوران"),
                        language = language,
                        x = component.rotationX,
                        y = component.rotationY,
                        z = component.rotationZ,
                        defaultValue = 0f,
                        onXChange = { onUpdate(objectId, component.type) { c -> c.copy(rotationX = it) } },
                        onYChange = { onUpdate(objectId, component.type) { c -> c.copy(rotationY = it) } },
                        onZChange = { onUpdate(objectId, component.type) { c -> c.copy(rotationZ = it) } },
                        onResetAll = {
                            onUpdate(objectId, component.type) { c ->
                                c.copy(rotationX = 0f, rotationY = 0f, rotationZ = 0f)
                            }
                        }
                    )

                    Vector3Row(
                        label = text(language, "Scale", "الحجم"),
                        language = language,
                        x = component.scaleX,
                        y = component.scaleY,
                        z = component.scaleZ,
                        defaultValue = 1f,
                        onXChange = {
                            onUpdate(objectId, component.type) { c ->
                                c.copy(scaleX = it.coerceAtLeast(0.01f))
                            }
                        },
                        onYChange = {
                            onUpdate(objectId, component.type) { c ->
                                c.copy(scaleY = it.coerceAtLeast(0.01f))
                            }
                        },
                        onZChange = {
                            onUpdate(objectId, component.type) { c ->
                                c.copy(scaleZ = it.coerceAtLeast(0.01f))
                            }
                        },
                        onResetAll = {
                            onUpdate(objectId, component.type) { c ->
                                c.copy(scaleX = 1f, scaleY = 1f, scaleZ = 1f)
                            }
                        }
                    )
                }

                ComponentType.MESH_RENDERER -> {

                    OutlinedTextField(
                        value = component.mesh,
                        onValueChange = { newValue ->
                            onUpdate(objectId, component.type) { c -> c.copy(mesh = newValue) }
                        },
                        label = { Text("Mesh") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = component.material,
                        onValueChange = { newValue ->
                            onUpdate(objectId, component.type) { c -> c.copy(material = newValue) }
                        },
                        label = { Text("Material") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                ComponentType.LIGHT -> {

                    ComponentNumberField(
                        label = "Intensity",
                        value = component.intensity
                    ) {
                        onUpdate(objectId, component.type) { c ->
                            c.copy(intensity = it.coerceAtLeast(0f))
                        }
                    }

                    ComponentNumberField(
                        label = "Range",
                        value = component.range
                    ) {
                        onUpdate(objectId, component.type) { c ->
                            c.copy(range = it.coerceAtLeast(0f))
                        }
                    }

                    Text(
                        text = text(language, "Color", "اللون"),
                        color = Color.Gray
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        val presets = listOf(
                            Triple(1f, 1f, 1f),
                            Triple(1f, 0.6f, 0.3f),
                            Triple(0.5f, 0.7f, 1f),
                            Triple(1f, 0.3f, 0.3f)
                        )

                        presets.forEach { (r, g, b) ->

                            Box(
                                modifier = Modifier
                                    .width(26.dp)
                                    .height(26.dp)
                                    .background(Color(r, g, b))
                                    .border(1.dp, Color.White)
                                    .clickable {
                                        onUpdate(objectId, component.type) { c ->
                                            c.copy(colorR = r, colorG = g, colorB = b)
                                        }
                                    }
                            )
                        }
                    }
                }

                ComponentType.CAMERA -> {

                    ComponentNumberField(
                        label = "Field Of View",
                        value = component.fieldOfView
                    ) {
                        onUpdate(objectId, component.type) { c ->
                            c.copy(fieldOfView = it.coerceIn(10f, 120f))
                        }
                    }

                    ComponentNumberField(
                        label = "Near Clip",
                        value = component.nearClip
                    ) {
                        onUpdate(objectId, component.type) { c ->
                            c.copy(nearClip = it.coerceAtLeast(0.01f))
                        }
                    }

                    ComponentNumberField(
                        label = "Far Clip",
                        value = component.farClip
                    ) {
                        onUpdate(objectId, component.type) { c ->
                            c.copy(farClip = it.coerceAtLeast(component.nearClip + 0.1f))
                        }
                    }
                }

                ComponentType.COLLIDER -> {

                    OutlinedTextField(
                        value = component.colliderShape,
                        onValueChange = { newValue ->
                            onUpdate(objectId, component.type) { c ->
                                c.copy(colliderShape = newValue)
                            }
                        },
                        label = { Text("Shape") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}}
