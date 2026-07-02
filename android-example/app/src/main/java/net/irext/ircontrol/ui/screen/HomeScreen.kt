package net.irext.ircontrol.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import net.irext.decode.sdk.utils.Constants
import net.irext.ircontrol.R
import net.irext.ircontrol.ui.composable.ItemSingleText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCategory: () -> Unit,
    onRemoteClick: (Long) -> Unit,
) {
    val remoteFlow = remember {
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { RemoteControlPagingSource() },
        ).flow
    }
    val remotes = remoteFlow.collectAsLazyPagingItems()

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
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(count = remotes.itemCount) { index ->
                remotes[index]?.let { remote ->
                    val displayName = if (remote.categoryId != Constants.CategoryID.STB.value) {
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

            when (val appendState = remotes.loadState.append) {
                is LoadState.Loading -> item { LoadingMoreItem() }
                is LoadState.Error -> item {
                    LoadErrorItem(
                        message = appendState.error.message ?: "加载更多遥控器失败",
                        onRetry = { remotes.retry() },
                    )
                }
                else -> Unit
            }
        }

        when (val refreshState = remotes.loadState.refresh) {
            is LoadState.Loading -> FullScreenLoading(modifier = Modifier.padding(padding))
            is LoadState.Error -> FullScreenError(
                message = refreshState.error.message ?: "遥控器加载失败",
                onRetry = { remotes.retry() },
                modifier = Modifier.padding(padding),
            )
            is LoadState.NotLoading -> {
                if (remotes.itemCount == 0) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.create_new),
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
