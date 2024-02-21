package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 口味持久层
 */

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入口味和菜品关系表数据
     * @param flavors
     */
    public void insert(List<DishFlavor> flavors);
}
