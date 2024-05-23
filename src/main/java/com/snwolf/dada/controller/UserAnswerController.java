package com.snwolf.dada.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snwolf.dada.annotation.CheckRole;
import com.snwolf.dada.domain.dto.*;
import com.snwolf.dada.domain.vo.UserAnswerVO;
import com.snwolf.dada.result.Result;
import com.snwolf.dada.service.IUserAnswerService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/userAnswer")
@RequiredArgsConstructor
@Api(tags = "用户答案相关接口")
public class UserAnswerController {

    private final IUserAnswerService userAnswerService;

    @PostMapping("/add")
    public Result add(@RequestBody UserAnswerAddDTO userAnswerAddDTO) throws Exception {
        userAnswerService.add(userAnswerAddDTO);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result delete(@RequestBody IdDTO idDTO){
        userAnswerService.delete(idDTO.getId());
        return Result.success();
    }

    @PostMapping("/update")
    public Result update(@RequestBody UserAnswerUpdateDTO userAnswerUpdateDTO){
        userAnswerService.updateWithChoices(userAnswerUpdateDTO);
        return Result.success();
    }

    @PostMapping("/get/vo")
    @CheckRole(role = "admin")
    public Result<UserAnswerVO> getUserAnswerByIdWithUserVO(@RequestBody IdDTO idDTO){
        UserAnswerVO userAnswerVO = userAnswerService.getUserAnswerByIdWithUserVO(idDTO.getId());
        return Result.success(userAnswerVO);
    }

    @PostMapping("/page")
    public Result<Page<UserAnswerVO>> pageQuery(@RequestBody UserAnswerQueryDTO userAnswerQueryDTO){
        Page<UserAnswerVO> pageResult = userAnswerService.pageQuery(userAnswerQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/edit")
    public Result edit(@RequestBody UserAnswerEditDTO userAnswerEditDTO){
        userAnswerService.edit(userAnswerEditDTO);
        return Result.success();
    }
}
