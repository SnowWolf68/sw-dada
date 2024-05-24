package com.snwolf.dada.scoring.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.snwolf.dada.aiService.ZhipuAiServiceImpl;
import com.snwolf.dada.aiService.properties.AiPromptConstants;
import com.snwolf.dada.domain.dto.QuestionAnswerDTO;
import com.snwolf.dada.domain.dto.QuestionContentDTO;
import com.snwolf.dada.domain.entity.App;
import com.snwolf.dada.domain.entity.Question;
import com.snwolf.dada.domain.entity.UserAnswer;
import com.snwolf.dada.scoring.ScoringStrategy;
import com.snwolf.dada.scoring.annotation.ScoringStrategyAnno;
import com.snwolf.dada.service.IQuestionService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 测评类评分
 */
@RequiredArgsConstructor
@ScoringStrategyAnno(appType = 1, scoringStrategy = 1)
public class AiTestScoringStrategyImpl implements ScoringStrategy {

    private final IQuestionService questionService;

    private final ZhipuAiServiceImpl zhipuAiService;

    private final RedissonClient redissonClient;

    private Cache<String, String> answerCache = Caffeine.newBuilder()
            .initialCapacity(1024)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private static final String AI_ANSWER_LOCK_PREFIX = "ai:answer:lock:";

    @Override
    public UserAnswer doScore(List<String> choices, App app) {
        Long appId = app.getId();

        String choiceJson = JSONUtil.toJsonStr(choices);
        String cacheKey = buildCacheKey(appId, choiceJson);
        String answerJson = answerCache.getIfPresent(cacheKey);

        if(StrUtil.isNotBlank(answerJson)){
            // 有缓存
            UserAnswer userAnswer = JSONUtil.toBean(answerJson, UserAnswer.class);
            userAnswer.setAppId(appId);
            userAnswer.setAppType(app.getAppType());
            userAnswer.setScoringStrategy(app.getScoringStrategy());
            userAnswer.setChoices(choiceJson);
            return userAnswer;
        }

        RLock lock = redissonClient.getLock(AI_ANSWER_LOCK_PREFIX + cacheKey);

        try {
            boolean result = lock.tryLock(3, 15, TimeUnit.SECONDS);

            if(!result){
                return null;
            }

            // 1. 根据 id 查询到题目和题目结果信息（按分数降序排序）
            Question question = questionService.lambdaQuery()
                    .eq(Question::getAppId, appId)
                    .one();

            String questionContentStr = question.getQuestionContent();
            List<QuestionContentDTO> questionContentList = JSONUtil.toList(questionContentStr, QuestionContentDTO.class);

            String userMessage = getAiTestScoringUserMessage(app, questionContentList, choices);

            String aiRespJson = zhipuAiService.doRequestSyncUnStable(AiPromptConstants.GENERATE_RESULT_SYSTEM_PROMPT, userMessage);

            // 截取需要的 JSON 信息
            int start = aiRespJson.indexOf("{");
            int end = aiRespJson.lastIndexOf("}");
            String json = aiRespJson.substring(start, end + 1);

            // 添加缓存
            answerCache.put(cacheKey, json);

            // 3. 构造返回值，填充答案对象的属性
            UserAnswer userAnswer = JSONUtil.toBean(json, UserAnswer.class);
            userAnswer.setAppId(appId);
            userAnswer.setAppType(app.getAppType());
            userAnswer.setScoringStrategy(app.getScoringStrategy());
            userAnswer.setChoices(choiceJson);
            return userAnswer;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if(lock != null && lock.isLocked()){
                if(lock.isHeldByCurrentThread()){
                    lock.unlock();
                }
            }
        }
    }

    /**
     * AI 评分用户消息封装
     *
     * @param app
     * @param questionContentDTOList
     * @param choices
     * @return
     */
    private String getAiTestScoringUserMessage(App app, List<QuestionContentDTO> questionContentDTOList, List<String> choices) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append(app.getAppName()).append("\n");
        userMessage.append(app.getAppDesc()).append("\n");
        List<QuestionAnswerDTO> questionAnswerDTOList = new ArrayList<>();
        for (int i = 0; i < questionContentDTOList.size(); i++) {
            QuestionAnswerDTO questionAnswerDTO = new QuestionAnswerDTO();
            questionAnswerDTO.setTitle(questionContentDTOList.get(i).getTitle());
            questionAnswerDTO.setUserAnswer(choices.get(i));
            questionAnswerDTOList.add(questionAnswerDTO);
        }
        userMessage.append(JSONUtil.toJsonStr(questionAnswerDTOList));
        return userMessage.toString();
    }

    /**
     * 构建缓存key
     * @param appId
     * @param choices
     * @return
     */
    private String buildCacheKey(Long appId, String choices){
        return appId + ":" + DigestUtils.md5DigestAsHex(choices.getBytes());
    }
}
