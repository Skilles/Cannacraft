package com.skilles.cannacraft;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.skilles.cannacraft.items.Seed;
import com.skilles.cannacraft.registry.ModBlocks;
import com.skilles.cannacraft.registry.ModEntities;
import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.registry.ModScreens;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.literal;

public class Cannacraft implements ModInitializer {

    public static final String MOD_ID = "cannacraft";

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "general"),
            () -> new ItemStack(ModItems.SEED));

    public static int giveSeed(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final ServerCommandSource source = ctx.getSource();
        CompoundTag compoundTag = new CompoundTag();
        final PlayerEntity self = source.getPlayer();
        if(!self.inventory.insertStack(new ItemStack(ModItems.SEED, 1))){
            throw new SimpleCommandExceptionType(new TranslatableText("inventory.isfull")).create();
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

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("giveSeed").executes(ctx -> {
                System.out.println("Seed given! command: "+command);
                giveSeed(ctx);
                return 1;
            }));
        });
    }
}
