package com.sigma.worldskitchenserver.service;

import com.sigma.worldskitchenserver.dto.Dish.DishAddRequest;
import com.sigma.worldskitchenserver.dto.Dish.DishDto;
import com.sigma.worldskitchenserver.dto.User.UserDto;
import com.sigma.worldskitchenserver.enums.ActivityType;
import com.sigma.worldskitchenserver.enums.Region;
import com.sigma.worldskitchenserver.mapper.DishMapper;
import com.sigma.worldskitchenserver.model.Dish;
import com.sigma.worldskitchenserver.model.Ingredient;
import com.sigma.worldskitchenserver.model.User;
import com.sigma.worldskitchenserver.repository.DishRepository;
import com.sigma.worldskitchenserver.repository.IngredientRepository;
import com.sigma.worldskitchenserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DishService {
    private static final Logger logger = LoggerFactory.getLogger(DishService.class);

    private final UserService userService;
    private final DishRepository dishRepository;
    private final DishMapper dishMapper;
    private final UserRepository userRepository;
    private final RecentActivityService recentActivityService;
    private final IngredientRepository ingredientRepository;

    public List<DishDto> mapDishesToDishesDto(List<Dish> dishes) {
        return dishes.stream()
                .map(dishMapper::toDishDto)
                .collect(Collectors.toList());
    }

    public List<DishDto> getCurrentUserDishes() {
        UserDto user = userService.getCurrentUserDto();
        List<Dish> allDishes = dishRepository.findByAuthor_Id(user.getId());

        return mapDishesToDishesDto(allDishes);
    }

    public List<DishDto> getUserLikedDishes() {
        User currentUser = userService.getCurrentUser();
        return mapDishesToDishesDto(currentUser.getLikedDishes());
    }

    public void likeDishById(Long dishId) {
        User user = userService.getCurrentUser();
        Optional<Dish> dish = dishRepository.findById(dishId);
        dish.ifPresent(meal -> {
            user.getLikedDishes().add(meal);
            userRepository.save(user);
            dishRepository.save(meal);
            recentActivityService.addActivity(user, meal, ActivityType.LIKE_MEAL);
            logger.info("User with id: {} liked dish {}", user.getId(), meal.getName());
        });
    }

    public void unlikeDishById(Long dishId) {
        User user = userService.getCurrentUser();
        Optional<Dish> dish = dishRepository.findById(dishId);
        dish.ifPresent(meal -> {
            user.getLikedDishes().remove(meal);
            userRepository.save(user);
            dishRepository.save(meal);
            recentActivityService.addActivity(user, meal, ActivityType.UNLIKE_MEAL);
            logger.info("User with id: {} unliked dish {}", user.getId(), meal.getName());
        });
    }

    public List<DishDto> getDishesByRegion(Region region) {
        return mapDishesToDishesDto(dishRepository.findByRegion(region));
    }

    public Boolean checkIsDishLiked(Long dishId) {
        User currentUser = userService.getCurrentUser();
        return dishRepository.findById(dishId)
                .map(dish -> currentUser.getLikedDishes().contains(dish))
                .orElse(false);
    }

    public DishDto createDish(DishAddRequest dish) {
        Dish newDish = dishMapper.toDish(dish);

        User user = userService.getCurrentUser();
        newDish.setAuthor(user);
        logger.info("User with id: {} created new dish {}", user.getId(), newDish.getName());

        Dish savedDish = dishRepository.save(newDish);

        List<Ingredient> ingredients = dish.getIngredients().stream()
                .map(ingredient -> {
                    Ingredient newIngredient = new Ingredient();
                    newIngredient.setDish(savedDish);
                    newIngredient.setIngredientName(ingredient.getIngredientName());
                    newIngredient.setPortion(ingredient.getPortion());
                    return newIngredient;
                })
                .collect(Collectors.toList());

        ingredientRepository.saveAll(ingredients);
        recentActivityService.addActivity(user, savedDish, ActivityType.ADD_MEAL);

        return dishMapper.toDishDto(savedDish);
    }
}
