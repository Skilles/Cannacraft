package com.skilles.cannacraft.items;

import com.skilles.cannacraft.components.StrainInterface;
import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.util.BundleUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import static com.skilles.cannacraft.Cannacraft.log;

public class WeedDistillate extends StrainItem {
    public WeedDistillate(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
        if (world.isClient) {
            ItemStack clientStack = playerEntity.getStackInHand(hand);
            StrainInterface clientStackInterface = ModMisc.STRAIN.get(clientStack);
            if (!playerEntity.isSneaking()) {
                System.out.println("Strain of held seed: "
                        + clientStackInterface.getStrain()
                        + " THC: " + clientStackInterface.getThc()
                        + " Identified: " + clientStackInterface.identified()
                        + " Genes: " + clientStackInterface.getGenetics()
                        + " Texture: " + BundleUtil.getTexture(clientStack)
                );
            } else {
                log(clientStack.getNbt());
            }
        }
        return TypedActionResult.pass(playerEntity.getStackInHand(hand));
    }
}
