package com.snwolf.dada.scoring.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.snwolf.dada.domain.dto.QuestionContentDTO;
import com.snwolf.dada.domain.entity.App;
import com.snwolf.dada.domain.entity.Question;
import com.snwolf.dada.domain.entity.ScoringResult;
import com.snwolf.dada.domain.entity.UserAnswer;
import com.snwolf.dada.domain.vo.QuestionVO;
import com.snwolf.dada.scoring.ScoringStrategy;
import com.snwolf.dada.scoring.annotation.ScoringStrategyAnno;
import com.snwolf.dada.service.IQuestionService;
import com.snwolf.dada.service.IScoringResultService;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测评类评分
 */
@RequiredArgsConstructor
@ScoringStrategyAnno(appType = 1, scoringStrategy = 0)
public class CustomTestScoringStrategyImpl implements ScoringStrategy {

    private final IQuestionService questionService;

    private final IScoringResultService scoringResultService;

    @Override
    public UserAnswer doScore(List<String> choices, App app) {
        Long appId = app.getId();
        // 1. 根据 id 查询到题目和题目结果信息（按分数降序排序）
        Question question = questionService.lambdaQuery()
                .eq(Question::getAppId, appId)
                .one();

        List<ScoringResult> scoringResultList = scoringResultService.lambdaQuery()
                .eq(ScoringResult::getAppId, appId)
                .orderByDesc(ScoringResult::getResultScoreRange)
                .list();


        QuestionVO questionVO = BeanUtil.copyProperties(question, QuestionVO.class, "questionContent");
        List<QuestionContentDTO> questionContentList = JSONUtil.toList(question.getQuestionContent(), QuestionContentDTO.class);
        questionVO.setQuestionContent(questionContentList);

        // 2. 统计用户每个选择对应的属性个数，如 I = 10 个，E = 5 个
        // 初始化一个Map，用于存储每个选项的计数
        Map<String, Integer> optionCount = new HashMap<>();

        // 遍历题目列表
        for (QuestionContentDTO questionContentDTO : questionContentList) {
            // 遍历答案列表
            for (String answer : choices) {
                // 遍历题目中的选项
                for (QuestionContentDTO.Option option : questionContentDTO.getOptions()) {
                    // 如果答案和选项的key匹配
                    if (option.getKey().equals(answer)) {
                        // 获取选项的result属性
                        String result = option.getResult();

                        // 如果result属性不在optionCount中，初始化为0
                        if (!optionCount.containsKey(result)) {
                            optionCount.put(result, 0);
                        }

                        // 在optionCount中增加计数
                        optionCount.put(result, optionCount.get(result) + 1);
                    }
                }
            }
        }

        // 3. 遍历每种评分结果，计算哪个结果的得分更高
        // 初始化最高分数和最高分数对应的评分结果
        int maxScore = 0;
        ScoringResult maxScoringResult = scoringResultList.get(0);

        // 遍历评分结果列表
        for (ScoringResult scoringResult : scoringResultList) {
            List<String> resultProp = JSONUtil.toList(scoringResult.getResultProp(), String.class);
            // 计算当前评分结果的分数，[I, E] => [10, 5] => 15
            int score = resultProp.stream()
                    .mapToInt(prop -> optionCount.getOrDefault(prop, 0))
                    .sum();

            // 如果分数高于当前最高分数，更新最高分数和最高分数对应的评分结果
            if (score > maxScore) {
                maxScore = score;
                maxScoringResult = scoringResult;
            }
        }

        // 4. 构造返回值，填充答案对象的属性
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswer.setResultId(maxScoringResult.getId());
        userAnswer.setResultName(maxScoringResult.getResultName());
        userAnswer.setResultDesc(maxScoringResult.getResultDesc());
        userAnswer.setResultPicture(maxScoringResult.getResultPicture());
        return userAnswer;
    }
}
