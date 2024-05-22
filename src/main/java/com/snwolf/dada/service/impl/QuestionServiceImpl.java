package com.snwolf.dada.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snwolf.dada.domain.dto.*;
import com.snwolf.dada.domain.entity.Question;
import com.snwolf.dada.domain.entity.User;
import com.snwolf.dada.domain.vo.QuestionVO;
import com.snwolf.dada.domain.vo.UserVO;
import com.snwolf.dada.exception.QuestionNotAllowedUpdateException;
import com.snwolf.dada.exception.QuestionNotExistException;
import com.snwolf.dada.mapper.QuestionMapper;
import com.snwolf.dada.service.IQuestionService;
import com.snwolf.dada.service.IUserService;
import com.snwolf.dada.utils.SqlUtils;
import com.snwolf.dada.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements IQuestionService {

    private final IUserService userService;

    @Override
    public void add(QuestionAddDTO questionAddDTO) {
        Question question = BeanUtil.copyProperties(questionAddDTO, Question.class);
        List<QuestionContentDTO> questionContent = questionAddDTO.getQuestionContent();
        String jsonStr = JSONUtil.toJsonStr(questionContent);
        question.setQuestionContent(jsonStr);
        question.setUserId(UserHolder.getUser().getId());
        save(question);
    }

    @Override
    public void updateQuestion(QuestionUpdateDTO questionUpdateDTO) {
        Long questionId = questionUpdateDTO.getId();
        List<QuestionContentDTO> questionContent = questionUpdateDTO.getQuestionContent();
        Question oldQuestion = getById(questionId);
        if(oldQuestion == null){
            throw new QuestionNotExistException("问题不存在");
        }
        if(oldQuestion.getUserId() != UserHolder.getUser().getId()){
            throw new QuestionNotAllowedUpdateException("您只能修改您自己创建的问题");
        }
        String jsonStr = JSONUtil.toJsonStr(questionContent);
        Question question = Question.builder()
                .id(questionId)
                .questionContent(jsonStr)
                .build();
        updateById(question);
    }

    @Override
    public QuestionVO getByIdWithUserVO(QuestionQueryDTO questionQueryDTO) {
        Long questionId = questionQueryDTO.getId();
        Question question = getById(questionId);
        if(question == null){
            throw new QuestionNotExistException("问题不存在");
        }
        String jsonStr = question.getQuestionContent();
        List<QuestionContentDTO> questionContent = JSONUtil.toList(jsonStr, QuestionContentDTO.class);
        QuestionVO questionVO = BeanUtil.copyProperties(question, QuestionVO.class, "questionContent");
        questionVO.setQuestionContent(questionContent);
        Long userId = question.getUserId();
        User user = userService.getById(userId);
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        questionVO.setUserVO(userVO);
        return questionVO;
    }

    @Override
    public Page<QuestionVO> pageQuery(QuestionQueryDTO questionQueryDTO) {
        QueryWrapper<Question> queryWrapper = getQueryWrapper(questionQueryDTO);
        Page<Question> page = new Page<>(questionQueryDTO.getCurrent(), questionQueryDTO.getPageSize());
        Page<Question> pageResult = page(page, queryWrapper);
        return getQuestionVOByPage(pageResult);
    }

    private Page<QuestionVO> getQuestionVOByPage(Page<Question> pageResult) {
        Page<QuestionVO> pageVOResult = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        List<QuestionVO> questionVOList = pageResult.getRecords()
                .stream()
                .map(question -> {
                    QuestionVO questionVO = BeanUtil.copyProperties(question, QuestionVO.class, "questionContent", "userVO");
                    String questionContentStr = question.getQuestionContent();
                    List<QuestionContentDTO> questionContent = JSONUtil.toList(questionContentStr, QuestionContentDTO.class);
                    questionVO.setQuestionContent(questionContent);
                    Long userId = question.getUserId();
                    User user = userService.getById(userId);
                    UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
                    questionVO.setUserVO(userVO);
                    return questionVO;
                })
                .collect(Collectors.toList());
        pageVOResult.setRecords(questionVOList);
        return pageVOResult;
    }

    public QueryWrapper<Question> getQueryWrapper(QuestionQueryDTO questionQueryDTO) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryDTO == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = questionQueryDTO.getId();
        String questionContent = questionQueryDTO.getQuestionContent();
        Long appId = questionQueryDTO.getAppId();
        Long userId = questionQueryDTO.getUserId();
        Long notId = questionQueryDTO.getNotId();
        String sortField = questionQueryDTO.getSortField();
        String sortOrder = questionQueryDTO.getSortOrder();

        // 补充需要的查询条件
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(questionContent), "question_content", questionContent);
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appId), "app_id", appId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "user_id", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals("ascend"),
                sortField);
        return queryWrapper;
    }
}
