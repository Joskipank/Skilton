package com.skillton.aihandler.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiResponseDto {
    private List<OrderLineResponse> orders; // Список извлеченных деталей, который мы превратим в HTML-таблицу
    private String assumptions; // Вопросы для уточнения к заказчику. Пункт 1: Предположения
    private List<String> questions; // Вопросы для уточнения к заказчику. Пункт 2: Конкретные вопросы
}
