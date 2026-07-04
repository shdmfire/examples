package net.irext.ircontrol.compose.ui.screen

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.irext.ircontrol.compose.bean.RemoteControl


/**
 * Filename:       RemoteControlPagingSource.kt
 * Created:        Date: 2026-07-14
 *
 * Description:    Provides the RemoteControlPagingSource source for the IRControl Android Compose sample.
 *
 * Revision log:
 * 2026-07-14: created by shdmfire and strawmanbobi
 */
class RemoteControlPagingSource : PagingSource<Int, RemoteControl>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RemoteControl> = try {
        val from = params.key ?: 0
        val count = params.loadSize
        val data = withContext(Dispatchers.IO) {
            RemoteControl.listRemoteControls(from, count)
        }

        LoadResult.Page(
            data = data,
            prevKey = if (from == 0) null else maxOf(0, from - count),
            nextKey = if (data.size < count) null else from + data.size,
        )
    } catch (e: Exception) {
        LoadResult.Error(e)
    }

    override fun getRefreshKey(state: PagingState<Int, RemoteControl>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.let { page ->
                page.prevKey?.plus(state.config.pageSize)
                    ?: page.nextKey?.minus(state.config.pageSize)
            }
        }
}
