package net.yazeed44.imagepicker.util;

import android.app.Application;
import android.content.Context;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.CacheManager;

/**
 * Created by yazeed44
 * on 6/13/15.
 */
public class OfflineSpiceService extends SpiceService {

    @Override
    public CacheManager createCacheManager(Application application) {
        return new CacheManager();
    }


    @Override
    protected NetworkStateChecker getNetworkStateChecker() {
        return new NetworkStateChecker() {
            @Override
            public boolean isNetworkAvailable(Context context) {
                return true;
            }

            @Override
            public void checkPermissions(Context context) {
// do nothing
            }
        };
    }
}
