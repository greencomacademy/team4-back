package com.deliveryinsider.domain.mock.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mock-data")
@RequiredArgsConstructor
public class MockOrderController {
    @PostMapping("/order")
    public ResponseEntity<?> create()  {
        return ResponseEntity.ok().build();
    }

}
