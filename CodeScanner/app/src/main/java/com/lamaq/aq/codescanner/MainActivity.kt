package com.lamaq.aq.codescanner

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.lamaq.aq.codescanner.ui.theme.CodeScannerTheme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CodeScannerTheme {
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }
                Surface(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    val options = GmsBarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                            Barcode.FORMAT_ALL_FORMATS,
                        )
                        .enableAutoZoom()
                        .allowManualInput()
                        .build()
                    val scanner = GmsBarcodeScanning.getClient(this, options)
                    var rawValue by remember {
                        mutableStateOf("Raw Value will be shown here...")
                    }
                    Scaffold(
                        snackbarHost = {
                            SnackbarHost(hostState = snackbarHostState){
                                Snackbar(
                                    snackbarData = it,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        },
                        content = {
                            Row {
                                Text(
                                    text = "Code Scanner",
                                    style = MaterialTheme.typography.headlineLarge,
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(10.dp)

                                )
                            }
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                OutlinedCard(
                                    modifier = Modifier
                                        .width(300.dp)
                                        .height(400.dp),
                                    elevation = CardDefaults.cardElevation(4.dp),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    ),
                                    border = BorderStroke(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    ),
                                    ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        Box(modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                            .align(Alignment.CenterHorizontally)
                                        ) {
                                            Text(
                                                text = rawValue,
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .selectable(
                                                        selected = true,
                                                        onClick = {
                                                            val clipboard: ClipboardManager =
                                                                getSystemService(
                                                                    CLIPBOARD_SERVICE
                                                                ) as ClipboardManager
                                                            val clip =
                                                                ClipData.newPlainText(
                                                                    rawValue,
                                                                    rawValue
                                                                )
                                                            clipboard.setPrimaryClip(clip)
                                                            Toast
                                                                .makeText(
                                                                    this@MainActivity,
                                                                    "Copied to Clipboard",
                                                                    Toast.LENGTH_SHORT
                                                                )
                                                                .show()
                                                        }
                                                    ),
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(50.dp))
                                        ElevatedButton(
                                            modifier = Modifier
                                                .align(Alignment.CenterHorizontally),
                                            onClick = {
                                                rawValue = "Loading..."
                                            scanner
                                                .startScan()
                                                .addOnSuccessListener { barcode ->
                                                    rawValue = barcode.rawValue.toString()
                                                    if (rawValue == "null") {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("No Barcode Found")
                                                        }
                                                    }else if (rawValue.contains("http")) {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("Opening URL")
                                                        }
                                                        startActivity(
                                                            Intent(
                                                                Intent.ACTION_VIEW,
                                                                Uri.parse(rawValue)
                                                            )
                                                        )
                                                    }else if (rawValue.contains("mailto")) {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("Opening Email")
                                                        }
                                                        startActivity(
                                                            Intent(
                                                                Intent.ACTION_VIEW,
                                                                Uri.parse(rawValue)
                                                            )
                                                        )
                                                    }else if (rawValue.contains("tel")) {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("Opening Dialer")
                                                        }
                                                        startActivity(
                                                            Intent(
                                                                Intent.ACTION_VIEW,
                                                                Uri.parse(rawValue)
                                                            )
                                                        )
                                                    } else {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("Barcode Found")
                                                        }
                                                    }
                                                }
                                                .addOnCanceledListener {
                                                    rawValue = "Task Cancelled"
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Task Cancelled by User")
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    rawValue = "Task Failed"
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Task Failed: ${e.message}")
                                                    }
                                                }
                                        }) {
                                            Text(
                                                text = "Scan Code",
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(15.dp))
                                        ElevatedButton(
                                            modifier = Modifier
                                                .align(Alignment.CenterHorizontally),
                                            onClick = {
                                                rawValue = "Loading..."
                                                val intent = Intent(this@MainActivity, CodeGen::class.java)
                                                startActivity(intent)
                                            }) {
                                            Text(
                                                text = "Generate Code",
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(50.dp))
                                        Text(
                                            text = "Powered By Google ML Kit",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}