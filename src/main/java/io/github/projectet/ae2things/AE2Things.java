package io.github.projectet.ae2things;

import java.util.Objects;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import io.github.projectet.ae2things.block.BlockAdvancedInscriber;
import io.github.projectet.ae2things.block.entity.BEAdvancedInscriber;
import io.github.projectet.ae2things.client.AE2ThingsClient;
import io.github.projectet.ae2things.command.Command;
import io.github.projectet.ae2things.gui.advancedInscriber.AdvancedInscriberMenu;
import io.github.projectet.ae2things.gui.cell.DISKItemCellGuiHandler;
import io.github.projectet.ae2things.item.AETItems;
import io.github.projectet.ae2things.storage.DISKCellHandler;
import io.github.projectet.ae2things.util.StorageManager;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import appeng.api.storage.StorageCells;
import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;

@Mod(AE2Things.MOD_ID)
public class AE2Things {

    public static final String MOD_ID = "ae2things";

    public static StorageManager STORAGE_INSTANCE = new StorageManager();

    public static final Supplier<CreativeModeTab> ITEM_GROUP = Suppliers
            .memoize(() -> new CreativeModeTab("ae2things.item_group") {
                @Override
                public ItemStack makeIcon() {
                    return new ItemStack(AETItems.DISK_HOUSING.get());
                }
            });

    // @formatter:off
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registry.BLOCK_REGISTRY, MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
            .create(Registry.BLOCK_ENTITY_TYPE_REGISTRY, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registry.ITEM_REGISTRY, MOD_ID);

    public static final RegistryObject<BlockAdvancedInscriber> ADVANCED_INSCRIBER = BLOCKS.register(
            "advanced_inscriber",
            () -> new BlockAdvancedInscriber(BlockBehaviour.Properties.of(Material.METAL).destroyTime(4f)));

    public static final RegistryObject<BlockEntityType<BEAdvancedInscriber>> ADVANCED_INSCRIBER_BE = BLOCK_ENTITIES
            .register("advanced_inscriber_be",
                    () -> BlockEntityType.Builder.of(BEAdvancedInscriber::new, ADVANCED_INSCRIBER.get()).build(null));

    public static final RegistryObject<Item> ADVANCED_INSCRIBER_ITEM = createBlockItem(ADVANCED_INSCRIBER);
    // @formatter:on

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    private static RegistryObject<Item> createBlockItem(RegistryObject<? extends Block> block) {
        return ITEMS.register(block.getId().getPath(),
                () -> new BlockItem(block.get(), new Item.Properties().tab(ITEM_GROUP.get())));
    }

    public AE2Things() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ITEMS.register(modEventBus);

        AETItems.init();

        modEventBus.addListener(AE2Things::registerMenus);
        modEventBus.addListener(AE2Things::commonSetup);

        MinecraftForge.EVENT_BUS.addListener(Command::commandRegister);
        MinecraftForge.EVENT_BUS.addListener(AE2Things::worldTick);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> AE2ThingsClient::init);
    }

    public static void registerMenus(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registry.MENU_REGISTRY)) {
            IForgeRegistry<MenuType<?>> registry = Objects.requireNonNull(event.getForgeRegistry());
            registry.register(id("advanced_inscriber"), AdvancedInscriberMenu.ADVANCED_INSCRIBER_SHT);
        }
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        AETItems.commonSetup();

        StorageCells.addCellHandler(DISKCellHandler.INSTANCE);
        StorageCells.addCellGuiHandler(new DISKItemCellGuiHandler());

        Upgrades.add(AEItems.SPEED_CARD, ADVANCED_INSCRIBER.get(), 5);

        ADVANCED_INSCRIBER.get().setBlockEntity(BEAdvancedInscriber.class, ADVANCED_INSCRIBER_BE.get(), null, null);
    }

    public static void worldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.side.isServer()) {
            STORAGE_INSTANCE = StorageManager.getInstance(event.level.getServer());
        }
    }
}
