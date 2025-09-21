package com.easycar.product_service.mapper;

import com.easycar.common.dto.PageDto;
import com.easycar.product_service.domain.entity.Dealer;
import com.easycar.product_service.dto.DealerCreateDto;
import com.easycar.product_service.dto.DealerDto;
import com.easycar.product_service.dto.DealerPatchDto;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;

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

    public static PageDto<DealerDto> mapDealerPageToPageDto(Page<Dealer> dealerPage) {
        List<DealerDto> content = dealerPage.getContent().stream()
                .map(DealerMapper::mapDealerToDealerDto)
                .collect(Collectors.toList());

        return PageDto.<DealerDto>builder()
                .content(content)
                .totalElements((int) dealerPage.getTotalElements())
                .totalPages(dealerPage.getTotalPages())
                .pageSize(dealerPage.getSize())
                .currentPage(dealerPage.getNumber())
                .first(dealerPage.isFirst())
                .last(dealerPage.isLast())
                .build();
    }

    public static void updateDealerFromPatchDto(Dealer dealer, DealerPatchDto dealerDto) {
        if (dealerDto.getName() != null) {
            dealer.setName(dealerDto.getName());
        }
        if (dealerDto.getAddress() != null) {
            dealer.setAddress(dealerDto.getAddress());
        }
    }
}
