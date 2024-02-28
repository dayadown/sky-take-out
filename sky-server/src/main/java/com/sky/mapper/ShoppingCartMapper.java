package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 通过不同的动态组合条件查询
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> getBy2Ids(ShoppingCart shoppingCart);

    /**
     * 只更新某一条数据的数量值
     * @param shoppingCart
     */
    @Update("update shopping_cart set number=#{number} where id=#{id}")
    void updateNumber(ShoppingCart shoppingCart);

    /**
     * 插入购物车数据
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
            "VALUE (#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据用户id清空购物车
     * @param id
     */
    @Delete("delete from shopping_cart where user_id=#{id}")
    void deleteByUserId(Long id);

    @Select("select * from shopping_cart where user_id=#{id}")
    List<ShoppingCart> getById(Long id);

    /**
     * 根据用户id减少购物车商品
     * @param shoppingCartDTO
     * @param userId
     */
    void sub(ShoppingCartDTO shoppingCartDTO, Long userId);

    /**
     * 根据id删除购物车数据
     * @param id
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);

    /**
     * 批量插入购物车数据
     *
     * @param shoppingCartList
     */
    void insertBatch(List<ShoppingCart> shoppingCartList);
}
