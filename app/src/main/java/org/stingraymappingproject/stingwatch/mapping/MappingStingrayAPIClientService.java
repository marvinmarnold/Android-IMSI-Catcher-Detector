package org.stingraymappingproject.stingwatch.mapping;

import android.content.Intent;
import android.os.IBinder;

import org.stingraymappingproject.api.clientandroid.StingrayAPIClientService;

/**
 * Created by Marvin Arnold on 27/08/15.
 */
public class MappingStingrayAPIClientService extends StingrayAPIClientService {
    protected final IBinder mBinder = new MappingClientServiceBinder();
    public class MappingClientServiceBinder extends ClientServiceBinder {
        public StingrayAPIClientService getService() {
            return MappingStingrayAPIClientService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
//        Log.d(TAG, "#onBind");
        return mBinder;
    }
}
