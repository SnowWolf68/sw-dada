package com.snwolf.dada.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snwolf.dada.domain.dto.*;
import com.snwolf.dada.domain.vo.QuestionVO;
import com.snwolf.dada.result.Result;
import com.snwolf.dada.service.IQuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/question")
@RequiredArgsConstructor
@Api(tags = "问题相关接口")
public class QuestionController {

    private final IQuestionService questionService;

    @PostMapping("/add")
    @ApiOperation("add")
    public Result add(@RequestBody QuestionAddDTO questionAddDTO){
        questionService.add(questionAddDTO);
        return Result.success();
    }

    @PostMapping("/delete")
    @ApiOperation("delete")
    public Result delete(@RequestBody IdDTO idDTO){
        Long id = idDTO.getId();
        questionService.removeById(id);
        return Result.success();
    }

    @PostMapping("/update")
    @ApiOperation("update")
    public Result updateQuestion(@RequestBody QuestionUpdateDTO questionUpdateDTO){
        questionService.updateQuestion(questionUpdateDTO);
        return Result.success();
    }

    @PostMapping("/get/vo")
    @ApiOperation("getByIdWithUserVO")
    public Result<QuestionVO> getByIdWithUserVO(@RequestBody QuestionQueryDTO questionQueryDTO){
        QuestionVO questionVO = questionService.getByIdWithUserVO(questionQueryDTO);
        return Result.success(questionVO);
    }

    @PostMapping("/page")
    @ApiOperation("page")
    public Result<Page<QuestionVO>> page(@RequestBody QuestionQueryDTO questionQueryDTO){
        Page<QuestionVO> pageResult = questionService.pageQuery(questionQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/generate")
    public Result<List<QuestionContentDTO>> aiGenerateQuestion(@RequestBody AiGenerateQuestionDTO aiGenerateQuestionDTO){
        List<QuestionContentDTO> questionList = questionService.aiGenerateQuestion(aiGenerateQuestionDTO);
        return Result.success(questionList);
    }

}
