package com.sigma.worldskitchenserver.service;

import com.sigma.worldskitchenserver.dto.Dish.DishAddRequest;
import com.sigma.worldskitchenserver.dto.Dish.DishDto;
import com.sigma.worldskitchenserver.dto.User.UserDto;
import com.sigma.worldskitchenserver.enums.ActivityType;
import com.sigma.worldskitchenserver.enums.Level;
import com.sigma.worldskitchenserver.enums.Region;
import com.sigma.worldskitchenserver.exception.ResourceNotFoundException;
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

    public Dish getDishById(Long dishId) {
        Optional<Dish> dish = dishRepository.findById(dishId);
        return dish.orElseThrow(() -> new ResourceNotFoundException("Dish", "id", dishId));
    }

    public void likeDishById(Long dishId) {
        User user = userService.getCurrentUser();
        Dish dish = getDishById(dishId);

        user.getLikedDishes().add(dish);
        userRepository.save(user);
        dishRepository.save(dish);
        recentActivityService.addActivity(user, dish, ActivityType.LIKE_MEAL);
        logger.info("User with id: {} liked dish {}", user.getId(), dish.getId());
    }

    public void unlikeDishById(Long dishId) {
        User user = userService.getCurrentUser();
        Dish dish = getDishById(dishId);

        user.getLikedDishes().remove(dish);
        userRepository.save(user);
        dishRepository.save(dish);
        recentActivityService.addActivity(user, dish, ActivityType.UNLIKE_MEAL);
        logger.info("User with id: {} unliked dish {}", user.getId(), dish.getId());
    }

    public List<DishDto> getDishesByRegion(Region region) {
        return mapDishesToDishesDto(dishRepository.findByRegion(region));
    }

    public List<DishDto> getAllDishes(Region region, Level level) {
        return mapDishesToDishesDto(dishRepository.findAll())
                .stream()
                .filter(meal -> (region == null || meal.getRegion() == region))
                .filter(meal -> (level == null || meal.getLevel() == level))
                .collect(Collectors.toList());
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

        logger.info("User with id: {} created new dish {}", user.getId(), newDish.getId());

        return dishMapper.toDishDto(savedDish);
    }
}
