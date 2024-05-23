package com.snwolf.dada.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snwolf.dada.domain.dto.*;
import com.snwolf.dada.domain.entity.Question;
import com.snwolf.dada.domain.vo.QuestionVO;

import java.util.List;

public interface IQuestionService extends IService<Question> {
    void add(QuestionAddDTO questionAddDTO);

    void updateQuestion(QuestionUpdateDTO questionUpdateDTO);

    QuestionVO getByIdWithUserVO(QuestionQueryDTO questionQueryDTO);

    Page<QuestionVO> pageQuery(QuestionQueryDTO questionQueryDTO);

    List<QuestionContentDTO> aiGenerateQuestion(AiGenerateQuestionDTO aiGenerateQuestionDTO);
}
