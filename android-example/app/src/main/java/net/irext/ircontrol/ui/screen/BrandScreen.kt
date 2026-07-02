package net.irext.ircontrol.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import net.irext.ircontrol.IRApplication
import net.irext.ircontrol.ui.composable.ItemSingleText
import net.irext.webapi.model.Brand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandScreen(
    categoryId: Int,
    categoryName: String,
    onBrandClick: (Brand) -> Unit,
    onBack: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as IRApplication
    val brandFlow = remember(app.mWeAPIs, categoryId) {
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { BrandPagingSource(app.mWeAPIs, categoryId) },
        ).flow
    }
    val brands = brandFlow.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName) },
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
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(count = brands.itemCount) { index ->
                brands[index]?.let { brand ->
                    ItemSingleText(
                        text = brand.name,
                        modifier = Modifier.clickable { onBrandClick(brand) }
                    )
                }
            }

            when (val appendState = brands.loadState.append) {
                is LoadState.Loading -> item { LoadingMoreItem() }
                is LoadState.Error -> item {
                    LoadErrorItem(
                        message = appendState.error.message ?: "加载更多品牌失败",
                        onRetry = { brands.retry() },
                    )
                }
                else -> Unit
            }
        }

        when (val refreshState = brands.loadState.refresh) {
            is LoadState.Loading -> FullScreenLoading(modifier = Modifier.padding(padding))
            is LoadState.Error -> FullScreenError(
                message = refreshState.error.message ?: "品牌加载失败",
                onRetry = { brands.retry() },
                modifier = Modifier.padding(padding),
            )
            else -> Unit
        }
    }
}
