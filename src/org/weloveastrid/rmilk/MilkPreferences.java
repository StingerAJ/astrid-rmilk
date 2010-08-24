package org.weloveastrid.rmilk;

import java.util.Date;

import org.weloveastrid.misc.TodorooPreferences;
import org.weloveastrid.rmilk.sync.RTMSyncProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.View;
import android.view.ViewGroup.OnHierarchyChangeListener;

import com.todoroo.andlib.AndroidUtilities;
import com.todoroo.andlib.ContextManager;
import com.todoroo.andlib.DateUtilities;
import com.todoroo.andlib.DialogUtilities;

/**
 * Displays synchronization preferences and an action panel so users can
 * initiate actions from the menu.
 *
 * @author timsu
 *
 */
public class MilkPreferences extends TodorooPreferences {

    private int statusColor = Color.BLACK;

    @Override
    public int getPreferenceResource() {
        return R.xml.preferences_rmilk;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContextManager.setContext(this);

        getListView().setOnHierarchyChangeListener(new OnHierarchyChangeListener() {

            @Override
            public void onChildViewRemoved(View parent, View child) {
                //
            }

            @Override
            public void onChildViewAdded(View parent, View child) {
                View view = findViewById(R.id.status);
                if(view != null)
                    view.setBackgroundColor(statusColor);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        MilkBackgroundService.scheduleService();
    }

    /**
     *
     * @param resource
     *            if null, updates all resources
     */
    @Override
    public void updatePreferences(Preference preference, Object value) {
        final Resources r = getResources();

        // interval
        if (r.getString(R.string.rmilk_MPr_interval_key).equals(
                preference.getKey())) {
            int index = AndroidUtilities.indexOf(
                    r.getStringArray(R.array.sync_SPr_interval_values),
                    (String) value);
            if (index <= 0)
                preference.setSummary(R.string.sync_SPr_interval_desc_disabled);
            else
                preference.setSummary(r.getString(
                        R.string.sync_SPr_interval_desc,
                        r.getStringArray(R.array.sync_SPr_interval_entries)[index]));
        }

        // status
        else if (r.getString(R.string.sync_SPr_status_key).equals(preference.getKey())) {
            boolean loggedIn = MilkUtilities.isLoggedIn();
            String status;
            String subtitle = ""; //$NON-NLS-1$

            // ! logged in - display message, click -> sync
            if(!loggedIn) {
                status = r.getString(R.string.sync_status_loggedout);
                statusColor = Color.RED;
                preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference p) {
                        startService(new Intent(MilkPreferences.this, MilkBackgroundService.class));
                        finish();
                        return true;
                    }
                });
            }
            // sync is occurring
            else if(MilkUtilities.isOngoing()) {
                status = r.getString(R.string.sync_status_ongoing);
                statusColor = Color.rgb(0, 0, 100);
            }
            // last sync was error
            else if(MilkUtilities.getLastAttemptedSyncDate() != 0) {
                status = r.getString(R.string.sync_status_failed,
                        DateUtilities.getDateStringWithTime(MilkPreferences.this,
                        new Date(MilkUtilities.getLastAttemptedSyncDate())));
                if(MilkUtilities.getLastSyncDate() > 0) {
                    subtitle = r.getString(R.string.sync_status_failed_subtitle,
                            DateUtilities.getDateStringWithTime(MilkPreferences.this,
                            new Date(MilkUtilities.getLastSyncDate())));
                }
                statusColor = Color.rgb(100, 0, 0);
                preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference p) {
                        String error = MilkUtilities.getLastError();
                        if(error != null)
                            DialogUtilities.okDialog(MilkPreferences.this, error, null);
                        return true;
                    }
                });
            } else if(MilkUtilities.getLastSyncDate() > 0) {
                status = r.getString(R.string.sync_status_success,
                        DateUtilities.getDateStringWithTime(MilkPreferences.this,
                        new Date(MilkUtilities.getLastSyncDate())));
                statusColor = Color.rgb(0, 100, 0);
            } else {
                status = r.getString(R.string.sync_status_never);
                statusColor = Color.rgb(0, 0, 100);
                preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference p) {
                        startService(new Intent(MilkPreferences.this, MilkBackgroundService.class));
                        finish();
                        return true;
                    }
                });
            }
            preference.setTitle(status);
            preference.setSummary(subtitle);

            View view = findViewById(R.id.status);
            if(view != null)
                view.setBackgroundColor(statusColor);
        }

        // sync button
        else if (r.getString(R.string.sync_SPr_sync_key).equals(preference.getKey())) {
            boolean loggedIn = MilkUtilities.isLoggedIn();
            preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference p) {
                    startService(new Intent(MilkPreferences.this, MilkBackgroundService.class));
                    finish();
                    return true;
                }
            });
            if(!loggedIn)
                preference.setTitle(R.string.sync_SPr_sync_log_in);
        }

        // log out button
        else if (r.getString(R.string.sync_SPr_forget_key).equals(preference.getKey())) {
            boolean loggedIn = MilkUtilities.isLoggedIn();
            preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference p) {
                    DialogUtilities.okCancelDialog(MilkPreferences.this,
                            r.getString(R.string.sync_forget_confirm), new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                int which) {
                            new RTMSyncProvider().signOut(MilkPreferences.this);
                            initializePreference(getPreferenceScreen());
                        }
                    }, null);
                    return true;
                }
            });
            if(!loggedIn)
                preference.setEnabled(false);
        }
    }

}