package org.sopt.haphap.status.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.sopt.haphap.realtime.application.StatusReportService;
import org.sopt.haphap.realtime.code.RealtimeSuccessCode;
import org.sopt.haphap.status.dto.StatusReportCreateRequest;
import org.sopt.haphap.status.dto.StatusReportCreateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/status-reports")
public class StatusReportController {

    private final StatusReportService statusReportService;

    @PostMapping
    public ResponseEntity<SuccessResponse<StatusReportCreateResponse>> create(
            @RequestHeader("X-Member-Id") Long memberId,
            @Valid @RequestBody StatusReportCreateRequest request
    ) {
        StatusReportCreateResponse response = statusReportService.create(memberId, request);
        return ResponseEntity
                .status(RealtimeSuccessCode.STATUS_REPORT_CREATED.getStatus())
                .body(ApiResponse.success(RealtimeSuccessCode.STATUS_REPORT_CREATED, response));
    }
}