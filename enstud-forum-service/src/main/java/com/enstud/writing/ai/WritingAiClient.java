package com.enstud.writing.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

public interface WritingAiClient {
    WritingCorrection correct(WritingRequest request);
}
