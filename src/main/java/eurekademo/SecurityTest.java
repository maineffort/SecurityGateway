package eurekademo;

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
import org.json.JSONException;
import org.json.JSONObject;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ClientApi;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import zap.report.OWASPZAPReport;
import zap.report.OWASPZAPReport.Site.Alerts.Alertitem;
// class for security testing the applications or microservices that send enrollment requests  
public class SecurityTest {
	
	private final static String USER_AGENT = "Mozilla/5.0";
	private static final String ZAP_ADDRESS = "seclab.hpi.uni-potsdam.de"; // "127.0.0.1";
	private static final int ZAP_PORT = 50050; //8080;
	private static final String ZAP_API_KEY = null; 													

//	public static ClientApi api = new ClientApi(ZAP_ADDRESS, ZAP_PORT);
	
	public static void main(String[] args) throws ClientProtocolException, IOException, JSONException {
	SecurityTest.tester ("https://cavas-test.herokuapp.com"); //("http://127.0.0.1:8761");
		
	}
	public static void tester(String target){
//		InstanceRegistry InstanceRegistry = new InstanceRegistry();

//    List<String> registeredApps = new ArrayList<String>(); 
//    for (String string : registeredApps) {
//		if (target.equalsIgnoreCase(string))
//			break;
//			return;
//	}
		
		ClientApi api2 = new ClientApi(ZAP_ADDRESS, ZAP_PORT);
		 try {
	            // Start spidering the target
	            System.out.println("Spider : " + target);
	            ApiResponse resp = api2.spider.scan(ZAP_API_KEY, target, null, null, null, null);
	            String scanid;
	            int progress;

	            // The scan now returns a scan id to support concurrent scanning
	            scanid = ((ApiResponseElement) resp).getValue();

	            // Poll the status until it completes
	            while (true) {
	                Thread.sleep(1000);
	                progress = Integer.parseInt(((ApiResponseElement) api2.spider.status(scanid)).getValue());
	                System.out.println("Spider progress : " + progress + "%");
	                if (progress >= 100) {
	                    break;
	                }
	            }
	            System.out.println("Spider complete");

	            // Give the passive scanner a chance to complete
	            Thread.sleep(2000);

	            System.out.println("Active scan : " + target);
	            resp = api2.ascan.scan(target, null, null, null, null, null);

	            // The scan now returns a scan id to support concurrent scanning
	            scanid = ((ApiResponseElement) resp).getValue();
	            System.out.println("Scan Idd : => "  + ((ApiResponseElement) resp).getValue());

	            // Poll the status until it completes
	            while (true) {
	                Thread.sleep(5000);
	                progress = Integer.parseInt(((ApiResponseElement) api2.ascan.status(scanid)).getValue());
	                System.out.println("Active Scan progress : " + progress + "%");
	                if (progress >= 100) {
	                    break;
	                }
	            }
	            System.out.println("Active Scan complete");

	            System.out.println("Alerts:");
	            System.out.println(new String(api2.core.xmlreport(ZAP_API_KEY)));
	            JacksonXmlModule module = new JacksonXmlModule();
	            module.setDefaultUseWrapper(false);
	            XmlMapper xmlMapper = new XmlMapper(module);
	            OWASPZAPReport oWASPZAPReport = new OWASPZAPReport();
	            oWASPZAPReport  =  xmlMapper.readValue(api2.core.xmlreport(ZAP_API_KEY), OWASPZAPReport.class);
//	            System.out.println(oWASPZAPReport.getGenerated());
//	            System.out.println(oWASPZAPReport.getSite().getAlerts());
	            List<Alertitem> reportList = oWASPZAPReport.getSite().getAlerts().getAlertitem();
	            for (Alertitem alertitem : reportList) {
					System.out.println(alertitem);
				}
	            
	            System.out.println(xmlMapper.readValue(api2.core.xmlreport(ZAP_API_KEY), OWASPZAPReport.class));
	            

	        } catch (Exception e) {
	            System.out.println("Exception : " + e.getMessage());
	            e.printStackTrace();
	        }
	    
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
