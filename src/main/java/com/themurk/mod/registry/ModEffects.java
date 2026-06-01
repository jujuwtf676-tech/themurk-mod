package com.themurk.mod.registry;

import com.themurk.mod.TheMurkMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
        DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, TheMurkMod.MODID);

    // The Murk's Gaze — slowness debuff when being watched at a distance
    public static final RegistryObject<MobEffect> MURKS_GAZE =
        EFFECTS.register("murks_gaze", () ->
            new net.minecraft.world.effect.MobEffect(MobEffectCategory.HARMFUL, 0x1a0033) {
                // Custom effect — applied via MurkEntity AI, no tick behavior needed
            }
        );
}
