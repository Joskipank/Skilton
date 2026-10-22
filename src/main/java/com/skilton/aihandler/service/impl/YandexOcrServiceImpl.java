package com.skilton.aihandler.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skilton.aihandler.service.YandexOcrService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class YandexOcrServiceImpl implements YandexOcrService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private static final Set<String> SUPPORTED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "application/pdf"
    );

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${yandex.ocr.url}")
    private String ocrUrl;

    @Value("${yandex.ocr.folder-id}")
    private String folderId;

    @Value("${yandex.ocr.api-key}")
    private String apiKey;

    public YandexOcrServiceImpl(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    @Override
    public String recognizeText(MultipartFile image) {

        validateImage(image);

        try {
            String mimeType = image.getContentType();

            String base64Content = Base64.getEncoder()
                    .encodeToString(image.getBytes());

            Map<String, Object> requestBody = Map.of(
                    "mimeType", mimeType,
                    "languageCodes", List.of("ru", "en"),
                    "model", "page",
                    "content", base64Content
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Api-Key " + apiKey);
            headers.set("x-folder-id", folderId);

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject(
                    ocrUrl,
                    request,
                    String.class
            );

            if (response == null || response.isBlank()) {
                throw new IllegalStateException(
                        "Yandex OCR вернул пустой ответ"
                );
            }

            return extractText(response);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Не удалось прочитать загруженный файл", e
            );
        } catch (RestClientException e) {
            throw new IllegalStateException(
                    "Ошибка при обращении к Yandex OCR", e
            );
        }
    }

    private void validateImage(MultipartFile image) {

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException(
                    "Необходимо загрузить изображение"
            );
        }

        if (image.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Размер файла не должен превышать 10 МБ"
            );
        }

        String contentType = image.getContentType();

        if (contentType == null
                || !SUPPORTED_TYPES.contains(
                contentType.toLowerCase()
        )) {
            throw new IllegalArgumentException(
                    "Поддерживаются только JPEG, PNG и PDF"
            );
        }
    }

    private String extractText(String response) {

        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode fullText = root.at(
                    "/result/textAnnotation/fullText"
            );

            if (fullText.isMissingNode()) {
                fullText = root.at(
                        "/textAnnotation/fullText"
                );
            }

            if (fullText.isMissingNode()
                    || fullText.isNull()
                    || fullText.asText().isBlank()) {
                throw new IllegalStateException(
                        "Yandex OCR не обнаружил текст в документе"
                );
            }

            return fullText.asText();

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Не удалось разобрать JSON-ответ Yandex OCR", e
            );
        }
    }
}