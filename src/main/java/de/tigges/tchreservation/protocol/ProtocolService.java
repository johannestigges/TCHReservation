package de.tigges.tchreservation.protocol;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/protocol")
public class ProtocolService {

	private ProtocolRepository protocolRepository;
	
	public ProtocolService(ProtocolRepository protocolRepository) {
		this.protocolRepository = protocolRepository;
	}

	@GetMapping("/get")
	public Iterable<Protocol> getAll() {
		return protocolRepository.findAll();
	}
}
