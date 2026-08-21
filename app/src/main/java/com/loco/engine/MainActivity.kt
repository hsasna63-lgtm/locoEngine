package com.loco.engine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LocoEngineApp()
        }
    }
}

data class Project(
    val name: String,
    val type: String
)

@Composable
fun LocoEngineApp() {

    var showCreateProject by remember {
        mutableStateOf(false)
    }

    val projects = remember {
        mutableStateListOf<Project>()
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
                modifier = Modifier.height(40.dp)
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
                modifier = Modifier.height(30.dp)
            )

            Button(
                onClick = {
                    showCreateProject = true
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
                modifier = Modifier.height(12.dp)
            )

            if (projects.isEmpty()) {

                Text(
                    text = "No projects yet",
                    color = Color.Gray,
                    fontSize = 16.sp
                )

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    items(projects) { project ->

                        ProjectCard(project)
                    }
                }
            }
        }

        if (showCreateProject) {

            CreateProjectDialog(
                onDismiss = {
                    showCreateProject = false
                },

                onCreate = { name, type ->

                    projects.add(
                        Project(
                            name = name,
                            type = type
                        )
                    )

                    showCreateProject = false
                }
            )
        }
    }
}

@Composable
fun ProjectCard(
    project: Project
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = project.name,
                fontSize = 20.sp
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Type: ${project.type}",
                fontSize = 14.sp
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = {
                    // سيتم فتح محرر المشروع هنا لاحقًا.
                }
            ) {
                Text("OPEN")
            }
        }
    }
}

@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {

    var projectName by remember {
        mutableStateOf("")
    }

    var projectType by remember {
        mutableStateOf("3D")
    }

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text("Create New Project")
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
                    },

                    singleLine = true,

                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
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
                    modifier = Modifier.height(10.dp)
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
                onClick = onDismiss
            ) {
                Text("CANCEL")
            }
        }
    )
}
