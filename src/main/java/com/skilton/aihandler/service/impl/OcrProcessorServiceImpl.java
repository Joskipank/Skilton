package com.skilton.aihandler.service.impl;

import com.skilton.aihandler.dto.response.AiResponseDto;
import com.skilton.aihandler.service.LmStudioService;
import com.skilton.aihandler.service.OcrProcessorService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OcrProcessorServiceImpl implements OcrProcessorService {

    private final LmStudioService lmStudioService;

    public OcrProcessorServiceImpl(
            LmStudioService lmStudioService
    ) {
        this.lmStudioService = lmStudioService;
    }

    @Override
    public AiResponseDto processImage(MultipartFile image) {
        return processOrder(null, image);
    }

    @Override
    public AiResponseDto processText(String text) {
        return processOrder(text, null);
    }

    @Override
    public AiResponseDto processOrder(
            String text,
            MultipartFile image
    ) {
        boolean hasText = text != null && !text.isBlank();
        boolean hasImage = image != null && !image.isEmpty();

        if (!hasText && !hasImage) {
            throw new IllegalArgumentException(
                    "Необходимо передать текст заявки или изображение"
            );
        }

        if (hasText && hasImage) {
            return lmStudioService.processTextAndImage(text, image);
        }

        if (hasImage) {
            return lmStudioService.processImage(image);
        }

        return lmStudioService.processText(text);
    }
}