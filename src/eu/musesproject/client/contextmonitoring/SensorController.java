package eu.musesproject.client.contextmonitoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import eu.musesproject.client.contextmonitoring.sensors.AppSensor;
import eu.musesproject.client.contextmonitoring.sensors.ConnectivitySensor;
import eu.musesproject.client.contextmonitoring.sensors.FileSensor;
import eu.musesproject.client.contextmonitoring.sensors.ISensor;
import eu.musesproject.client.contextmonitoring.sensors.OSSensor;
import eu.musesproject.client.contextmonitoring.sensors.SettingsSensor;
import eu.musesproject.client.model.actuators.Setting;
import eu.musesproject.client.model.actuators.Setting.SettingType;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.contextmodel.ContextEvent;

/**
 * @author Christoph
 * @version 28 feb 2014
 *
 * Class to control the lifecycle of all implemented sensors
 */
public class SensorController {
    private static final String TAG = SensorController.class.getSimpleName();

    private static SensorController sensorController = null;
    private final ContextEventBus contextEventBus = new ContextEventBus();

    private Context context;

    private Map<String, ISensor> activeSensors;
    // stores the latest fired ContextEvent of every sensor
    private Map<String, ContextEvent> lastFiredContextEvents;

    private SensorController(Context context) {
        this.context = context;
        activeSensors = new HashMap<String, ISensor>();
        lastFiredContextEvents = new HashMap<String, ContextEvent>();
    }

    public static SensorController getInstance(Context context) {
        if (sensorController == null) {
            sensorController = new SensorController(context);
        }
        return sensorController;
    }

    /**
     * Method to start and enable all sensors
     */
    public void startAllSensors() {
        initSensors();
    }

    private void initSensors() {
        Log.d(TAG, "called: initSensors()");
        activeSensors.put(AppSensor.TYPE, new AppSensor(context));
        activeSensors.put(ConnectivitySensor.TYPE, new ConnectivitySensor(context));
        activeSensors.put(SettingsSensor.TYPE, new SettingsSensor(context));
        activeSensors.put(OSSensor.TYPE, new OSSensor(context));
        activeSensors.put(FileSensor.TYPE, new FileSensor());
        for (ISensor sensor : activeSensors.values()) {
            sensor.addContextListener(contextEventBus);
            sensor.enable();
        }
    }

    /**
     * stops every enabled sensor
     */
    public void stopAllSensors() {
        Log.d(TAG, "called: stopAllSensors()");
        for (ISensor sensor : activeSensors.values()) {
            sensor.removeContextListener(null);
            sensor.disable();
        }
        activeSensors.clear();
    }

    /**
     * Method that performs the action of the setting which contains
     * enabling or disabling a specific sensor
     *
     * @param setting {@link Setting}
     */
    public void changeSetting(Setting setting) {
        ISensor sensor;
        // load sensor that is effected by the setting
        String sensorType = setting.getValue();
        if (activeSensors != null && activeSensors.containsKey(sensorType)) {
            sensor = activeSensors.get(sensorType);

            // perform the action described in Setting.java
            if (sensor != null) {
                if(setting.getSettingType() == SettingType.SETTING_SENSOR_ENABLE) {
                    sensor.enable();
                    // add sensor to the map of enabled sensors if not already set there
                    activeSensors.put(sensorType, sensor);
                    Log.d(TAG, "Sensor: " + sensor.getClass().getSimpleName() + " enabled");
                }
                else if(setting.getSettingType() == SettingType.SETTING_SENSOR_DISABLE) {
                    sensor.disable();
                    // remove the sensor of the map of enabled sensors
                    activeSensors.remove(sensorType);
                    Log.d(TAG, "Sensor: " + sensor.getClass().getSimpleName() + " disabled");
                }
            }
        }
    }

    /**
     * Method that returns the last fired events of all enabled sensors
     * @return {@link List} of {@link ContextEvent}
     */
    public List<ContextEvent> getLastFiredEvents() {
        List<ContextEvent> contextEvents = new ArrayList<ContextEvent>();
        if (activeSensors != null) {
            for (ISensor sensor : activeSensors.values()) {
                ContextEvent contextEvent = sensor.getLastFiredContextEvent();
                if(contextEvent != null) { // just add the context event if there is already one fired
                    contextEvents.add(contextEvent);
                }
            }
        }

        return contextEvents;
    }
    
    public Map<String, ISensor> getActiveSensors() {
		return activeSensors;
	}

	/**
     * Inner class that gets notified when a new {@link ContextEvent}
     * is fired by a {@link ISensor}
     *
     * @author christophstanik
     */
    class ContextEventBus implements ContextListener {

        @Override
        public void onEvent(ContextEvent contextEvent) {
            Log.d(TAG, "onEvent(ContextEvent contextEvent)");
            /*
             * Workflow of creating an action and sending it to the server
             *
             * If a context event is fired:
             * 1. check if there are already enough context events to create an user action
             * 2. create an user action based on the current and the previous context events
             * 3. update the lastFiredContextEvent list
             * 4. send the action via the {@link eu.musesproject.client.contextmonitoring.UserContextMonitoringController}
             *      to the server
             */

            Action userAction = null;
            // 1. if lastFiredContextEvents.size() is 0 than it is the initial context event and no further processing
            // have to be done
            if(lastFiredContextEvents.size() > 0) {
                // 2. create an user action
                userAction = UserActionGenerator.createUserAction(contextEvent, lastFiredContextEvents);
            }

            // 3. update Map with the new context event
            if(lastFiredContextEvents.containsKey(contextEvent.getType())) {
                lastFiredContextEvents.remove(contextEvent.getType());
            }
            lastFiredContextEvents.put(contextEvent.getType(), contextEvent);

            // 4. send action to the UserContextMonitoringController
            if(userAction != null) {
                //UserContextMonitoringController.getInstance(context).sendUserAction(userAction, null);
            }
        }
    }
}