package net.irext.ircontrol.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.irext.ircontrol.R
import net.irext.ircontrol.bean.RemoteControl
import net.irext.ircontrol.ui.composable.ItemSingleText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCategory: () -> Unit,
    onRemoteClick: (Long) -> Unit,
) {
    var remotes by remember { mutableStateOf<List<RemoteControl>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        scope.launch {
            isRefreshing = true
            val result = withContext(Dispatchers.IO) {
                RemoteControl.listRemoteControls(0, 20)
            }
            remotes = result
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("红外遥控") },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCategory) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        }
    ) { padding ->
        when {
            isRefreshing && remotes.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            remotes.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.create_new),
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            else -> {
                PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = { refresh() }) {
                    LazyColumn(modifier = Modifier.padding(padding)) {
                        items(remotes) { remote ->
                            val displayName = if (remote.categoryId != net.irext.decode.sdk.utils.Constants.CategoryID.STB.value) {
                                "${remote.categoryName}-${remote.brandName}"
                            } else {
                                "${remote.cityName}-${remote.operatorName}"
                            }
                            ItemSingleText(
                                text = displayName,
                                modifier = Modifier.clickable {
                                    onRemoteClick(remote.getID())
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
