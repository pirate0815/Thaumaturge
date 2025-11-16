package dev.overgrown.thaumaturge.registry;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.screen.AlchemicalFurnaceScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;

public class ModScreens {

    public static final ScreenHandlerType<AlchemicalFurnaceScreenHandler> ALCHEMICAL_FURNACE_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Thaumaturge.identifier("alchemical_furnace"), new ScreenHandlerType<>(AlchemicalFurnaceScreenHandler::new, FeatureSet.empty()));

    public static void initialise() {

    }
}
