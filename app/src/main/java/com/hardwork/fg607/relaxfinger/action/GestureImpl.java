package com.hardwork.fg607.relaxfinger.action;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.widget.Toast;

import com.hardwork.fg607.relaxfinger.MyApplication;
import com.hardwork.fg607.relaxfinger.manager.FloatViewManager;
import com.hardwork.fg607.relaxfinger.model.MenuDataSugar;
import com.hardwork.fg607.relaxfinger.service.NavAccessibilityService;
import com.hardwork.fg607.relaxfinger.utils.AccessibilityUtil;
import com.hardwork.fg607.relaxfinger.utils.FloatingBallUtils;
import com.hardwork.fg607.relaxfinger.view.BallView;
import com.hardwork.fg607.relaxfinger.view.MenuViewProxy;
import com.orm.SugarRecord;

import net.grandcentrix.tray.AppPreferences;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.hardwork.fg607.relaxfinger.utils.AccessibilityUtil.checkAccessibility;
import static com.hardwork.fg607.relaxfinger.view.MenuViewProxy.MENU_A;
import static com.hardwork.fg607.relaxfinger.view.MenuViewProxy.MENU_B;
import static com.hardwork.fg607.relaxfinger.view.MenuViewProxy.MENU_C;
import static com.hardwork.fg607.relaxfinger.view.MenuViewProxy.MENU_D;
import static com.hardwork.fg607.relaxfinger.view.MenuViewProxy.MENU_E;

/**
 * Created by fg607 on 16-11-25.
 */

public class GestureImpl implements BallView.OnGestureListener,MenuViewProxy.OnMenuItemClickListener{

    public static final int SINGLE_TAP = 0;
    public static final int DOUBLE_TAP = 1;
    public static final int LONGPRESS = 2;
    public static final int SWIPE_UP = 3;
    public static final int SWIPE_DOWN = 4;
    public static final int SWIPE_LEFT = 5;
    public static final int SWIPE_RIGHT= 6;

    private FloatViewManager mManager;
    private ArrayList<String> mCurrentFuncList = new ArrayList<>();
    private AppPreferences mPreferences;

    private boolean mIsDoubletapNone = false;
    private boolean mIsLongPressVibrate;
    private boolean mIsGestureFeedback;
    private Context mContext;

    public GestureImpl(FloatViewManager manager){

        SugarRecord.executeQuery("CREATE TABLE IF NOT EXISTS HIDE_APP_INFO (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, APP_NAME TEXT, PACKAGE_NAME TEXT UNIQUE)");

        mContext = MyApplication.getApplication();

        mManager = manager;

        mManager.setGestureListener(this);
        mManager.setMenuItemClickListener(this);

        mPreferences = FloatingBallUtils.getMultiProcessPreferences();

        mIsLongPressVibrate = mPreferences.getBoolean("vibratorSwitch",true);
        mIsGestureFeedback = mPreferences.getBoolean("feedbackSwitch",true);

        loadFunction();
    }

    public void loadFunction() {

        if (mCurrentFuncList.size() == 0) {
            mCurrentFuncList.add(mPreferences.getString("click", "返回键"));
            mCurrentFuncList.add(mPreferences.getString("doubleClick", "无操作"));
            mCurrentFuncList.add(mPreferences.getString("longPress", "无操作"));
            mCurrentFuncList.add(mPreferences.getString("swipeUp", "无操作"));
            mCurrentFuncList.add(mPreferences.getString("swipeDown", "无操作"));
            mCurrentFuncList.add(mPreferences.getString("swipeLeft", "无操作"));
            mCurrentFuncList.add(mPreferences.getString("swipeRight", "无操作"));
        } else {
            mCurrentFuncList.set(SINGLE_TAP, mPreferences.getString("click", "返回键"));
            mCurrentFuncList.set(DOUBLE_TAP, mPreferences.getString("doubleClick", "无操作"));
            mCurrentFuncList.set(LONGPRESS, mPreferences.getString("longPress", "无操作"));
            mCurrentFuncList.set(SWIPE_UP, mPreferences.getString("swipeUp", "无操作"));
            mCurrentFuncList.set(SWIPE_DOWN, mPreferences.getString("swipeDown", "无操作"));
            mCurrentFuncList.set(SWIPE_LEFT, mPreferences.getString("swipeLeft", "无操作"));
            mCurrentFuncList.set(SWIPE_RIGHT, mPreferences.getString("swipeRight", "无操作"));
        }

        if("无操作".equals(mCurrentFuncList.get(DOUBLE_TAP))){

            mIsDoubletapNone = true;

        }else {

            mIsDoubletapNone = false;
        }
    }

    @Override
    public void onSingleTap() {

        if(!mIsDoubletapNone){

            checkFeedback();

            //点击关闭临时移动模式
            if(mManager.isBallFreeMode()){

                if(mManager.isKeyboardShowing()){

                    executeAction(mCurrentFuncList.get(SINGLE_TAP));
                }
                mManager.setFloatAutoMove(false);

                if(mManager.isHalfHideMode()){

                    mManager.resetHalfHideTime();
                }


                return;
            }

            //显示通知时点击打开通知
            if(mManager.hasNotification()){

                mManager.openNotification();
                mManager.showNextNotify();

            }else {

                executeAction(mCurrentFuncList.get(SINGLE_TAP));
            }

            if(mManager.isHalfHideMode()){

                mManager.resetHalfHideTime();
            }

        }


    }

    @Override
    public void onQucikSingleTap() {

        if(mIsDoubletapNone){

            checkFeedback();

            //点击关闭临时移动模式
            if(mManager.isBallFreeMode()){

                mManager.setFloatAutoMove(false);

                if(mManager.isKeyboardShowing()){

                    FloatingBallUtils.keyBack(NavAccessibilityService.instance);
                }

                if(mManager.isHalfHideMode()){

                    mManager.resetHalfHideTime();
                }

                return;
            }

            //显示通知时点击打开通知
            if(mManager.hasNotification()){

                mManager.openNotification();
                mManager.showNextNotify();

            }else {

                executeAction(mCurrentFuncList.get(SINGLE_TAP));
            }

            if(mManager.isHalfHideMode()){

                mManager.resetHalfHideTime();
            }

        }
    }

    @Override
    public void onDoubleTap() {

        checkFeedback();
        executeAction(mCurrentFuncList.get(DOUBLE_TAP));

        if(mManager.isHalfHideMode()){

            mManager.resetHalfHideTime();
        }
    }

    @Override
    public void onLongPress() {

        checkLongPressVibrate();
        executeAction(mCurrentFuncList.get(LONGPRESS));

        if(mManager.isHalfHideMode()){

            mManager.resetHalfHideTime();
        }
    }

    @Override
    public void onScrollUp() {


        checkFeedback();

        mManager.showFromEdge();

        //显示通知时上滑忽略通知
        if(mManager.hasNotification()){

            mManager.ignoreNotification();
            mManager.showNextNotify();

        }else {

            executeAction(mCurrentFuncList.get(SWIPE_UP));
        }

        if(mManager.isHalfHideMode()){

            mManager.resetHalfHideTime();
        }

    }

    @Override
    public void onScrollDown() {

        checkFeedback();

        mManager.showFromEdge();

        //显示通知时下滑忽略所有通知
        if(mManager.hasNotification()){

            mManager.ignoreAllNotification();

        }else {

            executeAction(mCurrentFuncList.get(SWIPE_DOWN));
        }

        if(mManager.isHalfHideMode()){

            mManager.resetHalfHideTime();
        }

    }

    @Override
    public void onScrollLeft() {

        checkFeedback();

        //显示通知时左滑忽略通知
        if(mManager.hasNotification()){

            mManager.ignoreNotification();
            mManager.showNextNotify();

            return;

        }

        if(!mManager.showFromEdge()){

            executeAction(mCurrentFuncList.get(SWIPE_LEFT));
        }

        if(mManager.isHalfHideMode()){

            mManager.resetHalfHideTime();
        }
    }

    @Override
    public void onScrollRight() {

        checkFeedback();

        //显示通知时右滑忽略通知
        if(mManager.hasNotification()){

            mManager.ignoreNotification();
            mManager.showNextNotify();

            return;

        }

        if(!mManager.showFromEdge()){

            executeAction(mCurrentFuncList.get(SWIPE_RIGHT));
        }

        if(mManager.isHalfHideMode()){

            mManager.resetHalfHideTime();
        }
    }

    @Override
    public void onMove() {

        if(!mManager.isBallFreeMode() && mManager.isShowHideArea()){

            mManager.showHideArea();
        }
    }

    @Override
    public void onDown() {

    }

    private void checkFeedback() {

        if(mIsGestureFeedback){

            mManager.performGestureVibrate();
        }
    }

    private void checkLongPressVibrate(){

        if(mIsLongPressVibrate){

            mManager.performFeedback();
        }
    }


    private void executeAction(String action){

        switch (action) {
            case "移动(固定)悬浮球":
                mManager.setBallMove(true);
                break;
            case "临时移动":
                mManager.setFloatAutoMove(true);
                break;
            case "快捷菜单":
                if (mManager.isExistMenuItem()) {

                    mManager.popUpMenu();

                } else {

                    Toast.makeText(mContext, "还没有设置快捷菜单！", Toast.LENGTH_SHORT).show();
                }

                break;
            case "返回键":
                if (checkAccessibility()) {

                    mManager.closeMenu();

                    FloatingBallUtils.keyBack(NavAccessibilityService.instance);

                }else {

                    AccessibilityUtil.openSettingActivity();
                }

                break;
            case "Home键":
                mManager.closeMenu();
                FloatingBallUtils.keyHome();
                break;
            case "最近任务键":
                if (!checkAccessibility()) {

                    AccessibilityUtil.openSettingActivity();
                    return;
                }
                FloatingBallUtils.openRecnetTask(NavAccessibilityService.instance);
                break;
            case "切换上一个应用":
                FloatingBallUtils.previousApp(NavAccessibilityService.instance);
                break;
            case "休眠(需要开启锁屏功能)":
                FloatingBallUtils.lockScreen();
                break;
            case "电源面板":
                if (!checkAccessibility()) {

                    AccessibilityUtil.openSettingActivity();
                    return;
                }
                FloatingBallUtils.openPowerDialog(NavAccessibilityService.instance);
                break;
            case "快速设置":
                if (!checkAccessibility()) {

                    AccessibilityUtil.openSettingActivity();
                    return;
                }
                FloatingBallUtils.openQuickSetting(NavAccessibilityService.instance);
                break;
            case "屏幕截图":
                FloatingBallUtils.screenShot();
                break;
            case "通知栏":
                if (!checkAccessibility()) {

                    AccessibilityUtil.openSettingActivity();
                    return;
                }
                FloatingBallUtils.openNotificationBar(NavAccessibilityService.instance);
                break;
            case "音量键加":
                FloatingBallUtils.volumeUp();
                break;
            case "音量键减":
                FloatingBallUtils.volumeDown();
                break;
            case "隐藏悬浮球":
                mManager.hideToNotifyBar();
                break;
            default:
                break;

        }


    }



    @Override
    public void clickeMenuA() {

        menuClick(MENU_A);
    }

    @Override
    public void clickeMenuB() {

        menuClick(MENU_B);
    }

    @Override
    public void clickeMenuC() {

        menuClick(MENU_C);
    }

    @Override
    public void clickeMenuD() {

        menuClick(MENU_D);
    }

    @Override
    public void clickeMenuE() {

        menuClick(MENU_E);
    }

    @Override
    public void closeMenu() {

        mManager.closeMenu();
    }

    /**
     * 点击功能键
     *
     * @param whichApp
     */
    private void menuClick(String whichApp) {

        List<MenuDataSugar> menuDatalist = MenuDataSugar.findWithQuery(MenuDataSugar.class, "select * from MENU_DATA_SUGAR" +
                " where WHICH_MENU='" + whichApp + "'");


        int size = menuDatalist.size();

        if (size == 0) {//菜单没有相关内容

            Toast.makeText(mContext, "找不到该应用程序！", Toast.LENGTH_SHORT).show();
            mManager.updateMenuIcon(whichApp);
            return;
        }

        if (size == 1) {

            MenuDataSugar dataSugar = menuDatalist.get(0);

            try {
                dataSugar.click();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mContext, "找不到该应用程序！", Toast.LENGTH_SHORT).show();
                MenuDataSugar.executeQuery("delete from MENU_DATA_SUGAR where ACTION='" + dataSugar.getAction() + "'");
                mManager.updateMenuIcon(whichApp);
            }

            mManager.closeMenu();


        } else if (size > 1) {//菜单是文件夹

            mManager.showMenuFolder(menuDatalist);

        }
    }


    public void setVibrator(boolean isActivate) {


        mIsLongPressVibrate = isActivate;

    }

    public void setFeedback(boolean isActivate) {

        mIsGestureFeedback = isActivate;

    }

    public void saveScreenShot(final Parcelable screenShot) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                Bitmap bitmap = null;

                if(screenShot instanceof Bitmap){

                    bitmap = (Bitmap) screenShot;

                }else {

                    return;
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");

                Date date = new Date();

                String strDate = dateFormat.format(date);

                try {
                    String filePath = FloatingBallUtils.saveBitmap(bitmap,strDate+".png");

                    FloatingBallUtils.scanFile(mContext,filePath);

                    //Toast.makeText(this, "截图成功！", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();

                    //Toast.makeText(this,"截图失败！",Toast.LENGTH_SHORT).show();
                }

                FloatingBallUtils.screenShotBitmap = null;
                bitmap.recycle();

            }
        }).start();
    }
}
