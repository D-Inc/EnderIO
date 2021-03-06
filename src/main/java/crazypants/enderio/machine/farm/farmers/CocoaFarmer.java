package crazypants.enderio.machine.farm.farmers;

import crazypants.enderio.config.Config;
import crazypants.enderio.machine.farm.TileFarmStation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.minecraft.block.BlockHorizontal.FACING;

public class CocoaFarmer extends CustomSeedFarmer {
  public CocoaFarmer() {
    super(Blocks.COCOA, new ItemStack(Items.DYE, 1, 3));
    this.requiresFarmland = false;
    if (!Config.farmHarvestJungleWhenCocoa) {
      this.disableTreeFarm = true;
    }
  }

  @Override
  public boolean canHarvest(TileFarmStation farm, BlockPos bc, Block block, IBlockState meta) {
    return block == getPlantedBlock() && meta.getValue(BlockCocoa.AGE) == 2;
  }

  @Override
  protected boolean plant(TileFarmStation farm, World worldObj, BlockPos bc) {
    EnumFacing dir = getPlantDirection(worldObj, bc);
    if (dir == null) {
      return false;
    }
    IBlockState iBlockState = getPlantedBlock().getDefaultState().withProperty(FACING, dir);
    if (worldObj.setBlockState(bc, iBlockState, 1 | 2)) {
      farm.actionPerformed(false);
      return true;
    }
    return false;
  }

  @Override
  protected boolean canPlant(TileFarmStation farm, World worldObj, BlockPos bc) {
    return getPlantDirection(worldObj, bc) != null;
  }

  private EnumFacing getPlantDirection(World worldObj, BlockPos bc) {
    if (!worldObj.isAirBlock(bc)) {
      return null;
    }

    for (EnumFacing dir : EnumFacing.HORIZONTALS) {
      BlockPos p = bc.offset(dir);
      if (validBlock(worldObj.getBlockState(p)))
        return dir;
    }

    return null;
  }

  private boolean validBlock(IBlockState iblockstate) {
    return iblockstate.getBlock() == Blocks.LOG && iblockstate.getValue(BlockOldLog.VARIANT) == BlockPlanks.EnumType.JUNGLE;
  }
}
