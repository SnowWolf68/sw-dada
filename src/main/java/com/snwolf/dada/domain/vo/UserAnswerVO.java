package com.snwolf.dada.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswerVO {
    /**
     *
     */
    private Long id;

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 应用类型（0-得分类，1-角色测评类）
     */
    private Integer appType;

    /**
     * 评分策略（0-自定义，1-AI）
     */
    private Integer scoringStrategy;

    /**
     * 用户答案（JSON 数组）
     */
    private List<String> choices;

    /**
     * 评分结果 id
     */
    private Long resultId;

    /**
     * 结果名称，如物流师
     */
    private String resultName;

    /**
     * 结果描述
     */
    private String resultDesc;

    /**
     * 结果图标
     */
    private String resultPicture;

    /**
     * 得分
     */
    private Integer resultScore;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建用户信息
     */
    private UserVO userVO;
}
