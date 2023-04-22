package io.izzel.arclight.common.mixin.core.server.commands;

import io.izzel.arclight.common.bridge.core.world.server.ServerWorldBridge;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SummonCommand.class)
public class SummonCommandMixin {

    @Inject(method = "spawnEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/SummonCommand;createEntity(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/core/Holder$Reference;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/nbt/CompoundTag;Z)Lnet/minecraft/world/entity/Entity;"))
    private static void arclight$summonReason(CommandSourceStack source, Holder.Reference<EntityType<?>> p_251948_, Vec3 p_251429_, CompoundTag p_250568_, boolean p_250229_, CallbackInfoReturnable<Integer> cir) {
        ((ServerWorldBridge) source.getLevel()).bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason.COMMAND);
    }
}
