package eu.musesproject.client.actuators;

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

import android.content.Context;
import android.util.Log;
import eu.musesproject.client.db.handler.DBManager;
import eu.musesproject.client.model.decisiontable.Decision;
import eu.musesproject.client.usercontexteventhandler.UserContextEventHandler;

/**
 * @author christophstanik
 *
 * Class that manages the lifecycle of the actuators
 */
public class ActuatorController implements IActuatorController {
    private static final String TAG = ActuatorController.class.getSimpleName();

    private static ActuatorController actuatorController = null;

    private Context context;
    private IUICallback callback;
    private final UserContextEventHandler uceHandler = UserContextEventHandler.getInstance();

    private FeedbackActuator feedbackActuator;
    private FileEraserActuator fileEraserActuator;
    private IBlockActuator blockActuator;
    
    private DBManager dbManager;

    public ActuatorController(Context context) {
        this.context = context;
        this.feedbackActuator = new FeedbackActuator(context);
        this.fileEraserActuator = new FileEraserActuator();
        this.blockActuator = new BlockActuator(uceHandler.getContext());
        this.dbManager = new DBManager(uceHandler.getContext());
    }

    public static ActuatorController getInstance(Context context) {
        if (actuatorController == null) {
            actuatorController = new ActuatorController(context);
        }
        return actuatorController;
    }

    public void showFeedback(Decision decision) {
        Log.d(TAG, "called: showFeedback(Decision decision)");

        //check for silent mode
        dbManager.closeDB();
        dbManager.openDB();
        boolean isSilentModeActive = dbManager.isSilentModeActive();
        dbManager.closeDB();
		if(decision != null && isSilentModeActive) {
        	decision.setName(Decision.GRANTED_ACCESS);
        }

		// show feedback
        feedbackActuator.showFeedback(decision);
    }

    public void removeFeedbackFromQueue() {
        feedbackActuator.removeFeedbackFromQueue();
    }

    public void sendFeedbackToMUSESAwareApp(Decision decision) {
        feedbackActuator.sendFeedbackToMUSESAwareApp(decision, context);
    }

    public void sendLoginResponse(boolean loginResponse, String msg) {
        Log.d(TAG, "called: sendLoginResponse(boolean loginResponse)");
        feedbackActuator.sendLoginResponseToUI(loginResponse, msg);
    }

    /**
     * Method to erase files from a given folder path.
     * The folder structure (sub-folders) are not deleted, just the files itself
     * @param folderPath
     */
    public void eraseFolderContent(String folderPath) {
        fileEraserActuator.eraseFolderContent(folderPath);
    }
    
    /**
     * Method to block a specific app by killing its process
     * 
     * @param packageName package name of the app that should be blocked
     */
    public void block(String packageName) {
    	blockActuator.block(packageName);
    }

    @Override
    public void registerCallback(IUICallback callback) {
        this.callback = callback;
        feedbackActuator.registerCallback(callback);
    }

    @Override
    public void unregisterCallback(IUICallback callback) {
        this.callback = callback;
        feedbackActuator.unregisterCallback(callback);
    }
}