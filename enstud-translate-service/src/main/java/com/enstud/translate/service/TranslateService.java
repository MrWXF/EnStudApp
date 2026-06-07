package com.enstud.translate.service;

import com.enstud.translate.dto.TranslateRequest;
import com.enstud.translate.dto.TranslateResponse;

public interface TranslateService {
    TranslateResponse translate(TranslateRequest request);
}
