package net.irext.ircontrol.compose.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import net.irext.ircontrol.compose.R
import net.irext.ircontrol.compose.controller.base.ControlCommand
import net.irext.ircontrol.compose.ui.theme.IRControlTheme

@Composable
fun ControlScreen(
    remoteId: Long,
    onBack: () -> Unit,
    viewModel: ControlViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ControlEvent.Toast -> Toast.makeText(
                    context,
                    event.message,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    LaunchedEffect(remoteId) {
        viewModel.loadRemote(remoteId)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.close() }
    }

    ControlContent(
        state = state,
        onBack = onBack,
        onCommand = viewModel::onCommand,
        onEmitterIpChange = viewModel::onEmitterIpChange,
        onConnectClick = viewModel::onConnectClick,
    )
}

@Composable
private fun ControlContent(
    state: ControlUiState,
    onBack: () -> Unit,
    onCommand: (ControlCommand) -> Unit,
    onEmitterIpChange: (String) -> Unit,
    onConnectClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ControlTitle(title = state.title, onBack = onBack)
            RemoteControlPanel(onCommand = onCommand)
            Spacer(modifier = Modifier.weight(1f))
            EmitterConnectionPanel(
                ip = state.emitterIp,
                connected = state.isEmitterConnected,
                isLoading = state.isLoading,
                onIpChange = onEmitterIpChange,
                onConnectClick = onConnectClick,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ControlTitle(
    title: String,
    onBack: () -> Unit,
) {
    CenterAlignedTopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        title = {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.button_back),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Unspecified,
            navigationIconContentColor = Color.Unspecified,
            titleContentColor = Color.Unspecified,
            actionIconContentColor = Color.Unspecified
        ),
    )
}

@Composable
private fun RemoteControlPanel(
    onCommand: (ControlCommand) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        RemoteButtonRow {
            RemoteButton(
                painter = painterResource(R.mipmap.button_power),
                contentDescription = stringResource(R.string.button_power),
                onClick = { onCommand(ControlCommand.POWER) },
            )
        }
        DividerLine()
        RemoteButtonRow {
            RemoteButton(
                painter = painterResource(R.mipmap.button_back),
                contentDescription = stringResource(R.string.button_back),
                onClick = { onCommand(ControlCommand.BACK) },
            )
            RemoteButton(
                painter = painterResource(R.mipmap.button_home),
                contentDescription = stringResource(R.string.button_home),
                onClick = { onCommand(ControlCommand.HOME) },
            )
            RemoteButton(
                painter = painterResource(R.mipmap.button_menu),
                contentDescription = stringResource(R.string.button_menu),
                onClick = { onCommand(ControlCommand.MENU) },
            )
        }
        RemoteButtonRow {
            RemoteButtonSpacer()
            RemoteButton(
                painter = painterResource(R.mipmap.button_up),
                contentDescription = stringResource(R.string.button_up),
                onClick = { onCommand(ControlCommand.UP) },
            )
            RemoteButtonSpacer()
        }
        RemoteButtonRow {
            RemoteButton(
                painter = painterResource(R.mipmap.button_left),
                contentDescription = stringResource(R.string.button_left),
                onClick = { onCommand(ControlCommand.LEFT) },
            )
            RemoteButton(
                painter = painterResource(R.mipmap.button_ok),
                contentDescription = stringResource(R.string.button_ok),
                onClick = { onCommand(ControlCommand.OK) },
            )
            RemoteButton(
                painter = painterResource(R.mipmap.button_right),
                contentDescription = stringResource(R.string.button_right),
                onClick = { onCommand(ControlCommand.RIGHT) },
            )
        }
        RemoteButtonRow {
            RemoteButtonSpacer()
            RemoteButton(
                painter = painterResource(R.mipmap.button_down),
                contentDescription = stringResource(R.string.button_down),
                onClick = { onCommand(ControlCommand.DOWN) },
            )
            RemoteButtonSpacer()
        }
        DividerLine()
        RemoteButtonRow {
            RemoteButton(
                painter = painterResource(R.mipmap.button_minus),
                contentDescription = stringResource(R.string.button_minus),
                onClick = { onCommand(ControlCommand.MINUS) },
            )
            RemoteButtonSpacer()
            RemoteButton(
                painter = painterResource(R.mipmap.button_plus),
                contentDescription = stringResource(R.string.button_plus),
                onClick = { onCommand(ControlCommand.PLUS) },
            )
        }
    }
}

@Composable
private fun RemoteButtonRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun RowScope.RemoteButton(
    painter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
        RemoteButtonImage(painter, contentDescription, onClick)
    }
}

@Composable
private fun RowScope.RemoteButtonSpacer() {
    Spacer(modifier = Modifier.weight(1f).height(64.dp))
}

@Composable
private fun RemoteButtonImage(
    painter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        onClick = onClick,
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
        )
    }
}

@Composable
private fun EmitterConnectionPanel(
    ip: String,
    connected: Boolean,
    isLoading: Boolean,
    onIpChange: (String) -> Unit,
    onConnectClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .padding(horizontal = 4.dp)
                .background(if (connected) Color(0xFF3FAFFF) else Color(0xFFFF3F3F)),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.emitter_ip),
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            OutlinedTextField(
                value = ip,
                onValueChange = onIpChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp),
            )
            IconButton(
                modifier = Modifier.size(60.dp),
                onClick = onConnectClick,
                enabled = !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Image(
                        painter = painterResource(
                            if (connected) R.mipmap.button_unlink else R.mipmap.button_link,
                        ),
                        contentDescription = stringResource(R.string.connect),
                        modifier = Modifier.size(36.dp),
                    )
                }
            }
        }
        DividerLine()
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 4.dp)
            .background(Color(0xFF7F7F7F)),
    )
}

@Preview(showBackground = true)
@Composable
private fun ControlContentPreview() {
    IRControlTheme {
        ControlContent(
            state = ControlUiState(
                title = "客厅电视",
                emitterIp = "192.168.1.10",
                isEmitterConnected = true,
            ),
            onBack = {},
            onCommand = {},
            onEmitterIpChange = {},
            onConnectClick = {},
        )
    }
}
