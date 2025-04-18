package com.easycar.product_service.mapper;

import com.easycar.product_service.domain.entity.Dealer;
import com.easycar.product_service.dto.DealerCreateDto;
import com.easycar.product_service.dto.DealerDto;

public class DealerMapper {
    public static DealerDto mapDealerToDealerDto(Dealer dealer) {
        return DealerDto.builder()
                .id(dealer.getId())
                .name(dealer.getName())
                .address(dealer.getAddress())
                .build();
    }

    public static Dealer mapDealerCreateDtoToDealer(DealerCreateDto dealerCreateDto) {
        return Dealer.builder()
                .name(dealerCreateDto.getName())
                .address(dealerCreateDto.getAddress())
                .build();
    }
}
