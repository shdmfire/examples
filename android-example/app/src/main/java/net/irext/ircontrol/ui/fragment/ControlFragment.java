package net.irext.ircontrol.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.*;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import net.irext.decode.sdk.IRDecode;
import net.irext.ircontrol.R;
import net.irext.ircontrol.bean.RemoteControl;
import net.irext.ircontrol.controller.ArduinoRemote;
import net.irext.ircontrol.controller.ArduinoSocket;
import net.irext.ircontrol.controller.PhoneRemote;
import net.irext.ircontrol.controller.base.IRemote;
import net.irext.ircontrol.ui.activity.ControlActivity;
import net.irext.ircontrol.utils.FileUtils;
import net.irext.ircontrol.utils.MessageUtils;
import net.irext.ircontrol.utils.MiscUtils;
import net.irext.ircontrol.utils.ToastUtils;

import java.lang.ref.WeakReference;

/**
 * Filename:       ControlFragment.java
 * Revised:        Date: 2017-04-22
 * Revision:       Revision: 1.0
 * <p>
 * Description:    Control fragment containing control panel
 * <p>
 * Revision log:
 * 2017-04-22: created by strawmanbobi
 */
@SuppressWarnings("unused")
public class ControlFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = ControlFragment.class.getSimpleName();

    private static final int VIB_TIME = 60;

    private static final int CMD_GET_REMOTE_CONTROL = 0;

    private ArduinoSocket mArduinoSocket;

    private MsgHandler mHandler;

    private ControlActivity mParent;
    private Long mRemoteID;
    private RemoteControl mCurrentRemoteControl;

    // define the single instance of IRDecode
    private IRDecode mIRDecode;
    private EditText mEtEmitterIp;
    private ImageButton mBtnConnect;
    private View mVWConnectStatus;

    public ControlFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mIRDecode = IRDecode.getInstance();
        mHandler = new MsgHandler(this);

        mParent = (ControlActivity)getActivity();
        View view = inflater.inflate(R.layout.fragment_control, container, false);

        ImageButton btnPower = view.findViewById(R.id.iv_power);
        ImageButton btnBack = view.findViewById(R.id.iv_back);
        ImageButton btnHome = view.findViewById(R.id.iv_home);
        ImageButton btnMenu = view.findViewById(R.id.iv_menu);
        ImageButton btnUp = view.findViewById(R.id.iv_up);
        ImageButton btnDown = view.findViewById(R.id.iv_down);
        ImageButton btnLeft = view.findViewById(R.id.iv_left);
        ImageButton btnRight = view.findViewById(R.id.iv_right);
        ImageButton btnOK = view.findViewById(R.id.iv_ok);
        ImageButton btnPlus = view.findViewById(R.id.iv_plus);
        ImageButton btnMinus = view.findViewById(R.id.iv_minus);

        btnPower.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnHome.setOnClickListener(this);
        btnMenu.setOnClickListener(this);
        btnUp.setOnClickListener(this);
        btnDown.setOnClickListener(this);
        btnLeft.setOnClickListener(this);
        btnRight.setOnClickListener(this);
        btnOK.setOnClickListener(this);
        btnPlus.setOnClickListener(this);
        btnMinus.setOnClickListener(this);

        mEtEmitterIp = view.findViewById(R.id.emitter_ip);
        mBtnConnect = view.findViewById(R.id.btn_connect_emitter);
        mVWConnectStatus = view.findViewById(R.id.vw_connect_status);

        // Initialize ArduinoSocket with callback
        mArduinoSocket = new ArduinoSocket(new ArduinoSocket.IRSocketEmitterCallback() {
            @Override
            public void onConnected() {
                onEmitterConnected();
            }

            @Override
            public void onDisconnected() {
                onEmitterDisconnected();
            }

            @Override
            public void onResponse(String response) {
                onEmitterResponse(response);
            }
        });

        mBtnConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                vibrate(mParent);
                String emitterIp = mEtEmitterIp.getText().toString();
                if (!MiscUtils.isValidIPv4(emitterIp)) {
                    Log.e(TAG, "IP address is invalid: " + emitterIp);
                    ToastUtils.showToast(mParent, mParent.getString(R.string.input_emitter_ip_address), null);
                    return;
                }
                mArduinoSocket.connectToEmitter(emitterIp, String.valueOf(ArduinoSocket.EMITTER_PORT));
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        assert getArguments() != null;
        mRemoteID  = getArguments().getLong(ControlActivity.KEY_REMOTE_ID, -1L);
        if (-1 == mRemoteID) {
            Log.d(TAG, "remote ID IS NULL");
        } else {
            getRemote();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mArduinoSocket.disconnect();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mArduinoSocket.disconnect();
    }

    private void getRemote() {
        new Thread() {
            @Override
            public void run() {
                MessageUtils.postMessage(mHandler, CMD_GET_REMOTE_CONTROL);
            }
        }.start();
    }

    private void showRemote() {
        mCurrentRemoteControl = RemoteControl.getRemoteControl(mRemoteID);
        if (null != mCurrentRemoteControl) {
            int category = mCurrentRemoteControl.getCategoryId();
            String binFileName = FileUtils.binDir + FileUtils.FILE_NAME_PREFIX +
                    mCurrentRemoteControl.getRemoteMap() + FileUtils.FILE_NAME_EXT;

            /* decode SDK - load binary file */
            int ret = mIRDecode.openFile(category, mCurrentRemoteControl.getSubCategory(), binFileName);
            Log.d(TAG, "binary opened : " + ret);
        }
    }

    public void closeIRBinary() {
        mIRDecode.closeBinary();
    }

    private void onEmitterConnected() {
        mParent.runOnUiThread(() -> {
            mBtnConnect.setImageDrawable(AppCompatResources.getDrawable(mParent, R.mipmap.button_unlink));
            mVWConnectStatus.setBackgroundColor(Color.parseColor("#3FAFFF"));
        });
    }
    private void onEmitterDisconnected() {
        mParent.runOnUiThread(() -> {
            ToastUtils.showToast(mParent, mParent.getString(R.string.connect_disconnected), Toast.LENGTH_SHORT);
            mBtnConnect.setImageDrawable(AppCompatResources.getDrawable(mParent, R.mipmap.button_link));
            mVWConnectStatus.setBackgroundColor(Color.parseColor("#FF7F7F"));
        });
    }

    private void processEHello(String response) {
        mArduinoSocket.sendHelloToEmitter();
    }

    private void processEBin(String response) {
        String binFileName = FileUtils.binDir + FileUtils.FILE_NAME_PREFIX +
                mCurrentRemoteControl.getRemoteMap() + FileUtils.FILE_NAME_EXT;
        byte []binContent = FileUtils.getByteArrayFromFile(binFileName);
        if (null != binContent) {
            mArduinoSocket.sendBinToEmitter(binContent, mCurrentRemoteControl.getCategoryId(), mCurrentRemoteControl.getSubCategory());
        } else {
            Log.e(TAG, "emitter sender could not open the binary file");
            ToastUtils.showToast(mParent, mParent.getString(R.string.file_could_not_open), Toast.LENGTH_SHORT);
        }
    }

    private void processECtrl(String response) {
        ;
    }

    private void onEmitterResponse(String response) {
        if (response.startsWith(ArduinoSocket.E_RESPONSE_HELLO)) {
            processEHello(response);
        } else if (response.startsWith(ArduinoSocket.E_RESPONSE_BIN)) {
            processEBin(response);
        } else if (response.startsWith(ArduinoSocket.E_RESPONSE_CTRL)) {
            processECtrl(response);
        } else {
            Log.e(TAG, "unexpected response : " + response);
        }
    }

    // control
    @Override
    public void onClick(View v) {
        vibrate(mParent);
        IRemote remote = null;
        int keyCode = 0;
        int id = v.getId();
        if (id == R.id.iv_power) {
            keyCode = IRemote.KEY_POWER;
        } else if (id == R.id.iv_up) {
            keyCode = IRemote.KEY_UP;
        } else if (id == R.id.iv_down) {
            keyCode = IRemote.KEY_DOWN;
        } else if (id == R.id.iv_left) {
            keyCode = IRemote.KEY_LEFT;
        } else if (id == R.id.iv_right) {
            keyCode = IRemote.KEY_RIGHT;
        } else if (id == R.id.iv_ok) {
            keyCode = IRemote.KEY_OK;
        } else if (id == R.id.iv_plus) {
            keyCode = IRemote.KEY_PLUS;
        } else if (id == R.id.iv_minus) {
            keyCode = IRemote.KEY_MINUS;
        } else if (id == R.id.iv_back) {
            keyCode = IRemote.KEY_BACK;
        } else if (id == R.id.iv_home) {
            keyCode = IRemote.KEY_HOME;
        } else if (id == R.id.iv_menu) {
            keyCode = IRemote.KEY_MENU;
        }

        if (mArduinoSocket.getConnectionStatus() == ArduinoSocket.EMITTER_WORKING) {
            remote = new ArduinoRemote(mParent, mArduinoSocket);
        } else {
            remote = new PhoneRemote(mParent);
        }
        remote.irControl(mCurrentRemoteControl.getCategoryId(), mCurrentRemoteControl.getSubCategory(), keyCode);
    }

    private static class MsgHandler extends Handler {

        WeakReference<ControlFragment> mMainFragment;

        MsgHandler(ControlFragment fragment) {
            super(Looper.getMainLooper());
            mMainFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            int cmd = msg.getData().getInt(MessageUtils.KEY_CMD);

            ControlFragment controlFragment = mMainFragment.get();
            if (cmd == CMD_GET_REMOTE_CONTROL) {
                controlFragment.showRemote();
            }
        }
    }

    // vibrate on button click in this fragment
    private static void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VIB_TIME);
    }
}
