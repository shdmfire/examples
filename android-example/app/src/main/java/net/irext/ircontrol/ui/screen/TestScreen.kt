package net.irext.ircontrol.ui.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.irext.ircontrol.IRApplication
import net.irext.ircontrol.R
import net.irext.ircontrol.controller.PhoneRemote
import net.irext.ircontrol.controller.base.Remote
import net.irext.ircontrol.ui.navigation.RouteTest
import net.irext.ircontrol.utils.FileUtils
import net.irext.webapi.model.RemoteIndex

private const val TEST_TAG = "TestScreen"

private sealed class TestState {
    data object Loading : TestState()
    data class Ready(
        val indexes: List<RemoteIndex>,
        val currentPos: Int = 0,
    ) : TestState()
    data class Error(val message: String) : TestState()
}

@Composable
fun TestScreen(
    route: RouteTest,
    onBack: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as IRApplication
    val context = LocalContext.current
    val appContext = context.applicationContext
    val phoneRemote = remember { PhoneRemote.getInstance(appContext) }
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<TestState>(TestState.Loading) }

    fun loadIndexes() {
        scope.launch {
            state = TestState.Loading
            try {
                val indexes = WebApiHelper.listRemoteIndexes(
                    app.mWeAPIs,
                    route.categoryId,
                    route.brandId,
                    route.cityCode,
                    route.operatorId,
                )
                state = if (indexes.isEmpty()) {
                    TestState.Error("No remote indexes found")
                } else {
                    TestState.Ready(indexes)
                }
            } catch (e: Exception) {
                Log.e(TEST_TAG, "load indexes failed", e)
                state = TestState.Error(e.message ?: "Load indexes failed")
            }
        }
    }

    fun sendPower() {
        val ready = state as? TestState.Ready ?: return
        val index = ready.indexes[ready.currentPos]
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val stream = WebApiHelper.downloadBin(app.mWeAPIs, index.remoteMap, index.id)
                    val binFile = FileUtils.getBinFile(appContext, index.remoteMap)
                    FileUtils.write(binFile, stream)

                    val openResult = phoneRemote.irOpen(
                        binFile.absolutePath,
                        index.categoryId,
                        index.subCate,
                    )
                    Log.d(TEST_TAG, "irOpen result=$openResult, file=${binFile.absolutePath}")
                    if (openResult != 0) {
                        throw IllegalStateException("Open IR binary failed: $openResult")
                    }

                    phoneRemote.irControl(index.categoryId, index.subCate, Remote.KEY_POWER)
                }
                Toast.makeText(context, context.getString(R.string.decode_and_send_success), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TEST_TAG, "send power failed", e)
                Toast.makeText(
                    context,
                    e.message ?: context.getString(R.string.decode_and_send_failed),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    fun previous() {
        val ready = state as? TestState.Ready ?: return
        if (ready.currentPos > 0) {
            state = ready.copy(currentPos = ready.currentPos - 1)
        }
    }

    fun next() {
        val ready = state as? TestState.Ready ?: return
        if (ready.currentPos < ready.indexes.lastIndex) {
            state = ready.copy(currentPos = ready.currentPos + 1)
        }
    }

    LaunchedEffect(route) { loadIndexes() }

    DisposableEffect(Unit) {
        onDispose { phoneRemote.irClose() }
    }

    TestScreenContent(
        state = state,
        onBack = onBack,
        onRetry = { loadIndexes() },
        onPowerClick = { sendPower() },
        onPreviousClick = { previous() },
        onNextClick = { next() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TestScreenContent(
    state: TestState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onPowerClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IR Test") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        }
    ) { padding ->
        when (val s = state) {
            is TestState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is TestState.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${s.message}")
                        Button(onClick = onRetry) {
                            Text("Retry")
                        }
                    }
                }
            }

            is TestState.Ready -> {
                val index = s.indexes[s.currentPos]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = index.remoteMap,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        text = "${s.currentPos + 1} / ${s.indexes.size}",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick = onPowerClick,
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PowerSettingsNew,
                            contentDescription = "Power",
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = onPreviousClick,
                            enabled = s.currentPos > 0,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Button(
                            onClick = onNextClick,
                            enabled = s.currentPos < s.indexes.lastIndex,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = "Next",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
