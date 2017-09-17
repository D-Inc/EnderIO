package crazypants.enderio.block;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.BlockEnder;

import crazypants.enderio.BlockEio;
import crazypants.enderio.EnderIOTab;
import crazypants.enderio.IModObject;
import crazypants.enderio.machine.painter.blocks.BlockItemPaintedBlock;
import crazypants.enderio.machine.painter.blocks.TileEntityPaintedBlock;
import crazypants.enderio.paint.IPaintable;
import crazypants.enderio.paint.PainterUtil2;
import crazypants.enderio.paint.render.PaintHelper;
import crazypants.enderio.render.IBlockStateWrapper;
import crazypants.enderio.render.IHaveRenderers;
import crazypants.enderio.render.pipeline.BlockStateWrapperRelay;
import crazypants.enderio.render.registry.SmartModelAttacher;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDetector extends BlockEio<TileEntityPaintedBlock> implements IPaintable.ISolidBlockPaintableBlock, IHaveRenderers {

  public static final PropertyBool IS_ON = PropertyBool.create("on");
  public static final PropertyDirection FACING = PropertyDirection.create("facing");

  public static BlockDetector create(IModObject modobject) {
    BlockDetector result = new BlockDetector(modobject.getUnlocalisedName(), false);
    result.init();
    return result;
  }

  public static BlockDetector createSilent(IModObject modobject) {
    BlockDetector result = new BlockDetector(modobject.getUnlocalisedName(), true);
    result.init();
    return result;
  }

  private final boolean silent;

  protected BlockDetector(@Nonnull String name, boolean silent) {
    super(name, TileEntityPaintedBlock.class);
    this.silent = silent;
    setCreativeTab(EnderIOTab.tabEnderIOMachines);
    initDefaultState();
  }

  @Override
  protected void init() {
    super.init();
    SmartModelAttacher.registerNoProps(this);
  }

  protected void initDefaultState() {
    setDefaultState(this.blockState.getBaseState());
  }

  @Override
  protected BlockStateContainer createBlockState() {
    return new BlockStateContainer(this, new IProperty[] { IS_ON, FACING });
  }

  @Override
  public IBlockState getStateFromMeta(int meta) {
    return getDefaultState().withProperty(IS_ON, (meta & 0x08) != 0).withProperty(FACING, EnumFacing.values()[meta & 0x7]);
  }

  @Override
  public int getMetaFromState(IBlockState state) {
    return (state.getValue(IS_ON) ? 8 : 0) + state.getValue(FACING).ordinal();
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerRenderers() {
    Item item = Item.getItemFromBlock(this);
    Map<IBlockState, ModelResourceLocation> locations = new DefaultStateMapper().putStateModelLocations(this);
    IBlockState state = getDefaultState().withProperty(IS_ON, true).withProperty(FACING, EnumFacing.UP);
    ModelResourceLocation mrl = locations.get(state);
    ModelLoader.setCustomModelResourceLocation(item, 0, mrl);
  }

  @Override
  public IBlockState withRotation(IBlockState state, Rotation rot) {
    return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
  }

  @Override
  public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
    return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
  }

  @Override
  public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
    if (state != null && world != null && pos != null) {
      IBlockStateWrapper blockStateWrapper = new BlockStateWrapperRelay(state, world, pos);
      blockStateWrapper.bakeModel();
      return blockStateWrapper;
    } else {
      return state;
    }
  }

  @Override
  protected ItemBlock createItemBlock() {
    return new BlockItemPaintedBlock(this, getName());
  }

  @Override
  public boolean doNormalDrops(IBlockAccess world, BlockPos pos) {
    return false;
  }

  @Override
  protected void processDrop(IBlockAccess world, BlockPos pos, @Nullable TileEntityPaintedBlock te, ItemStack drop) {
    PainterUtil2.setSourceBlock(drop, getPaintSource(null, world, pos));
  }

  @Override
  public int getWeakPower(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
    return side.getOpposite() != state.getValue(FACING) && state.getValue(IS_ON) ? 15 : 0;
  }

  @Override
  public boolean canProvidePower(IBlockState state) {
    return true;
  }

  protected void playClickSound(World worldIn, BlockPos pos, IBlockState state) {
    if (state.getValue(IS_ON)) {
      worldIn.playSound((EntityPlayer) null, pos, SoundEvents.BLOCK_STONE_PRESSPLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
    } else {
      worldIn.playSound((EntityPlayer) null, pos, SoundEvents.BLOCK_STONE_PRESSPLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.5F);
    }
  }

  @Override
  public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn) {
    IBlockState newState = state.withProperty(IS_ON, isTargetBlockAir(state, world, pos));
    if (newState != state) {
      world.setBlockState(pos, newState);
      if (!silent) {
        playClickSound(world, pos, newState);
      }
    }
  }

  protected boolean isTargetBlockAir(IBlockState state, World world, BlockPos pos) {
    return world.isAirBlock(pos.offset(state.getValue(FACING)));
  }

  @Override
  public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
    final IBlockState state = super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(FACING, facing);
    return state.withProperty(IS_ON, isTargetBlockAir(state, worldIn, pos));
  }

  @Override
  public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
    setPaintSource(state, world, pos, PainterUtil2.getSourceBlock(stack));
    if (!world.isRemote) {
      world.notifyBlockUpdate(pos, state, state, 3);
    }
  }

  @Override
  public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
    final ItemStack pickBlock = super.getPickBlock(state, target, world, pos, player);
    PainterUtil2.setSourceBlock(pickBlock, getPaintSource(null, world, pos));
    return pickBlock;
  }

  @Override
  public void setPaintSource(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable IBlockState paintSource) {
    TileEntity te = world.getTileEntity(pos);
    if (te instanceof IPaintable.IPaintableTileEntity) {
      ((IPaintableTileEntity) te).setPaintSource(paintSource);
    }
  }

  @Override
  public void setPaintSource(Block block, ItemStack stack, @Nullable IBlockState paintSource) {
    PainterUtil2.setSourceBlock(stack, paintSource);
  }

  @Override
  public IBlockState getPaintSource(IBlockState state, IBlockAccess world, BlockPos pos) {
    @SuppressWarnings("null")
    TileEntity te = BlockEnder.getAnyTileEntitySafe(world, pos);
    if (te instanceof IPaintable.IPaintableTileEntity) {
      return ((IPaintableTileEntity) te).getPaintSource();
    }
    return null;
  }

  @Override
  public IBlockState getPaintSource(Block block, ItemStack stack) {
    return PainterUtil2.getSourceBlock(stack);
  }

  @SuppressWarnings("null")
  @Override
  public @Nonnull IBlockState getFacade(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side) {
    IBlockState paintSource = getPaintSource(getDefaultState(), world, pos);
    return paintSource != null ? paintSource : world.getBlockState(pos);
  }

  @Override
  public BlockRenderLayer getBlockLayer() {
    return BlockRenderLayer.SOLID;
  }

  @Override
  public boolean canRenderInLayer(BlockRenderLayer layer) {
    return true;
  }

  @Override
  public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
    return state.getValue(FACING) == side.getOpposite();
  }

  @SideOnly(Side.CLIENT)
  @Override
  public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager effectRenderer) {
    return PaintHelper.addHitEffects(state, world, target, effectRenderer);
  }

  @SideOnly(Side.CLIENT)
  @Override
  public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager effectRenderer) {
    return PaintHelper.addDestroyEffects(world, pos, effectRenderer);
  }

}
