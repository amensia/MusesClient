package eu.musesproject.client.contextmonitoring.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.contextmodel.ContextEvent;
import eu.musesproject.contextmodel.PackageStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by christophstanik on 3/26/14.
 */
public class PackageSensor implements ISensor {
    private static final String TAG = PackageSensor.class.getSimpleName();


    // sensor identifier
    public static final String TYPE = "CONTEXT_SENSOR_PACKAGE";

    // context property keys
    public static final String PROPERTY_KEY_ID 					= "id";
    public static final String PROPERTY_KEY_PACKAGE_STATUS		= "packagestatus";
    public static final String PROPERTY_KEY_PACKAGE_NAME		= "packagename";
    public static final String PROPERTY_KEY_APP_NAME 			= "appname";
    public static final String PROPERTY_KEY_APP_VERSION			= "appversion";
    public static final String PROPERTY_KEY_INSTALLED_APPS   	= "installedapps";

    private boolean sensorEnabled;

    private Context context;
    private ContextListener listener;

    // stores all fired context events of this sensor
    private List<ContextEvent> contextEventHistory;

    // broadcast receiver fields
    final PackageBroadcastReceiver packageReceiver = new PackageBroadcastReceiver();
    final IntentFilter packageAddedIntentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
    final IntentFilter packageRemovedIntentFilter = new IntentFilter(
            Intent.ACTION_PACKAGE_REMOVED);
    final IntentFilter packageUpdatedIntentFilter = new IntentFilter(
            Intent.ACTION_PACKAGE_REPLACED);


    public PackageSensor(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        sensorEnabled = false;
        contextEventHistory = new ArrayList<ContextEvent>();
    }

    @Override
    public void enable() {
        if(!sensorEnabled) {
            Log.d(TAG, "start package sensor");
            sensorEnabled = true;
            registerPackageBroadcastReceiver();
        }
    }

    @Override
    public void disable() {
        if(sensorEnabled) {
            Log.d(TAG, "stop package sensor");
            sensorEnabled = false;
            unregisterPackageBroadcastReceiver();
        }
    }

    @Override
    public void addContextListener(ContextListener listener) {
        this.listener = listener;
    }

    @Override
    public ContextEvent getLastFiredContextEvent() {
        if(contextEventHistory.size() > 0) {
            return contextEventHistory.get(contextEventHistory.size() - 1);
        }
        else {
            return null;
        }
    }

    private void createContextEvent(String packageStatus) {
        ContextEvent contextEvent = new ContextEvent();
        contextEvent.setType(TYPE);
        contextEvent.setTimestamp(System.currentTimeMillis());
        contextEvent.addProperty(PROPERTY_KEY_ID, String.valueOf(contextEventHistory != null ? (contextEventHistory.size() + 1) : -1));
        contextEvent.addProperty(PROPERTY_KEY_PACKAGE_STATUS, packageStatus);
        contextEvent.addProperty(PROPERTY_KEY_PACKAGE_NAME, "");
        contextEvent.addProperty(PROPERTY_KEY_APP_NAME, "");
        contextEvent.addProperty(PROPERTY_KEY_APP_VERSION, "");
        contextEvent.addProperty(PROPERTY_KEY_INSTALLED_APPS, getInstalledApps().toString());

        // add context event to the context event history
        contextEventHistory.add(contextEvent);

        if(listener != null) {
            listener.onEvent(contextEvent);
        }
    }

    private void registerPackageBroadcastReceiver() {
        packageAddedIntentFilter.addDataScheme("package");
        packageRemovedIntentFilter.addDataScheme("package");
        packageUpdatedIntentFilter.addDataScheme("package");

        context.registerReceiver(packageReceiver, packageAddedIntentFilter);
        context.registerReceiver(packageReceiver, packageRemovedIntentFilter);
        context.registerReceiver(packageReceiver, packageUpdatedIntentFilter);
    }

    private List<String[]> getInstalledApps() {
        List<String[]> installedAppsFormatted = new ArrayList<String[]>();

        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for(ApplicationInfo appInfo : installedApps) {
            String appName = appInfo.loadLabel(packageManager).toString();
            String packageName = appInfo.packageName;
            installedAppsFormatted.add(new String[]{appName, packageName});
        }

        return installedAppsFormatted;
    }

    private void unregisterPackageBroadcastReceiver() {
        try {
            context.unregisterReceiver(packageReceiver);
        }
        catch(IllegalArgumentException e) {
            Log.d(TAG, "package receiver is already unregistered");
        }
    }

    public class PackageBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                final String action = intent.getAction();
                if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                    createContextEvent(PackageStatus.INSTALLED.toString());
                } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                    createContextEvent(PackageStatus.REMOVED.toString());
                } else if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
                    createContextEvent(PackageStatus.UPDATED.toString());
                }
            }
        }
    }
}