package crazypants.enderio.paint.render;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.enderio.core.common.util.IBlockAccessWrapper;

import crazypants.enderio.paint.IPaintable;
import crazypants.util.FacadeUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class PaintedBlockAccessWrapper extends IBlockAccessWrapper {

  private static final ConcurrentHashMap<Block, Boolean> teBlackList = new ConcurrentHashMap<Block, Boolean>();

  private static final ThreadLocal<PaintedBlockAccessWrapper> factory = new ThreadLocal<PaintedBlockAccessWrapper>() {
	    @Override
	    protected PaintedBlockAccessWrapper initialValue() {
	      return new PaintedBlockAccessWrapper();
	    }
	  };

  public static PaintedBlockAccessWrapper instance(IBlockAccess ba) {
    return factory.get().setWorld(ba);
  }

  private PaintedBlockAccessWrapper() {
    super(null);
  }

  public PaintedBlockAccessWrapper setWorld(IBlockAccess ba) {
    wrapped = ba;
    return this;
  }

  public void free() {
    wrapped = null;
  }

  @Override
  public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
    IBlockState paintSource = getPaintSource(pos);
    if (paintSource != null) {
      return paintSource.getBlock().isSideSolid(paintSource, this, pos, side);
    }
    return super.isSideSolid(pos, side, _default);
  }

  @Override
  public TileEntity getTileEntity(BlockPos pos) {
    IBlockState paintSource = getPaintSource(pos);
    if (paintSource != null && paintSource != super.getBlockState(pos)) {
      return createTileEntity(paintSource, pos.toImmutable());
    }
    return super.getTileEntity(pos);
  }

  public TileEntity getRealTileEntity(BlockPos pos) {
    return wrapped.getTileEntity(pos);
  }

  @Override
  public IBlockState getBlockState(BlockPos pos) {
    IBlockState paintSource = getPaintSource(pos);
    if (paintSource != null) {
      return paintSource;
    }
    return super.getBlockState(pos);
  }

  @SuppressWarnings("null")
  private IBlockState getPaintSource(BlockPos pos) {
    IBlockState state = super.getBlockState(pos);
    if (state.getBlock() instanceof IPaintable.IBlockPaintableBlock) {
      return ((IPaintable.IBlockPaintableBlock) state.getBlock()).getPaintSource(state, wrapped, pos);
    }
    return FacadeUtil.instance.getFacade(state, wrapped, pos, null);
  }

  private final Map<Block, TileEntity> teCache = new HashMap<Block, TileEntity>();
  
  private TileEntity createTileEntity(IBlockState state, BlockPos pos) {
    Block block = state.getBlock();
    if (!block.hasTileEntity(state) || teBlackList.containsKey(block)) {
      return null;
    }
    if (teCache.containsKey(block)) {
      try {
        TileEntity tileEntity = teCache.get(block);
        tileEntity.setPos(pos);
        return tileEntity;
      } catch (Throwable t) {
        teCache.remove(block);
      }
    }
    try {
      TileEntity tileEntity = block.createTileEntity(null, state);
      tileEntity.setPos(pos);
      teCache.put(block, tileEntity);
      return tileEntity;
    } catch (Throwable t) {
      teBlackList.put(block, true);
    }
    return null;
  }

}