package com.hardwork.fg607.relaxfinger.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtil {
    private static volatile ThreadUtil sTaskService;
    private final ExecutorService mExecutorService;
    private final Handler mHandler;


    private ThreadUtil(){
        mExecutorService = Executors.newCachedThreadPool();
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static ThreadUtil getInstance(){
        if(sTaskService==null){
            synchronized (ThreadUtil.class){
                if(sTaskService==null){
                    sTaskService = new ThreadUtil();
                }
            }
        }
        return sTaskService;
    }

    public void doBackTask(Runnable runnable){
        if(runnable!=null){
            mExecutorService.submit(runnable);
        }
    }

    public void doBackTaskDelay(final Runnable runnable, long delay){
        if(runnable!=null){
            if(delay<0){
                delay = 0;
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mExecutorService.submit(runnable);
                }
            },delay);
        }
    }

    public void postTaskInMain(Runnable runnable){
        if(runnable!=null){
            mHandler.post(runnable);
        }
    }

    public void  postTaskInMain(Runnable runnable, long delay){
        if(runnable!=null){
            mHandler.postDelayed(runnable, delay);
        }
    }

    public Handler getHandler(){
        return mHandler;
    }
}
