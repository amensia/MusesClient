package eu.musesproject;

/*
 * #%L
 * musesclient
 * %%
 * Copyright (C) 2013 - 2014 HITEC
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import eu.musesproject.client.R;
import eu.musesproject.client.contextmonitoring.UserContextMonitoringController;
import eu.musesproject.client.contextmonitoring.service.aidl.MusesServiceProvider;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.client.ui.MainActivity;
import eu.musesproject.client.usercontexteventhandler.UserContextEventHandler;
import eu.musesproject.client.utils.MusesUtils;

/**
 * This class is responsible to start the background
 * service which enables the application to run properly.
 * This service initializes the necessary code.
 * 
 * @author christophstanik
 *
 */
public class MUSESBackgroundService extends Service {
	private static final String TAG = MUSESBackgroundService.class.getSimpleName();

	private UserContextEventHandler userContextEventHandler;

	private boolean isAppInitialized;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "BACKGROUND - onCreate");

		isAppInitialized = false;
		UserContextMonitoringController.getInstance(this);
		userContextEventHandler = UserContextEventHandler.getInstance();

		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "BACKGROUND - on startComment called");
		if(!isAppInitialized) {
			Log.d(MusesUtils.TEST_TAG, "BACKGROUND - MUSES service started!!");
			isAppInitialized = true;

			MUSESBackgroundService mService = MUSESBackgroundService.this;
			Intent mainActivityintent = new Intent(mService.getApplicationContext(), MainActivity.class);
			mainActivityintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mService.startActivity(mainActivityintent);
//			UserContextMonitoringController.getInstance(this).startContextObservation();

			// try to auto login user
			userContextEventHandler.setContext(this);

			// send status of the service
			String actionDescription = getString(R.string.action_description_started);
			userContextEventHandler.send(createAction(actionDescription), null, null);
		}
		startService(new Intent(this, MusesServiceProvider.class));

		return Service.START_STICKY;
	}


	@Override
	public void onDestroy() {
		Log.v(MusesUtils.TEST_TAG, "BACKGROUND - onDestroy()");
		isAppInitialized = false;

		// send status of the service
		String actionDescription = getString(R.string.action_description_stopped);
		userContextEventHandler.send(createAction(actionDescription), null, null);

		super.onDestroy();
	}

	private Action createAction(String description) {
		Action action = new Action();
		action.setActionType(ActionType.MUSES_BACKGROUND_SERVICE);
		action.setTimestamp(System.currentTimeMillis());
		action.setDescription(description);

		return action;
	}
}