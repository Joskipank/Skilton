package com.skillton.aihandler.controller;

import com.skillton.aihandler.dto.response.AiResponseDto;
import com.skillton.aihandler.service.OcrProcessorService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OcrProcessorService ocrProcessorService;

    public OrderController(OcrProcessorService ocrProcessorService) {
        this.ocrProcessorService = ocrProcessorService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AiResponseDto processOrder(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) MultipartFile image
    ) {
        return ocrProcessorService.processOrder(text, image);
    }
}