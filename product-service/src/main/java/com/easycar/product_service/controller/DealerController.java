package com.easycar.product_service.controller;

import com.easycar.product_service.dto.*;
import com.easycar.product_service.service.DealerService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        path = "/api/dealers",
        produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
@Validated
@SuppressWarnings("unused")
public class DealerController {

    private DealerService dealerService;

    @GetMapping("/{id}")
    public ResponseEntity<DealerDto> getDealer(@PathVariable Long id) {
        DealerDto dealerDto = dealerService.findDealerById(id);
        return ResponseEntity.status(HttpStatus.OK).body(dealerDto);
    }

    @GetMapping
    @Parameters({
        @Parameter(
                name = "page",
                description = "page number (0-based)",
                in = ParameterIn.QUERY,
                schema = @Schema(type = "integer", defaultValue = "0")),
        @Parameter(
                name = "size",
                description = "page size",
                in = ParameterIn.QUERY,
                schema = @Schema(type = "integer", defaultValue = "100")),
        @Parameter(
                name = "sort",
                description = "sort specification by comma-separated value (e.g. 'address,asc')",
                in = ParameterIn.QUERY,
                schema = @Schema(type = "array"),
                explode = Explode.FALSE,
                style = ParameterStyle.SIMPLE),
    })
    public ResponseEntity<PageDto<DealerDto>> getDealers(
            @ParameterObject @PageableDefault(size = 100) Pageable pageable) {
        PageDto<DealerDto> dealers = dealerService.findDealers(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(dealers);
    }
}
