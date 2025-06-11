package com.easycar.message.functions;

import com.easycar.message.dto.OrderMessageDto;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import java.util.function.Function;

@Configuration
public class MessageFunctions {
    private static final Logger log = LoggerFactory.getLogger(MessageFunctions.class);

    @Bean
    public Function<OrderMessageDto, OrderMessageDto> email() {
        return orderMessageDto -> {
            log.info("Sending email with the details : " +  orderMessageDto.toString());
            return orderMessageDto;
        };
    }

    @Bean
    public Function<OrderMessageDto, Long> sms() {
        return orderMessageDto -> {
            log.info("Sending sms with the details : " +  orderMessageDto.toString());
            return orderMessageDto.orderId();
        };
    }
}
