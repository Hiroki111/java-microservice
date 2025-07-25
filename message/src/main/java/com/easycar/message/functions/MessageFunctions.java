package com.easycar.message.functions;

import com.easycar.message.dto.OrderMessageDto;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageFunctions {
    private static final Logger log = LoggerFactory.getLogger(MessageFunctions.class);

    @Bean
    public Consumer<OrderMessageDto> email() {
        return orderMessageDto -> {
            log.info("Sending email with the details : " + orderMessageDto.toString());
            // logic for sending an email
        };
    }
}
