package com.example.demo.controller;

import com.example.demo.dto.response.APIResponse;
import com.example.demo.dto.response.APIResponseDto;
import org.springframework.http.ResponseEntity;

public class RestfulController {

    private final String statusSuccess = "Success";

    protected ResponseEntity<APIResponse> ok(String message) {
        return ResponseEntity.ok(APIResponse.builder()
                .status(statusSuccess)
                .message(message)
                .build()
        );
    }

    protected ResponseEntity<APIResponse> ok(APIResponseDto data) {
        return ResponseEntity.ok(APIResponse.builder()
                .status(statusSuccess)
                .data(data)
                .build()
        );
    }

    protected ResponseEntity<APIResponse> ok(APIResponseDto data, String message) {
        return ResponseEntity.ok(APIResponse.builder()
                .status(statusSuccess)
                .message(message)
                .data(data)
                .build()
        );
    }
}
