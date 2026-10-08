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
import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var crashLog by remember { mutableStateOf<String?>(null) }
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
                if (isDebug) {
                    val file = File(context.filesDir, "last_crash.txt")
                    var logText = ""
                    var shouldShow = false
                    
                    if (file.exists()) {
                        logText += "--- last_crash.txt ---\n" + file.readText() + "\n\n"
                        shouldShow = true
                    }
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                        val reasons = am.getHistoricalProcessExitReasons(context.packageName, 0, 1)
                        if (reasons.isNotEmpty()) {
                            val exitInfo = reasons[0]
                            val reasonName = when (exitInfo.reason) {
                                ApplicationExitInfo.REASON_ANR -> "ANR"
                                ApplicationExitInfo.REASON_CRASH -> "CRASH"
                                ApplicationExitInfo.REASON_CRASH_NATIVE -> "CRASH_NATIVE"
                                ApplicationExitInfo.REASON_DEPENDENCY_DIED -> "DEPENDENCY_DIED"
                                ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> "EXCESSIVE_RESOURCE_USAGE"
                                ApplicationExitInfo.REASON_EXIT_SELF -> "EXIT_SELF"
                                ApplicationExitInfo.REASON_INITIALIZATION_FAILURE -> "INITIALIZATION_FAILURE"
                                ApplicationExitInfo.REASON_LOW_MEMORY -> "LOW_MEMORY"
                                ApplicationExitInfo.REASON_OTHER -> "OTHER"
                                ApplicationExitInfo.REASON_PERMISSION_CHANGE -> "PERMISSION_CHANGE"
                                ApplicationExitInfo.REASON_SIGNALED -> "SIGNAL"
                                ApplicationExitInfo.REASON_USER_REQUESTED -> "USER_REQUESTED"
                                ApplicationExitInfo.REASON_USER_STOPPED -> "USER_STOPPED"
                                else -> "UNKNOWN (${exitInfo.reason})"
                            }
                            
                            val timeString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(exitInfo.timestamp))
                            
                            logText += "--- Historical Exit Info ---\n"
                            logText += "Reason: $reasonName\n"
                            logText += "Timestamp: $timeString\n"
                            logText += "Description: ${exitInfo.description ?: "None"}\n"
                            
                            if (exitInfo.reason == ApplicationExitInfo.REASON_CRASH || 
                                exitInfo.reason == ApplicationExitInfo.REASON_CRASH_NATIVE ||
                                exitInfo.reason == ApplicationExitInfo.REASON_ANR) {
                                shouldShow = true
                            }
                        }
                    }
                    
                    if (shouldShow) {
                        crashLog = logText
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
