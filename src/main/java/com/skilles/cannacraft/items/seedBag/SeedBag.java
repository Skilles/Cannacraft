package com.skilles.cannacraft.items.seedBag;

import com.google.common.collect.Lists;
import com.skilles.cannacraft.blocks.ImplementedInventory;
import com.skilles.cannacraft.blocks.weedCrop.WeedCrop;
import com.skilles.cannacraft.items.WeedSeed;
import com.skilles.cannacraft.strain.Strain;
import com.skilles.cannacraft.util.StrainUtil;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class SeedBag extends Item {
    public SeedBag(Settings settings) {
        super(settings);
    }
    private static final TranslatableText NAME_DEACTIVATED = new TranslatableText("item.cannacraft.seed_bag");

    private static final List<ISorter> sorters = Lists.newArrayList();

    public static ISorter getSorter(int index) {
        return sorters.get(index % sorters.size());
    }

    public static int addSorter(ISorter sorter) {
        if (sorters.contains(sorter)) {
            return sorters.indexOf(sorter);
        } else {
            sorters.add(sorter);
            return sorters.size() - 1;
        }
    }

    public static int addSorter(Strain strain) {
        return addSorter(new StatSorter(strain));
    }

    public IContents getContents(ItemStack stack) {
        return SeedBagInventory.getInstance();
    }

    public boolean isActivated(ItemStack stack) {
        return true; //EnchantmentHelper.get(stack).containsKey(null);
    }

    public boolean incrementSorter(ItemStack stack, int delta) {
        if (this.isActivated(stack)) {
            IContents contents = this.getContents(stack);
            int newPos = contents.getSorterIndex() + delta;
            newPos = (newPos < 0) ? (newPos + sorters.size()) : (newPos % sorters.size());
            contents.setSorterIndex(newPos);
            return true;
        }
        return false;
    }

    @Override
    public ActionResult useOnBlock(@NotNull ItemUsageContext context) {
        IContents contents = this.getContents(context.getStack());
        if (this.isActivated(context.getStack())) {
            Hand hand = context.getHand();
            PlayerEntity player = context.getPlayer();
            if (hand == Hand.OFF_HAND) {
                // From off hand: interact with main to insert / extract seeds
                if (player != null && this.attemptExtractOrInsertSeed(player, contents)) {
                    return ActionResult.SUCCESS;
                }
            } else {
                // From main hand: interact with the world to plant the seed
                if (this.attemptPlantSeed(context.getWorld(), context.getBlockPos(), contents, player)) {
                    return ActionResult.SUCCESS;
                }
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public TypedActionResult<ItemStack> use(@NotNull World world, @NotNull PlayerEntity player, @NotNull Hand hand) {
        // From off hand: interact with main to insert / extract seeds
        if (hand == Hand.OFF_HAND) {
            ItemStack stack = player.getStackInHand(hand);
            IContents contents = this.getContents(stack);
            if (this.isActivated(stack) && this.attemptExtractOrInsertSeed(player, contents)) {
                return TypedActionResult.success(stack);
            }
        }
        // From main hand: world-interaction behaviour which is not handled in this method
        if (!world.isClient) {
            //This will call the createScreenHandlerFactory method from BlockWithEntity, which will return our blockEntity casted to
            //a namedScreenHandlerFactory. If your block class does not extend BlockWithEntity, it needs to implement createScreenHandlerFactory.
            NamedScreenHandlerFactory screenHandlerFactory = world.getBlockState(player.getBlockPos()).createScreenHandlerFactory(world, player.getBlockPos());
            NamedScreenHandlerFactory screenHandlerFactory1 = new NamedScreenHandlerFactory() {
                @Override
                public Text getDisplayName() {
                    return NAME_DEACTIVATED;
                }
                @NotNull
                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    ItemStack stack = player.getStackInHand(hand);
                    IContents contents = new SeedBagInventory();
                    return new SeedBagScreenHandler(syncId, inv, contents);
                }
            };
            //With this call the server will request the client to open the appropriate Screenhandler
            player.openHandledScreen(screenHandlerFactory1);
        }
        return TypedActionResult.pass(player.getStackInHand(hand));
    }

    protected boolean attemptPlantSeed(World world, BlockPos pos, IContents contents, @Nullable  PlayerEntity player) {
        return contents.getCount() > 0;
    }

    protected boolean attemptPlantOnCrops(WeedCrop crop, ItemStack seedStack, @Nullable  PlayerEntity player) {
        return true;
        //return AgriApi.getGenomeAdapterizer().valueOf(seedStack).map(seed -> crop.plantGenome(seed, player)).orElse(false);
    }

    protected boolean attemptPlantOnSoil(World world, BlockPos pos, ItemStack seedStack, @Nullable  PlayerEntity player) {
        BlockPos up = pos.up();
        return true;
    }

    protected boolean attemptExtractOrInsertSeed(PlayerEntity player, IContents contents) {
        ItemStack held = player.getStackInHand(Hand.MAIN_HAND);
        if (held.isEmpty()) {
            boolean last = player.isSneaking();
            if (last) {
                ItemStack out = contents.extractLastSeed(1, true);
                if (!out.isEmpty()) {
                    player.setStackInHand(Hand.MAIN_HAND, contents.extractLastSeed(1, false));
                    return true;
                }
            } else {
                ItemStack out =  contents.extractFirstSeed(1, true);
                if (!out.isEmpty()) {
                    player.setStackInHand(Hand.MAIN_HAND, contents.extractFirstSeed(1, false));
                    return true;
                }
            }
        } else if (held.getItem() instanceof WeedSeed) {
            ItemStack remaining = contents.insertSeed(held, true);
            if (remaining.isEmpty() || remaining.getCount() != held.getCount()) {
                player.setStackInHand(Hand.MAIN_HAND, contents.insertSeed(held, false));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return !this.isActivated(stack);
    }

    @Override
    public int getEnchantability() {
        return 1;
    }

    @NotNull
    @Override
    public Text getName(@NotNull ItemStack stack) {
        return this.isActivated(stack)
                ? super.getName(stack)
                : NAME_DEACTIVATED;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        // Overriding this method to leave a note:
        // We are handling the tooltip from an event handler to remove the enchantment tooltip
        super.appendTooltip(stack, world, tooltip, context);
    }

    public interface IContents extends ImplementedInventory {

        Strain getStrain();

        int getCount();

        @Override
        default int size() {
            return 27;
        }

        ISorter getSorter();

        int getSorterIndex();

        void setSorterIndex(int index);

        default ItemStack insertSeed(ItemStack stack, boolean simulate) {
            if (!simulate) {
                this.setStack(0, stack);
            }
            return this.getItems().get(0);
        }

        @Override
        DefaultedList<ItemStack> getItems();

        ItemStack extractFirstSeed(int amount, boolean simulate);

        ItemStack extractLastSeed(int amount, boolean simulate);
    }

    public interface ISorter extends Comparator<Strain> {
        String getName();
    }

    public record StatSorter(Strain strain) implements ISorter {

        public Strain getStrain() {
            return this.strain;
        }

        @Override
        public String getName() {
            return this.strain.name();
        }

        @Override
        public int compare(Strain o1, Strain o2) {
            int s1 = o1.hashCode();
            int s2 = o2.hashCode();
            if (s1 == s2) {
                return DEFAULT_SORTER.compare(o1, o2);
            }
            return s1 - s2;
        }
    }

    public static final ISorter DEFAULT_SORTER = new ISorter() {
        @Override
        public String getName() {
            return "";
        }

        @Override
        public int compare(Strain o1, Strain o2) {
            int s1 = o1.id();
            int s2 = o2.id();
            if (s1 != s2) {
                return s1 - s2;
            }
            return 0;
        }
    };

    private static final IContents EMPTY = new IContents() {
        @Override
        public Strain getStrain() {
            return StrainUtil.getStrain(0);
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public ISorter getSorter() {
            return DEFAULT_SORTER;
        }

        @Override
        public int getSorterIndex() {
            return 0;
        }

        @Override
        public void setSorterIndex(int index) {}

        @Override
        public ItemStack extractFirstSeed(int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractLastSeed(int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeStack(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public DefaultedList<ItemStack> getItems() {
            return ImplementedInventory.ofSize(size()).getItems();
        }

        @NotNull
        @Override
        public ItemStack getStack(int slot) {
            return ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack removeStack(int slot, int amount) {
            return ItemStack.EMPTY;
        }
    };

    static {
        addSorter(DEFAULT_SORTER);
    }
}
