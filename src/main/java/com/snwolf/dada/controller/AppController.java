package com.snwolf.dada.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snwolf.dada.annotation.CheckRole;
import com.snwolf.dada.domain.dto.*;
import com.snwolf.dada.domain.entity.App;
import com.snwolf.dada.domain.vo.AppVO;
import com.snwolf.dada.result.Result;
import com.snwolf.dada.service.IAppService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
@Api(tags = "应用相关接口")
public class AppController {

    private final IAppService appService;

    @PostMapping("/add")
    @ApiOperation("add")
    public Result<Long> add(@RequestBody AppAddDTO appAddDTO){
        Long appId = appService.add(appAddDTO);
        return Result.success(appId);
    }

    @PostMapping("/delete")
    @ApiOperation("delete")
    public Result delete(@RequestBody IdDTO idDTO){
        appService.delete(idDTO.getId());
        return Result.success();
    }

    @PostMapping("/update")
    @CheckRole(role = "admin")
    @ApiOperation("update")
    public Result update(@RequestBody AppUpdateDTO appUpdateDTO){
        App app = BeanUtil.copyProperties(appUpdateDTO, App.class);
        appService.updateById(app);
        return Result.success();
    }

    @PostMapping("/get/vo")
    @ApiOperation("getByIdWithUserVO")
    public Result<AppVO> getByIdWithUserVO(Long id){
        AppVO appVO = appService.getByIdWithUserVO(id);
        return Result.success(appVO);
    }

    @PostMapping("/page")
    @CheckRole(role = "admin")
    @ApiOperation("page")
    public Result<Page<App>> page(@RequestBody AppQueryDTO appQueryDTO){
        Page<App> pageResult = appService.pageQuery(appQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/review")
    @CheckRole(role = "admin")
    @ApiOperation("review")
    public Result review(@RequestBody ReviewDTO reviewDTO){
        appService.review(reviewDTO);
        return Result.success();
    }
}
