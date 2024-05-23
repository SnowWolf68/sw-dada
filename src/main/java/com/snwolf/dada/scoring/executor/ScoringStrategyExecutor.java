package com.snwolf.dada.scoring.executor;

import com.snwolf.dada.domain.entity.App;
import com.snwolf.dada.domain.entity.UserAnswer;
import com.snwolf.dada.exception.AppContentException;
import com.snwolf.dada.scoring.ScoringStrategy;
import com.snwolf.dada.scoring.annotation.ScoringStrategyAnno;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 评分策略执行器
 */
@Service
public class ScoringStrategyExecutor {

    // 策略列表
    @Resource
    private List<ScoringStrategy> scoringStrategyList;


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
        Integer appScoringStrategy = app.getScoringStrategy();
        if (appType == null || appScoringStrategy == null) {
            throw new AppContentException("应用内容配置有误, 无法找到匹配的策略");
        }
        // 根据注解获取策略
        for (ScoringStrategy strategy : scoringStrategyList) {
            if (strategy.getClass().isAnnotationPresent(ScoringStrategyAnno.class)) {
                ScoringStrategyAnno scoringStrategyAnno = strategy.getClass().getAnnotation(ScoringStrategyAnno.class);
                if (scoringStrategyAnno.appType() == appType && scoringStrategyAnno.scoringStrategy() == appScoringStrategy) {
                    return strategy.doScore(choiceList, app);
                }
            }
        }
        throw new AppContentException("应用内容配置有误, 无法找到匹配的策略");
    }
}
