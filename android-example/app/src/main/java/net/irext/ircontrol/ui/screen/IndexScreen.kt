package net.irext.ircontrol.ui.screen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.irext.ircontrol.IRApplication
import net.irext.ircontrol.bean.RemoteControl
import net.irext.ircontrol.ui.composable.ItemIndexText
import net.irext.ircontrol.ui.navigation.RouteIndex
import net.irext.ircontrol.utils.FileUtils
import net.irext.webapi.model.RemoteIndex

private sealed class IndexScreenState {
    data object Loading : IndexScreenState()
    data class ListReady(val indexes: List<RemoteIndex>) : IndexScreenState()
    data class Downloading(val index: RemoteIndex) : IndexScreenState()
    data object Saving : IndexScreenState()
    data class Error(val message: String) : IndexScreenState()
}

private const val TAG = "IndexScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexScreen(
    route: RouteIndex,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    onTestClick: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as IRApplication
    val appContext = LocalContext.current.applicationContext
    var state by remember { mutableStateOf<IndexScreenState>(IndexScreenState.Loading) }
    val scope = rememberCoroutineScope()

    fun loadIndexes() {
        Log.d(TAG, "loadIndexes: categoryId=${route.categoryId}, brandId=${route.brandId}, cityCode='${route.cityCode}', operatorId='${route.operatorId}'")
        scope.launch {
            state = IndexScreenState.Loading
            try {
                val indexes = WebApiHelper.listRemoteIndexes(
                    app.mWeAPIs,
                    route.categoryId, route.brandId,
                    route.cityCode, route.operatorId
                )
                Log.d(TAG, "loadIndexes success: count=${indexes.size}")
                state = IndexScreenState.ListReady(indexes)
            } catch (e: Exception) {
                Log.e(TAG, "loadIndexes error: ${e.message}", e)
                state = IndexScreenState.Error(e.message ?: "Failed to load indexes")
            }
        }
    }

    fun onIndexClick(index: RemoteIndex) {
        scope.launch {
            state = IndexScreenState.Downloading(index)
            try {
                val stream = WebApiHelper.downloadBin(app.mWeAPIs, index.remoteMap, index.id)
                state = IndexScreenState.Saving
                withContext(Dispatchers.IO) {
                    val binFile = FileUtils.getBinFile(appContext, index.remoteMap)
                    FileUtils.write(binFile, stream)
                }
                val rc = RemoteControl()
                rc.categoryId = index.categoryId
                rc.categoryName = route.categoryName
                rc.brandId = index.brandId
                rc.brandName = route.brandName
                rc.cityCode = index.cityCode
                rc.cityName = route.cityName
                rc.operatorId = index.operatorId
                rc.operatorName = route.operatorName
                rc.protocol = index.protocol
                rc.remote = index.remote
                rc.remoteMap = index.remoteMap
                rc.subCategory = index.subCate
                RemoteControl.createRemoteControl(rc)
                onSaved()
            } catch (e: Exception) {
                Log.e(TAG, "download error: ${e.message}", e)
                state = IndexScreenState.Error(e.message ?: "Download failed")
            }
        }
    }

    LaunchedEffect(Unit) { loadIndexes() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(route.brandName.ifEmpty { route.cityName.ifEmpty { "Index" } })
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onTestClick) {
                        Icon(
                            imageVector = Icons.Filled.Science,
                            contentDescription = "IR Test"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        }
    ) { padding ->
        when (val s = state) {
            is IndexScreenState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is IndexScreenState.Downloading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text("Downloading ${s.index.remoteMap}...")
                    }
                }
            }
            is IndexScreenState.Saving -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text("Saving...")
                    }
                }
            }
            is IndexScreenState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${s.message}", textAlign = TextAlign.Center)
                        TextButton(onClick = { loadIndexes() }) { Text("Retry") }
                    }
                }
            }
            is IndexScreenState.ListReady -> {
                if (s.indexes.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No remote indexes found", fontSize = 18.sp)
                            Text(
                                "cat=${route.categoryId} brand=${route.brandId}",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                            )
                            TextButton(onClick = { loadIndexes() }) { Text("Retry") }
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.padding(padding)) {
                        items(s.indexes) { index ->
                            val nameText = if (route.brandName.isNotEmpty()) {
                                "${route.brandName} ${index.remoteMap}"
                            } else {
                                "${route.operatorName} ${index.remoteMap}"
                            }
                            ItemIndexText(
                                nameText = nameText,
                                mapText = index.remoteMap,
                                modifier = Modifier.clickable { onIndexClick(index) }
                            )
                        }
                    }
                }
            }
        }
    }
}
