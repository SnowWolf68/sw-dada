package com.snwolf.dada.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snwolf.dada.aiService.ZhipuAiServiceImpl;
import com.snwolf.dada.aiService.properties.AiPromptConstants;
import com.snwolf.dada.domain.dto.*;
import com.snwolf.dada.domain.entity.App;
import com.snwolf.dada.domain.entity.Question;
import com.snwolf.dada.domain.entity.User;
import com.snwolf.dada.domain.vo.QuestionVO;
import com.snwolf.dada.domain.vo.UserVO;
import com.snwolf.dada.exception.AppNotExistException;
import com.snwolf.dada.exception.QuestionNotAllowedUpdateException;
import com.snwolf.dada.exception.QuestionNotExistException;
import com.snwolf.dada.mapper.QuestionMapper;
import com.snwolf.dada.service.IAppService;
import com.snwolf.dada.service.IQuestionService;
import com.snwolf.dada.service.IUserService;
import com.snwolf.dada.utils.SqlUtils;
import com.snwolf.dada.utils.UserHolder;
import com.zhipu.oapi.service.v4.model.ModelData;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements IQuestionService {

    private final IUserService userService;

    private final IAppService appService;

    private final ZhipuAiServiceImpl zhipuAiService;

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


    @Override
    public List<QuestionContentDTO> aiGenerateQuestion(AiGenerateQuestionDTO aiGenerateQuestionDTO) {
        Long appId = aiGenerateQuestionDTO.getAppId();
        int questionNumber = aiGenerateQuestionDTO.getQuestionNumber();
        int optionNumber = aiGenerateQuestionDTO.getOptionNumber();
        App app = appService.getById(appId);
        if(app == null){
            throw new AppNotExistException("应用不存在");
        }
        String userMessage = getGenerateQuestionUserMessage(app, questionNumber, optionNumber);
        String aiRespJson = zhipuAiService.doRequestSyncUnStable(AiPromptConstants.GENERATE_QUESTION_SYSTEM_PROMPT, userMessage);
        // 截取需要的 JSON 信息
        int start = aiRespJson.indexOf("[");
        int end = aiRespJson.lastIndexOf("]");
        String json = aiRespJson.substring(start, end + 1);
        return JSONUtil.toList(json, QuestionContentDTO.class);
    }

    @Override
    public SseEmitter aiGenerateQuestionWithSSE(AiGenerateQuestionDTO aiGenerateQuestionDTO) {
        Long appId = aiGenerateQuestionDTO.getAppId();
        int questionNumber = aiGenerateQuestionDTO.getQuestionNumber();
        int optionNumber = aiGenerateQuestionDTO.getOptionNumber();
        App app = appService.getById(appId);
        if(app == null){
            throw new AppNotExistException("应用不存在");
        }
        String userMessage = getGenerateQuestionUserMessage(app, questionNumber, optionNumber);
        // 建立SSE连接对象
        SseEmitter sseEmitter = new SseEmitter(0L);
        // todo: 可以把temperature再封装一下
        Flowable<ModelData> flowable = zhipuAiService.doStreamRequest(AiPromptConstants.GENERATE_QUESTION_SYSTEM_PROMPT, userMessage, 0.8f);
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder sb = new StringBuilder();
        flowable.map(modelData -> modelData.getChoices().get(0).getDelta().getContent())
                .map(str -> str.replaceAll("\\s", ""))
                .filter(StrUtil::isNotBlank)
                .flatMap(str -> Flowable.fromIterable(
                        str.chars()
                                .mapToObj(c -> (char) c)
                                .collect(Collectors.toList())
                ))
                .doOnNext(c -> {
                    if(c == '{'){
                        count.incrementAndGet();
                    }
                    if(count.get() > 0){
                        sb.append(c);
                    }
                    if(c == '}'){
                        count.decrementAndGet();
                        if(count.get() == 0){
                            String jsonStr = sb.toString();
                            log.info("jsonStr:{}", jsonStr);
                            sseEmitter.send(jsonStr);
                            sb.setLength(0);
                        }
                    }
                })
                .doOnError(e -> log.error("AI请求失败, e:{}", e))
                .doOnComplete(() -> sseEmitter.complete())
                .subscribe();
        return sseEmitter;
    }

    private String getGenerateQuestionUserMessage(App app, int questionNumber, int optionNumber) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append(app.getAppName()).append("\n");
        userMessage.append(app.getAppDesc()).append("\n");
        userMessage.append(getAppType(app.getAppType()) + "类").append("\n");
        userMessage.append(questionNumber).append("\n");
        userMessage.append(optionNumber);
        return userMessage.toString();
    }












    private String getAppType(int appType){
        return appType == 0 ? "得分" : "测评";
    }
}
