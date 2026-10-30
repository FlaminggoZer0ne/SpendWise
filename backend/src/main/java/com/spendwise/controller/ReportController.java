package com.spendwise.controller;

import com.spendwise.core.ApiResponse;
import com.spendwise.core.BaseController;
import com.spendwise.core.BusinessException;
import com.spendwise.model.PeriodType;
import com.spendwise.model.Report;
import com.spendwise.model.User;
import com.spendwise.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController extends BaseController {

    private static final String MEMBER = "MEMBER";
    private static final MediaType CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> dashboard(HttpSession s) {
        User user = requireRole(s, MEMBER);
        return ok(reportService.getDashboardSummary(user.getUserId()));
    }

    @GetMapping("/category")
    public ResponseEntity<ApiResponse> categoryReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpSession s) {
        User user = requireRole(s, MEMBER);
        return ok(reportService.createCategoryReport(user.getUserId(), from, to));
    }

    @GetMapping("/period")
    public ResponseEntity<ApiResponse> periodReport(
            @RequestParam(required = false) PeriodType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpSession s) {
        User user = requireRole(s, MEMBER);
        return ok(reportService.createPeriodReport(user.getUserId(), type, from, to));
    }

    /** Unduhan CSV (bukan JSON). {@code periodType} hanya dipakai untuk kind=period (default MONTHLY). */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam String kind,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) PeriodType periodType,
            HttpSession s) {
        User user = requireRole(s, MEMBER);
        Report report;
        String slug;
        if ("category".equals(kind)) {
            report = reportService.createCategoryReport(user.getUserId(), from, to);
            slug = "kategori";
        } else if ("period".equals(kind)) {
            report = reportService.createPeriodReport(user.getUserId(), periodType, from, to);
            slug = "periode";
        } else {
            throw new BusinessException("Jenis laporan harus 'category' atau 'period'");
        }
        String filename = "spendwise_laporan-" + slug + "_" + report.getStartDate() + "_" + report.getEndDate() + ".csv";
        return ResponseEntity.ok()
                .contentType(CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(report.toCSV().getBytes(StandardCharsets.UTF_8));
    }
}
