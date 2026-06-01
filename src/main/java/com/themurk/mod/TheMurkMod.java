package com.themurk.mod;

import com.themurk.mod.registry.ModEffects;
import com.themurk.mod.registry.ModEntities;
import com.themurk.mod.registry.ModItems;
import com.themurk.mod.registry.ModParticles;
import com.themurk.mod.registry.ModSounds;
import com.themurk.mod.event.MurkEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TheMurkMod.MODID)
public class TheMurkMod {

    public static final String MODID = "themurk";
    public static final Logger LOGGER = LogManager.getLogger();

    public TheMurkMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEffects.EFFECTS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModParticles.PARTICLE_TYPES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(new MurkEventHandler());
    }
}
