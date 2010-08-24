/**
 * See the file "LICENSE" for the full license governing this code.
 */
package org.weloveastrid.rmilk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.todoroo.andlib.ContextManager;

public class MilkStartupReceiver extends BroadcastReceiver {

    @Override
    /** Called when device is restarted */
    public void onReceive(final Context context, Intent intent) {
        ContextManager.setContext(context);
        MilkBackgroundService.scheduleService();
    }

}
