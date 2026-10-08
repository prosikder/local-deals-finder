package com.localdeals.finder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import com.localdeals.finder.ui.theme.LocalDealsFinderTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var crashLog by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                val isDebug = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
                if (isDebug) {
                    val file = File(filesDir, "last_crash.txt")
                    if (file.exists()) {
                        crashLog = file.readText()
                    }
                }
            }

            LocalDealsFinderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    com.localdeals.finder.ui.navigation.LocalDealsApp()
                }

                crashLog?.let { log ->
                    val clipboardManager = LocalClipboardManager.current
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text("The app crashed last time") },
                        text = {
                            Text(
                                text = log,
                                modifier = Modifier
                                    .fillMaxHeight(0.6f)
                                    .verticalScroll(rememberScrollState()),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                val file = File(filesDir, "last_crash.txt")
                                if (file.exists()) file.delete()
                                crashLog = null
                            }) {
                                Text("Dismiss")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                clipboardManager.setText(buildAnnotatedString { append(log) })
                            }) {
                                Text("Copy")
                            }
                        }
                    )
                }
            }
        }
    }
}
