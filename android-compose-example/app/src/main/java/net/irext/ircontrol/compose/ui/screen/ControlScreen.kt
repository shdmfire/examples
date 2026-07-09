package net.irext.ircontrol.compose.ui.screen

import android.widget.Toast
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import net.irext.ircontrol.compose.R
import net.irext.ircontrol.compose.controller.ControlCommand
import net.irext.ircontrol.compose.controller.RemoteCategory
import net.irext.ircontrol.compose.ui.theme.IRControlTheme

@Composable
fun ControlScreen(
    remoteId: Long,
    onBack: () -> Unit,
    viewModel: ControlViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context by rememberUpdatedState(LocalContext.current)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ControlEvent.Toast -> {
                    Toast.makeText(
                        context,
                        event.message,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    LaunchedEffect(remoteId, viewModel) {
        viewModel.loadRemote(remoteId)
    }

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.close()
        }
    }

    ControlContent(
        state = state,
        onBack = onBack,
        onCommand = viewModel::onCommand,
        onEmitterIpChange = viewModel::onEmitterIpChange,
        onConnectClick = viewModel::onConnectClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ControlContent(
    state: ControlUiState,
    onBack: () -> Unit,
    onCommand: (ControlCommand) -> Unit,
    onEmitterIpChange: (String) -> Unit,
    onConnectClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ControlTitle(
                title = state.title,
                onBack = onBack,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Dynamic control panel based on category
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (state.category) {
                    RemoteCategory.AC -> {
                        AcControlPanel(onCommand = onCommand)
                    }
                    RemoteCategory.DYSON -> {
                        DysonControlPanel(onCommand = onCommand)
                    }
                    RemoteCategory.TV,
                    RemoteCategory.STB,
                    RemoteCategory.NETBOX,
                    RemoteCategory.IPTV,
                    RemoteCategory.DVD,
                    RemoteCategory.PROJECTOR,
                    RemoteCategory.STEREO,
                    RemoteCategory.BSTB -> {
                        MediaControlPanel(category = state.category, onCommand = onCommand)
                    }
                    RemoteCategory.FAN,
                    RemoteCategory.LIGHT,
                    RemoteCategory.CLEANING_ROBOT,
                    RemoteCategory.AIRCLEANER -> {
                        GridControlPanel(category = state.category, onCommand = onCommand)
                    }
                    else -> {
                        Text(
                            text = "Loading remote...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Emitter connection panel
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

@Composable
private fun MediaControlPanel(
    category: RemoteCategory,
    onCommand: (ControlCommand) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Power & Mute
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onCommand(ControlCommand.Power) },
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Power",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            }

            if (category in listOf(RemoteCategory.TV, RemoteCategory.STB, RemoteCategory.IPTV, RemoteCategory.STEREO, RemoteCategory.BSTB)) {
                FilledTonalIconButton(
                    onClick = { onCommand(ControlCommand.Mute) },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeMute,
                        contentDescription = "Mute"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.1f))

        // D-Pad
        DPad(onCommand = onCommand)

        Spacer(modifier = Modifier.weight(0.1f))

        // Navigation helpers (Back, Home, Menu)
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = { onCommand(ControlCommand.Back) },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

            if (category in listOf(RemoteCategory.TV, RemoteCategory.NETBOX, RemoteCategory.DVD)) {
                FilledTonalIconButton(
                    onClick = { onCommand(ControlCommand.Home) },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home"
                    )
                }
            }

            FilledTonalIconButton(
                onClick = { onCommand(ControlCommand.Menu) },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.1f))

        // Volume Rocker and Page/Playback controls
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            VolumeRocker(onCommand = onCommand)

            // Category specific extra keys
            if (category in listOf(RemoteCategory.STB, RemoteCategory.IPTV, RemoteCategory.BSTB)) {
                Row(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = { onCommand(ControlCommand.PageDown) },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Color.Transparent)
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Page Down")
                    }
                    Text(
                        text = "PAGE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    FilledTonalIconButton(
                        onClick = { onCommand(ControlCommand.PageUp) },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Color.Transparent)
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Page Up")
                    }
                }
            } else if (category == RemoteCategory.DVD) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalIconButton(onClick = { onCommand(ControlCommand.Play) }, modifier = Modifier.size(44.dp)) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play")
                    }
                    FilledTonalIconButton(onClick = { onCommand(ControlCommand.Pause) }, modifier = Modifier.size(44.dp)) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = "Pause")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.2f))
    }
}

@Composable
private fun AcControlPanel(
    onCommand: (ControlCommand) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Virtual Screen Display
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(120.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MODE: COOL",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "FAN: AUTO",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Text(
                    text = "24°C",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.1f))

        // Temperature Up / Down Capsule
        Row(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            FilledTonalIconButton(
                onClick = { onCommand(ControlCommand.AcTempMinus) },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Temp Down")
            }
            Text(
                text = "TEMP",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilledTonalIconButton(
                onClick = { onCommand(ControlCommand.AcTempPlus) },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Temp Up")
            }
        }

        Spacer(modifier = Modifier.weight(0.1f))

        // AC Controls
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledTonalIconButton(
                    onClick = { onCommand(ControlCommand.Power) },
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    )
                ) {
                    Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = "Power", tint = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Power", style = MaterialTheme.typography.labelSmall)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledTonalIconButton(
                    onClick = { onCommand(ControlCommand.AcModeSwitch) },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Mode")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Mode", style = MaterialTheme.typography.labelSmall)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledTonalIconButton(
                    onClick = { onCommand(ControlCommand.AcWindSpeed) },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = "Wind Speed")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Speed", style = MaterialTheme.typography.labelSmall)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledTonalIconButton(
                    onClick = { onCommand(ControlCommand.AcWindSwing) },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(imageVector = Icons.Default.Sync, contentDescription = "Swing")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Swing", style = MaterialTheme.typography.labelSmall)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledTonalIconButton(
                    onClick = { onCommand(ControlCommand.AcWindFix) },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Wind Fix")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Wind Fix", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.weight(0.2f))
    }
}

@Composable
private fun DysonControlPanel(
    onCommand: (ControlCommand) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Dyson Screen View
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(100.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DYSON COOL/HEAT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SPEED & TEMP DUAL CTRL",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // Speed & Temp adjustment keys
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Speed Rocker
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(onClick = { onCommand(ControlCommand.DysonWindSpeedMinus) }, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Wind -")
                    }
                    Text(text = "SPD", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                    FilledTonalIconButton(onClick = { onCommand(ControlCommand.DysonWindSpeedPlus) }, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Wind +")
                    }
                }
            }

            // Temp Rocker
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(onClick = { onCommand(ControlCommand.DysonTempMinus) }, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Temp -")
                    }
                    Text(text = "TMP", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                    FilledTonalIconButton(onClick = { onCommand(ControlCommand.DysonTempPlus) }, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Temp +")
                    }
                }
            }
        }

        // Row of main functions
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledTonalIconButton(onClick = { onCommand(ControlCommand.Power) }, modifier = Modifier.size(48.dp), colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))) {
                    Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = "Power", tint = MaterialTheme.colorScheme.error)
                }
                Text(text = "Power", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledTonalIconButton(onClick = { onCommand(ControlCommand.DysonAuto) }, modifier = Modifier.size(48.dp)) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Auto")
                }
                Text(text = "Auto", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledTonalIconButton(onClick = { onCommand(ControlCommand.DysonSwing) }, modifier = Modifier.size(48.dp)) {
                    Icon(imageVector = Icons.Default.Sync, contentDescription = "Swing")
                }
                Text(text = "Swing", style = MaterialTheme.typography.labelSmall)
            }
        }

        // More functions
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledTonalIconButton(onClick = { onCommand(ControlCommand.DysonCool) }, modifier = Modifier.size(48.dp)) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Cool")
                }
                Text(text = "Cool", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledTonalIconButton(onClick = { onCommand(ControlCommand.DysonSleep) }, modifier = Modifier.size(48.dp)) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Sleep")
                }
                Text(text = "Sleep", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledTonalIconButton(onClick = { onCommand(ControlCommand.DysonDiffusion) }, modifier = Modifier.size(48.dp)) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Diffuse")
                }
                Text(text = "Diffuse", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.weight(0.2f))
    }
}

private data class GridKeyItem(
    val icon: ImageVector,
    val label: String,
    val command: ControlCommand,
)

@Composable
private fun GridControlPanel(
    category: RemoteCategory,
    onCommand: (ControlCommand) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keys = when (category) {
        RemoteCategory.FAN -> listOf(
            GridKeyItem(Icons.Default.PowerSettingsNew, "Power", ControlCommand.Power),
            GridKeyItem(Icons.Default.Speed, "Wind Speed", ControlCommand.WindSpeed),
            GridKeyItem(Icons.Default.Refresh, "Wind Type", ControlCommand.WindType),
            GridKeyItem(Icons.Default.Sync, "Swing", ControlCommand.Swing),
            GridKeyItem(Icons.Default.Add, "Wind +", ControlCommand.WindPlus),
            GridKeyItem(Icons.Default.Remove, "Wind -", ControlCommand.WindMinus)
        )
        RemoteCategory.LIGHT -> listOf(
            GridKeyItem(Icons.Default.PowerSettingsNew, "Power", ControlCommand.Power),
            GridKeyItem(Icons.Default.Add, "Bright +", ControlCommand.BulbBrightPlus),
            GridKeyItem(Icons.Default.Remove, "Bright -", ControlCommand.BulbBrightMinus),
            GridKeyItem(Icons.Default.Home, "Bright On", ControlCommand.BulbBrightPowerOn),
            GridKeyItem(Icons.Default.Menu, "Bright Off", ControlCommand.BulbBrightPowerOff),
            GridKeyItem(Icons.Default.Star, "Rainbow", ControlCommand.BulbBrightRainbow),
            GridKeyItem(Icons.Default.Check, "Color 0", ControlCommand.BulbColor0),
            GridKeyItem(Icons.Default.Check, "Color 1", ControlCommand.BulbColor1),
            GridKeyItem(Icons.Default.Check, "Color 2", ControlCommand.BulbColor2),
            GridKeyItem(Icons.Default.Check, "Color 3", ControlCommand.BulbColor3),
            GridKeyItem(Icons.Default.Check, "Color 4", ControlCommand.BulbColor4)
        )
        RemoteCategory.CLEANING_ROBOT -> listOf(
            GridKeyItem(Icons.Default.PowerSettingsNew, "Power", ControlCommand.Power),
            GridKeyItem(Icons.Default.PlayArrow, "Start", ControlCommand.RobotStart),
            GridKeyItem(Icons.Default.Stop, "Stop", ControlCommand.RobotStop),
            GridKeyItem(Icons.Default.ArrowUpward, "Forward", ControlCommand.RobotForward),
            GridKeyItem(Icons.Default.ArrowDownward, "Backward", ControlCommand.RobotBackward),
            GridKeyItem(Icons.AutoMirrored.Filled.ArrowBack, "Left", ControlCommand.RobotLeft),
            GridKeyItem(Icons.AutoMirrored.Filled.ArrowForward, "Right", ControlCommand.RobotRight),
            GridKeyItem(Icons.Default.Sync, "Auto", ControlCommand.RobotAuto),
            GridKeyItem(Icons.Default.Check, "Spot", ControlCommand.RobotSpot),
            GridKeyItem(Icons.Default.Speed, "Speed", ControlCommand.RobotSpeed),
            GridKeyItem(Icons.Default.Settings, "Timer", ControlCommand.RobotTimer),
            GridKeyItem(Icons.Default.Home, "Charge", ControlCommand.RobotCharge),
            GridKeyItem(Icons.Default.Info, "Preserve", ControlCommand.RobotPreserve)
        )
        RemoteCategory.AIRCLEANER -> listOf(
            GridKeyItem(Icons.Default.PowerSettingsNew, "Power", ControlCommand.Power),
            GridKeyItem(Icons.Default.Star, "Ion", ControlCommand.AirCleanerIon),
            GridKeyItem(Icons.Default.Sync, "Auto", ControlCommand.AirCleanerAuto),
            GridKeyItem(Icons.Default.Speed, "Wind Speed", ControlCommand.AirCleanerWindSpeed),
            GridKeyItem(Icons.Default.Refresh, "Mode Switch", ControlCommand.AirCleanerModeSwitch),
            GridKeyItem(Icons.Default.Settings, "Timer", ControlCommand.AirCleanerTimer),
            GridKeyItem(Icons.Default.Home, "Light", ControlCommand.AirCleanerLight),
            GridKeyItem(Icons.Default.Info, "Force", ControlCommand.AirCleanerForce)
        )
        else -> emptyList()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(keys) { key ->
            ElevatedCard(
                onClick = { onCommand(key.command) },
                modifier = Modifier.height(72.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (key.command == ControlCommand.Power) {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = key.icon,
                        contentDescription = key.label,
                        tint = if (key.command == ControlCommand.Power) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = key.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
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
        modifier = Modifier.fillMaxWidth(),
        title = {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
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
        ),
    )
}

@Composable
private fun DPad(
    onCommand: (ControlCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(200.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        FilledTonalButton(
            onClick = { onCommand(ControlCommand.Ok) },
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.button_ok),
                modifier = Modifier.size(28.dp)
            )
        }

        IconButton(
            onClick = { onCommand(ControlCommand.Up) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = stringResource(R.string.button_up),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }

        IconButton(
            onClick = { onCommand(ControlCommand.Down) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.button_down),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }

        IconButton(
            onClick = { onCommand(ControlCommand.Left) },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.button_left),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }

        IconButton(
            onClick = { onCommand(ControlCommand.Right) },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.button_right),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun VolumeRocker(
    onCommand: (ControlCommand) -> Unit,
) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FilledTonalIconButton(
            onClick = { onCommand(ControlCommand.Minus) },
            modifier = Modifier.size(48.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color.Transparent
            )
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = stringResource(R.string.button_minus),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "VOL",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        FilledTonalIconButton(
            onClick = { onCommand(ControlCommand.Plus) },
            modifier = Modifier.size(48.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color.Transparent
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.button_plus),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LedIndicator(connected = connected)

                Text(
                    text = if (connected) "Connected" else "Disconnected",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (connected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = ip,
                    onValueChange = onIpChange,
                    label = { Text(stringResource(R.string.emitter_ip)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                )

                FilledIconButton(
                    onClick = onConnectClick,
                    enabled = !isLoading,
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (connected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (connected) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (connected) Icons.Default.LinkOff else Icons.Default.Link,
                            contentDescription = stringResource(R.string.connect),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LedIndicator(connected: Boolean) {
    val transition = rememberInfiniteTransition(label = "LedGlow")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    Box(
        modifier = Modifier
            .size(10.dp)
            .alpha(if (connected) 1.0f else alpha)
            .background(
                color = if (connected) Color(0xFF4CAF50) else Color(0xFFF44336),
                shape = CircleShape
            )
    )
}

@Preview(showBackground = true)
@Composable
private fun ControlContentPreview() {
    IRControlTheme {
        ControlContent(
            state = ControlUiState(
                title = "IR Remote Control",
                emitterIp = "192.168.1.10",
                isEmitterConnected = true,
                category = RemoteCategory.TV
            ),
            onBack = {},
            onCommand = {},
            onEmitterIpChange = {},
            onConnectClick = {},
        )
    }
}
