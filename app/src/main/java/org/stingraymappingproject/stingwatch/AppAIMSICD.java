/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package org.stingraymappingproject.stingwatch;


import android.app.Activity;
import android.app.Application;
import android.util.Log;
import android.util.SparseArray;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import org.stingraymappingproject.stingwatch.constants.TinyDbKeys;
import org.stingraymappingproject.stingwatch.utils.BaseAsyncTask;
import org.stingraymappingproject.stingwatch.utils.TinyDB;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

// DO NOT REMOVE BELOW COMMENTED-OUT CODE BEFORE ASKING!
//import com.squareup.leakcanary.LeakCanary;

public class AppAIMSICD extends Application {
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "6iOIyy2Cs70rq7QQbjam9ZtIG";
    private static final String TWITTER_SECRET = "GJQrufmB2C6fqZRTzVAemy1k6eJgkwVQWiIYE1ZqfKB1lL0Fqx";

    final static String TAG = "AIMSICD";
    final static String mTAG = "AppAIMSICD";

    /**
     * Maps between an activity class name and the list of currently running
     * AsyncTasks that were spawned while it was active.
     */
    private SparseArray<List<BaseAsyncTask<?, ?, ?>>> mActivityTaskMap;

    public AppAIMSICD() {
        mActivityTaskMap = new SparseArray<>();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        // DO NOT REMOVE BELOW COMMENTED-OUT CODE BEFORE ASKING!
        //LeakCanary.install(this);
        TinyDB.getInstance().init(getApplicationContext());
        TinyDB.getInstance().putBoolean(TinyDbKeys.FINISHED_LOAD_IN_MAP, true);
    }

    public void removeTask(BaseAsyncTask<?, ?, ?> pTask) {
        int key;
        for (int i = 0; i < mActivityTaskMap.size(); i++) {
            key = mActivityTaskMap.keyAt(i);
            List<BaseAsyncTask<?, ?, ?>> tasks = mActivityTaskMap.get(key);
            for (BaseAsyncTask<?, ?, ?> lTask : tasks) {
                if (lTask.equals(pTask)) {
                    tasks.remove(lTask);
                    if (BuildConfig.DEBUG) {
                        Log.v(TAG, mTAG + ": BaseTask removed:" + pTask.toString());
                    }
                    break;
                }
            }
            if (tasks.size() == 0) {
                mActivityTaskMap.remove(key);
                return;
            }
        }
    }

    public void addTask(Activity activity, BaseAsyncTask<?, ?, ?> pTask) {
        if (activity == null) {
            return;
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, mTAG + ": BaseTask addTask activity:" + activity.getClass().getCanonicalName());
        }
        int key = activity.getClass().getCanonicalName().hashCode();
        List<BaseAsyncTask<?, ?, ?>> tasks = mActivityTaskMap.get(key);
        if (tasks == null) {
            tasks = new ArrayList<>();
            mActivityTaskMap.put(key, tasks);
        }
        if (BuildConfig.DEBUG) {
            Log.v(TAG, mTAG + ": BaseTask added:" + pTask.toString());
        }
        tasks.add(pTask);
    }

    public void detach(Activity activity) {
        if (activity == null) {
            return;
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, mTAG + ": BaseTask detach:" + activity.getClass().getCanonicalName());
        }

        List<BaseAsyncTask<?, ?, ?>> tasks = mActivityTaskMap.get(activity.getClass().getCanonicalName().hashCode());
        if (tasks != null) {
            for (BaseAsyncTask<?, ?, ?> task : tasks) {
                task.setActivity(null);
            }
        }
    }

    public void attach(Activity activity) {
        if (activity == null) {
            return;
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, mTAG + ": BaseTask attach:" + activity.getClass().getCanonicalName());
        }
        List<BaseAsyncTask<?, ?, ?>> tasks = mActivityTaskMap.get(activity.getClass().getCanonicalName().hashCode());
        if (tasks != null) {
            for (BaseAsyncTask<?, ?, ?> task : tasks) {
                task.setActivity(activity);
            }
        }
    }

}