package crazypants.enderio.integration.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.enderio.core.common.util.FluidUtil;

import crazypants.enderio.fluid.Buckets;
import crazypants.enderio.fluid.Fluids;
import crazypants.enderio.material.Material;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import static crazypants.enderio.ModObject.blockTank;
import static crazypants.enderio.ModObject.itemMaterial;

@JEIPlugin
public class JeiPlugin extends BlankModPlugin {

  private static IJeiRuntime jeiRuntime = null;

  @Override
  public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
    DarkSteelUpgradeRecipeCategory.registerSubtypes(subtypeRegistry);
  }

  @Override
  public void register(@Nonnull IModRegistry registry) {

    IJeiHelpers jeiHelpers = registry.getJeiHelpers();
    IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
        
    AlloyRecipeCategory.register(registry,guiHelper);
    SagMillRecipeCategory.register(registry,guiHelper);
    EnchanterRecipeCategory.register(registry,guiHelper);
    SliceAndSpliceRecipeCategory.register(registry,guiHelper);
    SoulBinderRecipeCategory.register(registry, guiHelper);
    PainterRecipeCategory.register(registry, jeiHelpers);
    VatRecipeCategory.register(registry, guiHelper);
    DarkSteelUpgradeRecipeCategory.register(registry, guiHelper);
    TankRecipeCategory.register(registry, guiHelper);
    CombustionRecipeCategory.register(registry, guiHelper);
    CrafterRecipeTransferHandler.register(registry);
    InventoryPanelRecipeTransferHandler.register(registry);

    registry.addAdvancedGuiHandlers(new AdvancedGuiHandlerEnderIO());
    
    //Add a couple of example recipes for the nut.dist stick as the custom recipe isn't picked up
    List<ItemStack> inputs = new ArrayList<ItemStack>();
    inputs.add(new ItemStack(Items.STICK));
    inputs.add(Buckets.itemBucketNutrientDistillation.copy());
    ShapelessRecipes res = new ShapelessRecipes(new ItemStack(itemMaterial.getItem(), 1, Material.NUTRITIOUS_STICK.ordinal()), inputs);
    registry.addRecipes(Collections.singletonList(res));
    
    ItemStack tank = new ItemStack(blockTank.getBlock());
    IFluidHandler cap = FluidUtil.getFluidHandlerCapability(tank);
    cap.fill(new FluidStack(Fluids.fluidNutrientDistillation,  8 * Fluid.BUCKET_VOLUME), true);
    inputs = new ArrayList<ItemStack>();
    inputs.add(new ItemStack(Items.STICK));
    inputs.add(tank);
    res = new ShapelessRecipes(new ItemStack(itemMaterial.getItem(), 1, Material.NUTRITIOUS_STICK.ordinal()), inputs);
    registry.addRecipes(Collections.singletonList(res));
    
  }

  @Override
  public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
    JeiPlugin.jeiRuntime = jeiRuntime;
    JeiAccessor.jeiRuntimeAvailable = true;
  }

  public static void setFilterText(@Nonnull String filterText) {
    jeiRuntime.getItemListOverlay().setFilterText(filterText);
  }

  public static @Nonnull String getFilterText() {
    return jeiRuntime.getItemListOverlay().getFilterText();
  }

}
