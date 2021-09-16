package com.skilles.cannacraft.registry;

import com.skilles.cannacraft.mixins.CompostableMixin;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.function.Function;

import static com.skilles.cannacraft.Cannacraft.id;

public class RegistryManager {

    private static final HashMap<Object, Identifier> objIdentMap = new HashMap<>();

    /**
     * Registers Block and BlockItem in vanilla registries
     *
     * @param block Block Block to register
     * @param builder Item.Settings Settings builder for BlockItem
     * @param name Identifier Registry name for block and item
     */
    public static void registerBlock(Block block, Item.Settings builder, Identifier name) {
        Registry.register(Registry.BLOCK, name, block);
        BlockItem itemBlock = new BlockItem(block, builder);
        Registry.register(Registry.ITEM, name, itemBlock);
    }

    public static void registerBlock(Block block, Function<Block, BlockItem> blockItemFunction, Identifier name) {
        Registry.register(Registry.BLOCK, name, block);
        BlockItem itemBlock = blockItemFunction.apply(block);
        Registry.register(Registry.ITEM, name, itemBlock);
    }

    /**
     * Registers Block and BlockItem in vanilla registries.
     * Block should have registered identifier in RebornRegistry via {@link #registerIdent registerIdent} method
     *
     * @param block Block Block to register
     * @param itemGroup Item.Settings Settings builder for BlockItem
     */
    public static void registerBlock(Block block, Item.Settings itemGroup) {
        Validate.isTrue(objIdentMap.containsKey(block));
        registerBlock(block, itemGroup, objIdentMap.get(block));
    }

    public static void registerBlock(Block block, Function<Block, BlockItem> blockItemFunction) {
        Validate.isTrue(objIdentMap.containsKey(block));
        registerBlock(block, blockItemFunction, objIdentMap.get(block));
    }

    /**
     * Register only Block, without BlockItem in vanilla registries
     * Block should have registered identifier in RebornRegistry via {@link #registerIdent registerIdent} method
     * @param block Block Block to register
     */
    public static void registerBlockNoItem(Block block) {
        Validate.isTrue(objIdentMap.containsKey(block));
        Registry.register(Registry.BLOCK, objIdentMap.get(block), block);
    }


    /**
     * Register Item in vanilla registries
     *
     * @param item Item Item to register
     * @param name Identifier Registry name for item
     */
    public static void registerItem(Item item, Identifier name) {
        Registry.register(Registry.ITEM, name, item);
    }

    /**
     * Register Item in vanilla registries
     * Item should have registered identifier in RebornRegistry via {@link #registerIdent registerIdent} method
     *
     * @param item Item Item to register
     */
    public static void registerItem(Item item) {
        Validate.isTrue(objIdentMap.containsKey(item));
        registerItem(item, objIdentMap.get(item));
    }

    /**
     * Registers Identifier in internal RebornCore map
     *
     * @param object Object Item, Block or whatever to be put into map
     * @param identifier Identifier Registry name for object
     */
    public static void registerIdent(Object object, Identifier identifier) {
        objIdentMap.put(object, identifier);
    }

    public static void registerCompostable(float levelIncreaseChance, ItemConvertible item) {
        CompostableMixin.registerCompostableItem(levelIncreaseChance, item);
    }

    public static <I extends Item> I setup(I item, String name) {
        registerIdent(item, id(name));
        return item;
    }

    public static <B extends Block> B setup(B block, String name) {
        registerIdent(block, id(name));
        return block;
    }

    public static SoundEvent setup(String name) {
        Identifier identifier = id(name);
        return Registry.register(Registry.SOUND_EVENT, identifier, new SoundEvent(identifier));
    }



}
