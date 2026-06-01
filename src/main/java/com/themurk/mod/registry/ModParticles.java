package com.themurk.mod.registry;

import com.themurk.mod.TheMurkMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
        DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, TheMurkMod.MODID);

    // Dark smoke cloud — emitted by the Murk constantly
    public static final RegistryObject<SimpleParticleType> MURK_SMOKE =
        PARTICLE_TYPES.register("murk_smoke", () -> new SimpleParticleType(false));

    // Scratch mark particle — placed on nearby trees
    public static final RegistryObject<SimpleParticleType> MURK_SCRATCH =
        PARTICLE_TYPES.register("murk_scratch", () -> new SimpleParticleType(false));
}
