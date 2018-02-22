package eurekademo;
/*
 * Copyright 2013-2015 the original author or authors.
 *
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
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRenewedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.zaproxy.clientapi.core.Alert;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.EurekaServerConfig;
import com.netflix.eureka.lease.Lease;
import com.netflix.eureka.registry.PeerAwareInstanceRegistryImpl;
import com.netflix.eureka.resources.ServerCodecs;

import lombok.extern.apachecommons.CommonsLog;

/**
 * @author Spencer Gibb
 */
@CommonsLog
public class InstanceRegistry extends PeerAwareInstanceRegistryImpl implements ApplicationContextAware {
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(InstanceRegistry.class);
	private ApplicationContext ctxt;
	private int defaultOpenForTrafficCount;
	private static List<String> probationList = new ArrayList<String>(); // probation list for temporary registration

	private final static String USER_AGENT = "Mozilla/5.0";
	private static final String ZAP_ADDRESS = "localhost";
	private static final int ZAP_PORT = 8181;
	private static final String ZAP_API_KEY = null;
	private static final String TESTING_MODE = "strict";

	public static ClientApi api = new ClientApi(ZAP_ADDRESS, ZAP_PORT);

	// register the instances that are enrolled in the service
	public InstanceRegistry(EurekaServerConfig serverConfig, EurekaClientConfig clientConfig, ServerCodecs serverCodecs,
			EurekaClient eurekaClient, int expectedNumberOfRenewsPerMin, int defaultOpenForTrafficCount) {
		super(serverConfig, clientConfig, serverCodecs, eurekaClient);

		this.expectedNumberOfRenewsPerMin = expectedNumberOfRenewsPerMin;
		this.defaultOpenForTrafficCount = defaultOpenForTrafficCount;

	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.ctxt = context;
	}

	/**
	 * If
	 * {@link PeerAwareInstanceRegistryImpl#openForTraffic(ApplicationInfoManager, int)}
	 * is called with a zero argument, it means that leases are not automatically
	 * cancelled if the instance hasn't sent any renewals recently. This happens for
	 * a standalone server. It seems like a bad default, so we set it to the
	 * smallest non-zero value we can, so that any instances that subsequently
	 * register can bump up the threshold.
	 */
	@Override
	public void openForTraffic(ApplicationInfoManager applicationInfoManager, int count) {
		super.openForTraffic(applicationInfoManager, count == 0 ? this.defaultOpenForTrafficCount : count);
	}

	@Override
	public void register(InstanceInfo info, int leaseDuration, boolean isReplication) {
		System.out.println("=========================== regsitry 01 ============================");

		try {
			handleRegistration(info, leaseDuration, isReplication);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.register(info, leaseDuration, isReplication);
	}

	@Override
	public void register(InstanceInfo info, final boolean isReplication) {
		System.out.println("=========================== regsitry 02 ============================");
		if ((probationList.contains(info.getAppName())) && (!probationList.isEmpty())) {
			System.out.println(info.getAppName() + "   " + info.getHomePageUrl() + "  " +  info.getHealthCheckUrl()
					+ " already in probation list ? ");
			System.out.println("checking the alternative scanning information per microservice -- " + info.getIPAddr()
					+ ":" + info.getPort());
			System.out.println(
					"========================    probationList.size() ======================= " + probationList.size()); // info.getIPAddr();

			try {
				
				System.out.println("handlieRegistration");
				handleRegistration(info, resolveInstanceLeaseDuration(info), isReplication);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			super.register(info, isReplication);
		} else {
			try {
				System.out.println(info.getAppName() + "   " + info.getHomePageUrl() + " added to probation list ? ");
				probationList.add(info.getAppName());

				// set the intabce status to starting similar to
				// https://stackoverflow.com/questions/46123498/how-to-delay-eureka-client-registration-with-eureka-server
				info.setStatus(com.netflix.appinfo.InstanceInfo.InstanceStatus.STARTING);

				// set the instance as starting ... not ready for traffic yet

				System.out.println("instance still in starting mode to allow time for security : " + info.getStatus());
				handleTempRegistration(info, resolveInstanceLeaseDuration(info), isReplication);
			} catch (JSONException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean cancel(String appName, String serverId, boolean isReplication) {
		handleCancelation(appName, serverId, isReplication);
		return super.cancel(appName, serverId, isReplication);
		// SecurityTest.tester(info.getHomePageUrl());

	}

	@Override
	public boolean renew(final String appName, final String serverId, boolean isReplication) {
		log("renew " + appName + " serverId " + serverId + ", isReplication {}" + isReplication);
		List<Application> applications = getSortedApplications();
		for (Application input : applications) {
			if (input.getName().equals(appName)) {
				InstanceInfo instance = null;
				for (InstanceInfo info : input.getInstances()) {
					if (info.getId().equals(serverId)) {
						instance = info;
						break;
					}
				}
				publishEvent(new EurekaInstanceRenewedEvent(this, appName, serverId, instance, isReplication));
				break;
			}
		}
		return super.renew(appName, serverId, isReplication);
	}

	@Override
	protected boolean internalCancel(String appName, String id, boolean isReplication) {
		handleCancelation(appName, id, isReplication);
		return super.internalCancel(appName, id, isReplication);
	}

	private void handleCancelation(String appName, String id, boolean isReplication) {
		log("cancel " + appName + ", serverId " + id + ", isReplication " + isReplication);
		publishEvent(new EurekaInstanceCanceledEvent(this, appName, id, isReplication));
	}

	// assign a temporary vip within a short lease time, scan the application and
	// based on the results reassign a more exclusive IP address within the local ip
	// address range
	private void handleRegistration(InstanceInfo info, int leaseDuration, boolean isReplication) throws JSONException {
		log("register " + info.getAppName() + ", vip " + info.getVIPAddress() + ", leaseDuration " + leaseDuration
				+ ", isReplication " + isReplication);
		publishEvent(new EurekaInstanceRegisteredEvent(this, info, leaseDuration, isReplication));
	}

	// SecurityTest.tester(info.getHomePageUrl());

	private void handleTempRegistration(InstanceInfo info, int leaseDuration, boolean isReplication)
			throws JSONException, IOException, ClientApiException {

		System.out.println("============  probationary registration!!  ==========  : " + info.getHomePageUrl());

		log("probationary register " + info.getAppName() + ", vip " + info.getVIPAddress() + ", leaseDuration "
				+ leaseDuration + ", isReplication " + isReplication);

		// trigger pre-registration security test , ideally should send this request to
		// the security service
		preRegistrationSecurityTest(info.getHomePageUrl(), info, isReplication);

		// publishEvent(new EurekaInstanceRegisteredEvent(this, info, leaseDuration,
		// isReplication));
		// if the results are fine... else abort
		register(info, isReplication);

	}

	private void log(String message) {
		if (log.isDebugEnabled()) {
			log.debug(message);
		}
	}

	public void publishEvent(ApplicationEvent applicationEvent) {
		this.ctxt.publishEvent(applicationEvent);

	}

	private int resolveInstanceLeaseDuration(final InstanceInfo info) {
		int leaseDuration = Lease.DEFAULT_DURATION_IN_SECS;
		if (info.getLeaseInfo() != null && info.getLeaseInfo().getDurationInSecs() > 0) {
			leaseDuration = info.getLeaseInfo().getDurationInSecs();
		}
		return leaseDuration;
	}

	public void preRegistrationSecurityTest(String target, InstanceInfo info, boolean isReplication) throws JSONException, ClientApiException{

		JSONObject obj = new JSONObject();
		long startTime = System.currentTimeMillis();
//		String microserviceName = info.getAppName();
//		int microservicePort = info.getPort();
//		String microserviceIpAddress = info.getIPAddr();
//		String microserviceId = info.getId();
		String timeStamp = SecurityTest.getTime();
		String alertString;
//		obj.put("microserviceId",microserviceId);
//		obj.put("timeStamp", timeStamp);
//		obj.put("microserviceIpAddress", microserviceIpAddress);
//		obj.put("microserviceName", microserviceName);
//		obj.put("microservicePort", microservicePort);
		target = "http://localhost:"+info.getPort();
		ClientApi api2 = new ClientApi(ZAP_ADDRESS, ZAP_PORT);
		System.out.println(" Prepping for pre-assessment security test@instance registry  " + target);
		String swaggerUrl = "http://localhost:"+info.getPort()+"/v2/api-docs";

		System.out.println("requesting OpenAPI from swaggerUrl : " + swaggerUrl);
		Map<String, String> map = new HashMap<>();
		 map.put("url", swaggerUrl);
			ApiResponse openApiResp = 
					api2.callApi("openapi", "action", "importUrl", map); //importUrl
			System.out.println("exploring the api for using OpenAPI  @instance registry " + swaggerUrl);
			

		String scanResult = null;
		System.out.println("prepping to test target : " + target);
		try {
			// Start spidering the target TODO - fetch the OpenAPI and scan the target application
			System.out.println("Spider : " + target);
//			ApiResponse resp = api2.spider.scan(ZAP_API_KEY, target, null, null, null, null);
			ApiResponse resp = api2.spider.scan(target, "10", null, null, null);
			
			String scanid;
			int progress;

			// The scan now returns a scan id to support concurrent scanning
			scanid = ((ApiResponseElement) resp).getValue();

			// Poll the status until it completes
			while (true) {
				Thread.sleep(1000);
				
				progress = Integer.parseInt(((ApiResponseElement) api2.spider.status(scanid)).getValue());
				System.out.println("Spider progress for : " + target + " ---- " +  progress + "%"  + "@instance registry" );
				if (progress >= 100) {
					break;
				}
			}
			System.out.println("Spider complete");

			// Give the passive scanner a chance to complete
			Thread.sleep(2000);

			System.out.println("Active scan for : " + target);
//			resp = api2.ascan.scan(target, null, null, null, null, null);
			//TODO --  create a one minute or time-based timing
			resp = api2.ascan.scan(target, null, null, "Default Policy", null, null);

			// The scan now returns a scan id to support concurrent scanning
			scanid = ((ApiResponseElement) resp).getValue();
			System.out.println("Scan Idd : => " + ((ApiResponseElement) resp).getValue());

			// Poll the status until it completes
			while (true) {
				Thread.sleep(60000);
				System.out.println("stopping active  scan after one minute ");
				api2.ascan.stop(scanid);
				break;
//				Thread.sleep(5000);
//				progress = Integer.parseInt(((ApiResponseElement) api2.ascan.status(scanid)).getValue());
//				System.out.println("Active Scan progress for : " + target + " ---- " +  progress + "%");
//				if (progress >= 100) {
//					break;
//				}
			}
			System.out.println("Active Scan complete for " + target + " "  + "@instance registry" );
	
			JSONObject mut = new JSONObject();
			mut.put("microserviceName", info.getAppName());//http://localhost:8761/
			mut.put("microservicePort", info.getPort());
			mut.put("microserviceIpAddress", info.getIPAddr());
			mut.put("microserviceId", info.getId());
			mut.put("timeStamp", timeStamp);
			
			List<Alert> alertList = api2.getAlerts(target, 0, 10);
			for (Alert alert : alertList) {
				ApiResponse hh = api2.core.numberOfAlerts(target);
				System.out.println("the number of alerts is : " + hh);
				System.out.println(alert.getAlert());
				
				mut.put("alert", alert.getAlert());
				mut.put("risk", alert.getRisk());
				mut.put("confidence", alert.getConfidence());
				mut.put("url", alert.getUrl());
				mut.put("param", alert.getParam());
				mut.put("solution", alert.getSolution());
				mut.put("cweid", alert.getCweId());
				mut.put("wascid", alert.getWascId());
				mut.put("attack", alert.getAttack());
				mut.put("description", alert.getDescription());
				mut.put("evidence", alert.getEvidence());
				mut.put("name", alert.getName());
				mut.put("pluginid", alert.getPluginId());
				mut.put("reference", alert.getReference());
				mut.put("reliability", alert.getReliability());
				
//			alertString = alertList.get(1).toString() + mut.toString();
			
			
			//TODO  policy check to confirm if to allow instance
			System.out.println("setting the instance status to UP i.e.ready to receive traffic !");
			System.out.println("alertString : ---- " + mut.toString().toString());
			info.setStatus(com.netflix.appinfo.InstanceInfo.InstanceStatus.UP);
			
			//assuming that the instance failed the security test -- not effective here better approach required	
			
//			handleCancelation(info.getAppGroupName(), info.getId(), false);
		
			
			String reportAggregator = "http://localhost:8081/alerts";
					DefaultHttpClient client = new DefaultHttpClient();
			
					
			// trigger the scan report retrieval		
			HttpPost post = new HttpPost(reportAggregator);
			post.addHeader("User-Agent", USER_AGENT);
			
//			String request = mut.toString();
			
			
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
//			post.setEntity(new UrlEncodedFormEntity(urlParameters));

			System.out.println("Sending persistence request for : ");
			
			HttpResponse response = client.execute(post);	
			// get the results 
			System.out.println("\nSending 'POST' request to URL : " + reportAggregator);
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
			probationList.remove(target);

//		}
//			// get the summary of the test i.e. passed or failed
//			// the security policy modes are also enforced here
//			if(result.toString().equals("fine") || result.toString()=="fine"){
//				for (String instances : probationList) {
//					if(instances.equals(target))
////						if (TESTING_MODE.equals("strict")) {
////							
////						}
//						
//						probationList.remove(target);
//					System.out.println(target + "removed from the probation list");
//					System.out.println(instances);
//				}
//				
//				
//			}
//			GetTest.getJsonRReport(target);
		} catch (Exception e) {
			System.out.println("Exception : " + e.getMessage());
			e.printStackTrace();
		}
		long stopTime = System.currentTimeMillis();
		long timetaken = stopTime - startTime;
		long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(timetaken);
		System.out.println("============  Done testing  for ==========  : " + info.getHomePageUrl() );
		System.out.println("time taken for the scanning in milliseconds  "  + timetaken + " milliseconds");

		System.out.println("time taken for the scanning is "  + timeSeconds + " seconds");
//		handleRegistration(info, defaultOpenForTrafficCount, isReplication);
	}
}
