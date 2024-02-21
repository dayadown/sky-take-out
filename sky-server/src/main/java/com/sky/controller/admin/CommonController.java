package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
public class CommonController {

    //TODO AliOssUtil没有加@component注解为什么也可以注入

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传接口
     * @return
     */
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file){
        try {
            String o_name=file.getOriginalFilename();
            String extension=o_name.substring(o_name.lastIndexOf("."));
            String new_name= UUID.randomUUID().toString()+extension;

            String filepath= aliOssUtil.upload(file.getBytes(), new_name);
            return Result.success(filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
