package com.easycar.product_service.validation;

import com.easycar.product_service.repository.DealerRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueDealerAddressValidator implements ConstraintValidator<UniqueDealerAddress, String> {

    private final DealerRepository dealerRepository;

    @Override
    public boolean isValid(String address, ConstraintValidatorContext context) {
        if (address == null || address.trim().isEmpty()) {
            return true; // Let @NotBlank handle this
        }
        return !dealerRepository.existsByAddress(address);
    }
}
