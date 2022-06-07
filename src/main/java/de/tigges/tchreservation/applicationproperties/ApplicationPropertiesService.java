package de.tigges.tchreservation.applicationproperties;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/application")
@RequiredArgsConstructor
public class ApplicationPropertiesService {

	private final ApplicationPropertiesConfiguration configuration;

	@GetMapping("properties")
	ApplicationProperties getProperties() {
		return map(configuration);
	}

	private static ApplicationProperties map(ApplicationPropertiesConfiguration c) {
		return ApplicationProperties.builder().title(c.getTitle()).build();
	}
}
