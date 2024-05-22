package com.snwolf.dada.domain.dto;

import lombok.Data;

/**
 * 审核请求
 */
@Data
public class ReviewDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 状态：0-待审核, 1-通过, 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;
}