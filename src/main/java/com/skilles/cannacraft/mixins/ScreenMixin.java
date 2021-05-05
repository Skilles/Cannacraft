package com.skilles.cannacraft.mixins;

import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.strain.Gene;
import com.skilles.cannacraft.strain.GeneticsManager;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.gui.screen.Screen.hasShiftDown;

@Mixin(Screen.class)
public class ScreenMixin {
    @Shadow
    protected
    MinecraftClient client;

    @Inject(method = "getTooltipFromItem", at = @At(value = "RETURN", target = "Lnet/minecraft/client/gui/screen/Screen;getTooltipFromItem(Lnet/minecraft/item/ItemStack;)Ljava/util/List;"), cancellable = true)
    private void keyTooltip(ItemStack stack, CallbackInfoReturnable<List<Text>> cir) {
        if((stack.isOf(ModItems.WEED_SEED) || stack.isOf(ModItems.WEED_FRUIT)) && hasShiftDown() && stack.hasTag()) {
            NbtCompound tag = stack.getSubTag("cannacraft:strain");
            assert tag != null;
            if(tag.getBoolean("Identified") && tag.contains("Attributes") && !tag.getList("Attributes", NbtType.COMPOUND).isEmpty()) {
                List<Text> tooltip = cir.getReturnValue();
                NbtList genes = tag.getList("Attributes", NbtType.COMPOUND);
                assert tooltip.size() >= 6;
                tooltip.set(5, new LiteralText("Genes: ").formatted(Formatting.GRAY));
                ArrayList<Gene> geneList = GeneticsManager.fromNbtList(genes);
                for (int i = 0, geneListSize = geneList.size(); i < geneListSize; i++) {
                    Gene gene = geneList.get(i);
                    tooltip.add(6+i, new LiteralText("- ").formatted(Formatting.DARK_GRAY).append(new LiteralText(StringUtils.capitalize(gene.name())).formatted(Formatting.AQUA)).append(new LiteralText(" | ").formatted(Formatting.GRAY)).append(new LiteralText(String.valueOf(gene.level())).formatted(Formatting.GOLD)));
                }
                cir.setReturnValue(tooltip);
            }
        }
    }
}
