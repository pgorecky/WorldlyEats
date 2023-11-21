package com.sigma.worldskitchenserver.mapper;

import com.sigma.worldskitchenserver.dto.DishDto;
import com.sigma.worldskitchenserver.entity.Dish;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DishMapper {

    DishDto toDishDto(Dish dish);
}