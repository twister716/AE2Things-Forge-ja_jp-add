package io.github.projectet.ae2things.gui.crystalGrowth;

import io.github.projectet.ae2things.block.entity.BECrystalGrowth;
import io.github.projectet.ae2things.inventory.CrystalGrowthSlot;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.inventories.InternalInventory;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;

public class CrystalGrowthMenu extends UpgradeableMenu<BECrystalGrowth> {

    public static MenuType<CrystalGrowthMenu> CRYSTAL_GROWTH_SHT = MenuTypeBuilder
            .create(CrystalGrowthMenu::new, BECrystalGrowth.class).build("crystal_growth");

    private final InternalInventory inventory;

    public CrystalGrowthMenu(int id, Inventory ip, BECrystalGrowth crystalGrowth) {
        super(CRYSTAL_GROWTH_SHT, id, ip, crystalGrowth);
        inventory = crystalGrowth.getInternalInventory();
        for (int i = 0; i < inventory.size(); i++) {
            CrystalGrowthSlot slot = new CrystalGrowthSlot(inventory, i);
            this.addSlot(slot, SlotSemantics.STORAGE);
        }
    }
}
