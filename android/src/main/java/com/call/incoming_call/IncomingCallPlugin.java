package com.call.incoming_call;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

import static android.content.ContentValues.TAG;

/**
 * IncomingCallPlugin
 */
@TargetApi(Build.VERSION_CODES.M)
public class IncomingCallPlugin implements FlutterPlugin, MethodCallHandler, PluginRegistry.RequestPermissionsResultListener, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Activity activity;


    public IncomingCallPlugin() {

    }

    private IncomingCallPlugin(PluginRegistry.Registrar registrar) {
        activity = registrar.activity();
        channel = new MethodChannel(registrar.messenger(), "incoming_call");
        channel.setMethodCallHandler(this);
        if (PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.READ_CALL_LOG)) {
            registerCallListener();
        } else {
            String[] perm = {Manifest.permission.READ_CALL_LOG};
            activity.requestPermissions(perm, 0);
        }
    }

    private Result result;
    private PhoneStateListener mPhoneListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {

            try {
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        channel.invokeMethod("phone.disconnected", true);
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (!incomingNumber.isEmpty()) {
                            channel.invokeMethod("phone.incoming", incomingNumber);
                        } else {
                            try {
                                getCallDetails();
                            } catch (Exception e) {
                                int t = 0;
                                t = 1;
                            }

//                            getCallDetails();
                        }
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        channel.invokeMethod("phone.connected", true);
                        break;

                    default:
                        Log.d(TAG, "Unknown phone state=" + String.valueOf(state));
                }
            } catch (Exception e) {
                Log.e("TAG", "Exception");
            }
        }
    };

    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    public static void registerWith(PluginRegistry.Registrar registrar) {

        final IncomingCallPlugin callLogPlugin = new IncomingCallPlugin(registrar);

        registrar.addRequestPermissionsResultListener(callLogPlugin);

    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "incoming_call");
        channel.setMethodCallHandler(this);

    }

    public void getCallDetails() {

        Cursor managedCursor = activity.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC");
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        boolean find = false;
        while (managedCursor.moveToNext()) {
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            int dirCode = Integer.parseInt(callType);
            switch (dirCode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    channel.invokeMethod("phone.incoming", phNumber);
                    find = true;
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    break;
            }
            if (find) {
                break;
            }
        }
        managedCursor.close();
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);

    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();

        if (PackageManager.PERMISSION_GRANTED == binding.getActivity().checkSelfPermission(Manifest.permission.READ_CALL_LOG)) {
            registerCallListener();
        } else {
            String[] perm = {Manifest.permission.READ_CALL_LOG};
            binding.getActivity().requestPermissions(perm, 0);
        }
    }

    void registerCallListener() {
        TelephonyManager tm = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {

    }

    private void cleanup() {
        channel = null;
        result = null;
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            registerCallListener();
            return true;
        } else {
            if (result != null) {
                result.error("PERMISSION_NOT_GRANTED", null, null);
                cleanup();
            }
            return false;
        }
    }
}
