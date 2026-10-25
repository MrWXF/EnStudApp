package com.enstud.word.service;

import com.enstud.word.dto.VocabTestAnswerRequest;
import com.enstud.word.dto.VocabTestResultDTO;
import com.enstud.word.dto.VocabTestWordDTO;

import java.util.List;

public interface VocabTestService {

    /**
     * 获取一组测试单词（6 个频段各 5 词 + 2 个防乱猜伪词，乱序）
     */
    List<VocabTestWordDTO> getTestWords();

    /**
     * 提交作答，估算词汇量并保存记录
     */
    VocabTestResultDTO submit(Long userId, VocabTestAnswerRequest request);

    /**
     * 获取用户最近一次测试结果
     */
    VocabTestResultDTO getLatest(Long userId);
}
