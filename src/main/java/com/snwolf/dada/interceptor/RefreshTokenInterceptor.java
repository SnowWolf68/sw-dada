package com.snwolf.dada.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import com.snwolf.dada.constants.RedisConstants;
import com.snwolf.dada.domain.dto.UserDTO;
import com.snwolf.dada.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("authorization");
        if(StrUtil.isBlank(token)){
            return true;
        }
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(RedisConstants.USER_LOGIN_KEY + token);
        if(userMap.isEmpty()){
            return true;
        }
        UserDTO userDTO = BeanUtil.mapToBean(userMap, UserDTO.class, true, CopyOptions.create());
        UserHolder.saveUser(userDTO);
        stringRedisTemplate.expire(RedisConstants.USER_LOGIN_KEY + token, RedisConstants.USER_LOGIN_TTL, TimeUnit.MINUTES);
        return true;
    }

    /**
     * 移除用户, 避免内存泄漏
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
