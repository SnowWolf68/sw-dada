package com.snwolf.dada.scoring.executor;

import com.snwolf.dada.domain.entity.App;
import com.snwolf.dada.domain.entity.UserAnswer;
import com.snwolf.dada.exception.AppContentException;
import com.snwolf.dada.scoring.impl.CustomScoreScoringStrategyImpl;
import com.snwolf.dada.scoring.impl.CustomTestScoringStrategyImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Deprecated
@RequiredArgsConstructor
public class ScoringStrategyContext {

    private final CustomScoreScoringStrategyImpl customScoreScoringStrategy;

    private final CustomTestScoringStrategyImpl customTestScoringStrategy;

    /**
     * 评分
     *
     * @param choiceList
     * @param app
     * @return
     * @throws Exception
     */
    public UserAnswer doScore(List<String> choiceList, App app) throws Exception {
        Integer appType = app.getAppType();
        Integer scoringStrategy = app.getScoringStrategy();
        if (appType == null || scoringStrategy == null) {
            throw new AppContentException("应用内容配置有误, 无法找到匹配的策略");
        }
        // 根据不同的应用类别和评分策略，选择对应的策略执行
        switch (appType) {
            case 0:
                switch (scoringStrategy) {
                    case 0:
                        return customScoreScoringStrategy.doScore(choiceList, app);
                    case 1:
                        break;
                }
                break;
            case 1:
                switch (scoringStrategy) {
                    case 0:
                        return customTestScoringStrategy.doScore(choiceList, app);
                    case 1:
                        break;
                }
                break;
        }
        throw new AppContentException("应用内容配置有误, 无法找到匹配的策略");
    }
}
