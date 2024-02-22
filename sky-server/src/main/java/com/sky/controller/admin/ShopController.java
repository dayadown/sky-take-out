package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "商店相关接口")
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 设置商店状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置商店状态")
    public Result setStatus(@PathVariable Integer status){
        redisTemplate.opsForValue().set("shop_status",status);
        return Result.success();
    }

    /**
     * 查询商店状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("查询商店状态")
    public Result<Integer> getStatus(){
        Integer status=(Integer) redisTemplate.opsForValue().get("shop_status");
        return Result.success(status);
    }

}
