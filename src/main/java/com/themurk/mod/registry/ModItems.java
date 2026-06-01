package com.themurk.mod.registry;

import com.themurk.mod.TheMurkMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, TheMurkMod.MODID);

    // "Eye of the Murk" — rare lore drop
    public static final RegistryObject<Item> MURK_EYE =
        ITEMS.register("murk_eye", () ->
            new Item(new Item.Properties().stacksTo(1))
        );
}
