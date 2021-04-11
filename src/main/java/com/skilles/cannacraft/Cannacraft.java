package com.skilles.cannacraft;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.skilles.cannacraft.items.Seed;
import com.skilles.cannacraft.items.StrainComponent;
import com.skilles.cannacraft.items.StrainInterface;
import com.skilles.cannacraft.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Cannacraft implements ModInitializer {

    public static final String MOD_ID = "cannacraft";

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
            id("general"),
            () -> new ItemStack(ModItems.SEED));

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static int setSeed(CommandContext<ServerCommandSource> ctx, int strain) throws CommandSyntaxException {
        final ServerPlayerEntity self = ctx.getSource().getPlayer();
        ItemStack itemStack = self.getMainHandStack();
        if(itemStack.getItem().equals(ModItems.SEED)){
            /*CompoundTag newCompoundTag = new CompoundTag();
            StrainType strainType = new StrainType();
            strainType.setStrain(strain);
            newCompoundTag.put("Strain", strainType.getStrainTag());
            itemStack.setTag(newCompoundTag);*/
            ModComponents.STRAIN.get(itemStack).setIndex(strain);
            System.out.println("Index update! arg="+strain+" new="+ModComponents.STRAIN.get(itemStack).getStrain());
            /*if(!self.inventory.insertStack(self.inventory.selectedSlot, itemStack)){
                throw new SimpleCommandExceptionType(new TranslatableText("inventory.isfull")).create();
            }*/
        }

        command = true;
        return 1;
    }
    public static boolean command = false;
    @Override
    public void onInitialize() {

        ModItems.registerItems();
        System.out.println("Items registered!");

        ModEntities.registerEntities();
        System.out.println("Entities registered!");

        ModBlocks.registerBlocks();
        System.out.println("Blocks registered!");

        ModScreens.registerScreenHandlers();
        System.out.println("ScreenHandlers registered!");

        CommandRegistrationCallback.EVENT.register((dispatcher, integrated) -> {
            dispatcher.register(literal("setseed")
                    .then(argument("strain", IntegerArgumentType.integer(0,StrainType.strainCount))
                            .executes(ctx -> {
                System.out.println("Seed strain set!");
                setSeed(ctx, getInteger(ctx, "strain"));
                return 1;
            })));
        });
    }
}
