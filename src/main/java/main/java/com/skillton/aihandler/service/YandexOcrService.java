package com.skillton.aihandler.service;

import org.springframework.web.multipart.MultipartFile;

public interface YandexOcrService {
    String recognizeText(MultipartFile image);
}