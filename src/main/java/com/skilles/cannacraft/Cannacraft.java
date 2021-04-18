package com.skilles.cannacraft;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.skilles.cannacraft.items.ItemStrainComponent;
import com.skilles.cannacraft.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

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
            //NbtCompound tag = itemStack.getOrCreateSubTag("cannacraft:strain");
            ModComponents.STRAIN.get(itemStack).setIndex(strain); // BUG: index NBT is null when set to 0
            //itemStack.putSubTag("cannacraft:strain", tag);
        }
        return 1;
    }
    public static int identify(CommandContext<ServerCommandSource> ctx, int flag) throws CommandSyntaxException {
        final ServerPlayerEntity self = ctx.getSource().getPlayer();
        ItemStack handStack = self.getMainHandStack();
        if(handStack.getItem().equals(ModItems.SEED)) {
            if(flag == 0) {
                ModComponents.STRAIN.get(handStack).identify();
                System.out.println("Seed identified!");
                return 1;
            } else {
                int i;
                int j = 0;
                for(i = 0; self.getInventory().size() > i; i++) {
                    ItemStack itemStack = self.getInventory().getStack(i);
                    if (itemStack != null && itemStack.getItem().equals(ModItems.SEED) && !ModComponents.STRAIN.get(itemStack).identified()) {
                        ModComponents.STRAIN.get(itemStack).identify();
                        j++;
                    }
                }
                System.out.println(j+" seeds identified!");
            }
        }
        return 1;
    }
    @Override
    public void onInitialize() {
        LootTable.registerLoot();
        System.out.println("LootTables registered!");

        ModItems.registerItems();
        System.out.println("Items registered!");

        ModEntities.registerEntities();
        System.out.println("Entities registered!");

        ModBlocks.registerBlocks();
        System.out.println("Blocks registered!");

        ModScreens.registerScreenHandlers();
        System.out.println("ScreenHandlers registered!");

        CommandRegistrationCallback.EVENT.register((dispatcher, integrated) -> {
            dispatcher.register(literal("seed")
                    .then(literal("identify")
                            .executes(ctx -> {
                                identify(ctx, 0);
                                return 1;
                                }).then(literal("all")
                                    .executes(ctx -> {
                                    identify(ctx, 1);
                                    return 1;
                                    })))
                    .then(literal("set")
                            .then(argument("strain", IntegerArgumentType.integer(0, ItemStrainComponent.STRAIN_COUNT))
                                .executes(ctx -> {
                                    System.out.println("Seed strain set!");
                                    setSeed(ctx, getInteger(ctx, "strain"));
                                    return 1;
            }))));
        });
    }
}
