package com.skilles.cannacraft.items;

import com.skilles.cannacraft.components.StrainInterface;
import com.skilles.cannacraft.dna.genome.Genome;
import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.strain.StrainMap;
import com.skilles.cannacraft.util.MiscUtil;
import com.skilles.cannacraft.util.WeedRegistry;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.skilles.cannacraft.Cannacraft.log;

public abstract class StrainItem extends Item {

    public StrainItem(Settings settings) {
        super(settings);
    }

    public static void debugAction(World world, PlayerEntity playerEntity, Hand hand) {
        if (world.isClient) {
            ItemStack clientStack = playerEntity.getStackInHand(hand);
            StrainInterface clientStackInterface = ModMisc.STRAIN.get(clientStack);
            if (!playerEntity.isSneaking()) {
                Genome genome = clientStackInterface.getGenome();
                playerEntity.sendMessage(new LiteralText(genome.prettyPrint()).formatted(Formatting.RED), false);
            } else {
                log(clientStack.getNbt());
            }
        }
    }

    @Override
    public Text getName(ItemStack stack) {
        if (stack.hasNbt()) {
            if(WeedRegistry.getStrain(stack).type().equals(StrainMap.Type.UNKNOWN)) {
                log(WeedRegistry.getStrain(stack));
                log(stack.getNbt());
                stack.getSubNbt("cannacraft:strain").putInt("ID", 0);
            }
            return MiscUtil.getItemName(stack);
        }
        return super.getName(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if(stack.hasNbt()) {
            NbtCompound tag = stack.getSubNbt("cannacraft:strain");
            if (tag.contains("ID") && !(tag.getInt("ID") == 0)) {
                MiscUtil.appendTooltips(tooltip, tag, false);
            }
        }
        super.appendTooltip(stack, world, tooltip, context);
    }
}
