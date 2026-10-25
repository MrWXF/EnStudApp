package com.enstud.translate.service.impl;

import com.enstud.translate.ai.TranslateClient;
import com.enstud.translate.dto.TranslateRequest;
import com.enstud.translate.dto.TranslateResponse;
import com.enstud.translate.service.TranslateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TranslateServiceImpl implements TranslateService {

    private static final Logger log = LoggerFactory.getLogger(TranslateServiceImpl.class);
    private final TranslateClient translateClient;

    public TranslateServiceImpl(TranslateClient translateClient) {
        this.translateClient = translateClient;
    }

    @Override
    public TranslateResponse translate(TranslateRequest request) {
        String result = translateClient.translate(request.text(), request.from(), request.to());
        log.info("Translation: {} -> {} ({}->{})", request.text(), result, request.from(), request.to());
        return new TranslateResponse(request.text(), result,
                request.from() != null ? request.from() : "auto",
                request.to());
    }
}
