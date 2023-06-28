package io.github.projectet.ae2things;

import io.github.projectet.ae2things.client.AE2ThingsClient;
import io.github.projectet.ae2things.command.Command;
import io.github.projectet.ae2things.gui.cell.DISKItemCellGuiHandler;
import io.github.projectet.ae2things.item.AETItems;
import io.github.projectet.ae2things.storage.DISKCellHandler;
import io.github.projectet.ae2things.util.StorageManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.api.ids.AECreativeTabIds;
import appeng.api.storage.StorageCells;
import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;

@Mod(AE2Things.MOD_ID)
public class AE2Things {

    public static final String MOD_ID = "ae2things";

    public static StorageManager STORAGE_INSTANCE = new StorageManager();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public AE2Things() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);

        AETItems.init();

        modEventBus.addListener(AE2Things::commonSetup);
        modEventBus.addListener(AE2Things::addContentsToCreativeTab);

        MinecraftForge.EVENT_BUS.addListener(Command::commandRegister);
        MinecraftForge.EVENT_BUS.addListener(AE2Things::worldTick);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> AE2ThingsClient::init);
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        AETItems.commonSetup();

        StorageCells.addCellHandler(DISKCellHandler.INSTANCE);
        StorageCells.addCellGuiHandler(new DISKItemCellGuiHandler());

        event.enqueueWork(() -> {
            var disksText = "text.ae2things.disk_drives";

            for (var cell : AETItems.DISK_DRIVES) {
                Upgrades.add(AEItems.FUZZY_CARD, cell.get(), 1, disksText);
                Upgrades.add(AEItems.INVERTER_CARD, cell.get(), 1, disksText);
            }
        });
    }

    public static void addContentsToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(AECreativeTabIds.MAIN)) {
            return;
        }

        event.accept(AETItems.DISK_HOUSING);

        for (var cell : AETItems.DISK_DRIVES) {
            event.accept(cell);
        }
    }

    public static void worldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.side.isServer()) {
            STORAGE_INSTANCE = StorageManager.getInstance(event.level.getServer());
        }
    }
}
