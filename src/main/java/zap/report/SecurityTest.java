package zap.report;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.zaproxy.clientapi.core.Alert;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;

public class SecurityTest {
	
	private final static String USER_AGENT = "Mozilla/5.0";
	private static final String ZAP_ADDRESS = "localhost";
	private static final int ZAP_PORT = 8181;
	private static final String ZAP_API_KEY = null;
	private static final String TESTING_MODE = "strict";

	public static ClientApi clientApi = new ClientApi(ZAP_ADDRESS, ZAP_PORT);
	
	public static void main(String[] args) throws ClientProtocolException, IOException, JSONException, ClientApiException {
	SecurityTest.gateway ("http://localhost:8080"); //("https://cavas-test.herokuapp.com");
		
	}
	public static void gateway(String target) throws ClientApiException{

	//test	
//		ClientApi api2 = new ClientApi(ZAP_ADDRESS, ZAP_PORT);
		 try {
	            // Start spidering the target
	            System.out.println("Spider : " + target);
	            ApiResponse resp = clientApi.spider.scan(ZAP_API_KEY, target, null, null, null, null);
	            String scanid;
	            int progress;

	            // The scan now returns a scan id to support concurrent scanning
	            scanid = ((ApiResponseElement) resp).getValue();

	            // Poll the status until it completes
	            while (true) {
	                Thread.sleep(1000);
	                progress = Integer.parseInt(((ApiResponseElement) clientApi.spider.status(scanid)).getValue());
	                System.out.println("Spider progress : " + progress + "%");
	                if (progress >= 100) {
	                    break;
	                }
	            }
	            System.out.println("Spider complete");

	            // Give the passive scanner a chance to complete
	            Thread.sleep(2000);

	            System.out.println("Active scan : " + target);
	            resp = clientApi.ascan.scan(target, null, null, "Default Policy", null, null);

	            // The scan now returns a scan id to support concurrent scanning
	            scanid = ((ApiResponseElement) resp).getValue();
	            System.out.println("Scan Idd : => "  + ((ApiResponseElement) resp).getValue());

	            // Poll the status until it completes
	            while (true) {
	            	
	            	
	            	progress = Integer.parseInt(((ApiResponseElement) clientApi.ascan.status(scanid)).getValue());
					Thread.sleep(60000);// 5 minutes 300000 1 minute 60000
					System.out.println("stopping active  scan after one minute ");
	                clientApi.ascan.stop(scanid);
	            	break;
	                
//	                progress = Integer.parseInt(((ApiResponseElement) clientApi.ascan.status(scanid)).getValue());
//	                System.out.println("Active Scan progress : " + progress + "%");
//	                if (progress >= 100) {
//	                    break;
//	                }
	            }
	         
//				List<Alert> alertList = clientApi.getAlerts(target, 0, 0);
	        } catch (Exception e) {
	            System.out.println("Exception : " + e.getMessage());
	            e.printStackTrace();
	        }
		 ApiResponse hh = clientApi.core.numberOfAlerts(target);
		 System.out.println("the numberof alerts for target: " + target + " is " + hh  );
	    
	}

	
	public static void callScanner(String target) throws ClientProtocolException, IOException, JSONException{
		String urlLocal = "http://localhost:8081/EventService02/scanner"; 
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(urlLocal);

		// add header 
		post.addHeader("User-Agent", USER_AGENT); //x-amz-sns-message-type
		System.out.println("\nSending 'POST' request to URL : " + urlLocal);

		JSONObject object = new JSONObject();
		object.put("target", target);
		String request = object.toString();
//
		StringEntity input = null;
		 
		try {
			input = new StringEntity(request);
		} catch (UnsupportedEncodingException e1) {

			e1.printStackTrace();
		}
		input.setContentType(MediaType.APPLICATION_JSON);
		post.setEntity(input); 

		HttpResponse response = client.execute(post);	
		System.out.println("\nSending security test request to the deployed security scanner request to URL : " + urlLocal);
		System.out.println("Post parameters : " + post.getEntity());
		System.out.println("Response Code : " +
                                    response.getStatusLine().getStatusCode());

		BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		System.out.println(result.toString());
	}
	
	public static String getTime() {

		DateTime date = new DateTime();
		DateTimeZone cet = DateTimeZone.forID("CET");
		DateTime dateR = date.withZone(cet);

		System.out.println(date);

		return dateR.toString();

	}
	
	public void addInstance(String instanceName){
		
		List<String> instances = new ArrayList<String>();
		if(!((instances.size())== 0))
		for (String string : instances) {
			if(instanceName.equals(string)){
				break;
//				return;
			} else{
				instances.add(instanceName);
				
			}
		}
	}
	
	

}
