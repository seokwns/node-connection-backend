package node.connection.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import node.connection._core.response.Response;
import node.connection.dto.registry.RegistryDocumentDto;
import node.connection.dto.registry.request.RegistryCreateRequest;
import node.connection.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registry")
public class RegistryController {

    private final RegistryService registryService;

    public RegistryController(@Autowired RegistryService registryService) {
        this.registryService = registryService;
    }

    @PostMapping("")
    public ResponseEntity<?> createRegistry(@RequestBody RegistryCreateRequest request) throws JsonProcessingException {
        this.registryService.createRegistryDocument(request);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRegistry(@PathVariable("id") String id) throws JsonProcessingException {
        RegistryDocumentDto document = this.registryService.getRegistryDocumentById(id);
        return ResponseEntity.ok().body(Response.success(document));
    }
}
