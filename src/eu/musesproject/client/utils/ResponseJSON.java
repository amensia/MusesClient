package eu.musesproject.client.utils;

public class ResponseJSON {

	public static final String SUCCESSFUL_AUTHENTICATION_JSON 		  			=  "{\"auth-message\":\"Successfully authenticated\",\"auth-result\":\"SUCCESS\",\"requesttype\":\"auth-response\"}";
	public static final String CONFIG_UPDATE_JSON									=  "{\"sensor-configuration\":{\"sensor-property\":[{\"value\":\"avast! Mobile Security\",\"key\":\"trustedav\",\"sensor-type\":\"CONTEXT_SENSOR_DEVICE_PROTECTION\"},{\"value\":\"Mobile Security & Antivirus\",\"key\":\"trustedav\",\"sensor-type\":\"CONTEXT_SENSOR_DEVICE_PROTECTION\"},{\"value\":\"Avira Antivirus Security\",\"key\":\"trustedav\",\"sensor-type\":\"CONTEXT_SENSOR_DEVICE_PROTECTION\"},{\"value\":\"Norton Security & Antivirus\",\"key\":\"trustedav\",\"sensor-type\":\"CONTEXT_SENSOR_DEVICE_PROTECTION\"},{\"value\":\"CM Security & Find My Phone\",\"key\":\"trustedav\",\"sensor-type\":\"CONTEXT_SENSOR_DEVICE_PROTECTION\"},{\"value\":true,\"key\":\"enabled\",\"sensor-type\":\"CONTEXT_SENSOR_DEVICE_PROTECTION\"},{\"value\":10,\"key\":\"mindistance\",\"sensor-type\":\"CONTEXT_SENSOR_LOCATION\"},{\"value\":400,\"key\":\"mindtime\",\"sensor-type\":\"CONTEXT_SENSOR_LOCATION\"},{\"value\":12,\"key\":\"radius\",\"sensor-type\":\"CONTEXT_SENSOR_LOCATION\"},{\"value\":true,\"key\":\"enabled\",\"sensor-type\":\"CONTEXT_SENSOR_LOCATION\"},{\"value\":\"/SWE/\",\"key\":\"path\",\"sensor-type\":\"CONTEXT_SENSOR_FILEOBSERVER\"},{\"value\":true,\"key\":\"enabled\",\"sensor-type\":\"CONTEXT_SENSOR_FILEOBSERVER\"},{\"value\":true,\"key\":\"enabled\",\"sensor-type\":\"CONTEXT_SENSOR_APP\"},{\"value\":true,\"key\":\"enabled\",\"sensor-type\":\"CONTEXT_SENSOR_CONNECTIVITY\"},{\"value\":true,\"key\":\"enabled\",\"sensor-type\":\"CONTEXT_SENSOR_INTERACTION\"},{\"value\":true,\"key\":\"enabled\",\"sensor-type\":\"CONTEXT_SENSOR_PACKAGE\"},{\"value\":true,\"key\":\"enabled\",\"sensor-type\":\"CONTEXT_SENSOR_SETTINGS\"},{\"value\":true,\"key\":\"enabled\",\"sensor-type\":\"CONTEXT_SENSOR_NOTIFICATION\"}]},\"connection-config\":{\"sleep_poll_timeout\":60000,\"poll_timeout\":10000,\"login_attempts\":5,\"polling_enabled\":1,\"timeout\":5500},\"muses-config\":{\"config-name\":\"SILENT\",\"silent-mode\":false},\"requesttype\":\"config_update\"}"; 
	public static final String DEVICE_POLICY_ACCESIBILITY_DISABLED_JSON 			=  "{\"muses-device-policy\":{\"files\":{\"action\":{\"request_id\":1402549860,\"deny\":{\"id\":0,\"condition\":{\"accessibilityEnabled\":false},\"path\":\"device\",\"riskTreatment\":\"You are trying to disable accessibility, which is an important security mechanism for MUSES.\\n This can cause the device having a lower level of security.\"},\"type\":\"security_property_changed\"}},\"revision\":1,\"schema-version\":1},\"requesttype\":\"update_policies\"}";
	public static final String DEVICE_POLICY_INSUFFICIENT_SCREEN_TIMEOUT_JSON    	=  "{\"muses-device-policy\":{\"files\":{\"action\":{\"request_id\":-1898082329,\"deny\":{\"id\":0,\"condition\":{\"screenTimeoutInSeconds\":30},\"path\":\"device\",\"riskTreatment\":\"Insufficient screen lock timeout\\nTime set for screen lock timeout is not sufficient, while it is an important security mechanism.\\nWithout screen lock, other people with access to your device might access to restricted corporate information.\"},\"type\":\"security_property_changed\"}},\"revision\":1,\"schema-version\":1},\"requesttype\":\"update_policies\"}";
	public static final String DEVIEC_POLICY_OPEN_BACKLISTED_APP_JSON				=  "{\"muses-device-policy\":{\"files\":{\"action\":{\"request_id\":2013216759,\"deny\":{\"id\":0,\"condition\":{\"appname\":\"Wifi Analyzer\"},\"path\":\"Wifi Analyzer\",\"riskTreatment\":\"You are trying to open an application which is considered harmful.\\nOther people can gain control over your device.\"},\"type\":\"open_application\"}},\"revision\":1,\"schema-version\":1},\"requesttype\":\"update_policies\"}";
	
}
