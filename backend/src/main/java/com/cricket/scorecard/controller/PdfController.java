package com.cricket.scorecard.controller;

import com.cricket.scorecard.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
public class PdfController {

    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    // GET /api/matches/{matchKey}/report/pdf
    @GetMapping("/{matchKey}/report/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String matchKey) {
        try {
            byte[] pdf = pdfService.generateMatchReport(matchKey);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "match-" + matchKey + ".pdf");
            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
