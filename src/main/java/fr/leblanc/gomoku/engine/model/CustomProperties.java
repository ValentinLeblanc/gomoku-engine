package fr.leblanc.gomoku.engine.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "fr.leblanc.gomoku")
@Data
public class CustomProperties {
	
	private String webAppUrl;
	
	private boolean displayAnalysis = false;
	
}
