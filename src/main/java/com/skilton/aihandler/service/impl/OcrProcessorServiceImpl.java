package com.skilton.aihandler.service.impl;

import com.skilton.aihandler.dto.response.AiResponseDto;
import com.skilton.aihandler.service.LmStudioService;
import com.skilton.aihandler.service.OcrProcessorService;
import com.skilton.aihandler.service.YandexOcrService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OcrProcessorServiceImpl implements OcrProcessorService {

    private final YandexOcrService yandexOcrService;
    private final LmStudioService lmStudioService;

    public OcrProcessorServiceImpl(
            YandexOcrService yandexOcrService,
            LmStudioService lmStudioService
    ) {
        this.yandexOcrService = yandexOcrService;
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

    public AiResponseDto processOrder(String text, MultipartFile image) {
        boolean hasText = text != null && !text.isBlank();
        boolean hasImage = image != null && !image.isEmpty();

        if (!hasText && !hasImage) {
            throw new IllegalArgumentException(
                    "Необходимо передать текст заявки или изображение"
            );
        }

        StringBuilder combinedText = new StringBuilder();

        // Источник 1: текст, введённый пользователем.
        if (hasText) {
            combinedText.append("Текст заявки от пользователя:\n")
                    .append(text.trim())
                    .append("\n\n");
        }

        // Источник 2: текст, распознанный с изображения.
        if (hasImage) {
            String recognizedText = yandexOcrService.recognizeText(image);

            if (recognizedText == null || recognizedText.isBlank()) {
                throw new IllegalStateException(
                        "Не удалось распознать текст на изображении"
                );
            }

            combinedText.append("Текст, распознанный с изображения:\n")
                    .append(recognizedText.trim())
                    .append("\n");
        }

        // Обрабатываем объединённую заявку одним вызовом LLM.
        AiResponseDto result = lmStudioService.processText(
                combinedText.toString()
        );

        if (result == null) {
            throw new IllegalStateException(
                    "Не удалось обработать заявку с помощью языковой модели"
            );
        }

        return result;
    }
}