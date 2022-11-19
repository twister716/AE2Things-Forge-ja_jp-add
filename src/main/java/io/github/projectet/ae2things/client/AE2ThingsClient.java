package io.github.projectet.ae2things.client;

import io.github.projectet.ae2things.gui.advancedInscriber.AdvancedInscriberMenu;
import io.github.projectet.ae2things.gui.advancedInscriber.AdvancedInscriberRootPanel;
import io.github.projectet.ae2things.item.AETItems;
import io.github.projectet.ae2things.item.DISKDrive;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;

public class AE2ThingsClient {
    public static void init() {
        var eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(AE2ThingsClient::registerItemColors);
        eventBus.addListener(AE2ThingsClient::commonSetup);
    }

    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(DISKDrive::getColor, AETItems.DISK_DRIVE_1K.get(),
                AETItems.DISK_DRIVE_4K.get(), AETItems.DISK_DRIVE_16K.get(), AETItems.DISK_DRIVE_64K.get(),
                AETItems.DISK_DRIVE_256K.get());
    }

    @SuppressWarnings("RedundantTypeArguments") // sorry intellij, but javac cannot deduce them!
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.<AdvancedInscriberMenu, AdvancedInscriberRootPanel>register(
                    AdvancedInscriberMenu.ADVANCED_INSCRIBER_SHT, (menu, playerInv, title) -> {
                        ScreenStyle style;
                        try {
                            style = StyleManager.loadStyleDoc("/screens/advanced_inscriber.json");
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to read Screen JSON file", e);
                        }

                        return new AdvancedInscriberRootPanel(menu, playerInv, title, style);
                    });
        });
    }
}
