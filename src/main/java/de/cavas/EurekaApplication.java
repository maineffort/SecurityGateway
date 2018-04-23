package de.cavas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 *
 * @author Gunnar Hillert
 *
 */
@EnableAutoConfiguration
@SpringBootApplication
//@EnableEurekaServer
public class EurekaApplication {

	public static void main(String[] args) throws Exception {
		EurekaServerConfiguration eurekaServerConfiguration;
//		eurekaServerConfiguration.configureViewResolvers(registry);
		SpringApplication.run(EurekaApplication.class, args);
	}

}
