package org.sopt.haphap.domain.alram.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "알람 등록",description = "공고별 알람을 on/off 합니다.")
public interface AlramApiDocs {

    @Operation(summary = "알람 등록",
            description = """
                    알람을 등록합니다. 
                    - 공고 아이디를 받아 해당 공고의 알람 설정을 on합니다. 
                    """)
    ResponseEntity<SuccessResponse<Void>> setAlrams(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long postingId
    );

    @Operation(summary = "알람 삭제",
            description = """
                    알람을 삭제합니다. 
                    - 공고 아이디를 받아 해당 공고의 알람 설정을 off합니다. 
                    """)
    ResponseEntity<SuccessResponse<Void>> deleteAlrams(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long postingId
    );
}
