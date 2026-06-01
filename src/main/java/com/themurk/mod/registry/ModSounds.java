package com.themurk.mod.registry;

import com.themurk.mod.TheMurkMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TheMurkMod.MODID);

    public static final RegistryObject<SoundEvent> MURK_AMBIENT =
        SOUNDS.register("murk_ambient", () ->
            SoundEvent.createVariableRangeEvent(new ResourceLocation(TheMurkMod.MODID, "murk_ambient")));

    public static final RegistryObject<SoundEvent> MURK_ATTACK =
        SOUNDS.register("murk_attack", () ->
            SoundEvent.createVariableRangeEvent(new ResourceLocation(TheMurkMod.MODID, "murk_attack")));

    public static final RegistryObject<SoundEvent> MURK_FLEE =
        SOUNDS.register("murk_flee", () ->
            SoundEvent.createVariableRangeEvent(new ResourceLocation(TheMurkMod.MODID, "murk_flee")));

    public static final RegistryObject<SoundEvent> MURK_APPEAR =
        SOUNDS.register("murk_appear", () ->
            SoundEvent.createVariableRangeEvent(new ResourceLocation(TheMurkMod.MODID, "murk_appear")));
}
