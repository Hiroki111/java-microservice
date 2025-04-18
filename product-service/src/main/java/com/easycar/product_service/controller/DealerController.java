package com.easycar.product_service.controller;

import com.easycar.product_service.constants.ProductConstants;
import com.easycar.product_service.dto.DealerCreateDto;
import com.easycar.product_service.dto.ResponseDto;
import com.easycar.product_service.service.DealerService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        path = "/api/dealers",
        produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
@Validated
@SuppressWarnings("unused")
public class DealerController {

    private DealerService dealerService;

    @PostMapping
    public ResponseEntity<ResponseDto> createDealer(@Valid @RequestBody DealerCreateDto dealerDto) {
        dealerService.createDealer(dealerDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto(ProductConstants.STATUS_201, ProductConstants.MESSAGE_201));
    }
}
