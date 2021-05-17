package com.skilles.cannacraft.util;

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
        if(ounces != 0) {
            if(grams > 0 && grams % 28 != 0) return ounces + " oz " + new DecimalFormat("0.#").format(grams % 28) + " g";
            return ounces + " oz";
        } else if(grams != 0) {
            return new DecimalFormat("0.#").format(grams % 28) + " g";
        }
        return "null g";
    }
    public static double getGrams(ItemStack stack) {
        return stack.getCount() * 3.5;
    }
    public static void appendBundleTooltip(List<Text> tooltip, int count) {
        tooltip.add(new LiteralText("Amount: ").formatted(Formatting.GRAY).append(new LiteralText(getAmountString(count*3.5)).formatted(Formatting.GREEN)));
    }
    public static String getName(ItemStack stack) { // rounded down
        double grams = getGrams(stack);
        String name = " of ";
        if(grams == 1.0 ) {
            name = StringUtils.prependIfMissingIgnoreCase(name, "Gram");
        } else if(grams < 7.0) {
            name = StringUtils.prependIfMissingIgnoreCase(name, "Eighth");
        } else if(grams < 14.5) {
            name = StringUtils.prependIfMissingIgnoreCase(name, "Quarter");
        } else if(grams < 28.0) {
            name = StringUtils.prependIfMissingIgnoreCase(name, "Half");
        } else if(grams < 112.0) {
            name = StringUtils.prependIfMissingIgnoreCase(name, "Ounce");
        } else if(grams < 448.0) {
            name = StringUtils.prependIfMissingIgnoreCase(name, "Quarter Pound");
        } else {
            name = StringUtils.prependIfMissingIgnoreCase(name, "Pound");
        }
        return name;
    }
}
