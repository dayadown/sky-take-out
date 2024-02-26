package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.Utilities;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜品业务层
 */
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品和口味
     * @param dishDTO
     */
    @Override
    @Transactional // 保证该操作的原子性
    public void saveWithFlavor(DishDTO dishDTO) {

        // 新增菜品
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);

        Long dishID=dish.getId();

        // 新增口味菜品的关系
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null && !flavors.isEmpty()){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishID);
            });
            dishFlavorMapper.insert(flavors);
        }



    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void delete(List<Long> ids) {

        // 在售卖中的菜品不能删除
        for(Long id:ids){
            Dish dish=dishMapper.getById(id);
            if(dish.getStatus()== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 与套餐有关联的菜品不能删除
        List<Long> setMealIds=setmealDishMapper.getByMealId(ids);
        if(!setMealIds.isEmpty()){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        for(Long id:ids){
            // TODO 优化，直接批量删除
            //删除菜品
            dishMapper.delete(id);
            //删除菜品口味表中的相关菜品
            dishFlavorMapper.delete(id);
        }

    }

    /**
     * 根据id查询菜品和口味
     * @param id
     * @return
     */
    @Override
    public DishVO getByIDWithFlavor(Long id) {

        // 查菜品
        Dish dish = dishMapper.getById(id);

        //查口味
        List<DishFlavor> dishFlavors=dishFlavorMapper.getByDishId(id);

        DishVO dishVO = new DishVO();

        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Override
    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);

        //删除原有的口味数据
        dishFlavorMapper.delete(dishDTO.getId());

        //口味菜品表中重新插入口味数据
        List<DishFlavor> dishFlavors=dishDTO.getFlavors();
        if(dishFlavors!=null && !dishFlavors.isEmpty()){
            dishFlavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insert(dishFlavors);
        }

    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
