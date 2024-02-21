package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 套餐菜品表的持久层
 */

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询该表数据
     * @param ids
     * @return
     */
    public List<Long> getByMealId(List<Long> ids);
}
