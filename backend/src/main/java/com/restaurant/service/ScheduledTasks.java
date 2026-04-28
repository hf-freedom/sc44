package com.restaurant.service;

import com.restaurant.model.Ingredient;
import com.restaurant.model.PurchaseSuggestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private IngredientService ingredientService;

    @Autowired
    private WasteService wasteService;

    @Autowired
    private PurchaseService purchaseService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void checkExpiredIngredients() {
        logger.info("开始执行过期原材料检测任务");
        try {
            List<Ingredient> expiredIngredients = ingredientService.getExpiredIngredients();
            if (!expiredIngredients.isEmpty()) {
                logger.info("检测到 {} 种过期原材料，生成损耗单", expiredIngredients.size());
                wasteService.createExpiredWasteOrder();
                logger.info("过期原材料损耗单生成完成");
            } else {
                logger.info("未检测到过期原材料");
            }
        } catch (Exception e) {
            logger.error("过期原材料检测任务执行失败", e);
        }
    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void checkLowInventory() {
        logger.info("开始执行库存预警检测任务");
        try {
            List<Ingredient> lowInventoryIngredients = ingredientService.getLowInventoryIngredients();
            if (!lowInventoryIngredients.isEmpty()) {
                logger.info("检测到 {} 种原材料库存低于安全库存，生成采购建议", lowInventoryIngredients.size());
                for (Ingredient ingredient : lowInventoryIngredients) {
                    boolean hasUnconvertedSuggestion = false;
                    for (PurchaseSuggestion suggestion : purchaseService.getAllPurchaseSuggestions()) {
                        if (suggestion.getIngredientId().equals(ingredient.getId()) && !suggestion.isConverted()) {
                            hasUnconvertedSuggestion = true;
                            break;
                        }
                    }
                    if (!hasUnconvertedSuggestion) {
                        purchaseService.createPurchaseSuggestion(ingredient);
                        logger.info("为原材料 {} 生成采购建议", ingredient.getName());
                    }
                }
                logger.info("采购建议生成完成");
            } else {
                logger.info("所有原材料库存充足");
            }
        } catch (Exception e) {
            logger.error("库存预警检测任务执行失败", e);
        }
    }
}