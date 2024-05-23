package com.snwolf.dada.controller;

import com.snwolf.dada.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/file")
@Api(tags = "文件相关接口")
@Slf4j
public class FileController {

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file){
        if (!file.isEmpty()) {
            try {
                // 文件存放服务端的位置
                String rootPath = "./files";
                File dir = new File(rootPath + File.separator + "tmpFiles");
                if (!dir.exists())
                    dir.mkdirs();
                // 写文件到服务器
                File serverFile = new File(dir.getAbsolutePath() + File.separator + file.getOriginalFilename());
                file.transferTo(serverFile);
                log.info(serverFile.getAbsolutePath());
                return Result.success("You successfully uploaded file=" +  file.getOriginalFilename());
            } catch (Exception e) {
                return Result.error("You failed to upload " +  file.getOriginalFilename() + " => " + e.getMessage());
            }
        } else {
            return Result.error("You failed to upload " +  file.getOriginalFilename() + " because the file was empty.");
        }
    }
}
