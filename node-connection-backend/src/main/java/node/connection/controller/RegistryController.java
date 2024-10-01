package node.connection.controller;

import node.connection._core.response.Response;
import node.connection._core.security.CustomUserDetails;
import node.connection.dto.registry.RegistryDocumentDto;
import node.connection.dto.registry.response.RegistryDocumentByHashDto;
import node.connection.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/registry")
public class RegistryController {

    private final RegistryService registryService;

    public RegistryController(@Autowired RegistryService registryService) {
        this.registryService = registryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRegistryDocument(@AuthenticationPrincipal CustomUserDetails userDetails,
                                         @PathVariable("id") String id
    ) {
        RegistryDocumentDto document = this.registryService.getRegistryDocumentById(userDetails, id);
        return ResponseEntity.ok().body(Response.success(document));
    }

    @GetMapping("/address")
    public ResponseEntity<?> getRegistryDocumentByAddress(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                          @Param("address") String address,
                                                          @Param("detailAddress") String detailAddress
    ) {
        List<RegistryDocumentDto> documents = this.registryService.getRegistryDocumentByAddress(userDetails, address, detailAddress);
        return ResponseEntity.ok().body(Response.success(documents));
    }

    @GetMapping("/issuance-hash/{hash}")
    public ResponseEntity<?> getRegistryDocumentByIssuanceHash(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                               @PathVariable("hash") String issuanceHash
    ) {
        RegistryDocumentByHashDto dto = this.registryService.getRegistryDocumentByHash(userDetails, issuanceHash);
        return ResponseEntity.ok().body(Response.success(dto));
    }
}
