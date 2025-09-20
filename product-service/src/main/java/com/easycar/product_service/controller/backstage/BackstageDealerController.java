package com.easycar.product_service.controller.backstage;

import com.easycar.common.dto.ResponseDto;
import com.easycar.product_service.constants.ProductConstants;
import com.easycar.product_service.dto.*;
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
        path = "/api/backstage/dealers",
        produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
@Validated
@SuppressWarnings("unused")
public class BackstageDealerController {
    private DealerService dealerService;

    @PostMapping
    public ResponseEntity<ResponseDto> createDealer(@Valid @RequestBody DealerCreateDto dealerDto) {
        dealerService.createDealer(dealerDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto(ProductConstants.STATUS_201, ProductConstants.MESSAGE_201));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseDto> patchDealer(
            @PathVariable Long id, @Valid @RequestBody DealerPatchDto dealerDto) {
        dealerService.patchDealer(id, dealerDto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseDto(ProductConstants.STATUS_200, ProductConstants.MESSAGE_200));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> deleteDealer(@PathVariable Long id) {
        dealerService.deleteDealer(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseDto(ProductConstants.STATUS_200, ProductConstants.MESSAGE_200));
    }
}
