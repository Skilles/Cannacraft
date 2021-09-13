package com.skilles.cannacraft.mixins;

import com.skilles.cannacraft.components.StrainInterface;
import com.skilles.cannacraft.dna.genome.gene.TraitGene;
import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.util.MiscUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.text.WordUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.minecraft.client.gui.screen.Screen.hasShiftDown;

@Mixin(Screen.class)
public class ScreenMixin {
    @Shadow
    protected
    MinecraftClient client;

    @Inject(method = "getTooltipFromItem", at = @At(value = "RETURN", target = "Lnet/minecraft/client/gui/screen/Screen;getTooltipFromItem(Lnet/minecraft/item/ItemStack;)Ljava/util/List;"), cancellable = true)
    private void keyTooltip(ItemStack stack, CallbackInfoReturnable<List<Text>> cir) {
        if(stack.isOf(ModItems.WEED_SEED) && hasShiftDown() && stack.hasNbt()) {
            NbtCompound tag = stack.getSubNbt("cannacraft:strain");
            if (!tag.contains("NBT")) {
                cir.cancel();
            }
            StrainInterface stackInterface = ModMisc.STRAIN.get(stack);
            List<TraitGene> expressedGenes = stackInterface.getExpressed();
            if (tag.getBoolean("Identified") && !expressedGenes.isEmpty()) {
                List<Text> tooltip = cir.getReturnValue();
                int offset = client.options.advancedItemTooltips ? 4 : 2;
                tooltip.set(tooltip.size() - offset, new LiteralText("Genes: ").formatted(Formatting.GRAY));
                for (TraitGene gene : expressedGenes) {
                    tooltip.add(7, new LiteralText("- ").formatted(Formatting.DARK_GRAY).append(new LiteralText(WordUtils.capitalizeFully(gene.phenotype.name())).formatted(Formatting.AQUA)).append(new LiteralText(" | ").formatted(Formatting.GRAY)).append(new LiteralText(String.valueOf(gene.value)).formatted(Formatting.GOLD)));
                }
                cir.setReturnValue(tooltip);
            }
        } else if (stack.isOf(ModItems.WEED_BUNDLE) && stack.hasNbt()) {
            NbtCompound tag = stack.getSubNbt("cannacraft:strain");
            List<Text> tooltip = cir.getReturnValue();
            tooltip.add(2, new LiteralText("Press ").append( new LiteralText("SHIFT ").formatted(Formatting.GOLD).append( new LiteralText("to view info").formatted(Formatting.WHITE))));
            assert tag != null;
            if (tag.contains("DNA") && hasShiftDown()) {
                tooltip.clear();
                MiscUtil.appendTooltips(tooltip, stack, true);
                StrainInterface stackInterface = ModMisc.STRAIN.get(stack);
                List<TraitGene> expressedGenes = stackInterface.getExpressed();
                if (!expressedGenes.isEmpty()) {
                    tooltip.set(tooltip.size() - 2, new LiteralText("Genes: ").formatted(Formatting.GRAY));
                    for (TraitGene gene : expressedGenes) {
                        tooltip.add(7, new LiteralText("- ").formatted(Formatting.DARK_GRAY).append(new LiteralText(WordUtils.capitalizeFully(gene.phenotype.name())).formatted(Formatting.AQUA)).append(new LiteralText(" | ").formatted(Formatting.GRAY)).append(new LiteralText(String.valueOf(gene.value)).formatted(Formatting.GOLD)));
                    }
                }
            }
            cir.setReturnValue(tooltip);
        }
    }
}
