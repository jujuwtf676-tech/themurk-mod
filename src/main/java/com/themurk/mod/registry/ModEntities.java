package com.themurk.mod.registry;

import com.themurk.mod.TheMurkMod;
import com.themurk.mod.entity.MurkEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TheMurkMod.MODID);

    public static final RegistryObject<EntityType<MurkEntity>> MURK =
        ENTITIES.register("murk", () ->
            EntityType.Builder.<MurkEntity>of(MurkEntity::new, MobCategory.MONSTER)
                .sized(1.0f, 2.0f)
                .clientTrackingRange(16)
                .updateInterval(3)
                .build("murk")
        );
}
