package com.snwolf.dada.aspect;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.snwolf.dada.annotation.CheckRole;
import com.snwolf.dada.domain.dto.UserDTO;
import com.snwolf.dada.result.Result;
import com.snwolf.dada.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RoleCheckAspect {

    // 切点表达式抽取
    @Pointcut("execution(* com.snwolf.dada.*.*.*(..)) && @annotation(com.snwolf.dada.annotation.CheckRole)")
    public void checkRolePointCut(){}

    @Around("checkRolePointCut()")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.info("当前切点为: {}", signature.getMethod().toString());
        CheckRole checkRole = signature.getMethod().getAnnotation(CheckRole.class);
        String[] roleArr = checkRole.role();
        // 对role进行非空过滤
        List<String> roleList = Arrays.stream(roleArr).filter(StrUtil::isNotBlank).collect(Collectors.toList());
        log.info("checkRole: {}", roleList);
        if(CollectionUtil.isEmpty(roleList)){
            return joinPoint.proceed();
        }
        UserDTO userDTO = UserHolder.getUser();
        Long userId = userDTO.getId();
        String userRole = userDTO.getUserRole();
        boolean isMatch = false;
        for (String role : roleList) {
            if(role.equals(userRole)){
                isMatch = true;
                break;
            }
        }
        if(!isMatch){
            log.info("用户: {} 无权限访问", userId);
            return Result.error("用户无权限访问");
        }else {
            return joinPoint.proceed();
        }
    }
}
