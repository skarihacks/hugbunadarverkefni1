package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.service.DataDumpService;
import is.hi.hbv501g.demo.service.DataDumpService.DumpResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data-dump")
public class DataDumpController {

    private final DataDumpService dataDumpService;

    public DataDumpController(DataDumpService dataDumpService) {
        this.dataDumpService = dataDumpService;
    }

    //get request for retrieving a full data dump
    @GetMapping
    public ResponseEntity<DumpResponse> dump() {
        return ResponseEntity.ok(dataDumpService.dumpAll());
    }
}
