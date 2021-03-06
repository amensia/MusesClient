package eu.musesproject.client.utils;

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
import android.content.Context;
import android.os.Environment;

import java.io.*;

public class MusesUtils {
	public static final String TEST_TAG = "muses_workflow_test_tag";
	public static final String TAG_SEND = "muses_send_test_tag";
	static Context sContext;
	public static String serverCertificate = "";
	public static String getCertificateFromSDCard(Context context)  {
		serverCertificate = "";
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		String certificateName = "localhost.crt";
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(baseDir + File.separator + certificateName));
			serverCertificate = inputStreamToString(in);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return serverCertificate;
			
	}
	
	public static String getCertificate(){
		return serverCertificate;
	}
	
	private static String inputStreamToString(InputStream is) throws IOException {
		ByteArrayOutputStream byeArrayOutputStream = new ByteArrayOutputStream(8192);
		byte[] buffer = new byte[8192];
		int count = 0;
		try {
		  while ((count = is.read(buffer)) != -1) {
		    byeArrayOutputStream.write(buffer, 0, count);
		  }
		}
		finally {
		  try {
		    is.close();
		  }
		  catch (Exception ignore) {
		  }
		}

		String charset = "UTF-8";
		String resultString = byeArrayOutputStream.toString(charset);
		return resultString;
	}
	
	public static  String getMusesConf() {
		String settings = "192.168.44.101";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator +"muses.conf"));
			settings = reader.readLine();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return settings;
	}
    
    
	
	public static Context getMusesAppContext(){
		return sContext;
	}
	
	public static void setAppContext(Context context){
		sContext = context;
	}
}