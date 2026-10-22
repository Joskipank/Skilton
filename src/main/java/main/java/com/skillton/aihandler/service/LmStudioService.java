package com.skillton.aihandler.service;

import com.skillton.aihandler.dto.response.AiResponseDto;

public interface LmStudioService {

    AiResponseDto processText(String text);
}