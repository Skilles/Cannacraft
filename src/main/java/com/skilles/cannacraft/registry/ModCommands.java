package com.skilles.cannacraft.registry;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.skilles.cannacraft.strain.GeneticsManager;
import com.skilles.cannacraft.strain.StrainMap;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ModCommands {
    public static int setStrain(CommandContext<ServerCommandSource> ctx, int strain) throws CommandSyntaxException {
        final ServerPlayerEntity self = ctx.getSource().getPlayer();
        ItemStack itemStack = self.getMainHandStack();
        if (itemStack.getItem().equals(ModItems.WEED_SEED) || itemStack.getItem().equals(ModItems.WEED_FRUIT)) {
            //NbtCompound tag = itemStack.getOrCreateSubTag("cannacraft:strain");
            ModComponents.STRAIN.get(itemStack).setStrain(strain); // BUG: index NBT is null when set to 0
            self.sendSystemMessage(Text.of("Strain set to: " + ModComponents.STRAIN.get(itemStack).getStrain()), Util.NIL_UUID);
            //itemStack.putSubTag("cannacraft:strain", tag);
        }
        return 1;
    }

    public static int setSex(CommandContext<ServerCommandSource> ctx, String gender) throws CommandSyntaxException {
        final ServerPlayerEntity self = ctx.getSource().getPlayer();
        ItemStack itemStack = self.getMainHandStack();
        if (itemStack.getItem().equals(ModItems.WEED_SEED)) {
            //NbtCompound tag = itemStack.getOrCreateSubTag("cannacraft:strain");
            if (gender.equalsIgnoreCase("male")) {
                ModComponents.STRAIN.get(itemStack).setMale(true);
            } else if (gender.equalsIgnoreCase("female")) {
                ModComponents.STRAIN.get(itemStack).setMale(false);
            } else {
                self.sendSystemMessage(Text.of("Unknown gender"), Util.NIL_UUID);
            }

            //itemStack.putSubTag("cannacraft:strain", tag);
        }
        return 1;
    }

    public static int identify(CommandContext<ServerCommandSource> ctx, int flag) throws CommandSyntaxException {
        final ServerPlayerEntity self = ctx.getSource().getPlayer();
        ItemStack handStack = self.getMainHandStack();
        if (handStack.getItem().equals(ModItems.WEED_SEED) || handStack.getItem().equals(ModItems.WEED_FRUIT)) {
            if (flag == 0) {
                ModComponents.STRAIN.get(handStack).identify();
                self.sendSystemMessage(Text.of("Seed identified"), Util.NIL_UUID);
                return 1;
            } else {
                int i;
                int j = 0;
                for (i = 0; self.getInventory().size() > i; i++) {
                    ItemStack itemStack = self.getInventory().getStack(i);
                    if (itemStack != null && itemStack.getItem().equals(ModItems.WEED_SEED) && !ModComponents.STRAIN.get(itemStack).identified()) {
                        ModComponents.STRAIN.get(itemStack).identify();
                        j++;
                    }
                }
                self.sendSystemMessage(Text.of(j + " seeds identified"), Util.NIL_UUID);
            }
        }
        return 1;
    }

    public static int addStrain(CommandContext<ServerCommandSource> ctx, String name, String type) throws CommandSyntaxException {
        final ServerPlayerEntity self = ctx.getSource().getPlayer();
        StrainMap.addStrain(name, StrainMap.Type.valueOf(type.toUpperCase()));
        self.sendSystemMessage(Text.of("Strain added: " + StrainMap.toStrain(name)), Util.NIL_UUID);
        return 1;
    }

    public static int listStrain(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final ServerPlayerEntity self = ctx.getSource().getPlayer();
        self.sendSystemMessage(Text.of(StrainMap.getStrains().toString()), Util.NIL_UUID);
        return 1;
    }

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, integrated) -> {
            dispatcher.register(literal("strain")
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
                            .then(argument("index", IntegerArgumentType.integer(0, StrainMap.getStrainCount()))
                                    .executes(ctx -> {
                                        setStrain(ctx, getInteger(ctx, "index"));
                                        return 1;
                                    })))
                    .then(literal("gender")
                            .then(argument("sex", StringArgumentType.string()).executes(ctx -> {
                                setSex(ctx, getString(ctx, "sex"));
                                return 1;
                            })))
                    .then(literal("add")
                            .then(argument("name", StringArgumentType.string())
                                    .then(argument("type", StringArgumentType.string())
                                            .executes(ctx -> {
                                                addStrain(ctx, getString(ctx, "name"), getString(ctx, "type"));
                                                return 1;
                                            }))))
                    .then(literal("remove")
                            .then(argument("index", IntegerArgumentType.integer(0, StrainMap.getStrainCount()))
                                    .executes(ctx -> {
                                        final ServerPlayerEntity self = ctx.getSource().getPlayer();
                                        self.sendSystemMessage(Text.of("Strain removed: "+StrainMap.getStrain(getInteger(ctx, "index"))), Util.NIL_UUID);
                                        StrainMap.removeStrain(getInteger(ctx, "index"));
                                        return 1;
                                    })))
                    .then(literal("list")
                            .executes(ctx -> {
                                listStrain(ctx);
                                return 1;
                            }))
                    .then(literal("random")
                            .executes(ctx -> {
                                final ServerPlayerEntity self = ctx.getSource().getPlayer();
                                ItemStack stack = new ItemStack(ModItems.WEED_SEED);
                                ModComponents.STRAIN.get(stack).setStrain(Math.abs(GeneticsManager.random().nextInt(StrainMap.getStrainCount() - 1)) + 1);
                                ModComponents.STRAIN.get(stack).setThc(StrainMap.normalDist(18, 5, 13));
                                self.giveItemStack(stack);
                                self.sendSystemMessage(Text.of("Random seed given"), Util.NIL_UUID);
                                return 1;
                    })
                            .then(argument("sex", StringArgumentType.string())
                                    .executes(ctx -> {
                                        setSex(ctx, getString(ctx, "sex"));
                                        return 1;
                    })))
                    .then(literal("resetlist").executes(ctx -> {
                        StrainMap.resetStrains();
                        final ServerPlayerEntity self = ctx.getSource().getPlayer();
                        self.sendSystemMessage(Text.of("Strains reset"), Util.NIL_UUID);
                        return 1;
                    }))
            );
        });
    }
}
