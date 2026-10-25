package com.enstud.writing.service;

import com.enstud.writing.dto.*;
import java.util.List;

public interface WritingService {
    CorrectionDTO submitAndCorrect(Long userId, SubmitWritingRequest request);
    CorrectionDTO getCorrection(Long writingId);
    List<WritingDTO> getHistory(Long userId);
    List<ModelEssayDTO> getModelEssays(String topicType);
    void deleteWriting(Long userId, Long writingId);
}
