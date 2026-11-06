package com.fobias.entity;

import com.fobias.world.FlashlightHandler;
import com.fobias.world.PurgatoryGame;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * El Sonriente.
 *
 * Fase 1 (observando): se queda inmóvil, mirandote fijamente con la cabeza ladeada.
 * Fase 2 (cazando): te persigue. Es invulnerable.
 *   - Linterna encendida y apuntandole: se frena y se tapa la cara.
 *   - Linterna apagada y agachado: te pierde a mas de 8 bloques.
 *   - Linterna apagada caminando: te oye a menos de 30 bloques.
 */
public class StalkerEntity extends Monster {
    public static final int STATE_WATCHING = 0;
    public static final int STATE_HUNTING = 1;
    public static final int STATE_STUNNED = 2;

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(StalkerEntity.class, EntityDataSerializers.INT);

    /** Ticks que se queda mirando antes de empezar a perseguir (25 s). */
    private static final int WATCH_TICKS = 20 * 25;

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
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STATE, STATE_WATCHING);
    }

    /** Estado visible para el cliente: lo usa el modelo para animarse. */
    public int getState() {
        return this.entityData.get(DATA_STATE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Brazos muy largos: alcanza a ~2,5 bloques.
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true) {
            @Override
            protected double getAttackReachSqr(LivingEntity target) {
                return 6.0D;
            }
        });
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 64.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false,
                target -> canSense(target)));
    }

    /** Tambien ataca en dificultad Pacifico. */
    @Override
    public boolean canAttack(LivingEntity target) {
        return target.canBeSeenAsEnemy();
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    /** Nunca desaparece por distancia ni por dificultad. */
    @Override
    public void checkDespawn() {
    }

    /** Detecta al jugador segun su luz y su sigilo. */
    private boolean canSense(LivingEntity target) {
        if (!hunting || !(target instanceof Player player)) {
            return false;
        }
        double distance = this.distanceTo(player);
        if (FlashlightHandler.isOn(player)) {
            return distance < 64.0D;
        }
        if (player.isCrouching()) {
            return distance < 8.0D;
        }
        return distance < 30.0D;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }

        // Si no hay ninguna partida que lo use (p. ej. tras reiniciar el servidor), desaparece.
        if (this.tickCount > 40 && !PurgatoryGame.isStalkerActive(this.getUUID())) {
            this.discard();
            return;
        }

        Player player = this.level().getNearestPlayer(this, 80.0D);

        if (!hunting && this.tickCount >= WATCH_TICKS) {
            hunting = true;
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.translatable("message.fobias.hunting"), true);
                serverPlayer.playNotifySound(SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 0.6F, 1.0F);
            }
        }

        if (this.getTarget() != null && !canSense(this.getTarget())) {
            this.setTarget(null);
        }

        boolean lit = hunting && player != null && FlashlightHandler.isOn(player) && isLitBy(player);
        int state = !hunting ? STATE_WATCHING : (lit ? STATE_STUNNED : STATE_HUNTING);
        if (state != getState()) {
            this.entityData.set(DATA_STATE, state);
        }

        double speed = lit ? SPEED_LIT : SPEED_HUNTING;
        if (!hunting) {
            speed = 0.0D;
            this.getNavigation().stop();
            if (player != null) {
                faceSlowly(player);
            }
        }
        AttributeInstance attribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null && attribute.getBaseValue() != speed) {
            attribute.setBaseValue(speed);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            float distance = this.distanceTo(player);
            if (!hunting) {
                if (this.tickCount % 160 == 0 && distance < 40.0F) {
                    serverPlayer.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSER, SoundSource.HOSTILE, 0.5F, 0.8F);
                }
            } else if (distance < 24.0F) {
                // Latidos que se aceleran cuando se acerca.
                int interval = distance < 8.0F ? 10 : (distance < 14.0F ? 18 : 30);
                if (this.tickCount % interval == 0) {
                    serverPlayer.playNotifySound(SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.0F, 1.0F);
                }
            }
        }
    }

    /** Gira el cuerpo hacia el jugador poco a poco: siempre te mira. */
    private void faceSlowly(Player player) {
        double dx = player.getX() - this.getX();
        double dz = player.getZ() - this.getZ();
        float target = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        float yaw = Mth.approachDegrees(this.getYRot(), target, 3.0F);
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    /** True si el jugador apunta hacia el acechador con la linterna. */
    private boolean isLitBy(Player player) {
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 toStalker = new Vec3(
                this.getX() - player.getX(),
                this.getEyeY() - player.getEyeY(),
                this.getZ() - player.getZ());
        double distance = toStalker.length();
        if (distance < 0.001D || distance > FlashlightHandler.RANGE) {
            return false;
        }
        return look.dot(toStalker.normalize()) > 0.93D && player.hasLineOfSight(this);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!hunting) {
            return false;
        }
        if (target instanceof ServerPlayer player && PurgatoryGame.isPlaying(player)) {
            PurgatoryGame.end(player, PurgatoryGame.Outcome.CAUGHT);
            return true;
        }
        return super.doHurtTarget(target);
    }
}
