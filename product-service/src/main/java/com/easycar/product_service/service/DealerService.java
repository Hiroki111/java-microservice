package com.easycar.product_service.service;

import com.easycar.product_service.domain.entity.Dealer;
import com.easycar.product_service.dto.DealerCreateDto;
import com.easycar.product_service.dto.DealerDto;
import com.easycar.product_service.dto.DealerPatchDto;
import com.easycar.product_service.dto.PageDto;
import com.easycar.product_service.exception.ResourceNotFoundException;
import com.easycar.product_service.mapper.DealerMapper;
import com.easycar.product_service.repository.DealerRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DealerService {
    private DealerRepository dealerRepository;

    public DealerDto findDealerById(Long id) {
        Dealer dealer = dealerRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id.toString()));
        return DealerMapper.mapDealerToDealerDto(dealer);
    }

    public PageDto<DealerDto> findDealers(Pageable pageable) {
        Page<Dealer> dealerPage = dealerRepository.findAll(pageable);
        return DealerMapper.mapDealerPageToPageDto(dealerPage);
    }

    public void createDealer(DealerCreateDto dealerDto) {
        Dealer dealer = DealerMapper.mapDealerCreateDtoToDealer(dealerDto);
        dealerRepository.save(dealer);
    }

    public void patchDealer(Long id, DealerPatchDto dealerDto) {
        Dealer dealer = dealerRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer", "id", id.toString()));
        DealerMapper.updateDealerFromPatchDto(dealer, dealerDto);
        dealerRepository.save(dealer);
    }

    public void deleteDealer(Long id) {
        Dealer dealer = dealerRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer", "id", id.toString()));
        dealerRepository.delete(dealer);
    }
}
