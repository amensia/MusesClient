package eu.musesproject.client.connectionmanager;
/*
 * #%L
 * MUSES Client
 * %%
 * Copyright (C) 2013 - 2014 Sweden Connectivity
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
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import eu.musesproject.client.model.RequestType;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.client.usercontexteventhandler.JSONManager;
import eu.musesproject.client.usercontexteventhandler.UserContextEventHandler;
import eu.musesproject.client.utils.BuildConfig;
import eu.musesproject.client.utils.BuildConfig.RUNNING_MODE;
import eu.musesproject.client.utils.ResponseJSON;

/**
 * Connection Manager class implements the iConnection interface
 * 
 * @author Yasir Ali 
 * @version Jan 27, 2014
 */

public class ConnectionManager extends HttpConnectionsHelper implements IConnectionManager{

	private static String URL = "";
	private static String certificate;
	public static IConnectionCallbacks callBacks;
	public static final boolean FAKE_MODE_ON = true;
	public static final String CONNECT = "connect";
	public static final String POLL = "poll";
	public static final String DATA = "data";
	public static final String DISCONNECT = "disconnect";
	public static final String ACK = "ack";
	private static final String TAG = ConnectionManager.class.getSimpleName();
	private static final String APP_TAG = "APP_TAG";
	public static final String POLL_TAG = "POLL_TAG";
	private static Boolean isServerConnectionSet = false; 
	static private int lastSentStatus = Statuses.OFFLINE;
	private AtomicInteger mCommandOngoing = new AtomicInteger(0);
	private AlarmReceiver alarmReceiver;
	private Context context;
	private int detailedOnlineStatus;
	public static boolean isNewSession = true;
		
	public ConnectionManager(){
		alarmReceiver = new AlarmReceiver();
		AlarmReceiver.setManager(this);
	}
	
	/**
	 * Connect to the server with specified parameter
	 * @param url
	 * @param pollInterval
	 * @param sleepPollInterval
	 * @param callbacks
	 * @param context
	 * @return void
	 */
	
	@Override
	public void connect(String url, String cert, int pollInterval, int sleepPollInterval, IConnectionCallbacks callbacks, Context context) {
		/* FIXME, temporary fix for dual calls */
		
		synchronized (this)
		{
			if (isServerConnectionSet)
			{
				Log.d(TAG, "connect: More then one more connect call!");
				return;
			}
			isServerConnectionSet = true;
		}
		
		URL = url;
		
		callBacks = callbacks;
		this.context = context;
		
		/* Check that cert is ok, spec length */
		/* FIXME which Length.. */
		if (cert.isEmpty() || cert.length() < 1000)
		{
			callBacks.statusCb(Statuses.CONNECTION_FAILED, DetailedStatuses.INCORRECT_CERTIFICATE, 0);
			Log.d(TAG, "connect: Incorrect certificate!");
			return;
		}
		certificate = cert;
			
		NetworkChecker networkChecker = new NetworkChecker(context);
		PhoneModeReceiver phoneModeReceiver = new PhoneModeReceiver(context);
		phoneModeReceiver.register();
		Log.d(TAG, "Connecting to URL:"+url);
		
		if (networkChecker.isInternetConnected()) {
			Log.d(TAG, "InternetConnected");
		}
				
        setCommandOngoing();
        
		// For Mock mode
		if (BuildConfig.CURRENT_RUNNING_MODE == RUNNING_MODE.MOCK) {
			setMockModeCofig();
			return;
		}
        
        // For testing
        //DBG SweFileLog.write("Connect to :"+URL+",0,0");
        
        startHttpThread( CONNECT,
				URL, Integer.toString(pollInterval),"", "");
				
		alarmReceiver.setPollInterval(pollInterval, sleepPollInterval);
		alarmReceiver.setDefaultPollInterval(pollInterval, sleepPollInterval);
		alarmReceiver.setAlarm(context);
		
	}

	private void setMockModeCofig() {
		Statuses.CURRENT_STATUS = Statuses.ONLINE;
		detailedOnlineStatus = DetailedStatuses.SUCCESS;
		UserContextEventHandler.serverOnlineAndUserAuthenticated = true;
		
		if (isNewSession){
			
			isNewSession = false;
			if (Statuses.CURRENT_STATUS == Statuses.ONLINE)
			{
				// For testing
				//DBG SweFileLog.write("New sessionId, ,");
				setServerStatusAndCallBack(Statuses.NEW_SESSION_CREATED, 0, 0);
			}
			else
			{
				// If this is a new session, inform using detailed status
				detailedOnlineStatus = DetailedStatuses.SUCCESS_NEW_SESSION;
			}
			
		}
		setServerStatusAndCallBack(Statuses.ONLINE, DetailedStatuses.SUCCESS, 0);
		setServerStatusAndCallBack(Statuses.CONNECTION_OK, DetailedStatuses.SUCCESS, 0);
		
	}

	/**
	 * Send data to the server 
	 * @param data
	 * @return void 
	 */
	
	@Override
	public void sendData(String data, int dataId) {
		if (BuildConfig.CURRENT_RUNNING_MODE == RUNNING_MODE.MOCK) {
			setServerStatusAndCallBack(Statuses.ONLINE, DetailedStatuses.SUCCESS, dataId);
			setServerStatusAndCallBack(Statuses.DATA_SEND_OK, DetailedStatuses.SUCCESS, dataId);
			processMockRequest(data,dataId);
			return;
		}
		String dataIdStr = "";
		dataIdStr = Integer.toString(dataId);
		setCommandOngoing();
		Log.d(APP_TAG, "ConnManager=> send data to server: "+data);
		startHttpThread(DATA, URL, 
				Integer.toString(AlarmReceiver.getCurrentPollInterval()), data, dataIdStr); 
	}
	
	private void processMockRequest(String data, int dataId) {
		Log.d(APP_TAG, "Mock=> Send data to server: "+data);
		if (JSONManager.getRequestType(data).equalsIgnoreCase(RequestType.LOGIN)) {
			callBacks.receiveCb(ResponseJSON.SUCCESSFUL_AUTHENTICATION_JSON);
			Log.d(APP_TAG, "Mock=> Server responded with JSON: "+ ResponseJSON.SUCCESSFUL_AUTHENTICATION_JSON);
		}
		if (JSONManager.getRequestType(data).equalsIgnoreCase(RequestType.LOGOUT)) {
			callBacks.receiveCb(ResponseJSON.LOGGED_OUT_JSON);
			Log.d(APP_TAG, "Mock=> Server responded with JSON: "+ ResponseJSON.LOGGED_OUT_JSON);
		}
		if (JSONManager.getRequestType(data).equalsIgnoreCase(RequestType.CONFIG_SYNC)) {
			callBacks.receiveCb(ResponseJSON.CONFIG_UPDATE_JSON);
			Log.d(APP_TAG, "Mock=> Server responded with JSON: "+ResponseJSON.CONFIG_UPDATE_JSON);
		}
		if (JSONManager.getRequestType(data).equalsIgnoreCase(RequestType.ONLINE_DECISION)) {
			if (!JSONManager.isAccessbilityEnabled(data)) {
				callBacks.receiveCb(updateRequestIdWithNewRequest(Integer.toString(JSONManager.getRequestIdFromRequestJSON(data)),ResponseJSON.DEVICE_POLICY_ACCESIBILITY_DISABLED_JSON));
				Log.d(APP_TAG, "Mock=> Server responded with JSON: "+ResponseJSON.DEVICE_POLICY_ACCESIBILITY_DISABLED_JSON);
			}
			if (JSONManager.getScreenTimeout(data) < 30){
				callBacks.receiveCb(updateRequestIdWithNewRequest(Integer.toString(JSONManager.getRequestIdFromRequestJSON(data)),ResponseJSON.DEVICE_POLICY_INSUFFICIENT_SCREEN_TIMEOUT_JSON));
				Log.d(APP_TAG, "Mock=> Server responded with JSON: "+ResponseJSON.DEVICE_POLICY_INSUFFICIENT_SCREEN_TIMEOUT_JSON);
			}
			if (JSONManager.getActionType(data).equalsIgnoreCase(ActionType.OPEN_APPLICATION)){
				if (data.contains("com.farproc.wifi.analyzer")){
					callBacks.receiveCb(updateRequestIdWithNewRequest(Integer.toString(JSONManager.getRequestIdFromRequestJSON(data)),ResponseJSON.DEVICE_POLICY_OPEN_BACKLISTED_APP_JSON) );
					Log.d(APP_TAG, "Mock=> Server responded with JSON: "+ResponseJSON.DEVICE_POLICY_OPEN_BACKLISTED_APP_JSON);
				}
			}
			if (JSONManager.getActionType(data).equalsIgnoreCase(ActionType.OPEN_ASSET)){
				if (JSONManager.getFilePath(data).contains("confidential")){
					if (JSONManager.getWifiEncryption(data).contains("WPA2")){
						callBacks.receiveCb(updateRequestIdWithNewRequest(Integer.toString(JSONManager.getRequestIdFromRequestJSON(data)),ResponseJSON.DEVICE_POLICY_CONFIDENTIAL_SECURE_JSON));
						Log.d(APP_TAG, "Mock=> Server responded with JSON: "+ResponseJSON.DEVICE_POLICY_CONFIDENTIAL_SECURE_JSON);
					}else{
						callBacks.receiveCb(updateRequestIdWithNewRequest(Integer.toString(JSONManager.getRequestIdFromRequestJSON(data)),ResponseJSON.DEVICE_POLICY_CONFIDENTIAL_UNSECURE_JSON));
						Log.d(APP_TAG, "Mock=> Server responded with JSON: "+ResponseJSON.DEVICE_POLICY_CONFIDENTIAL_UNSECURE_JSON);
					}
				}
			}
			if (JSONManager.getActionType(data).equalsIgnoreCase(ActionType.OPEN_ASSET)){
				if (JSONManager.getFilePath(data).contains("companyfile.txt")){
					callBacks.receiveCb(updateRequestIdWithNewRequest(Integer.toString(JSONManager.getRequestIdFromRequestJSON(data)),ResponseJSON.DEVICE_POLICY_OPEN_FILE_MONITORED_FOLDER_JSON));
					Log.d(APP_TAG, "Mock=> Server responded with JSON: "+ResponseJSON.DEVICE_POLICY_OPEN_FILE_MONITORED_FOLDER_JSON);
				}
				
			}
			if (JSONManager.getActionType(data).equalsIgnoreCase(ActionType.OPEN_ASSET)){
				if (JSONManager.getFilePath(data).contains("MUSES_partner_grades.txt")){
					if (JSONManager.getWifiEncryption(data).contains("WPA2")){
						callBacks.receiveCb(updateRequestIdWithNewRequest(Integer.toString(JSONManager.getRequestIdFromRequestJSON(data)),ResponseJSON.DEVICE_POLICY_OPEN_CONF_ASSET_SECURE_JSON));
						Log.d(APP_TAG, "Mock=> Server responded with JSON: "+ResponseJSON.DEVICE_POLICY_OPEN_CONF_ASSET_SECURE_JSON);
					}else{
						callBacks.receiveCb(updateRequestIdWithNewRequest(Integer.toString(JSONManager.getRequestIdFromRequestJSON(data)),ResponseJSON.DEVICE_POLICY_OPEN_CONF_ASSET_UNSECURE_JSON));
						Log.d(APP_TAG, "Mock=> Server responded with JSON: "+ResponseJSON.DEVICE_POLICY_OPEN_CONF_ASSET_UNSECURE_JSON);
					}
				}
			}
		}
	}

	private String updateRequestIdWithNewRequest(String requestId, String devicePolicyAccesibilityDisabledJson) {
		JSONObject responseJSON;
		try {
			responseJSON = new JSONObject(devicePolicyAccesibilityDisabledJson);
			JSONObject policyJSON = responseJSON.getJSONObject("muses-device-policy");
			JSONObject fileJSON = policyJSON.getJSONObject("files");
			JSONObject actionJSON = fileJSON.getJSONObject("action");
			actionJSON = actionJSON.put("request_id",requestId);
			fileJSON = fileJSON.put("action", actionJSON);
			policyJSON = policyJSON.put("files", fileJSON);
			responseJSON = responseJSON.put("muses-device-policy", policyJSON);
			responseJSON = responseJSON.put("requesttype","update_policies");
			devicePolicyAccesibilityDisabledJson = responseJSON.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return devicePolicyAccesibilityDisabledJson;
	}

	/**
	 * Disconnects session from the server 
	 * @return void 
	 */
	
	@Override
	public void disconnect() { // FIXME What if the server is not online, How should we stop polling from here
		// As we are disconnecting we need to stop the polling 
		Log.d(TAG, "Disconnecting ..");
		synchronized (this)
		{
			isServerConnectionSet = false;
		}
		
		
		Log.d(APP_TAG, "ConnManager=> disconnecting session to server");
		startHttpThread(DISCONNECT, URL, 
				Integer.toString(AlarmReceiver.getCurrentPollInterval()), "", "");
		
			
			//callBacks.statusCb(Statuses.DISCONNECTED, Statuses.DISCONNECTED);
		
		
		alarmReceiver.cancelAlarm(context);
	}

	/**
	 * Sets poll timeouts
	 * @param pollInterval
	 * @param sleepPollInterval
	 * @return void 
	 */
	
	
	@Override
	public void setPollTimeOuts(int pollInterval, int sleepPollInterval) {
		alarmReceiver.setPollInterval(pollInterval, sleepPollInterval);
		alarmReceiver.setDefaultPollInterval(pollInterval, sleepPollInterval);
	}

	/**
	 * Starts to poll with the server either in sleep/active mode
	 * @return void
	 */
	
	public void periodicPoll() {
		//Log.d(APP_TAG, "Polling !!");
		
		// If ongoing command, don't poll
		if (mCommandOngoing.get()==0)
		{
			poll();
		}
			
	
	}
	
	/**
	 * Starts to poll with the server either in sleep/active mode
	 * @return void
	 */
	
	public void poll() {
		//Log.d(APP_TAG, "Polling !!");
		
		// If ongoing command, don't poll
		if (BuildConfig.CURRENT_RUNNING_MODE == RUNNING_MODE.MOCK) {
			return;
		}
		
		setCommandOngoing();
		startHttpThread(POLL, URL, 
				Integer.toString(AlarmReceiver.getCurrentPollInterval()), "", "");
			
	
	}

	/**
	 * Sends ack to the server to tell that data has been received on the client
	 * @return void
	 */
	
	public void ack() {
		Log.d(TAG, "Sending ack..");
		startHttpThread(ACK, URL, 
				Integer.toString(AlarmReceiver.getCurrentPollInterval()), "", "");
			
	
	}
	
	private void startHttpThread(String cmd, String url, String pollInterval, String data, String dataId) {
		HttpClientAsyncThread httpClientAsyncThread = new HttpClientAsyncThread();
		httpClientAsyncThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cmd, url, 
				pollInterval, data, certificate, dataId);

		/* If too many threads, use serial executor */
//		httpClientAsyncThread.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, cmd, url, 
//				pollInterval, data, certificate);

	}

	private void setCommandOngoing()
	{
		mCommandOngoing.set(1);	
	}
	
	private void setCommandNotOngoing()
	{	
		mCommandOngoing.set(0);	
	}
	
	/**
	 * Handler Http/Https (Networking in background or separate thread)
	 * @author Yasir Ali
	 * @version Jan 27, 2014
	 */
	
    private class HttpClientAsyncThread extends AsyncTask<String, Void, String> {

    	/* FIXME use onPost Execute to send callbacks */
    	@Override
    	protected String doInBackground(String... params) {
    		HttpResponse response = null;
			Request request = new Request(params[0], 
					params[1], params[2], params[3], params[4], params[5]);
			if (!NetworkChecker.isInternetConnected) 
			{
				Log.d(POLL_TAG,"doInBackground: Only respond to caller. ");
				if (request.getType().contentEquals(CONNECT))
				{
					callBacks.statusCb(Statuses.CONNECTION_FAILED, DetailedStatuses.NO_INTERNET_CONNECTION, request.getDataId());
				}
				else if (request.getType().contentEquals(DATA))
				{
					Log.d(APP_TAG, "ConnManager=> can't send data with no internet connection, calling statusCB");
					sendServerStatus(Statuses.OFFLINE, DetailedStatuses.NO_INTERNET_CONNECTION, request.getDataId());
					callBacks.statusCb(Statuses.DATA_SEND_FAILED, DetailedStatuses.NO_INTERNET_CONNECTION, request.getDataId());
					
				} else if (request.getType().contentEquals(DISCONNECT))
				{
					callBacks.statusCb(Statuses.DISCONNECTED, DetailedStatuses.NO_INTERNET_CONNECTION, request.getDataId());
					HttpConnectionsHelper.current_cookie = null;
				}
			}
			else
			{
				Log.d(POLL_TAG,"doInBackground: parameters: "+params[0]+", "+params[1]+", "+params[2]+", "+params[3]);
				try {
					HttpResponseHandler httpResponseHandler = doSecurePost(request, params[4]);
					if (httpResponseHandler != null)
					{
						
						httpResponseHandler.checkHttpResponse();
					}
					
				} catch (ClientProtocolException e) {
					e.printStackTrace();
					Log.d(APP_TAG, Log.getStackTraceString(e));
				} catch (IOException e) {
					e.printStackTrace();
					Log.d(APP_TAG, Log.getStackTraceString(e));
				} catch (Exception e) {
					e.printStackTrace();
					Log.d(APP_TAG, Log.getStackTraceString(e));
				}
				
				if (request.getType().contentEquals(DISCONNECT))
				{
					HttpConnectionsHelper.current_cookie = null;
				}
			}
			
			setCommandNotOngoing();
			Log.d(POLL_TAG,"doInBackground: doInBackground finished.");
			return null;

    	}

    }

	@Override
	public void setTimeout(int timeout) {
		CONNECTION_TIMEOUT = timeout;
	}

	@Override
	public void setPolling(int polling) {
		POLLING_ENABLED = polling;
	}

	public static void sendServerStatus(int status, int detailedStatus, int dataId) {
		// TODO Auto-generated method stub
		if (status == Statuses.OFFLINE || status == Statuses.ONLINE)
		{
			if (lastSentStatus != status )
			{
				ConnectionManager.callBacks.statusCb(status, detailedStatus, dataId);
				lastSentStatus = status;
				// For testing
				//DBG SweFileLog.write((status==Statuses.ONLINE?"ONLINE,,":"OFFLINE,,"));
				
			}
		}
		else
		{
			//DBG SweFileLog.write("Weird status: "+Integer.toString(status)+", , ");
		}


	}
	
	/**
	 * For Mock testing duplicate from HttpResponseHandler
	 * @param status
	 * @param detailedStatus
	 * @param dataId
	 */
	
	private void setServerStatusAndCallBack(int status, int detailedStatus, int dataId) {
		
		if (status == Statuses.OFFLINE || status == Statuses.ONLINE)
		{
			sendServerStatus(status, detailedStatus, dataId);
			
			
		}
		else
		{	
			callBacks.statusCb(status, detailedStatus, dataId);
		}
		
	}
	
	

}