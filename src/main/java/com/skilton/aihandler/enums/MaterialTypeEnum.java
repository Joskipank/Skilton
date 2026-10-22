package com.skilton.aihandler.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MaterialTypeEnum {
    PINE("Сосна"),
    SPRUCE("Ель"),
    LARCH("Лиственница"),
    NOT_SPECIFIED("Не указан");

    private final String russianName;

    MaterialTypeEnum (String russianName) {
        this.russianName = russianName;
    }

    @JsonValue // Указывает Jackson, что в JSON нужно отдавать именно русский текст
    public String getRussianName() {
        return russianName;
    }

    // Умный метод для сопоставления текста от ИИ с элементами Enum.
    // Защищает от багов регистра букв, окончаний и пробелов.
    @JsonCreator
    public static MaterialTypeEnum  fromString(String value) {
        if (value == null || value.isBlank()) {
            return NOT_SPECIFIED;
        }

        String cleanValue = value.trim().toLowerCase();

        if (cleanValue.contains("сосн")) return PINE;
        if (cleanValue.contains("ел") || cleanValue.contains("ёлко")) return SPRUCE;
        if (cleanValue.contains("лист")) return LARCH;

        return NOT_SPECIFIED; // Если ИИ написал "Не указан" или что-то неизвестное
    }

}
