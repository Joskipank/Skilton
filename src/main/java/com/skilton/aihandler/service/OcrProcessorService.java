package com.skilton.aihandler.service;

import com.skilton.aihandler.dto.response.AiResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface OcrProcessorService {

    AiResponseDto processImage(MultipartFile image);

    AiResponseDto processText(String text);

    AiResponseDto processOrder(String text, MultipartFile image);
}