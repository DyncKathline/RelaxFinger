package com.hardwork.fg607.relaxfinger.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import com.hardwork.fg607.relaxfinger.SettingActivity;
import com.hardwork.fg607.relaxfinger.service.FloatService;
import com.hardwork.fg607.relaxfinger.model.Config;
import com.hardwork.fg607.relaxfinger.service.NavAccessibilityService;
import com.hardwork.fg607.relaxfinger.utils.FloatingBallUtils;

import net.grandcentrix.tray.AppPreferences;
import net.grandcentrix.tray.TrayAppPreferences;
import net.grandcentrix.tray.TrayPreferences;

/**
 * Created by fg607 on 15-8-23.
 */
public class FloatBallReceiver extends BroadcastReceiver {

    private Context mContext;
    private SharedPreferences sp;


    public void onReceive(Context context, Intent intent) {


        mContext = context;

        sp = FloatingBallUtils.getSharedPreferences();

        String action = intent.getAction();

        ComponentName selfComponentName = new ComponentName(context.getPackageName(),
                NavAccessibilityService.class.getName());
        String flattenToString = selfComponentName.flattenToString();
        Settings.Secure.putString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                flattenToString);
        Settings.Secure.putInt(context.getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED, 1);

        switch (action){

            case Intent.ACTION_BOOT_COMPLETED:

                /*if(sp.getBoolean("autoStartSwitch",false) && sp.getBoolean("floatSwitch", false)){

                    sendMsg(Config.FLOAT_SWITCH, "ballstate", true);
                }*/

                if(sp.getBoolean("floatSwitch", false)){


                    sendMsg(Config.FLOAT_SWITCH, "ballstate", true);
                }
                break;
            default:
                break;

        }
    }

    public  void sendMsg(int what,String name,boolean action) {
        Intent intent = new Intent();
        intent.putExtra("what",what);
        intent.putExtra(name, action);
        intent.setClass(mContext, FloatService.class);
        mContext.startService(intent);
    }
}