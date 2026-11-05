package com.fobias.entity;

import com.fobias.world.DarkForestGame;
import com.fobias.world.FlashlightHandler;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * El Acechador: al empezar se queda quieto observandote desde las sombras
 * y luego te persigue. Es invulnerable; la unica defensa es la linterna:
 * si lo apuntas con ella se frena casi por completo.
 */
public class StalkerEntity extends Monster {
    /** Ticks que se queda mirando antes de empezar a perseguir (25 s). */
    private static final int WATCH_TICKS = 20 * 25;

    private static final double SPEED_WATCHING = 0.0D;
    private static final double SPEED_LIT = 0.05D;
    private static final double SPEED_HUNTING = 0.30D;

    private boolean hunting = false;

    public StalkerEntity(EntityType<? extends StalkerEntity> type, Level level) {
        super(type, level);
        this.setInvulnerable(true);
        this.xpReward = 0;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, SPEED_HUNTING)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 64.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    /** Nunca desaparece por distancia ni por dificultad. */
    @Override
    public void checkDespawn() {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }

        if (!hunting && this.tickCount >= WATCH_TICKS) {
            hunting = true;
            Player nearby = this.level().getNearestPlayer(this, 80.0D);
            if (nearby instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.translatable("message.fobias.stalker_hunting"), true);
                serverPlayer.playNotifySound(SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 0.6F, 1.0F);
            }
        }

        Player player = this.level().getNearestPlayer(this, 80.0D);

        double speed;
        if (!hunting) {
            speed = SPEED_WATCHING;
        } else if (player != null && isLitBy(player)) {
            speed = SPEED_LIT;
        } else {
            speed = SPEED_HUNTING;
        }

        AttributeInstance attribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null && attribute.getBaseValue() != speed) {
            attribute.setBaseValue(speed);
        }
        if (speed == 0.0D) {
            this.getNavigation().stop();
        }

        // Latidos que se aceleran cuando se acerca.
        if (hunting && player instanceof ServerPlayer serverPlayer) {
            float distance = this.distanceTo(player);
            if (distance < 24.0F) {
                int interval = distance < 8.0F ? 10 : (distance < 14.0F ? 18 : 30);
                if (this.tickCount % interval == 0) {
                    serverPlayer.playNotifySound(SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.0F, 1.0F);
                }
            }
        }
    }

    /** True si el jugador sostiene la linterna y apunta hacia el acechador. */
    private boolean isLitBy(Player player) {
        if (!FlashlightHandler.isHolding(player)) {
            return false;
        }
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 toStalker = new Vec3(
                this.getX() - player.getX(),
                this.getEyeY() - player.getEyeY(),
                this.getZ() - player.getZ());
        double distance = toStalker.length();
        if (distance < 0.001D || distance > 18.0D) {
            return false;
        }
        return look.dot(toStalker.normalize()) > 0.93D && player.hasLineOfSight(this);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!hunting) {
            return false;
        }
        if (target instanceof ServerPlayer player && DarkForestGame.isPlaying(player)) {
            DarkForestGame.end(player, DarkForestGame.Outcome.CAUGHT);
            return true;
        }
        return super.doHurtTarget(target);
    }
}
