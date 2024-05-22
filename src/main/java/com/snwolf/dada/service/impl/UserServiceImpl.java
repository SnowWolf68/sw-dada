package com.snwolf.dada.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snwolf.dada.constants.RedisConstants;
import com.snwolf.dada.domain.dto.UserDTO;
import com.snwolf.dada.domain.dto.UserLoginDTO;
import com.snwolf.dada.domain.dto.UserRegisterDTO;
import com.snwolf.dada.domain.entity.User;
import com.snwolf.dada.exception.AccountAlreadyExistException;
import com.snwolf.dada.exception.AccountOrPasswordException;
import com.snwolf.dada.exception.CheckPasswordException;
import com.snwolf.dada.mapper.UserMapper;
import com.snwolf.dada.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void register(UserRegisterDTO userRegisterDTO) throws CheckPasswordException {
        String user_password = userRegisterDTO.getuser_password();
        String checkPassword = userRegisterDTO.getCheckPassword();
        String user_account = userRegisterDTO.getuser_account();
        if(!checkPassword.equals(user_password)){
            throw new CheckPasswordException("两次密码输入不一致");
        }
        User oldUser = lambdaQuery()
                .eq(User::getuser_account, user_account)
                .one();
        if(oldUser != null){
            throw new AccountAlreadyExistException("账号已存在");
        }
        User user = BeanUtil.copyProperties(userRegisterDTO, User.class);
        save(user);
    }

    @Override
    public String login(UserLoginDTO userLoginDTO) {
        String user_account = userLoginDTO.getuser_account();
        String user_password = userLoginDTO.getuser_password();
        User user = lambdaQuery()
                .eq(User::getuser_account, user_account)
                .eq(User::getuser_password, user_password)
                .one();
        if(user == null){
            throw new AccountOrPasswordException("账号或密码错误");
        }
        String token = UUID.randomUUID().toString(true);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue == null ? null : fieldValue.toString()));
        stringRedisTemplate.opsForHash().putAll(RedisConstants.USER_LOGIN_KEY + token, userMap);
        stringRedisTemplate.expire(RedisConstants.USER_LOGIN_KEY + token, RedisConstants.USER_LOGIN_TTL, TimeUnit.MINUTES);
        return token;
    }
}
