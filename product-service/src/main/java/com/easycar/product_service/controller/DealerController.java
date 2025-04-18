package com.easycar.product_service.controller;

import com.easycar.product_service.constants.ProductConstants;
import com.easycar.product_service.dto.DealerCreateDto;
import com.easycar.product_service.dto.DealerDto;
import com.easycar.product_service.dto.ResponseDto;
import com.easycar.product_service.service.DealerService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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

    @PostMapping
    public ResponseEntity<ResponseDto> createDealer(@Valid @RequestBody DealerCreateDto dealerDto) {
        dealerService.createDealer(dealerDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto(ProductConstants.STATUS_201, ProductConstants.MESSAGE_201));
    }
}
