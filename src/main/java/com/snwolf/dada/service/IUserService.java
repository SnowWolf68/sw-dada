package com.snwolf.dada.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.snwolf.dada.domain.dto.UserLoginDTO;
import com.snwolf.dada.domain.dto.UserRegisterDTO;
import com.snwolf.dada.domain.entity.User;
import com.snwolf.dada.exception.CheckPasswordException;

public interface IUserService extends IService<User> {
    void register(UserRegisterDTO userRegisterDTO) throws CheckPasswordException;

    String login(UserLoginDTO userLoginDTO);
}
