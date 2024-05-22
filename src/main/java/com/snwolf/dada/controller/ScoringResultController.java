package com.snwolf.dada.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snwolf.dada.annotation.CheckRole;
import com.snwolf.dada.domain.dto.*;
import com.snwolf.dada.domain.vo.ScoringResultVO;
import com.snwolf.dada.result.Result;
import com.snwolf.dada.service.IScoringResultService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scoreResult")
@RequiredArgsConstructor
@Api(tags = "评分结果相关接口")
public class ScoringResultController {
    private final IScoringResultService scoringResultService;

    @PostMapping("/add")
    public Result add(@RequestBody ScoringResultAddDTO scoringResultAddDTO){
        scoringResultService.add(scoringResultAddDTO);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result delete(@RequestBody IdDTO idDTO){
        scoringResultService.delete(idDTO.getId());
        return Result.success();
    }

    @PostMapping("/update")
    @CheckRole(role = "admin")
    public Result update(@RequestBody ScoringResultUpdateDTO scoringResultUpdateDTO){
        scoringResultService.updateWithResultProp(scoringResultUpdateDTO);
        return Result.success();
    }

    @PostMapping("/get/vo")
    public Result<ScoringResultVO> getByIdWithUserVO(@RequestBody IdDTO idDTO){
        ScoringResultVO scoringResultVO = scoringResultService.getByIdWithUserVO(idDTO.getId());
        return Result.success(scoringResultVO);
    }

    @PostMapping("/page")
    @CheckRole(role = "admin")
    public Result<Page<ScoringResultVO>> pageQuery(@RequestBody ScoringResultQueryDTO scoringResultQueryDTO){
        Page<ScoringResultVO> pageResult = scoringResultService.pageQuery(scoringResultQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/edit")
    public Result edit(@RequestBody ScoringResultEditDTO scoringResultEditDTO){
        scoringResultService.edit(scoringResultEditDTO);
        return Result.success();
    }
}
