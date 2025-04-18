package com.easycar.product_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueDealerAddressValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface UniqueDealerAddress {
    String message() default "address already exists";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
