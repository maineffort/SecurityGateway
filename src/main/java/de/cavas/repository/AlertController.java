package de.cavas.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.cavas.model.Alert;

//@Controller
@EnableJpaRepositories
public class AlertController {

	
	@Autowired
	private AlertRepository alertRepository;
	
	@GetMapping(path="/add") // Map ONLY GET Requests
	public @ResponseBody String addNewAlert (@RequestParam Alert cavasAlert) {
		// @ResponseBody means the returned String is the response, not a view name
		// @RequestParam means it is a parameter from the GET or POST request

//		Alert alert = new Alert();
//		alert.setAlert(cavasAlert.getAlert());
//		alert.setAttack(cavasAlert.getAttack());
//		System.out.println("cavasAlert: " + cavasAlert);
//		System.out.println("alert: " + alert);
		System.out.println("cavasAlert @ AlertController: " + cavasAlert);
		alertRepository.save(cavasAlert);
//		(microserviceName,  microservicePort,  microserviceIpAddress, microserviceId,
//				 timeStamp,  sourceid,  other,  method,  evidence,  pluginId,
//				 cweid,  confidence,  wascid,  description,  messageId,  url,
//				 reference,  solution,  alert,  param,  attack,  name,  risk,
//				 id);
//		n.setName(name);
//		n.setEmail(email);
//		userRepository.save(n);
		return "Saved";
	}
	

}
