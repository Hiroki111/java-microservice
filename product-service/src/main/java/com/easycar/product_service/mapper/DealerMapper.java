package com.easycar.product_service.mapper;

import com.easycar.product_service.domain.entity.Dealer;
import com.easycar.product_service.dto.DealerCreateDto;

public class DealerMapper {

    public static Dealer mapDealerCreateDtoToDealer(DealerCreateDto dealerCreateDto) {
        return Dealer.builder()
                .name(dealerCreateDto.getName())
                .address(dealerCreateDto.getAddress())
                .build();
    }
}
