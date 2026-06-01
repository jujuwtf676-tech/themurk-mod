package com.themurk.mod.entity;

import com.themurk.mod.registry.ModEffects;
import com.themurk.mod.registry.ModItems;
import com.themurk.mod.registry.ModParticles;
import com.themurk.mod.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;

import java.util.List;

public class MurkEntity extends Monster {

    // How many ticks the Murk has been observed by the player this cycle
    private int observedTicks = 0;
    // Cooldown before next scratch mark is placed
    private int scratchCooldown = 0;
    // Cooldown before checking approach behavior
    private int approachCheckCooldown = 0;
    // Whether the Murk is currently in attack mode
    private boolean isAttacking = false;

    private static final int OBSERVE_FREEZE_THRESHOLD = 5; // ticks before it freezes
    private static final double FLEE_DISTANCE = 6.0;       // blocks — triggers flee/attack
    private static final double FLEE_TELEPORT_DISTANCE = 40.0;
    private static final double AMBIENT_SOUND_RANGE = 20.0;
    private static final double TORCH_EXTINGUISH_RANGE = 8.0;

    public MurkEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setNoAi(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 40.0)
            .add(Attributes.MOVEMENT_SPEED, 0.22)   // Slow — it's a stalker
            .add(Attributes.ATTACK_DAMAGE, 3.0)      // Weak — the fear is the point
            .add(Attributes.FOLLOW_RANGE, 64.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0); // Can't be knocked back
    }

    @Override
    protected void registerGoals() {
        // The Murk does NOT use standard MeleeAttackGoal.
        // All behavior is controlled in tick() for precision.
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 16.0f));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            spawnAmbientParticles();
            return;
        }

        Player target = this.level().getNearestPlayer(this, 64.0);
        if (target == null) return;

        // ── Only active at night ──────────────────────────────────────────────
        if (this.level().isDay()) {
            // Burns in daylight like endermen
            if (this.level().canSeeSky(this.blockPosition())) {
                this.setSecondsOnFire(1);
            }
            return;
        }

        double distanceSq = this.distanceToSqr(target);

        // ── Freeze if player is looking at the Murk ──────────────────────────
        if (isPlayerLookingAtMe(target)) {
            observedTicks++;
            if (observedTicks >= OBSERVE_FREEZE_THRESHOLD) {
                this.getNavigation().stop();
                this.setDeltaMovement(Vec3.ZERO);
                // Apply oppressive slowness when stared at
                target.addEffect(new MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false));
            }
        } else {
            observedTicks = Math.max(0, observedTicks - 2);
            // Silently creep toward player when not observed
            if (distanceSq > FLEE_DISTANCE * FLEE_DISTANCE) {
                moveTowardPlayer(target);
            }
        }

        // ── Ambient sound ─────────────────────────────────────────────────────
        if (this.tickCount % 80 == 0 && distanceSq < AMBIENT_SOUND_RANGE * AMBIENT_SOUND_RANGE) {
            this.level().playSound(null, target.blockPosition(),
                ModSounds.MURK_AMBIENT.get(),
                net.minecraft.sounds.SoundSource.HOSTILE, 0.6f,
                0.8f + this.random.nextFloat() * 0.4f);
        }

        // ── Scratch marks on nearby trees ─────────────────────────────────────
        if (scratchCooldown > 0) {
            scratchCooldown--;
        } else {
            tryPlaceScratchMark();
            scratchCooldown = 200 + this.random.nextInt(200); // Every 10-20s
        }

        // ── Torch extinguishing ───────────────────────────────────────────────
        if (this.tickCount % 40 == 0) {
            tryExtinguishTorches();
        }

        // ── Animals flee ─────────────────────────────────────────────────────
        if (this.tickCount % 60 == 0) {
            scareAnimals();
        }

        // ── Approach reaction: flee or attack ─────────────────────────────────
        if (approachCheckCooldown > 0) {
            approachCheckCooldown--;
        }
        if (distanceSq < FLEE_DISTANCE * FLEE_DISTANCE && approachCheckCooldown == 0) {
            approachCheckCooldown = 100;
            if (this.random.nextFloat() < 0.75f) {
                // 75% — ATTACK
                initiateAttack(target);
            } else {
                // 25% — FLEE
                initiateFlee(target);
            }
        }

        // ── Apply gaze slow if within range but not adjacent ─────────────────
        if (distanceSq < 20 * 20 && distanceSq > FLEE_DISTANCE * FLEE_DISTANCE) {
            target.addEffect(new MobEffectInstance(
                ModEffects.MURKS_GAZE.get(), 60, 0, false, false));
        }
    }

    // ── Attack: deal damage + apply fear effects ──────────────────────────────
    private void initiateAttack(Player target) {
        this.level().playSound(null, this.blockPosition(),
            ModSounds.MURK_ATTACK.get(),
            net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 0.8f);

        target.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));

        // Fear effects
        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 1200, 1, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 1200, 2, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0, false, false));

        isAttacking = true;
        approachCheckCooldown = 200;
    }

    // ── Flee: teleport far away ────────────────────────────────────────────────
    private void initiateFlee(Player target) {
        this.level().playSound(null, this.blockPosition(),
            ModSounds.MURK_FLEE.get(),
            net.minecraft.sounds.SoundSource.HOSTILE, 0.8f, 1.2f);

        // Apply fear effects even on flee
        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 1200, 1, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 1200, 2, false, false));

        teleportAwayFrom(target);
    }

    // ── Teleport to a random location far from the player ────────────────────
    private void teleportAwayFrom(Player player) {
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double dist = FLEE_TELEPORT_DISTANCE + this.random.nextDouble() * 20;
            double tx = player.getX() + Math.cos(angle) * dist;
            double tz = player.getZ() + Math.sin(angle) * dist;
            BlockPos teleportPos = new BlockPos((int)tx, (int)player.getY(), (int)tz);

            // Find a valid Y
            while (teleportPos.getY() > this.level().getMinBuildHeight() &&
                   this.level().getBlockState(teleportPos).isAir() &&
                   this.level().getBlockState(teleportPos.below()).isAir()) {
                teleportPos = teleportPos.below();
            }

            if (!this.level().getBlockState(teleportPos).isAir()) {
                teleportPos = teleportPos.above();
                this.teleportTo(teleportPos.getX() + 0.5, teleportPos.getY(), teleportPos.getZ() + 0.5);
                return;
            }
        }
    }

    // ── Silently move toward player ────────────────────────────────────────────
    private void moveTowardPlayer(Player target) {
        this.getNavigation().moveTo(target, 0.8);
    }

    // ── Check if player's camera is looking at this entity ────────────────────
    private boolean isPlayerLookingAtMe(Player player) {
        Vec3 playerLook = player.getLookAngle().normalize();
        Vec3 toMurk = this.position().subtract(player.getEyePosition()).normalize();
        double dot = playerLook.dot(toMurk);
        double distanceSq = this.distanceToSqr(player);
        // The further away, the narrower the angle threshold
        double threshold = distanceSq < 100 ? 0.97 : 0.99;
        return dot > threshold;
    }

    // ── Scratch mark particles on nearby logs ─────────────────────────────────
    private void tryPlaceScratchMark() {
        BlockPos origin = this.blockPosition();
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                BlockPos pos = origin.offset(dx, 0, dz);
                if (isLog(pos)) {
                    // Spawn scratch particles on the face of the log
                   ((net.minecraft.server.level.ServerLevel) this.level()).sendParticles(
    ModParticles.MURK_SCRATCH.get(),
    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
    8, 0.3, 0.5, 0.3, 0.01);
                    return; // One per cycle is enough
                }
            }
        }
    }

    private boolean isLog(BlockPos pos) {
        net.minecraft.world.level.block.state.BlockState state = this.level().getBlockState(pos);
        return state.is(net.minecraft.tags.BlockTags.LOGS);
    }

    // ── Extinguish nearby torches ─────────────────────────────────────────────
    private void tryExtinguishTorches() {
        BlockPos origin = this.blockPosition();
        int range = (int) TORCH_EXTINGUISH_RANGE;
        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -2; dy <= 3; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    net.minecraft.world.level.block.state.BlockState state = this.level().getBlockState(pos);
                    if (state.is(net.minecraft.tags.BlockTags.WALL_POST_OVERRIDE) ||
                        state.getBlock() instanceof net.minecraft.world.level.block.TorchBlock ||
                        state.getBlock() instanceof net.minecraft.world.level.block.WallTorchBlock) {
                        this.level().removeBlock(pos, false);
                        return; // One torch per check
                    }
                }
            }
        }
    }

    // ── Make nearby passive animals flee ──────────────────────────────────────
    private void scareAnimals() {
        List<PathfinderMob> animals = this.level().getEntitiesOfClass(
            PathfinderMob.class,
            this.getBoundingBox().inflate(12.0),
            e -> e instanceof net.minecraft.world.entity.animal.Animal
        );
        for (PathfinderMob animal : animals) {
            Vec3 fleeDir = animal.position().subtract(this.position()).normalize();
            animal.setDeltaMovement(fleeDir.x * 0.5, 0.3, fleeDir.z * 0.5);
        }
    }

    // ── Client-side particle aura ─────────────────────────────────────────────
    private void spawnAmbientParticles() {
        for (int i = 0; i < 3; i++) {
            double ox = (this.random.nextDouble() - 0.5) * 1.0;
            double oy = this.random.nextDouble() * 2.2;
            double oz = (this.random.nextDouble() - 0.5) * 1.0;
            this.level().addParticle(ModParticles.MURK_SMOKE.get(),
                this.getX() + ox, this.getY() + oy, this.getZ() + oz,
                0, 0.02, 0);
        }
    }

    // ── Loot: drops Eye of the Murk with low chance ───────────────────────────
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        if (this.random.nextFloat() < 0.05f + looting * 0.02f) {
            this.spawnAtLocation(new ItemStack(ModItems.MURK_EYE.get()));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() { return ModSounds.MURK_AMBIENT.get(); }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return ModSounds.MURK_AMBIENT.get(); }

    @Override
    protected SoundEvent getDeathSound() { return ModSounds.MURK_FLEE.get(); }

    // ── The Murk cannot be seen in broad daylight — it despawns ──────────────
    @Override
    public boolean checkSpawnObstruction(net.minecraft.world.level.LevelReader level) {
        return super.checkSpawnObstruction(level);
    }

    @Override
    public boolean requiresCustomPersistence() { return false; }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        scratchCooldown = tag.getInt("scratchCooldown");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("scratchCooldown", scratchCooldown);
    }
}
