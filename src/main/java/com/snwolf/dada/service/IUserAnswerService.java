package com.snwolf.dada.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snwolf.dada.domain.dto.UserAnswerAddDTO;
import com.snwolf.dada.domain.dto.UserAnswerEditDTO;
import com.snwolf.dada.domain.dto.UserAnswerQueryDTO;
import com.snwolf.dada.domain.dto.UserAnswerUpdateDTO;
import com.snwolf.dada.domain.entity.UserAnswer;
import com.snwolf.dada.domain.vo.UserAnswerVO;

public interface IUserAnswerService extends IService<UserAnswer> {
    void add(UserAnswerAddDTO userAnswerAddDTO);

    void delete(Long id);

    void updateWithChoices(UserAnswerUpdateDTO userAnswerUpdateDTO);

    UserAnswerVO getUserAnswerByIdWithUserVO(Long id);

    Page<UserAnswerVO> pageQuery(UserAnswerQueryDTO userAnswerQueryDTO);

    void edit(UserAnswerEditDTO userAnswerEditDTO);
}
