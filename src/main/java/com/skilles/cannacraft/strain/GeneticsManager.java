package com.skilles.cannacraft.strain;

import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.strain.StrainMap.Type;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.skilles.cannacraft.strain.StrainMap.strainList;

/**
 * This class contains utilities for modifying strain attributes such as THC, name, and type
 */
public final class GeneticsManager {
    private static final List<String> suffixes = new ArrayList<String>() {{  // common endings of strains
        add("OG");
        add("Kush");
        add("Cookies");
        add("Dream");
        add("Poison");
        add("Crack");
        add("Chem");
        add("Dawg");
    }};
    private static final Random random = new Random();
    public static ArrayList<Pair<Genes, Integer>> geneArray = new ArrayList<>();

    public static int crossThc(int thc1, int thc2) {
        return (thc1 + thc2) / 2;
    }

    /**
     * @return returns a set with all possible combinations of existing strains
     */
    public static Set<String> crossStrainSet() {

        Set<String> nameSet = strainList.keySet();
        nameSet.remove("Unknown");
        Set<String> set = new HashSet<>(); // no dupes
        for (String name1 : nameSet) { // loops 3 times
            for (int i = 0; i < nameSet.size(); i++) { // loops 3 times
                if (!name1.equals(nameSet.toArray()[i])) { // prevent "OG OG"
                    String cross1 = GeneticsManager.crossNames(name1, (String) nameSet.toArray()[i]);
                    String cross2 = GeneticsManager.crossNames((String) nameSet.toArray()[i], name1);
                    String cross3 = GeneticsManager.altCrossNames(name1, (String) nameSet.toArray()[i]);
                    String cross4 = GeneticsManager.altCrossNames((String) nameSet.toArray()[i], name1);
                    set.add(cross1);
                    set.add(cross2);
                    set.add(cross3);
                    set.add(cross4);
                }
            }
        }
        return set;
    }

    /**
     * @param name1 the first word of this argument will be the first word of the new name
     * @param name2 the second word of this argument will be the second word of the new name (first if only one word)
     * @return Returns a combined strain name
     */
    private static String crossNames(String name1, String name2) {
        int index = name2.indexOf(' ');
        String nameOne = name1.substring(0, name1.indexOf(' ')).trim();
        String nameTwo = name2.substring(index).trim();

        if (index > -1) {
            return nameOne + " " + name2;
        }
        return nameOne + " " + nameTwo;
    }

    /**
     * @param name1 has priority in suffixes
     * @param name2 will default use as the first word
     * @return returns the crossed name of strains according to the prefix list
     * TODO: add priority to suffixes
     * TODO: add prefixes
     */
    public static String crossStrains(String name1, String name2) {
        String[] nameOne = StringUtils.split(name1);
        String[] nameTwo = StringUtils.split(name2);
        if (nameOne.length > 1 && nameTwo.length > 1) {
            if (suffixes.contains(nameTwo[1]) && !suffixes.contains(nameOne[1])) {
                return altCrossNames(nameOne[0], nameTwo[1]);
            } else {
                return altCrossNames(nameTwo[0], nameOne[1]);
            }
        } else if (nameOne.length > 1 && nameTwo.length == 1) {
            return altCrossNames(nameTwo[0], nameOne[1]);
        } else if (nameTwo.length > 1 && nameOne.length == 1) {
            return altCrossNames(nameOne[0], nameTwo[1]);
        } else { // both length 1
            if (suffixes.contains(nameTwo[0])) {
                return altCrossNames(name1, name2);
            } else {
                return altCrossNames(name2, name1);
            }
        }
    }

    /**
     * @param name1 the first word of this argument will be the first word of the new name
     * @param name2 the first word of this argument will be the second word of the new name
     * @return Returns a combined strain name
     */
    private static String altCrossNames(String name1, String name2) {
        if (StringUtils.contains("Unknown", name1) || StringUtils.contains("Unknown", 2)) {
            return null;
        }
        int name1index = name1.indexOf(' ');
        int name2index = name2.indexOf(' ');
        String nameOne;
        String nameTwo;
        if (name1index > -1) {
            nameOne = name1.substring(0, name1index);
        } else {
            nameOne = name1;
        }
        if (name2index > -1) {
            nameTwo = name2.substring(0, name2index);
        } else {
            nameTwo = name2;
        }

        return nameOne + " " + nameTwo;
    }

    /**
     * @param type1 dominant type which has priority
     * @param type2
     * @return returns hybrid ONLY if sativa x indica or hybrid x hybrid
     */
    public static Type crossTypes(Type type1, Type type2) {
        if (type2.equals(Type.UNKNOWN)) return Type.UNKNOWN;
        switch (type1) {
            case INDICA:
                if (type2.equals(Type.SATIVA)) return Type.HYBRID;
                return Type.INDICA;
            case SATIVA:
                if (type2.equals(Type.INDICA)) return Type.HYBRID;
                return Type.SATIVA;
            case HYBRID:
                if (type2.equals(Type.HYBRID)) return Type.HYBRID;
                return type2;
            default:
                return Type.UNKNOWN;
        }
    }

    public static Random random() {
        return random;
    }

    public static void appendTooltips(List<Text> tooltip, CompoundTag tag) {
        String sex = tag.getBoolean("Male") ? "Male" : "Female";
        int id = tag.getInt("ID");
        int thc = tag.getInt("THC");
        if (tag.getBoolean("Identified")) {
            tooltip.add(new LiteralText("Strain: ").formatted(Formatting.GRAY).append(new LiteralText(StrainMap.getStrain(id).name()).formatted(Formatting.GREEN)));
            tooltip.add(new LiteralText("Type: ").formatted(Formatting.GRAY).append(new LiteralText(StringUtils.capitalize(StringUtils.capitalize(StringUtils.lowerCase(StrainMap.getStrain(id).type().name())))).formatted(Formatting.DARK_GREEN)));
            tooltip.add(new LiteralText("THC: ").formatted(Formatting.GRAY).append(new LiteralText(thc + "%").formatted(Formatting.DARK_GREEN)));
            tooltip.add(new LiteralText("Sex: ").formatted(Formatting.GRAY).append(new LiteralText(sex).formatted(Formatting.DARK_GREEN)));
        } else {
            tooltip.add(new LiteralText("Strain: ").formatted(Formatting.GRAY).append(new LiteralText("Unidentified").formatted(Formatting.GREEN)));
            tooltip.add(new LiteralText("Type: ").formatted(Formatting.GRAY).append(new LiteralText("Unknown").formatted(Formatting.DARK_GREEN)));
            tooltip.add(new LiteralText("Sex: ").formatted(Formatting.GRAY).append(new LiteralText("Unknown").formatted(Formatting.DARK_GREEN)));
        }
    }

    public static int durationToAmplifier(int duration) {
        if (duration <= 1200) {
            return 0;
        } else if (duration <= 1800) {
            return 1;
        } else if (duration <= 2400) {
            return 2;
        } else {
            return 3;
        }
    }

    private static void sendHighMessage(PlayerEntity player) {
        StatusEffectInstance currentEffect = player.getStatusEffect(ModMisc.HIGH);
        int amplifier = currentEffect.getAmplifier();
        switch (amplifier) {
            case 0:
                player.applyStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, currentEffect.getDuration(), 0, true, false));
                player.sendMessage(new LiteralText("The buzz has made you resistant to fire").formatted(Formatting.GREEN), true);
                break;
            case 1:
                player.applyStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, currentEffect.getDuration(), 0, true, false));
                player.sendMessage(new LiteralText("Why are your hands shaking").formatted(Formatting.GREEN), true);
                break;
            case 2:
                if (GeneticsManager.random().nextInt(2) == 0) {
                    player.applyStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, currentEffect.getDuration(), 0, true, false));
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

    public static void applyHigh(LivingEntity user, int index, int thc) {
        int duration;
        int switchNum = 0;
        ModMisc.PLAYER.get(user).setStrain(index);
        if (thc <= 18) switchNum = 1;
        if (19 <= thc && thc <= 25) switchNum = 2;
        if (26 <= thc) switchNum = 3;
        switch (switchNum) {
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
        if (user.hasStatusEffect(ModMisc.HIGH)) {
            StatusEffectInstance currentEffect = user.getStatusEffect(ModMisc.HIGH);

            switch (switchNum) {
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
        int amplifier = durationToAmplifier(duration);
        user.addStatusEffect(new StatusEffectInstance(ModMisc.HIGH, duration, amplifier));
        sendHighMessage((PlayerEntity) user);
    }

    @Deprecated
    public static ListTag toNbtList(ArrayList<Pair<Genes, Integer>> list) {
        ListTag ListTag = new ListTag();
        for (Pair<Genes, Integer> entry : list) {
            CompoundTag compound = new CompoundTag();
            compound.putString("Gene", entry.getLeft().getName());
            compound.putInt("Level", entry.getRight());
            ListTag.add(compound);
        }
        return ListTag;
    }

    @Deprecated
    public static ArrayList<Pair<Genes, Integer>> fromNbtList(ListTag list) {
        ArrayList<Pair<Genes, Integer>> arrayList = new ArrayList<>();
        for (Tag compoundEntry : list) {
            Genes gene = Genes.byName(((CompoundTag) compoundEntry).getString("Gene"));
            int level = ((CompoundTag) compoundEntry).getInt("Level");
            Pair<Genes, Integer> pair = new Pair<>(gene, level);
            arrayList.add(pair);
        }
        return arrayList;
    }

    public static void test() {
        ArrayList<Pair<Genes, Integer>> arrayList = new ArrayList<>();
        arrayList.add(new Pair<>(Genes.SPEED, 1));
        arrayList.add(new Pair<>(Genes.YIELD, 2));
        ListTag ListTag = toNbtList(arrayList);
        System.out.println(arrayList);
        System.out.println(ListTag);
        System.out.println(fromNbtList(ListTag));
    }

    public static ArrayList<Pair<Genes, Integer>> getTestArray() {
        ArrayList<Pair<Genes, Integer>> arrayList = new ArrayList<>();
        arrayList.add(new Pair<>(Genes.SPEED, 1));
        arrayList.add(new Pair<>(Genes.YIELD, 2));
        return arrayList;
    }

    public Pair<Genes, Integer> crossGenes(int level1, int level2, Genes type) {
        int levelDiff = Math.abs(level1 - level2);
        int newLevel = 0;
        Random random = new Random();

        switch (levelDiff) {
            case 0:
                newLevel = level1;
                break;
            case 1:
                int i = random.nextInt(2); // 0 - 1
                switch (i) {
                    case 0: // 50%
                        newLevel = Integer.min(level1, level2);
                        break;
                    case 1: // 50%
                        newLevel = Integer.max(level1, level2);
                        break;
                }
            case 2:
                i = random.nextInt(4); // 0 - 3
                if (i == 0) { // 0 25%
                    newLevel = Integer.min(level1, level2);
                    break;
                } else if (i <= 2) { // 1 or 2 50%
                    newLevel = Integer.sum(level1, level2) / 2;
                    break;
                } else { // 3 25%
                    newLevel = Integer.max(level1, level2);
                    break;
                }
            case 3: // 1: 0, 2: 3
                i = random.nextInt(4); // 4 cases
                if (i < 2) { // 0 or 1 50%
                    newLevel = Integer.min(level1, level2);
                    break;
                } else if (i == 2) { // 2 25%
                    newLevel = Integer.sum(level1, level2) / 2;
                    break;
                } else { // 3 25%
                    newLevel = Integer.max(level1, level2);
                    break;
                }
        }
        return new Pair<>(type, newLevel);
    }
}
