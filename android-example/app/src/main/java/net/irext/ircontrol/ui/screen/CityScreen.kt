package net.irext.ircontrol.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.irext.ircontrol.IRApplication
import net.irext.ircontrol.ui.composable.ItemSingleText
import net.irext.webapi.model.City

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityScreen(
    provinceCode: String,
    onCityClick: (City) -> Unit,
    onBack: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as IRApplication
    var cities by remember { mutableStateOf<List<City>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        scope.launch {
            isRefreshing = true
            cities = WebApiHelper.listCities(app.mWeAPIs, provinceCode)
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select City") },
                navigationIcon = {
                    Text("←", Modifier.clickable { onBack() }.padding(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        }
    ) { padding ->
        when {
            isRefreshing && cities.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = { refresh() }) {
                    LazyColumn(modifier = Modifier.padding(padding)) {
                        items(cities) { city ->
                            ItemSingleText(
                                text = city.name,
                                modifier = Modifier.clickable { onCityClick(city) }
                            )
                        }
                    }
                }
            }
        }
    }
}
