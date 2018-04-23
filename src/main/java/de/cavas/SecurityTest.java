package de.cavas;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.zaproxy.clientapi.core.Alert;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;
import org.zaproxy.clientapi.core.Alert.Risk;

import com.netflix.appinfo.InstanceInfo;

import de.cavas.repository.AlertController;
import de.cavas.repository.AlertRepository;
import de.zap.SecurityTester;

//@Controller
public class SecurityTest {

	private final static String USER_AGENT = "Mozilla/5.0";
	private static final String TESTING_MODE = "strict";

	@Autowired
//	static AlertRepository alertRepository;
	AlertController controller;// = new AlertController();

	public  String preRegistrationTest(String target, InstanceInfo info, boolean isReplication)
			throws JSONException, ClientApiException, IOException {

		String testOutcome = null;

		JSONObject obj = new JSONObject();
		long startTime = System.currentTimeMillis();
		String riskCheck = null;
		// int lowRiskCount = 0;
		// int mediumRiskCount = 0;
		// int highRiskCount = 0;
		// int informationalRiskCount = 0;
		List<Risk> riskList = new ArrayList<>();

		String timeStamp = SecurityTest.getTime();

		ClientApi zapClient = SecurityTest.getZapServer();
		System.out.println(" Prepping for pre-assessment security test@instance registry  " + target);

		// test for gateway default port -- workaround
		URL mutURL;
		String url;
		int port;
		if (info.getAppName().equalsIgnoreCase("gateway")) {
			url = info.getHealthCheckUrl();
			mutURL = new URL(info.getHealthCheckUrl());
			port = mutURL.getPort();
		} else {
			port = info.getPort();
		}
		target = "http://localhost:" + port;

		// append the OpenAPI document retrieval endpoint
		String swaggerUrl = "http://localhost:" + port + "/v2/api-docs";

		System.out.println("requesting OpenAPI from swaggerUrl : " + swaggerUrl);
		Map<String, String> map = new HashMap<>();
		map.put("url", swaggerUrl);
		ApiResponse openApiResp = zapClient.callApi("openapi", "action", "importUrl", map); // importUrl
		System.out.println("exploring the api for using OpenAPI  @instance registry " + swaggerUrl);

		String scanResult = null;
		System.out.println("prepping to test target : " + target);
		try {
			// Start spidering the target TODO - fetch the OpenAPI and scan the target
			// application
			System.out.println("Spider : " + target);
			// ApiResponse resp = api2.spider.scan(ZAP_API_KEY, target, null, null, null,
			// null);
			ApiResponse resp = zapClient.spider.scan(target, "10", null, null, null);

			String scanid;
			int progress;

			// The scan now returns a scan id to support concurrent scanning
			scanid = ((ApiResponseElement) resp).getValue();

			// Poll the status until it completes
			while (true) {
				Thread.sleep(1000);

				progress = Integer.parseInt(((ApiResponseElement) zapClient.spider.status(scanid)).getValue());
				System.out.println("Spider progress for : " + target + " ---- " + progress + "% ");
				if (progress >= 100) {
					break;
				}
			}
			System.out.println("Spider complete");

			// Give the passive scanner a chance to complete
			Thread.sleep(2000);

			// can the passive scan work ??
			// zapClient.pscan.setEnabled("true");
			// zapClient.pscan.enableAllScanners();
			System.out.println("Active scan for : " + target);
			// resp = api2.ascan.scan(target, null, null, null, null, null);
			resp = zapClient.ascan.scan(target, null, null, "Default Policy", null, null);// Advanced Policy-- the
																							// number
																							// of alerts is : 58 |
																							// Default Policy |Hyper
																							// Policy

			// The scan now returns a scan id to support concurrent scanning
			scanid = ((ApiResponseElement) resp).getValue();
			System.out.println("Scan Idd : => " + ((ApiResponseElement) resp).getValue());
			ApiResponse testResponse;
			// Poll the status until it completes
			// int testDuration = 60000;
			while (true) {

				progress = Integer.parseInt(((ApiResponseElement) zapClient.ascan.status(scanid)).getValue());
				Thread.sleep(60000);// 5 minutes test 300000 2 minute
				System.out.println("stopping active  scan after one minute ");
				zapClient.ascan.stop(scanid);
				break;
				// Thread.sleep(5000);
				// System.out.println("Active Scan progress for : " + target + " ---- " +
				// progress + "%");
				// if (progress >= 100) {
				// break;
				// }
			}
			System.out.println("Active Scan complete for " + target + " " + "@instance registry");
			// testResponse = zapClient.spider.fullResults(scanid);
			// System.out.println("########## Results based on testResponse; =
			// zapClient.spider.fullResults(scanid); ###########");
			// System.out.println(testResponse = zapClient.spider.fullResults(scanid));
			JSONObject mut = new JSONObject();
			mut.put("microserviceName", info.getAppName());// http://localhost:8761/
			mut.put("microservicePort", port);
			mut.put("microserviceIpAddress", info.getIPAddr());
			mut.put("microserviceId", info.getId());
			mut.put("timeStamp", timeStamp);

			ApiResponse hh = zapClient.core.numberOfAlerts(target);
			List<Alert> alertList = zapClient.getAlerts(target, 0, 0);
			// zapClient.core.alerts(target, start, count);
			System.out.println("the number of alerts is : " + hh);
			de.cavas.model.Alert cavasAlert = new de.cavas.model.Alert();
			for (Alert alert : alertList) {
				// System.out.println(alert.getAlert());
				// mut.put("alert", alert.getAlert());
				cavasAlert.setMicroserviceName(info.getAppName());// http://localhost:8761/
				cavasAlert.setMicroservicePort(String.valueOf(port));
				cavasAlert.setMicroservicePort((info.getIPAddr()));
				cavasAlert.setMicroserviceId((info.getId()));
				cavasAlert.setTimeStamp(getTime());
				cavasAlert.setAlert(alert.getAlert());
				// mut.put("risk", alert.getRisk());
				cavasAlert.setRisk(alert.getRisk().toString());
				System.out.println("risk :" + alert.getRisk());
				riskList.add(alert.getRisk());
				testOutcome = PolicyCheck.riskCheck(riskList);
				// riskCheck = String.valueOf(alert.getRisk());
				// mut.put("confidence", alert.getConfidence());
				cavasAlert.setConfidence(alert.getConfidence().toString());
				// cavasAlert.setAlert(alert.getAlert());
				mut.put("url", alert.getUrl());
				cavasAlert.setUrl((alert.getUrl().toString()));
				// mut.put("param", alert.getParam());
				cavasAlert.setParam(alert.getParam().toString());
				// mut.put("solution", alert.getSolution());
				cavasAlert.setSolution(alert.getSolution());
				// mut.put("cweid", alert.getCweId());
				cavasAlert.setCweid(String.valueOf(alert.getCweId()));
				// mut.put("wascid", alert.getWascId());
				cavasAlert.setWascid(String.valueOf(alert.getWascId()));
				// mut.put("attack", alert.getAttack());
				cavasAlert.setAttack(alert.getAttack());
				// mut.put("description", alert.getDescription());
				cavasAlert.setDescription(alert.getDescription());
				// mut.put("evidence", alert.getEvidence());
				cavasAlert.setEvidence(alert.getEvidence());
				// mut.put("name", alert.getName());
				cavasAlert.setName(alert.getName());
				// mut.put("pluginid", alert.getPluginId());
				cavasAlert.setPluginId(alert.getPluginId());
				// mut.put("reference", alert.getReference());
				cavasAlert.setReference(alert.getReference());
				// mut.put("reliability", alert.getReliability());
				// cavasAlert.setRel(alert.getReliability());

				System.out.println("setting the instance status to UP i.e.ready to receive traffic !");
				// System.out.println("alertString : ---- " + mut.toString().toString());
				// info.setStatus(com.netflix.appinfo.InstanceInfo.InstanceStatus.UP);
				System.out.println("saving to db ....");
				System.out.println(cavasAlert);
				System.out.println("roger ....");
				
				// walk-around for the bug 
				 String reportAggregator = "http://localhost:8081/alerts";
				 DefaultHttpClient client = new DefaultHttpClient();
				
				 // trigger the scan report retrieval
				 HttpPost post = new HttpPost(reportAggregator);
				 post.addHeader("User-Agent", USER_AGENT);
				
				 StringEntity input = null;
				
				 try {
				 input = new StringEntity(mut.toString().toString());
				 } catch (UnsupportedEncodingException e1) {
				 // TODO Auto-generated catch block
				 e1.printStackTrace();
				 }
				
				 System.out.println("sending the result : " + input);
				 input.setContentType(MediaType.APPLICATION_JSON);
				 post.setEntity(input);
				
				 System.out.println("Sending persistence request for : ");
				
				 HttpResponse response = client.execute(post);
				 // get the results
				 System.out.println("\nSending 'POST' request to URL : " + reportAggregator);
				 System.out.println("Post parameters : " + post.getEntity());
				 System.out.println("Response Code : " +
				 response.getStatusLine().getStatusCode());
				//
				 BufferedReader rd = new BufferedReader(new
				 InputStreamReader(response.getEntity().getContent()));
				
				 StringBuffer result = new StringBuffer();
				 String line = "";
				 while ((line = rd.readLine()) != null) {
				 result.append(line);
				 }
//
				// System.out.println(result.toString());
				// System.out.println("the results: " + alertList.size());

				// System.out.println("########### Result Summary ####################");

				System.out.println(testOutcome + "    at Security test");
				// implement policy

				
				
				// for using spring data
//				controller.addNewAlert(cavasAlert);
				// if (riskCheck.equalsIgnoreCase("Medium")) {
				// System.err.println(target + " FAILED POLICY CHECK");
				// probationList.remove(target);
				// System.err.println(" REGISTRATION REQUEST REJECTED ");
				// handleCancelation(info.getAppGroupName(), info.getId(), false);
				// //
				// } else {
				// System.out.println( target + " PASSED POLICY CHECK");
				// probationList.remove(target);
				// System.out.println(" REGISTRATION REQUEST APPROVED ");
				// info.setStatus(com.netflix.appinfo.InstanceInfo.InstanceStatus.UP);
				//
				// }

			}
//			alertRepository.save(cavasAlert);
		} catch (Exception e) {
			System.out.println("Exception : " + e.getMessage());
			e.printStackTrace();
		}

//		long stopTime = System.currentTimeMillis();
//		long timetaken = stopTime - startTime;
//		long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(timetaken);
//		System.out.println("============  Done testing  for ==========  : " + info.getHomePageUrl());
//		System.out.println("time taken for the scanning in milliseconds  " + timetaken + " milliseconds");
//
//		System.out.println("time taken for the scanning is " + timeSeconds + " seconds");

		return testOutcome;
	}

	public static ClientApi getZapServer() throws IOException {
		Properties zapProperties = new Properties();
		InputStream input = new FileInputStream("security.properties");
		zapProperties.load(input);
		System.out.println(zapProperties.getProperty("zap.host") + ":" + zapProperties.getProperty("zap.port"));
		ClientApi clientApi = new ClientApi(zapProperties.getProperty("zap.host"),
				Integer.parseInt(zapProperties.getProperty("zap.port")));
		// ZAP_ADDRESS, ZAP_PORT
		return clientApi;
	}

	public static String getTime() {

		DateTime date = new DateTime();
		DateTimeZone cet = DateTimeZone.forID("CET");
		DateTime dateR = date.withZone(cet);

		System.out.println(date);

		return dateR.toString();

	}

}
