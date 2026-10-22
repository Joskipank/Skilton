package com.skilton.aihandler.service;

import com.skilton.aihandler.dto.response.AiResponseDto;

public interface LmStudioService {

    AiResponseDto processText(String text);
}