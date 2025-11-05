package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.service.AuthService;
import is.hi.hbv501g.demo.service.DataDumpService;
import is.hi.hbv501g.demo.service.DataDumpService.DumpResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data-dump")
public class DataDumpController {

    private final DataDumpService dataDumpService;
    private final AuthService authService;

    public DataDumpController(DataDumpService dataDumpService, AuthService authService) {
        this.dataDumpService = dataDumpService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<DumpResponse> dump(@RequestHeader(AuthController.SESSION_HEADER) String sessionId) {
        authService.requireUser(sessionId);
        return ResponseEntity.ok(dataDumpService.dumpAll());
    }
}
