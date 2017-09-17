package crazypants.enderio.integration.immersiveengineering;

import crazypants.enderio.machine.farm.TileFarmStation;
import crazypants.enderio.machine.farm.farmers.CustomSeedFarmer;
import crazypants.enderio.machine.farm.farmers.IFarmerJoe;
import crazypants.enderio.machine.farm.farmers.IHarvestResult;
import crazypants.enderio.machine.farm.farmers.StemFarmer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class HempFarmerIE implements IFarmerJoe {

  public static HempFarmerIE create() {
    if (Block.REGISTRY.containsKey(new ResourceLocation("ImmersiveEngineering", "hemp"))) {
      Block hempBlock = Block.REGISTRY.getObject(new ResourceLocation("ImmersiveEngineering", "hemp"));
      Item hempSeedItem = Item.REGISTRY.getObject(new ResourceLocation("ImmersiveEngineering", "seed"));
      if (hempSeedItem != null) {
        ItemStack hempSeed = new ItemStack(hempSeedItem);
        return new HempFarmerIE(new CustomSeedFarmer(hempBlock, 4, hempSeed), new StemFarmer(hempBlock, hempSeed));
      }
    }
    return null;
  }

  private IFarmerJoe seedFarmer;
  private IFarmerJoe stemFarmer;

  public HempFarmerIE(IFarmerJoe seedFarmer, IFarmerJoe stemFarmer) {
    this.seedFarmer = seedFarmer;
    this.stemFarmer = stemFarmer;
  }

  @Override
  public boolean prepareBlock(TileFarmStation farm, BlockPos bc, Block block, IBlockState state) {
    return seedFarmer.prepareBlock(farm, bc, block, state);
  }

  @Override
  public boolean canHarvest(TileFarmStation farm, BlockPos bc, Block block, IBlockState state) {
    return seedFarmer.canHarvest(farm, bc, block, state);
  }

  @Override
  public boolean canPlant(ItemStack stack) {
    return seedFarmer.canPlant(stack);
  }

  @Override
  public IHarvestResult harvestBlock(TileFarmStation farm, BlockPos bc, Block block, IBlockState state) {
    return stemFarmer.harvestBlock(farm, bc, block, state);
  }

}
