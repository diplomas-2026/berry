package com.company.product.api.repository;

import com.company.product.api.entity.MealSlot;
import com.company.product.api.entity.MenuItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    @EntityGraph(attributePaths = {"dish"})
    List<MenuItem> findByMenuDateOrderByMealSlotAsc(LocalDate menuDate);

    @EntityGraph(attributePaths = {"dish"})
    List<MenuItem> findByMenuDateAndMealSlot(LocalDate menuDate, MealSlot mealSlot);

    @Query("select distinct m.menuDate from MenuItem m order by m.menuDate desc")
    List<LocalDate> findDistinctMenuDates();
}
