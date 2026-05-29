package com.quickeat.compat;

import com.quickeat.QuickEatMod;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

/**
 * Central mod compatibility handler.
 * Detects installed mods and delegates compatibility checks.
 * 
 * Supported mods:
 * - Pam's HarvestCraft 2
 * - Farmer's Delight
 * - Spice of Life: Carrot Edition
 * - AppleSkin
 * - Diet Mod
 * - Tough As Nails
 * - Serene Seasons
 * - Create
 * - Botania
 * - Tinkers' Construct
 * - Curios API
 * - Sophisticated Backpacks
 * - Iron Chests
 * - Storage Drawers
 * - Applied Energistics 2
 * - Refined Storage
 * - JEI (Just Enough Items)
 * - WAILA/Jade
 * - Mouse Tweaks
 * - Quark
 */
public class ModCompat {

    private static final List<IModCompat> compatModules = new ArrayList<>();
    private static boolean initialized = false;

    // Mod IDs
    public static final String FARMERS_DELIGHT = "farmersdelight";
    public static final String PAMS_HARVESTCRAFT = "pamhc2foodcore";
    public static final String SPICE_OF_LIFE = "solcarrot";
    public static final String APPLESKIN = "appleskin";
    public static final String DIET = "diet";
    public static final String TOUGH_AS_NAILS = "toughasnails";
    public static final String SERENE_SEASONS = "sereneseasons";
    public static final String CREATE = "create";
    public static final String BOTANIA = "botania";
    public static final String TINKERS = "tconstruct";
    public static final String CURIOS = "curios";
    public static final String SOPHISTICATED_BACKPACKS = "sophisticatedbackpacks";
    public static final String IRON_CHESTS = "ironchest";
    public static final String STORAGE_DRAWERS = "storagedrawers";
    public static final String AE2 = "ae2";
    public static final String REFINED_STORAGE = "refinedstorage";
    public static final String JEI = "jei";
    public static final String JADE = "jade";
    public static final String MOUSE_TWEAKS = "mousetweaks";
    public static final String QUARK = "quark";
    public static final String LEGENDARY_SURVIVAL = "legendarysurvivaloverhaul";

    public static void init() {
        if (initialized) return;
        initialized = true;

        // Register compatibility modules for detected mods
        if (isModLoaded(FARMERS_DELIGHT)) {
            QuickEatMod.LOGGER.info("Quick Eat: Farmer's Delight detected - full food support enabled");
            compatModules.add(new FarmersDelightCompat());
        }

        if (isModLoaded(PAMS_HARVESTCRAFT)) {
            QuickEatMod.LOGGER.info("Quick Eat: Pam's HarvestCraft detected - all foods supported");
            compatModules.add(new GenericFoodCompat(PAMS_HARVESTCRAFT));
        }

        if (isModLoaded(SPICE_OF_LIFE)) {
            QuickEatMod.LOGGER.info("Quick Eat: Spice of Life: Carrot Edition detected - diversity tracking compatible");
            compatModules.add(new SpiceOfLifeCompat());
        }

        if (isModLoaded(TOUGH_AS_NAILS)) {
            QuickEatMod.LOGGER.info("Quick Eat: Tough As Nails detected - thirst items supported");
            compatModules.add(new ThirstCompat(TOUGH_AS_NAILS));
        }

        if (isModLoaded(CREATE)) {
            QuickEatMod.LOGGER.info("Quick Eat: Create detected - all Create foods supported");
            compatModules.add(new GenericFoodCompat(CREATE));
        }

        if (isModLoaded(BOTANIA)) {
            QuickEatMod.LOGGER.info("Quick Eat: Botania detected - magical foods supported");
            compatModules.add(new GenericFoodCompat(BOTANIA));
        }

        if (isModLoaded(TINKERS)) {
            QuickEatMod.LOGGER.info("Quick Eat: Tinkers' Construct detected");
            compatModules.add(new GenericFoodCompat(TINKERS));
        }

        if (isModLoaded(DIET)) {
            QuickEatMod.LOGGER.info("Quick Eat: Diet mod detected - diet groups respected");
        }

        if (isModLoaded(APPLESKIN)) {
            QuickEatMod.LOGGER.info("Quick Eat: AppleSkin detected - saturation overlay compatible");
        }

        if (isModLoaded(SERENE_SEASONS)) {
            QuickEatMod.LOGGER.info("Quick Eat: Serene Seasons detected");
        }

        if (isModLoaded(CURIOS)) {
            QuickEatMod.LOGGER.info("Quick Eat: Curios API detected - curio slot eating supported");
            compatModules.add(new CuriosCompat());
        }

        if (isModLoaded(SOPHISTICATED_BACKPACKS)) {
            QuickEatMod.LOGGER.info("Quick Eat: Sophisticated Backpacks detected - backpack eating supported");
        }

        if (isModLoaded(IRON_CHESTS)) {
            QuickEatMod.LOGGER.info("Quick Eat: Iron Chests detected - all chest types supported");
        }

        if (isModLoaded(STORAGE_DRAWERS)) {
            QuickEatMod.LOGGER.info("Quick Eat: Storage Drawers detected");
        }

        if (isModLoaded(AE2)) {
            QuickEatMod.LOGGER.info("Quick Eat: Applied Energistics 2 detected");
        }

        if (isModLoaded(REFINED_STORAGE)) {
            QuickEatMod.LOGGER.info("Quick Eat: Refined Storage detected");
        }

        if (isModLoaded(JEI)) {
            QuickEatMod.LOGGER.info("Quick Eat: JEI detected - recipe integration available");
        }

        if (isModLoaded(JADE)) {
            QuickEatMod.LOGGER.info("Quick Eat: Jade/WAILA detected");
        }

        if (isModLoaded(QUARK)) {
            QuickEatMod.LOGGER.info("Quick Eat: Quark detected - compatible");
        }

        if (isModLoaded(LEGENDARY_SURVIVAL)) {
            QuickEatMod.LOGGER.info("Quick Eat: Legendary Survival Overhaul detected - thirst fully supported");
            compatModules.add(new ThirstCompat(LEGENDARY_SURVIVAL));
        }

        QuickEatMod.LOGGER.info("Quick Eat: {} mod compatibility modules loaded", compatModules.size());
    }

    /**
     * Additional consumable check from compat modules.
     * Called after the base isConsumable check fails.
     */
    public static boolean isConsumableCompat(ItemStack stack) {
        for (IModCompat compat : compatModules) {
            if (compat.isConsumable(stack)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if eating should be prevented by a compat mod.
     */
    public static boolean shouldPreventEating(ItemStack stack) {
        for (IModCompat compat : compatModules) {
            if (compat.shouldPreventEating(stack)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
