package node.connection.controller;


import node.connection._core.response.Response;
import node.connection.dto.user.request.JoinDTO;
import node.connection.service.JoinService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class JoinController {

    private final JoinService joinService;

    public JoinController(JoinService joinService) {

        this.joinService = joinService;
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinProcess(JoinDTO joinDTO) {

        joinService.joinProcess(joinDTO);

        return ResponseEntity.ok().body(Response.success(null));
    }
}
