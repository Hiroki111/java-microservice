package com.easycar.order_service.controller;

import com.easycar.common.dto.ContactInfoDto;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        path = "/api/config",
        produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
@SuppressWarnings("unused")
public class ConfigDataController {

    @Autowired
    private Environment environment;

    @Autowired
    private ContactInfoDto contactInfoDto;

    @GetMapping("/java-version")
    public ResponseEntity<String> getJavaVersion() {
        return ResponseEntity.status(HttpStatus.OK).body(environment.getProperty("java.version"));
    }

    @GetMapping("/contact-info")
    public ResponseEntity<ContactInfoDto> getContactInfo() {
        return ResponseEntity.status(HttpStatus.OK).body(contactInfoDto);
    }
}
