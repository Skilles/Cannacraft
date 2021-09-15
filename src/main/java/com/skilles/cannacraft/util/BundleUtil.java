package com.skilles.cannacraft.util;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.List;

public class BundleUtil {

    public static String getAmountString(double grams) {
        int ounces = (int) (grams / 28);
        if (ounces != 0) {
            if (grams > 0 && grams % 28 != 0) return ounces + " oz " + new DecimalFormat("0.#").format(grams % 28) + " g";
            return ounces + " oz";
        } else if (grams != 0) {
            return new DecimalFormat("0.#").format(grams % 28) + " g";
        }
        return "null g";
    }

    public static double weigh(ItemStack stack) {
        return stack.getCount() * 3.5;
    }

    public static float getTexture(ItemStack stack) {
        return ((int)(stack.getCount() - 1)/11)/3.0F;
    }

    public static void appendBundleTooltip(List<Text> tooltip, int count) {
        tooltip.add(new LiteralText("Amount: ").formatted(Formatting.GRAY).append(new LiteralText(getAmountString(count*3.5)).formatted(Formatting.GREEN)));
    }

    public static String getName(ItemStack stack) { // rounded down
        double grams = weigh(stack);
        String name = " of ";
        if (grams == 1.0 ) {
            name = StringUtils.prependIfMissingIgnoreCase(name, "Gram");
        } else if (grams < 7.0) {
            name = StringUtils.prependIfMissingIgnoreCase(name, "Eighth");
        } else if (grams < 14.5) {
            name = StringUtils.prependIfMissingIgnoreCase(name, "Quarter");
        } else if (grams < 28.0) {
            name = StringUtils.prependIfMissingIgnoreCase(name, "Half");
        }  else if (grams < 56.0) {
            name = StringUtils.prependIfMissingIgnoreCase(name, "Ounce");
        } else if (grams < 112.0) {
            name = StringUtils.prependIfMissingIgnoreCase(name, "2 Ounces");
        } else if (grams < 448.0) {
            name = StringUtils.prependIfMissingIgnoreCase(name, "Quarter Pound");
        } else {
            name = StringUtils.prependIfMissingIgnoreCase(name, "Pound");
        }
        return name;
    }

    /**
     * Converts from TriState to int for NBT tags
     * @param status of the bundle
     * @return 0 if DRY, 0.5 if GROUND, 1 if WET
     */
    public static float convertStatus(TriState status) {
        if (status.equals(TriState.TRUE)) {
            return 1;
        } else if (status.equals(TriState.FALSE)) {
            return 0.5F;
        } else {
            return 0;
        }
    }

    /**
     * Converts from int to TriState status
     */
    public static TriState convertStatus(float status) {
        if (status == 0.5F) {
            return TriState.FALSE; // GROUND
        } else if (status == 1) {
            return TriState.TRUE; // WET
        } else {
            return TriState.DEFAULT; // DRY
        }
    }

    /**
     * Used to convert to float for model predicate
     */
    public static float getStatus(ItemStack stack) {
        if (stack.hasNbt()) {
            return (float) stack.getSubNbt("cannacraft:strain").getInt("Status");
        }
        return 2.0F; // wet
    }
}
