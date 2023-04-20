package io.izzel.arclight.common.mixin.core.world.item.crafting;

import io.izzel.arclight.common.bridge.core.item.crafting.IRecipeBridge;
import io.izzel.arclight.common.mod.util.ArclightSpecialRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v.inventory.CraftRecipe;
import org.bukkit.craftbukkit.v.inventory.CraftSmithingRecipe;
import org.bukkit.craftbukkit.v.inventory.CraftSmithingTrimRecipe;
import org.bukkit.craftbukkit.v.util.CraftNamespacedKey;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SmithingTrimRecipe.class)
public class SmithingTrimRecipeMixin implements IRecipeBridge {

    // @formatter:off
    @Shadow @Final private ResourceLocation id;
    @Shadow @Final
    Ingredient base;
    @Shadow @Final Ingredient addition;
    // @formatter:on

    @Shadow @Final private Ingredient template;

    @Override
    public Recipe bridge$toBukkitRecipe() {
        return new CraftSmithingTrimRecipe(CraftNamespacedKey.fromMinecraft(this.id), CraftRecipe.toBukkit(this.template), CraftRecipe.toBukkit(this.base), CraftRecipe.toBukkit(this.addition));
    }
}
