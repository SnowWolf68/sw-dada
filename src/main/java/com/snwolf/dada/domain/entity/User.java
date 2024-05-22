package com.snwolf.dada.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户账号
     */
    private String user_account;

    /**
     * 用户密码
     */
    private String user_password;

    /**
     * 开放平台id
     */
    private String union_id;

    /**
     * 公众号openId
     */
    private String mp_open_id;

    /**
     * 用户昵称
     */
    private String user_name;

    /**
     * 用户头像
     */
    private String user_avatar;

    /**
     * 用户简介
     */
    private String user_profile;

    /**
     * 用户角色：user/admin/ban
     */
    private String user_role;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 更新时间
     */
    private LocalDateTime update_time;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer is_delete;
}
