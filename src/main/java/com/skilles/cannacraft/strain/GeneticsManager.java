package com.skilles.cannacraft.strain;

import com.google.common.collect.Lists;
import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.strain.StrainMap.Type;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.skilles.cannacraft.strain.StrainMap.strainList;

/**
 * This class contains utilities for modifying strain attributes such as THC, name, and type
 */
public final class GeneticsManager {
    public static int crossThc(int thc1, int thc2) {
        return (thc1 + thc2) / 2;
    }
    public static ArrayList<Pair<GeneTypes, Integer>> geneArray = new ArrayList<>();
    private static final Map<String, Integer> suffixesMap = new HashMap<String, Integer>() {{
        put("OG", 5);
        put("Kush", 2);
        put("Cookies", 1);
        put("Dream", 1);
        put("Poison", 0);
        put("Crack", 0);
        put("Dawg", 0);
        put("Punch", 1);
        put("Trix", 0);
        put("Cake", 0);
    }
    };

    private static final Random random =  new Random();

    /**
     * @return a list with all possible combinations of strains
     */
    public static List<String> getStrainCombinations(List<String> nameOne, List<String> nameTwo) {
        List<List<String>> combinationLists = Lists.cartesianProduct(nameOne, nameTwo);
        List<String> nameList = new ArrayList<>();
        for (List<String> stringList: combinationLists) {
            nameList.add(stringList.get(0) + " " + stringList.get(1));
            nameList.add(stringList.get(1) + " " + stringList.get(0));
        }
        return nameList;
    }

    /**
     * @param map to parse
     * @return returns the key with the highest value
     */
    public static <K, V extends Comparable<V>> K maxEntry(Map<K, V> map) {
        Optional<Map.Entry<K, V>> maxEntry = map.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue()
                );
        assert maxEntry.isPresent();
        return maxEntry.get().getKey();
    }
    /**
     * TODO: follow standard of mother + father
     * @param name1 by default is the first word
     * @param name2 by default is the second word
     * @return returns the crossed name of strains according to the prefix list
     */
    public static String crossStrains(String name1, String name2) {
        List<String> nameOneFinal = Arrays.asList(StringUtils.split(name1));
        List<String> nameTwoFinal = Arrays.asList(StringUtils.split(name2));
        final List<String> finalNames = new ArrayList<String>() {{
            addAll(nameOneFinal);
            addAll(nameTwoFinal);
        }};
        if(nameOneFinal.contains("Unknown") || nameTwoFinal.contains("Unknown")) return "Unknown";


        // Finds all combinations of names and returns a value if any are a known strain
        List<String> nameList = getStrainCombinations(nameOneFinal, nameTwoFinal);
        Optional<String> optional = nameList.stream().filter(strainList::containsKey).findAny();
        if(optional.isPresent()) {
            return optional.get();
        }
        // Early return if both names are one word
        if(nameOneFinal.size() == 1 && nameTwoFinal.size() == 1){
            return nameOneFinal.get(0) + " " + nameTwoFinal.get(0);
        }
        // Creates a mutable array of all words
        List<String> nameOne = new LinkedList<>(Arrays.asList(StringUtils.split(name1)));
        List<String> nameTwo = new LinkedList<>(Arrays.asList(StringUtils.split(name2)));
        List<String> names = new ArrayList<String>() {{
            addAll(nameOneFinal);
            addAll(nameTwoFinal);
        }};
        // Set suffix and remove from names
        Map<String, Integer> tempSuffixMap = new HashMap<>();
        for(int i = 0; i < finalNames.size(); i++) {
            if(suffixesMap.containsKey(finalNames.get(i))) {
                tempSuffixMap.put(finalNames.get(i), suffixesMap.get(finalNames.get(i)));
                // Checks if suffix is part of name1 or name2, then removes if it is
                if(nameOne.contains(finalNames.get(i))) {
                    filterName(nameOne, tempSuffixMap, names);
                } else if(nameTwo.contains(finalNames.get(i))){
                    filterName(nameTwo, tempSuffixMap, names);
                }
            }
        }
        String newName1;
        String newName2;
        // Checks if suffix was found, if true then suffix is one with highest priority
        if(!tempSuffixMap.isEmpty()) {
            newName2 = maxEntry(tempSuffixMap);
        } else {
            // If no suffix was not found, set suffix to last element in name list
            newName2 = names.get(names.size() - 1);
        }
        // Sets prefix to first element in name list
        if(nameOne.isEmpty()) {
            newName1 = nameTwo.get(0);
        } else if(nameTwo.isEmpty()) {
            newName1 = nameOne.get(0);
        } else {
            if(nameOneFinal.contains(newName2)) {
                newName1 = nameTwo.get(0);
            } else {
                newName1 = nameOne.get(0);
            }
        }
        System.out.println(names);
        return newName1+ " " + newName2;
    }

    /**
     * This method eventually sorts the output list to only contain names that are not part of the suffix name
     * @param name name to filter
     * @param tempSuffixMap suffix map that contains suffixes from name1 and name2
     * @param names output list
     * @return returns how many names were removed from the compound word (0 = simple word)
     */
    private static int filterName(List<String> name, Map<String, Integer> tempSuffixMap, List<String> names) {
        if(name.size() > 1) {
            // Check if nameTwo has two prefixes and clears if it does
            if(suffixesMap.containsKey(name.get(0)) && suffixesMap.containsKey(name.get(1))) {
                names.remove(name.get(0));
                names.remove(name.get(1));
                name.clear();
                return 2;
            } else {
                if (tempSuffixMap.containsKey(name.get(0))) {
                    names.remove(name.remove(0));
                    return 1;
                } else if (tempSuffixMap.containsKey(name.get(1))) {
                    names.remove(name.remove(1));
                    return 1;
                }
            }
        } else {
            // Remove name from names because it is for sure a lone prefix
            names.remove(name.get(0));
            name.clear();
        }
        return 0;
    }
    /**
     * @param type1 dominant type which has priority
     * @return returns hybrid ONLY if sativa x indica or hybrid x hybrid
     * TODO: set type dynamically based on name
     */
    public static Type crossTypes(Type type1, Type type2) {
        if(type2.equals(Type.UNKNOWN)) return Type.UNKNOWN;
        switch(type1) {
            case INDICA:
                if(type2.equals(Type.SATIVA)) return Type.HYBRID;
                return Type.INDICA;
            case SATIVA:
                if(type2.equals(Type.INDICA)) return Type.HYBRID;
                return Type.SATIVA;
            case HYBRID:
                if(type2.equals(Type.HYBRID)) return Type.HYBRID;
                return type2;
            default:
                return Type.UNKNOWN;
        }
    }
    public static Random random() {
        return random;
    }
    public static void appendTooltips(List<Text> tooltip, NbtCompound tag) {
        String sex = "";
        if(tag.contains("Male")) sex = tag.getBoolean("Male") ? "Male" : "Female";
        int id = tag.getInt("ID");
        int thc = tag.getInt("THC");
        NbtList genes = new NbtList();
        if(tag.contains("Attributes")) genes = tag.getList("Attributes", NbtType.COMPOUND);
        if(tag.getBoolean("Identified")) {
            tooltip.add(new LiteralText("Strain: ").formatted(Formatting.GRAY).append(new LiteralText(StrainMap.getStrain(id).name()).formatted(Formatting.GREEN)));
            tooltip.add(new LiteralText("Type: ").formatted(Formatting.GRAY).append(new LiteralText(StringUtils.capitalize(StringUtils.capitalize(StringUtils.lowerCase(StrainMap.getStrain(id).type().name())))).formatted(Formatting.DARK_GREEN)));
            tooltip.add(new LiteralText("THC: ").formatted(Formatting.GRAY).append(new LiteralText(thc + "%").formatted(Formatting.DARK_GREEN)));
            if(!sex.isEmpty()) tooltip.add(new LiteralText("Sex: ").formatted(Formatting.GRAY).append(new LiteralText(sex).formatted(Formatting.DARK_GREEN)));
            if(!genes.isEmpty()) {
                tooltip.add(new LiteralText("Press ").append( new LiteralText("SHIFT ").formatted(Formatting.GOLD).append( new LiteralText("to view Genes").formatted(Formatting.WHITE))));
            }
        } else {
            tooltip.add(new LiteralText("Strain: ").formatted(Formatting.GRAY).append(new LiteralText("Unidentified").formatted(Formatting.GREEN)));
            tooltip.add(new LiteralText("Type: ").formatted(Formatting.GRAY).append(new LiteralText("Unknown").formatted(Formatting.DARK_GREEN)));
            tooltip.add(new LiteralText("Sex: ").formatted(Formatting.GRAY).append(new LiteralText("Unknown").formatted(Formatting.DARK_GREEN)));
        }
    }
    public static int durationToAmplifier(int duration) {
        if(duration <= 1200) {
            return 0;
        } else if(duration <= 1800) {
            return 1;
        } else if (duration <= 2400) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * Sends a player a message according to how high they are
     * @param player the player to send the message to
     */
    private static void sendHighMessage(PlayerEntity player) {
        StatusEffectInstance currentEffect = player.getStatusEffect(ModMisc.HIGH);
        int amplifier = currentEffect.getAmplifier();
        switch(amplifier) {
            case 0:
                player.applyStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, currentEffect.getDuration(), 0, true,false));
                player.sendMessage(new LiteralText("The buzz has made you resistant to fire").formatted(Formatting.GREEN), true);
                break;
            case 1:
                player.applyStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, currentEffect.getDuration(), 0, true, false));
                player.sendMessage(new LiteralText("Why are your hands shaking").formatted(Formatting.GREEN), true);
                break;
            case 2:
                if (GeneticsManager.random().nextInt(2) == 0) {
                    player.applyStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, currentEffect.getDuration(), 0, true,false));
                    player.sendMessage(new LiteralText("You feel stronger for some reason").formatted(Formatting.GREEN), true);
                } else {
                    player.applyStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, currentEffect.getDuration(), 0, true, false));
                    player.applyStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, currentEffect.getDuration(), 0, true, false));
                    player.sendMessage(new LiteralText("You feel like you're floating").formatted(Formatting.GREEN), true);
                }
                break;
            case 3:
                if (GeneticsManager.random().nextInt(2) == 0) {
                    player.applyStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, currentEffect.getDuration(), 0, true, false));
                    player.sendMessage(new LiteralText("Sonic").formatted(Formatting.GREEN), true);
                } else {
                    player.applyStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, currentEffect.getDuration(), 0, true, false));
                    player.applyStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, currentEffect.getDuration(), 0, true, false));
                    player.sendMessage(new LiteralText("You could really use some oreos").formatted(Formatting.GREEN), true);
                }
                break;
            default:
                break;
        }
    }

    /**
     * @param user entity to
     */
    public static void applyHigh(LivingEntity user) {
        int duration;
        int amplifier;
        int switchNum = 0;
        assert user.getActiveItem().hasTag();
        int index = user.getActiveItem().getSubTag("cannacraft:strain").getInt("ID");
        int thc = user.getActiveItem().getSubTag("cannacraft:strain").getInt("THC");
        ModMisc.PLAYER.get(user).setStrain(index);
        if(thc <= 18) switchNum = 1;
        if(19 <= thc && thc <= 25) switchNum = 2;
        if(26 <= thc) switchNum = 3;
        switch(switchNum) {
            case 1:
                duration = 1200;
                break;
            case 2:
                duration = 1800;
                break;
            case 3:
                duration = 2400;
                break;
            default:
                duration = 0;
        }
        if(user.hasStatusEffect(ModMisc.HIGH)) {
            StatusEffectInstance currentEffect = user.getStatusEffect(ModMisc.HIGH);
            switch(switchNum) {
                case 1:
                    duration = currentEffect.getDuration() + 600;
                    break;
                case 2:
                    duration = currentEffect.getDuration() + 1200;
                    break;
                case 3:
                    duration = currentEffect.getDuration() + 1800;
                    break;
                default:
                    duration = 0;
            }
        }
        amplifier = durationToAmplifier(duration);
        if(amplifier >  2 && !user.world.isDay()) {
            //user.setSleepingPosition(user.getBlockPos());
            user.setSleepingPosition(user.getBlockPos());
        }
        user.addStatusEffect(new StatusEffectInstance(ModMisc.HIGH, duration, amplifier));
        sendHighMessage((PlayerEntity) user);
    }
    /**
     * Drops an itemstack with NBT
     */
    public static void dropStack(World world, BlockPos pos, Item type, boolean brokenWithShears) {
        ItemStack toDrop = new ItemStack(type);
        if(world.getBlockEntity(pos) != null) {
            NbtCompound tag = world.getBlockEntity(pos).writeNbt(new NbtCompound());
            if (tag != null) {
                tag.putInt("THC", tag.getInt("Seed THC"));
                if (type.equals(ModItems.WEED_SEED) && !tag.getBoolean("Male")) {
                    toDrop.putSubTag("cannacraft:strain", trimTag(tag, type));
                    Block.dropStack(world, pos, toDrop);
                } else if (brokenWithShears && type.equals(ModItems.WEED_FRUIT)) {
                    toDrop.putSubTag("cannacraft:strain", trimTag(tag));
                    Block.dropStack(world, pos, toDrop);
                }
            } else {
                System.out.println("Error: NULLTAG");
            }
        } else {
            System.out.println("Error: NULLBENTITY");
        }
    }
    public static void dropStack(World world, BlockPos pos, Item type) {
        if (type.equals(ModItems.WEED_SEED)) {
            dropStack(world, pos, type, true);
        } else {
            dropStack(world, pos, type, false);
        }
    }
    public static void dropStack(WorldAccess world, BlockPos pos, Item type) {
        dropStack(world.getBlockEntity(pos).getWorld(), pos, type, true);
    }

    /**
     * @param tag to randomize genes for
     */
    public static void randomizeTag(NbtCompound tag) {
        Random random = random();
        float chance = random.nextFloat();
        NbtList nbtList = new NbtList();
        ArrayList<Gene> geneList = new ArrayList<>();
        if(chance <= 0.1F) { // 10% chance
            geneList.add(new Gene(GeneTypes.SPEED, random.nextInt(GeneTypes.SPEED.getMax()) + 1));
            geneList.add(new Gene(GeneTypes.YIELD, random.nextInt(GeneTypes.YIELD.getMax()) + 1));
            tag.put("Attributes", nbtList);
        } else if(chance > 0.1F && chance <= 0.25F) { // 15% chance
            geneList.add(new Gene(GeneTypes.YIELD, random.nextInt(GeneTypes.YIELD.getMax() - 1) + 1));
        } else if(chance > 0.25F && chance <= 0.45F) { // 20% chance
            geneList.add(new Gene(GeneTypes.SPEED, random.nextInt(GeneTypes.SPEED.getMax() - 1) + 1));
        } else if(chance > 0.45F && chance <= 1F) { // 65% chance
        }
        tag.put("Attributes", toNbtList(geneList));
        tag.putInt("ID", GeneticsManager.random().nextInt((StrainMap.ogStrainCount - 1)) + 1); // random id
    }
    /**
     * Format Block NBT to conform with ItemStack
     * @param tag block NBT tag
     * @param type type of format
     * @return tag with trimmed NBT
     */
    public static NbtCompound trimTag(NbtCompound tag, Item type){
        NbtCompound newTag = tag;
        if(tag != null) {
            newTag.remove("id");
            newTag.remove("x");
            newTag.remove("y");
            newTag.remove("z");
            newTag.putInt("THC", newTag.getInt("Seed THC"));
            newTag.remove("Seed THC");
            if(newTag.contains("Male") && !newTag.getBoolean("Male")) newTag.remove("Male");
            if(type !=null && type.equals(ModItems.WEED_SEED)) newTag.putInt("ID", newTag.getInt("Seed ID"));
            newTag.remove("Seed ID");
        }
        return newTag;
    }
    public static NbtCompound trimTag(NbtCompound tag) {
        return trimTag(tag, null);
    }
    public static NbtList toNbtList(ArrayList<Gene> list) {
        NbtList nbtList = new NbtList();
        for (Gene entry: list) {
            NbtCompound compound = new NbtCompound();
            compound.putString("Gene", entry.name());
            compound.putInt("Level", entry.level());
            String string = entry.name() + ":" + entry.level();
            nbtList.add(compound);
        }
        return nbtList;
    }
    public static ArrayList<Gene> fromNbtList(List<NbtCompound> list) {
        ArrayList<Gene> arrayList = new ArrayList<>();
        for (NbtCompound compoundEntry : list) {
            GeneTypes gene = GeneTypes.byName(compoundEntry.getString("Gene"));
            int level = compoundEntry.getInt("Level");
            arrayList.add(new Gene(gene, level));
        }
        return arrayList;
    }
    public static ArrayList<Gene> fromNbtList(NbtList list) {
        ArrayList<Gene> arrayList = new ArrayList<>();
        for (NbtElement compoundEntry : list) {
            GeneTypes geneType = GeneTypes.byName(((NbtCompound)compoundEntry).getString("Gene"));
            int level = ((NbtCompound)compoundEntry).getInt("Level");
            Gene geneObj = new Gene(geneType, level);
            arrayList.add(geneObj);
        }
        return arrayList;
    }
    public static boolean NbtListContains(NbtList nbtList, String name) {
        if(nbtList == null || nbtList.isEmpty()) return false;
        for (NbtElement nbtElement : nbtList) {
            NbtCompound entry = (NbtCompound) nbtElement;
            if (entry.getString("Gene").equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
    public Pair<GeneTypes, Integer> crossGenes(int level1, int level2, GeneTypes type) {
        int levelDiff = Math.abs(level1 - level2);
        int newLevel = 0;
        Random random = new Random();

        switch(levelDiff) {
            case 0:
                newLevel = level1;
                break;
            case 1:
                int i = random.nextInt(2); // 0 - 1
                switch(i) {
                    case 0: // 50%
                        newLevel = Integer.min(level1, level2);
                        break;
                    case 1: // 50%
                        newLevel = Integer.max(level1, level2);
                        break;
                }
            case 2:
                i = random.nextInt(4); // 0 - 3
                if(i == 0) { // 0 25%
                    newLevel = Integer.min(level1, level2);
                    break;
                } else if(i <= 2) { // 1 or 2 50%
                    newLevel = Integer.sum(level1, level2)/2;
                    break;
                } else { // 3 25%
                    newLevel = Integer.max(level1, level2);
                    break;
                }
            case 3: // 1: 0, 2: 3
                i = random.nextInt(4); // 4 cases
                if(i < 2) { // 0 or 1 50%
                    newLevel = Integer.min(level1, level2);
                    break;
                } else if(i == 2) { // 2 25%
                    newLevel = Integer.sum(level1, level2)/2;
                    break;
                } else { // 3 25%
                    newLevel = Integer.max(level1, level2);
                    break;
                }
        }
        return new Pair<>(type, newLevel);
    }
}
