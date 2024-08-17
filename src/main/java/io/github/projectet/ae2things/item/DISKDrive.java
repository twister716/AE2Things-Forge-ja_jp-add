package io.github.projectet.ae2things.item;

import static appeng.api.storage.StorageCells.getCellInventory;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.storage.DISKCellInventory;
import io.github.projectet.ae2things.storage.IDISKCellItem;
import io.github.projectet.ae2things.util.DataStorage;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.cells.CellState;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.hooks.AEToolItem;
import appeng.items.contents.CellConfig;
import appeng.util.ConfigInventory;
import appeng.util.InteractionUtil;

public class DISKDrive extends Item implements IDISKCellItem, AEToolItem {

    private final int bytes;
    private final double idleDrain;
    private final ItemLike coreItem;

    public DISKDrive(ItemLike coreItem, int kilobytes, double idleDrain) {
        super(new Properties().stacksTo(1).fireResistant().component(AE2Things.DATA_DISK_ITEM_COUNT, 0L)
                .component(AE2Things.DATA_FUZZY_MODE, FuzzyMode.IGNORE_ALL));
        this.bytes = kilobytes * 1000;
        this.coreItem = coreItem;
        this.idleDrain = idleDrain;
    }

    @Override
    public AEKeyType getKeyType() {
        return AEKeyType.items();
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return bytes;
    }

    @Override
    public double getIdleDrain() {
        return idleDrain;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return CellConfig.create(Set.of(getKeyType()), is);
    }

    @Override
    public ItemStack clone(ItemStack item) {
        var diskId = item.get(AE2Things.DATA_DISK_ID);
        if (diskId != null) {
            UUID id = UUID.randomUUID();
            ItemStack newStack = item.copy();
            newStack.set(AE2Things.DATA_DISK_ID, id);
            newStack.setCount(newStack.getMaxStackSize());

            // Clone the disk if we can (this does not work in MP)
            var storageManager = AE2Things.currentStorageManager();
            if (storageManager != null) {
                DataStorage storage = storageManager.getOrCreateDisk(diskId);
                newStack.set(AE2Things.DATA_DISK_ITEM_COUNT, storage.itemCount);
                storageManager.updateDisk(id, storage);
            } else {
                newStack.remove(AE2Things.DATA_DISK_ITEM_COUNT);
            }

            return newStack;
        } else {
            return item.copy();
        }
    }

    @Override
    public FuzzyMode getFuzzyMode(final ItemStack is) {
        return is.getOrDefault(AE2Things.DATA_FUZZY_MODE, FuzzyMode.IGNORE_ALL);
    }

    @Override
    public void setFuzzyMode(final ItemStack is, final FuzzyMode fzMode) {
        is.set(AE2Things.DATA_FUZZY_MODE, fzMode);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        this.disassembleDrive(player.getItemInHand(hand), level, player);

        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                player.getItemInHand(hand));
    }

    @Nullable
    @Override
    public IUpgradeInventory getUpgrades(ItemStack is) {
        return UpgradeInventories.forItem(is, 2);
    }

    private boolean disassembleDrive(final ItemStack stack, final Level level, final Player player) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            if (level.isClientSide()) {
                return false;
            }

            final Inventory playerInventory = player.getInventory();
            var inv = getCellInventory(stack, null);
            if (inv != null && playerInventory.getSelected() == stack) {
                var list = inv.getAvailableStacks();
                if (list.isEmpty()) {
                    playerInventory.setItem(playerInventory.selected, ItemStack.EMPTY);

                    // drop core
                    playerInventory.placeItemBackInInventory(new ItemStack(coreItem));

                    // drop upgrades
                    for (ItemStack upgrade : this.getUpgrades(stack)) {
                        playerInventory.placeItemBackInInventory(upgrade);
                    }

                    // drop empty storage cell case
                    playerInventory.placeItemBackInInventory(new ItemStack(AETItems.DISK_HOUSING.get()));

                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return this.disassembleDrive(stack, context.getLevel(), context.getPlayer())
                ? InteractionResult.sidedSuccess(context.getLevel().isClientSide())
                : InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
            TooltipFlag tooltipFlag) {
        tooltip.add(Component.literal("Deep Item Storage disK - Storage for dummies")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        addCellInformationToTooltip(stack, tooltip);
    }

    public static int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 1) {
            // Create the inventory with an explicit null storage manager since we only need read-only data
            // And do not want to reach into the server-side storage manager.
            var cellInv = DISKCellInventory.createInventory(stack, null, null);
            var cellStatus = cellInv != null ? cellInv.getClientStatus() : CellState.EMPTY;
            return 0xFF000000 | cellStatus.getStateColor();
        } else {
            // White
            return 0xFFFFFFFF;
        }
    }
}
