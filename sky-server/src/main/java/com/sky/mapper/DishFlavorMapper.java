package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

    /**
     * 根据菜品id删除数据
     * @param id
     */
    @Delete("delete from setmeal_dish where dish_id=#{id}")
    void delete(Long id);

    /**
     * 根据菜品id查询口味数据
     * @param id
     * @return
     */
    @Select("select * from dish_flavor where dish_id=#{id}")
    List<DishFlavor> getByDishId(Long id);
}
