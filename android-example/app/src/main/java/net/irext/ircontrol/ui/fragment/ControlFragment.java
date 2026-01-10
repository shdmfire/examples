package net.irext.ircontrol.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.hardware.ConsumerIrManager;
import android.os.*;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import net.irext.decode.sdk.IRDecode;
import net.irext.decode.sdk.bean.ACStatus;
import net.irext.decode.sdk.utils.Constants;
import net.irext.ircontrol.R;
import net.irext.ircontrol.bean.RemoteControl;
import net.irext.ircontrol.ui.activity.ControlActivity;
import net.irext.ircontrol.utils.FileUtils;
import net.irext.ircontrol.utils.MessageUtil;
import net.irext.ircontrol.utils.ToastUtils;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

import net.irext.ircontrol.utils.IRSocketEmitter;

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

    private static final int KEY_POWER = 0;
    private static final int KEY_UP = 1;
    private static final int KEY_DOWN = 2;
    private static final int KEY_LEFT = 3;
    private static final int KEY_RIGHT = 4;
    private static final int KEY_OK = 5;
    private static final int KEY_PLUS = 6;
    private static final int KEY_MINUS = 7;
    private static final int KEY_BACK = 8;
    private static final int KEY_HOME = 9;
    private static final int KEY_MENU = 10;

    private IRSocketEmitter mIRSocketEmitter;

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

        // Initialize IRSocketEmitter with callback
        mIRSocketEmitter = new IRSocketEmitter(new IRSocketEmitter.IRSocketEmitterCallback() {
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
                if (isIpAddress(emitterIp)) {
                    ToastUtils.showToast(mParent, mParent.getString(R.string.input_emitter_ip_address), null);
                    return;
                }
                mIRSocketEmitter.connectToEmitter(emitterIp, String.valueOf(IRSocketEmitter.EMITTER_PORT));
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

    private void getRemote() {
        new Thread() {
            @Override
            public void run() {
                MessageUtil.postMessage(mHandler, CMD_GET_REMOTE_CONTROL);
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

    private int[] irControl(int keyCode) {
        int inputKeyCode;
        ACStatus acStatus = new ACStatus();
        /* decode SDK - decode according to key code */
        if (Constants.CategoryID.AIR_CONDITIONER.getValue() ==
                mCurrentRemoteControl.getCategoryId()) {
            acStatus.setAcPower(Constants.ACPower.POWER_OFF.getValue());
            acStatus.setAcMode(Constants.ACMode.MODE_COOL.getValue());
            acStatus.setAcTemp(Constants.ACTemperature.TEMP_24.getValue());
            acStatus.setAcWindSpeed(Constants.ACWindSpeed.SPEED_AUTO.getValue());
            acStatus.setAcWindDir(Constants.ACSwing.SWING_ON.getValue());
            acStatus.setChangeWindDir(0);
            acStatus.setAcDisplay(0);
            acStatus.setAcTimer(0);
            acStatus.setAcSleep(0);

            switch(keyCode) {
                case KEY_POWER:
                    // power key --> change power
                    inputKeyCode = Constants.ACFunction.FUNCTION_SWITCH_POWER.getValue();
                    break;
                case KEY_UP:
                    // up key --> change wind speed
                    inputKeyCode = Constants.ACFunction.FUNCTION_SWITCH_WIND_SPEED.getValue();
                    break;
                case KEY_DOWN:
                    // down key --> change wind dir
                    inputKeyCode = Constants.ACFunction.FUNCTION_SWITCH_WIND_DIR.getValue();
                    break;
                case KEY_RIGHT:
                    // right key --> change mode
                    inputKeyCode = Constants.ACFunction.FUNCTION_CHANGE_MODE.getValue();
                    break;
                case KEY_OK:
                    // center key --> fix wind dir
                    inputKeyCode = Constants.ACFunction.FUNCTION_SWITCH_SWING.getValue();
                    break;
                case KEY_PLUS:
                    // plus key --> temp up
                    inputKeyCode = Constants.ACFunction.FUNCTION_TEMPERATURE_UP.getValue();
                    break;
                case KEY_MINUS:
                    // minus key --> temp down
                    inputKeyCode = Constants.ACFunction.FUNCTION_TEMPERATURE_DOWN.getValue();
                    break;

                default:
                    return null;
            }
        } else {
            inputKeyCode = keyCode;
        }

        /* decode SDK - decode from binary */
        /* translate key code for AC according to the mapping above */
        /* ac status is useless for decoding devices other than AC, it's an optional parameter */
        /* change wind dir is an optional parameter, set to 0 as default */
        return mIRDecode.decodeBinary(inputKeyCode, acStatus, 0);
    }

    private void onEmitterConnected() {
        Log.d(TAG, "the emitter is connected");
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
        mIRSocketEmitter.sendHelloToEmitter();
    }

    private void processEBin(String response) {
        String binFileName = FileUtils.binDir + FileUtils.FILE_NAME_PREFIX +
                mCurrentRemoteControl.getRemoteMap() + FileUtils.FILE_NAME_EXT;
        byte []binContent = FileUtils.getByteArrayFromFile(binFileName);
        mIRSocketEmitter.sendBinToEmitter(binContent, mCurrentRemoteControl.getCategoryId(), mCurrentRemoteControl.getSubCategory());
    }

    private void processECtrl(String response) {

    }

    private void onEmitterResponse(String response) {
        Log.d(TAG, "emitter: " + response);
        if (response.startsWith(IRSocketEmitter.E_RESPONSE_HELLO)) {
            processEHello(response);
        } else if (response.startsWith(IRSocketEmitter.E_RESPONSE_BIN)) {
            processEBin(response);
        } else if (response.startsWith(IRSocketEmitter.E_RESPONSE_CTRL)) {
            processECtrl(response);
        } else {
            Log.e(TAG, "unexpected response : " + response);
        }
    }

    // control
    @Override
    public void onClick(View v) {
        vibrate(mParent);
        // decode directly in mobile phone
        int []decoded = null;
        int id = v.getId();
        if (id == R.id.iv_power) {
            decoded = irControl(KEY_POWER);
        } else if (id == R.id.iv_up) {
            decoded = irControl(KEY_UP);
        } else if (id == R.id.iv_down) {
            decoded = irControl(KEY_DOWN);
        } else if (id == R.id.iv_left) {
            decoded = irControl(KEY_LEFT);
        } else if (id == R.id.iv_right) {
            decoded = irControl(KEY_RIGHT);
        } else if (id == R.id.iv_ok) {
            decoded = irControl(KEY_OK);
        } else if (id == R.id.iv_plus) {
            decoded = irControl(KEY_PLUS);
        } else if (id == R.id.iv_minus) {
            decoded = irControl(KEY_MINUS);
        } else if (id == R.id.iv_back) {
            decoded = irControl(KEY_BACK);
        } else if (id == R.id.iv_home) {
            decoded = irControl(KEY_HOME);
        } else if (id == R.id.iv_menu) {
            decoded = irControl(KEY_MENU);
        }

        // debug decoded value
        StringBuilder decodedValue = new StringBuilder();
        for (int i = 0; i < Objects.requireNonNull(decoded).length; i++) {
            decodedValue.append(decoded[i]);
            decodedValue.append(",");
        }
        Log.d(TAG, "decodedValue : " + decodedValue);
        if (mIRSocketEmitter.isConnected()) {
            Log.d(TAG, "emitter available, send decoded to emitter");
            mIRSocketEmitter.sendDecodedToEmitter(decodedValue.toString());
        }
        // send decoded integer array to IR emitter
        ConsumerIrManager irEmitter =
                (ConsumerIrManager) mParent.getSystemService(Context.CONSUMER_IR_SERVICE);
        if (null != irEmitter && irEmitter.hasIrEmitter()) {
            if (decoded.length > 0) {
                irEmitter.transmit(38000, decoded);
            }
        } else {
            ToastUtils.showToast(mParent, this.getString(R.string.ir_not_supported), null);
        }
    }

    private boolean isIpAddress(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            return Objects.equals(inetAddress.getHostAddress(), ipAddress);
        } catch (UnknownHostException e) {
            return false;
        }
    }

    private static class MsgHandler extends Handler {

        WeakReference<ControlFragment> mMainFragment;

        MsgHandler(ControlFragment fragment) {
            super(Looper.getMainLooper());
            mMainFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            int cmd = msg.getData().getInt(MessageUtil.KEY_CMD);

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
