package com.fobias.registry;

import com.fobias.FobiasMod;
import com.fobias.entity.StalkerEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FobiasMod.MODID);

    public static final RegistryObject<EntityType<StalkerEntity>> STALKER = ENTITY_TYPES.register("stalker",
            () -> EntityType.Builder.<StalkerEntity>of(StalkerEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 2.25F)
                    .clientTrackingRange(10)
                    .fireImmune()
                    .build(new ResourceLocation(FobiasMod.MODID, "stalker").toString()));

    private ModEntities() {
    }
}
