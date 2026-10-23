package com.skilton.aihandler.service;

import com.skilton.aihandler.dto.response.AiResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface LmStudioService {

    AiResponseDto processText(String text);

    AiResponseDto processImage(MultipartFile image);

    AiResponseDto processTextAndImage(
            String text,
            MultipartFile image);

}