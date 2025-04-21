package com.easycar.product_service.dto;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "product-service")
@Getter
@Setter
public class ContactInfoDto {

    private String message;
    private Map<String, String> contactDetails;
    private List<String> onCallSupport;
}
