package com.skilles.cannacraft;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.skilles.cannacraft.items.Seed;
import com.skilles.cannacraft.registry.ModBlocks;
import com.skilles.cannacraft.registry.ModEntities;
import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.registry.ModScreens;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Cannacraft implements ModInitializer {

    public static final String MOD_ID = "cannacraft";

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "general"),
            () -> new ItemStack(ModItems.SEED));

    public static int setSeed(CommandContext<ServerCommandSource> ctx, int strain) throws CommandSyntaxException {
        final ServerCommandSource source = ctx.getSource();
        final ServerPlayerEntity self = source.getPlayer();
        ItemStack itemStack = self.getMainHandStack();
        CompoundTag compoundTag = itemStack.getTag();
        if(itemStack.getItem().equals(ModItems.SEED)){
            Seed seed = (Seed) itemStack.getItem();
            Item seed1 = itemStack.getItem();
            seed.strainType = strain;
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
