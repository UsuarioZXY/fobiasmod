package com.fobias.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Linterna: clic derecho para encenderla o apagarla. */
public class FlashlightItem extends Item {
    private static final String TAG_ON = "On";

    public FlashlightItem(Properties properties) {
        super(properties);
    }

    public static boolean isOn(ItemStack stack) {
        if (!(stack.getItem() instanceof FlashlightItem)) {
            return false;
        }
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(TAG_ON);
    }

    public static void setOn(ItemStack stack, boolean on) {
        stack.getOrCreateTag().putBoolean(TAG_ON, on);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean on = !isOn(stack);
        setOn(stack, on);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, 0.6F, on ? 1.4F : 1.0F);
        player.getCooldowns().addCooldown(this, 6);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /** Cambiar encendido/apagado no debe repetir la animacion de sacar el objeto. */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !oldStack.is(newStack.getItem());
    }
}
