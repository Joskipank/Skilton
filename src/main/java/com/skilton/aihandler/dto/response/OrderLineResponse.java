package com.skilton.aihandler.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderLineResponse {
    private String name;
    private Integer width; // Требуемая ширина (в мм)
    private Integer height; // Требуемая высота (в мм)
    private Integer length; // Требуемая длина (строго в мм)
    private Integer count; // Количество (шт)
    private String material; // Материал (например, Сосна, Ель, или 'Не указан')
    private String comment; // Комментарий (фаски, сроки, имя клиента, специфика обработки)
}
