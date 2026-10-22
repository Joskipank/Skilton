package com.skillton.aihandler.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillton.aihandler.dto.response.AiResponseDto;
import com.skillton.aihandler.service.LmStudioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class LmStudioServiceImpl implements LmStudioService {

    private static final String SYSTEM_PROMPT = """
            Ты — специалист по обработке заявок на изготовление клеёного бруса.

            Твоя задача — преобразовать текст заявки в структурированный JSON.

            Требуемый формат:
            {
              "orders": [
                {
                  "name": "Название детали",
                  "width": 200,
                  "height": 450,
                  "length": 7200,
                  "count": null,
                  "material": null,
                  "comment": "Комментарий"
                }
              ],
              "assumptions": "Сделанные предположения",
              "questions": [
                "Вопрос заказчику"
              ]
            }

            Правила:
            1. Размеры width, height и length указываются в миллиметрах.
            2. count — количество деталей в штуках.
            3. Не придумывай отсутствующие значения.
            4. Если количество или размеры неизвестны, используй null.
            5. Поле material должно содержать название материала из заявки.
               Не придумывай материал, если он не указан.
               Если материал неизвестен, возвращай null.
            6. Если материал не указан, используй null.
            7. Если информации недостаточно, укажи предположения
               в поле assumptions и вопросы в поле questions.
            8. Если данных достаточно, questions должен быть пустым массивом.
            9. Сохраняй все детали, указанные в заявке.
            10. Возвращай только JSON без Markdown и пояснений.
            
            ВАЖНОЕ ПРАВИЛО ОБРАБОТКИ ПРОТИВОРЕЧИЙ:
            Если сведения из текстовых данных и предоставленного изображения противоречат друг другу:
            1. ЗАПРЕЩЕНО выбирать значение наугад или пытаться угадать правильное.
            2. Обязательно зафиксируйте и подробно опишите данное противоречие в поле "questions".
            3. Если на основе контекста невозможно однозначно определить истинное значение, оставьте соответствующее поле данных равным null.
            """;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${lm-studio.url}")
    private String lmStudioUrl;

    @Value("${lm-studio.model}")
    private String modelName;

    public LmStudioServiceImpl(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    @Override
    public AiResponseDto processText(String text) {

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Текст заявки не должен быть пустым"
            );
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<Map<String, String>> messages = List.of(
                Map.of(
                        "role", "system",
                        "content", SYSTEM_PROMPT
                ),
                Map.of(
                        "role", "user",
                        "content", text
                )
        );

        Map<String, Object> requestBody = Map.of(
                "model", modelName,
                "messages", messages,
                "temperature", 0.1
        );

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

        try {
            Map<?, ?> response = restTemplate.postForObject(
                    lmStudioUrl,
                    entity,
                    Map.class
            );

            if (response == null) {
                throw new IllegalStateException(
                        "LM Studio вернула пустой ответ"
                );
            }

            Object choicesObject = response.get("choices");

            if (!(choicesObject instanceof List<?> choices)
                    || choices.isEmpty()
                    || !(choices.get(0) instanceof Map<?, ?> choice)) {
                throw new IllegalStateException(
                        "В ответе LM Studio отсутствует choices"
                );
            }

            Object messageObject = choice.get("message");

            if (!(messageObject instanceof Map<?, ?> message)) {
                throw new IllegalStateException(
                        "В ответе LM Studio отсутствует message"
                );
            }

            Object contentObject = message.get("content");

            if (!(contentObject instanceof String content)
                    || content.isBlank()) {
                throw new IllegalStateException(
                        "LM Studio вернула пустой текст"
                );
            }

            return parseResponse(content);

        } catch (RestClientException e) {
            throw new IllegalStateException(
                    "Не удалось выполнить запрос к LM Studio", e
            );
        }
    }

    private AiResponseDto parseResponse(String content) {

        String json = content.trim();

        // На случай, если модель всё же вернула Markdown.
        if (json.startsWith("```json")) {
            json = json.substring(7).trim();
        } else if (json.startsWith("```")) {
            json = json.substring(3).trim();
        }

        if (json.endsWith("```")) {
            json = json.substring(0, json.length() - 3).trim();
        }

        try {
            AiResponseDto result =
                    objectMapper.readValue(json, AiResponseDto.class);

            if (result.getOrders() == null) {
                throw new IllegalStateException(
                        "В ответе модели отсутствует список orders"
                );
            }

            if (result.getQuestions() == null) {
                result.setQuestions(List.of());
            }

            return result;

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "LM Studio вернула некорректный JSON: " + json, e
            );
        }
    }
}