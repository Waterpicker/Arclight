package io.izzel.arclight.common.mixin.core.world.entity.monster;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.entity.MobEntityBridge;
import io.izzel.arclight.common.bridge.core.world.WorldBridge;
import io.izzel.arclight.common.mixin.core.world.entity.PathfinderMobMixin;
import io.izzel.arclight.mixin.Eject;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.entity.living.ZombieEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(net.minecraft.world.entity.monster.Zombie.class)
public abstract class ZombieMixin extends PathfinderMobMixin {

    @Inject(method = "convertToZombieType", at = @At("HEAD"))
    private void arclight$transformReason(EntityType<? extends net.minecraft.world.entity.monster.Zombie> entityType, CallbackInfo ci) {
        this.bridge$pushTransformReason(EntityTransformEvent.TransformReason.DROWNED);
        ((WorldBridge) this.level).bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason.DROWNED);
    }

    @Inject(method = "convertToZombieType", locals = LocalCapture.CAPTURE_FAILHARD, at = @At("RETURN"))
    private void arclight$stopConversion(EntityType<? extends net.minecraft.world.entity.monster.Zombie> entityType, CallbackInfo ci, net.minecraft.world.entity.monster.Zombie zombieEntity) {
        if (zombieEntity == null) {
            ((Zombie) this.bridge$getBukkitEntity()).setConversionTime(-1);
        }
    }

//    @Inject(method = "hurt", locals = LocalCapture.PRINT, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Zombie;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/entity/SpawnGroupData;")) TODO: Untangle this mess
//    private void arclight$spawnWithReason(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, ServerLevel world, LivingEntity livingEntity, int i, int j, int k, ZombieEvent.SummonAidEvent event, net.minecraft.world.entity.monster.Zombie zombieEntity) {
//        ((WorldBridge) world).bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason.REINFORCEMENTS);
//        if (livingEntity != null) {
//            ((MobEntityBridge) zombieEntity).bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.REINFORCEMENT_TARGET, true);
//        }
//    }

    @Redirect(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setSecondsOnFire(I)V"))
    private void arclight$entityCombust(Entity entity, int seconds) {
        EntityCombustByEntityEvent event = new EntityCombustByEntityEvent(this.getBukkitEntity(), ((EntityBridge) entity).bridge$getBukkitEntity(), seconds);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            ((EntityBridge) entity).bridge$setOnFire(event.getDuration(), false);
        }
    }

    @Eject(method = "wasKilled", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;convertTo(Lnet/minecraft/world/entity/EntityType;Z)Lnet/minecraft/world/entity/Mob;"))
    private <T extends Mob> T arclight$transform(Villager villagerEntity, EntityType<T> entityType, boolean flag, CallbackInfoReturnable<Boolean> cir) {
        ((WorldBridge) villagerEntity.level).bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason.INFECTION);
        ((MobEntityBridge) villagerEntity).bridge$pushTransformReason(EntityTransformEvent.TransformReason.INFECTION);
        T t = villagerEntity.convertTo(entityType, flag);
        if (t == null) {
            cir.setReturnValue(false);
        }
        return t;
    }

    @Inject(method = "finalizeSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerLevelAccessor;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void arclight$mount(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, SpawnGroupData spawnDataIn, CompoundTag dataTag, CallbackInfoReturnable<SpawnGroupData> cir) {
        ((WorldBridge) worldIn.getLevel()).bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason.MOUNT);
    }

    private static ZombieVillager zombifyVillager(ServerLevel level, Villager villager, BlockPos blockPosition, boolean silent, CreatureSpawnEvent.SpawnReason spawnReason) {
        ((WorldBridge) villager.level).bridge$pushAddEntityReason(spawnReason);
        ((MobEntityBridge) villager).bridge$pushTransformReason(EntityTransformEvent.TransformReason.INFECTION);
        ZombieVillager zombieVillager = villager.convertTo(EntityType.ZOMBIE_VILLAGER, false);
        if (zombieVillager != null) {
            zombieVillager.finalizeSpawn(level, level.getCurrentDifficultyAt(zombieVillager.blockPosition()), MobSpawnType.CONVERSION, new net.minecraft.world.entity.monster.Zombie.ZombieGroupData(false, true), null);
            zombieVillager.setVillagerData(villager.getVillagerData());
            zombieVillager.setGossips(villager.getGossips().store(NbtOps.INSTANCE));
            zombieVillager.setTradeOffers(villager.getOffers().createTag());
            zombieVillager.setVillagerXp(villager.getVillagerXp());
            net.minecraftforge.event.ForgeEventFactory.onLivingConvert(villager, zombieVillager);
            if (!silent) {
                level.levelEvent(null, 1026, blockPosition, 0);
            }
        }
        return zombieVillager;
    }
}

