package com.snwolf.dada.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String user_account;

    /**
     * 密码
     */
    private String user_password;

    /**
     * 用户昵称
     */
    private String user_name;

    /**
     * 用户头像
     */
    private String user_avatar;

    /**
     * 用户角色：user/admin
     */
    private String user_role;

    /**
     * 开放平台id
     */
    private String union_id;

    /**
     * 公众号openId
     */
    private String mp_open_id;

    /**
     * 用户简介
     */
    private String user_profile;

    /**
     * 是否删除
     */
    private Integer is_delete;
}
