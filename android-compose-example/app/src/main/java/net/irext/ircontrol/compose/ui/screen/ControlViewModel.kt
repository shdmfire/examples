package net.irext.ircontrol.compose.ui.screen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.irext.decode.sdk.utils.Constants
import net.irext.ircontrol.compose.R
import net.irext.ircontrol.compose.bean.RemoteControl
import net.irext.ircontrol.compose.controller.ArduinoRemote
import net.irext.ircontrol.compose.controller.ControlResult
import net.irext.ircontrol.compose.controller.PhoneRemote
import net.irext.ircontrol.compose.controller.RemoteController
import net.irext.ircontrol.compose.controller.base.ControlCommand
import net.irext.ircontrol.compose.utils.FileUtils

class ControlViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val appContext = application.applicationContext

    private val _uiState = MutableStateFlow(
        ControlUiState(emitterIp = application.getString(R.string.default_ip)),
    )
    val uiState: StateFlow<ControlUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ControlEvent>()
    val events: SharedFlow<ControlEvent> = _events.asSharedFlow()

    private var phoneRemote: PhoneRemote? = null
    private var arduinoRemote: ArduinoRemote? = null
    private var remoteController: RemoteController? = null
    private var currentRemoteControl: RemoteControl? = null
    private var currentRemoteId: Long = -1L

    fun loadRemote(remoteId: Long) {
        currentRemoteId = remoteId
        ensureControllers()
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }

            val remote = RemoteControl.getRemoteControl(remoteId)
            currentRemoteControl = remote

            if (remote == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            val categoryId = remote.categoryId
            val subCategoryId = remote.subCategory
            val title = buildRemoteTitle(remote)
            val binFileName = FileUtils.getBinFile(appContext, remote.remoteMap).absolutePath
            val ret = phoneRemote?.irOpen(binFileName, categoryId, subCategoryId)
            Log.d(TAG, "binary opened : $ret")

            _uiState.update {
                it.copy(
                    title = title,
                    isLoading = false,
                )
            }
        }
    }

    fun onCommand(command: ControlCommand) {
        viewModelScope.launch(Dispatchers.IO) {
            val remote = currentRemoteControl ?: return@launch
            val controller = ensureControllers()
            val result = controller.send(remote, command)
            processControlResult(result)
        }
    }

    fun onEmitterIpChange(value: String) {
        _uiState.update { it.copy(emitterIp = value) }
    }

    fun onConnectClick() {
        val state = _uiState.value
        if (state.isEmitterConnected) {
            disconnectEmitter()
        } else {
            connectEmitter(state.emitterIp)
        }
    }

    fun close() {
        arduinoRemote?.disconnect()
        phoneRemote?.irClose()
        _uiState.update {
            it.copy(
                isEmitterConnected = false,
                isLoading = false,
            )
        }
    }

    override fun onCleared() {
        close()
        super.onCleared()
    }

    private fun ensureControllers(): RemoteController {
        val phone = phoneRemote ?: PhoneRemote.getInstance(appContext).also { phoneRemote = it }
        val arduino = arduinoRemote ?: ArduinoRemote(appContext, createEmitterCallback()).also { arduinoRemote = it }
        return remoteController ?: RemoteController(phone, arduino).also { remoteController = it }
    }

    private fun createEmitterCallback(): ArduinoRemote.IRSocketEmitterCallback {
        return object : ArduinoRemote.IRSocketEmitterCallback {
            override fun onConnected() {
                onEmitterConnected()
            }

            override fun onDisconnected() {
                onEmitterDisconnected()
            }

            override fun onResponse(response: String) {
                Log.d(TAG, "onResponse: $response")
                onEmitterResponse(response)
            }
        }
    }

    private fun connectEmitter(ip: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            ensureControllers()
            arduinoRemote?.connectToEmitter(ip, ArduinoRemote.EMITTER_PORT.toString())
        }
    }

    private fun disconnectEmitter() {
        viewModelScope.launch(Dispatchers.IO) {
            arduinoRemote?.disconnect()
            _uiState.update {
                it.copy(
                    isEmitterConnected = false,
                    isLoading = false,
                )
            }
        }
    }

    private fun onEmitterConnected() {
        Log.d(TAG, "onEmitterConnected, update UI state")
        _uiState.update {
            it.copy(
                isEmitterConnected = true,
                isLoading = false,
            )
        }
    }

    private fun onEmitterDisconnected() {
        Log.d(TAG, "onEmitterDisconnected, update UI state")
        _uiState.update {
            it.copy(
                isEmitterConnected = false,
                isLoading = false,
            )
        }
        viewModelScope.launch {
            _events.emit(ControlEvent.Toast(appContext.getString(R.string.connect_disconnected)))
        }
    }

    private fun onEmitterResponse(response: String) {
        when {
            response.startsWith(ArduinoRemote.E_RESPONSE_HELLO) -> processEHello(response)
            response.startsWith(ArduinoRemote.E_RESPONSE_BIN) -> processEBin(response)
            response.startsWith(ArduinoRemote.E_RESPONSE_CTRL) -> processECtrl(response)
            response.startsWith(ArduinoRemote.E_INDICATION_SUCCESS) ||
                response.startsWith(ArduinoRemote.E_INDICATION_FAILED) -> processControlResult(response)
            else -> Log.e(TAG, "unexpected response : $response")
        }
    }

    private fun processEHello(response: String) {
        arduinoRemote?.sendHelloToEmitter()
    }

    private fun processEBin(response: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "processEBin: current remote ID = $currentRemoteId")
            val remote = currentRemoteControl ?: RemoteControl.getRemoteControl(currentRemoteId)
            if (remote == null) {
                return@launch
            }
            currentRemoteControl = remote
            Log.d(
                TAG,
                "processEBin, will send binary for remote control, id = $currentRemoteId, " +
                    "remoteControl.id = ${remote.getID()}, remoteControl.category = ${remote.categoryId}",
            )
            val binFileName = FileUtils.getBinFile(appContext, remote.remoteMap).absolutePath
            val binContent = FileUtils.getByteArrayFromFile(binFileName)
            if (binContent != null) {
                arduinoRemote?.sendBinToEmitter(
                    binContent,
                    remote.categoryId,
                    remote.subCategory,
                )
            } else {
                Log.e(TAG, "emitter sender could not open the binary file")
                _events.emit(ControlEvent.Toast(appContext.getString(R.string.file_could_not_open)))
            }
        }
    }

    private fun processECtrl(response: String) = Unit

    private fun processControlResult(result: ControlResult) {
        viewModelScope.launch {
            when (result) {
                ControlResult.Success -> _events.emit(
                    ControlEvent.Toast(appContext.getString(R.string.decode_and_send_success)),
                )
                ControlResult.Failed -> _events.emit(
                    ControlEvent.Toast(appContext.getString(R.string.decode_and_send_failed)),
                )
                ControlResult.PendingEmitterResult -> Unit
            }
        }
    }

    private fun processControlResult(response: String) {
        viewModelScope.launch {
            if (response.startsWith(ArduinoRemote.E_INDICATION_SUCCESS)) {
                _events.emit(ControlEvent.Toast(appContext.getString(R.string.decode_and_send_success)))
            } else {
                _events.emit(ControlEvent.Toast(appContext.getString(R.string.decode_and_send_failed)))
            }
        }
    }

    private fun buildRemoteTitle(remote: RemoteControl): String {
        return if (Constants.CategoryID.STB.value == remote.categoryId) {
            remote.cityName + remote.operatorName + remote.categoryName + " - " + remote.remote
        } else {
            remote.brandName + remote.categoryName + " - " + remote.remote
        }
    }

    private companion object {
        private const val TAG = "ControlViewModel"
    }
}
