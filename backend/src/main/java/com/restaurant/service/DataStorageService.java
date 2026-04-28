package com.restaurant.service;

import com.restaurant.model.*;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DataStorageService {

    private final Map<String, Ingredient> ingredients = new ConcurrentHashMap<>();
    private final Map<String, Dish> dishes = new ConcurrentHashMap<>();
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final Map<String, PurchaseOrder> purchaseOrders = new ConcurrentHashMap<>();
    private final Map<String, PurchaseSuggestion> purchaseSuggestions = new ConcurrentHashMap<>();
    private final Map<String, WasteOrder> wasteOrders = new ConcurrentHashMap<>();
    private final Map<String, DailySettlement> settlements = new ConcurrentHashMap<>();
    private final Map<String, SettlementAdjustment> adjustments = new ConcurrentHashMap<>();

    private final AtomicInteger orderCounter = new AtomicInteger(1);
    private final AtomicInteger purchaseCounter = new AtomicInteger(1);
    private final AtomicInteger wasteCounter = new AtomicInteger(1);
    private final AtomicInteger suggestionCounter = new AtomicInteger(1);
    private final AtomicInteger adjustmentCounter = new AtomicInteger(1);

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");

    @PostConstruct
    public void init() {
        initBaseData();
    }

    private void initBaseData() {
        initIngredients();
        initDishes();
    }

    private void initIngredients() {
        LocalDate tomorrow = LocalDate.now().plusDays(30);
        
        Ingredient noodle = new Ingredient();
        noodle.setId(UUID.randomUUID().toString());
        noodle.setName("面条");
        noodle.setUnit("份");
        noodle.setInventory(new BigDecimal("50"));
        noodle.setLockedInventory(BigDecimal.ZERO);
        noodle.setSafeInventory(new BigDecimal("20"));
        noodle.setExpiryDate(tomorrow);
        noodle.setUnitPrice(new BigDecimal("2.00"));
        noodle.setLastUpdateTime(LocalDate.now());
        ingredients.put(noodle.getId(), noodle);

        Ingredient beef = new Ingredient();
        beef.setId(UUID.randomUUID().toString());
        beef.setName("牛肉");
        beef.setUnit("克");
        beef.setInventory(new BigDecimal("5000"));
        beef.setLockedInventory(BigDecimal.ZERO);
        beef.setSafeInventory(new BigDecimal("2000"));
        beef.setExpiryDate(tomorrow);
        beef.setUnitPrice(new BigDecimal("0.08"));
        beef.setLastUpdateTime(LocalDate.now());
        ingredients.put(beef.getId(), beef);

        Ingredient greens = new Ingredient();
        greens.setId(UUID.randomUUID().toString());
        greens.setName("青菜");
        greens.setUnit("份");
        greens.setInventory(new BigDecimal("40"));
        greens.setLockedInventory(BigDecimal.ZERO);
        greens.setSafeInventory(new BigDecimal("15"));
        greens.setExpiryDate(tomorrow);
        greens.setUnitPrice(new BigDecimal("1.50"));
        greens.setLastUpdateTime(LocalDate.now());
        ingredients.put(greens.getId(), greens);

        Ingredient rice = new Ingredient();
        rice.setId(UUID.randomUUID().toString());
        rice.setName("米饭");
        rice.setUnit("份");
        rice.setInventory(new BigDecimal("60"));
        rice.setLockedInventory(BigDecimal.ZERO);
        rice.setSafeInventory(new BigDecimal("25"));
        rice.setExpiryDate(LocalDate.now().plusDays(1));
        rice.setUnitPrice(new BigDecimal("1.00"));
        rice.setLastUpdateTime(LocalDate.now());
        ingredients.put(rice.getId(), rice);

        Ingredient egg = new Ingredient();
        egg.setId(UUID.randomUUID().toString());
        egg.setName("鸡蛋");
        egg.setUnit("个");
        egg.setInventory(new BigDecimal("100"));
        egg.setLockedInventory(BigDecimal.ZERO);
        egg.setSafeInventory(new BigDecimal("30"));
        egg.setExpiryDate(LocalDate.now().plusDays(15));
        egg.setUnitPrice(new BigDecimal("0.80"));
        egg.setLastUpdateTime(LocalDate.now());
        ingredients.put(egg.getId(), egg);

        Ingredient tomato = new Ingredient();
        tomato.setId(UUID.randomUUID().toString());
        tomato.setName("番茄");
        tomato.setUnit("个");
        tomato.setInventory(new BigDecimal("30"));
        tomato.setLockedInventory(BigDecimal.ZERO);
        tomato.setSafeInventory(new BigDecimal("10"));
        tomato.setExpiryDate(LocalDate.now().plusDays(5));
        tomato.setUnitPrice(new BigDecimal("1.20"));
        tomato.setLastUpdateTime(LocalDate.now());
        ingredients.put(tomato.getId(), tomato);
    }

    private void initDishes() {
        List<Ingredient> ingredientList = new ArrayList<>(ingredients.values());
        
        Ingredient noodle = findIngredientByName("面条");
        Ingredient beef = findIngredientByName("牛肉");
        Ingredient greens = findIngredientByName("青菜");
        Ingredient rice = findIngredientByName("米饭");
        Ingredient egg = findIngredientByName("鸡蛋");
        Ingredient tomato = findIngredientByName("番茄");

        Dish beefNoodle = new Dish();
        beefNoodle.setId(UUID.randomUUID().toString());
        beefNoodle.setName("牛肉面");
        beefNoodle.setPrice(new BigDecimal("25.00"));
        beefNoodle.setStatus(DishStatus.AVAILABLE);
        List<RecipeItem> beefNoodleRecipe = new ArrayList<>();
        beefNoodleRecipe.add(new RecipeItem(noodle.getId(), noodle.getName(), new BigDecimal("1")));
        beefNoodleRecipe.add(new RecipeItem(beef.getId(), beef.getName(), new BigDecimal("100")));
        beefNoodleRecipe.add(new RecipeItem(greens.getId(), greens.getName(), new BigDecimal("1")));
        beefNoodle.setRecipe(beefNoodleRecipe);
        dishes.put(beefNoodle.getId(), beefNoodle);

        Dish eggFriedRice = new Dish();
        eggFriedRice.setId(UUID.randomUUID().toString());
        eggFriedRice.setName("蛋炒饭");
        eggFriedRice.setPrice(new BigDecimal("15.00"));
        eggFriedRice.setStatus(DishStatus.AVAILABLE);
        List<RecipeItem> eggFriedRiceRecipe = new ArrayList<>();
        eggFriedRiceRecipe.add(new RecipeItem(rice.getId(), rice.getName(), new BigDecimal("1")));
        eggFriedRiceRecipe.add(new RecipeItem(egg.getId(), egg.getName(), new BigDecimal("2")));
        eggFriedRice.setRecipe(eggFriedRiceRecipe);
        dishes.put(eggFriedRice.getId(), eggFriedRice);

        Dish tomatoEgg = new Dish();
        tomatoEgg.setId(UUID.randomUUID().toString());
        tomatoEgg.setName("番茄炒蛋");
        tomatoEgg.setPrice(new BigDecimal("18.00"));
        tomatoEgg.setStatus(DishStatus.AVAILABLE);
        List<RecipeItem> tomatoEggRecipe = new ArrayList<>();
        tomatoEggRecipe.add(new RecipeItem(tomato.getId(), tomato.getName(), new BigDecimal("2")));
        tomatoEggRecipe.add(new RecipeItem(egg.getId(), egg.getName(), new BigDecimal("2")));
        tomatoEgg.setRecipe(tomatoEggRecipe);
        dishes.put(tomatoEgg.getId(), tomatoEgg);
    }

    private Ingredient findIngredientByName(String name) {
        return ingredients.values().stream()
                .filter(i -> i.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public String generateOrderNo() {
        String date = LocalDateTime.now().format(dateFormatter);
        int count = orderCounter.getAndIncrement();
        return "ORD" + date + String.format("%06d", count);
    }

    public String generatePurchaseNo() {
        String date = LocalDateTime.now().format(dateFormatter);
        int count = purchaseCounter.getAndIncrement();
        return "PO" + date + String.format("%06d", count);
    }

    public String generateWasteNo() {
        String date = LocalDateTime.now().format(dateFormatter);
        int count = wasteCounter.getAndIncrement();
        return "WO" + date + String.format("%06d", count);
    }

    public String generateSuggestionNo() {
        String date = LocalDateTime.now().format(dateFormatter);
        int count = suggestionCounter.getAndIncrement();
        return "SUG" + date + String.format("%06d", count);
    }

    public String generateAdjustmentNo() {
        String date = LocalDateTime.now().format(dateFormatter);
        int count = adjustmentCounter.getAndIncrement();
        return "ADJ" + date + String.format("%06d", count);
    }

    public Map<String, Ingredient> getIngredients() {
        return ingredients;
    }

    public Map<String, Dish> getDishes() {
        return dishes;
    }

    public Map<String, Order> getOrders() {
        return orders;
    }

    public Map<String, PurchaseOrder> getPurchaseOrders() {
        return purchaseOrders;
    }

    public Map<String, PurchaseSuggestion> getPurchaseSuggestions() {
        return purchaseSuggestions;
    }

    public Map<String, WasteOrder> getWasteOrders() {
        return wasteOrders;
    }

    public Map<String, DailySettlement> getSettlements() {
        return settlements;
    }

    public Map<String, SettlementAdjustment> getAdjustments() {
        return adjustments;
    }
}