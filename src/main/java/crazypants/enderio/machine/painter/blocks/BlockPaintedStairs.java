package crazypants.enderio.machine.painter.blocks;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.BlockEnder;

import crazypants.enderio.ModObject;
import crazypants.enderio.machine.MachineRecipeRegistry;
import crazypants.enderio.machine.painter.recipe.BasicPainterTemplate;
import crazypants.enderio.paint.IPaintable;
import crazypants.enderio.paint.PainterUtil2;
import crazypants.enderio.paint.render.PaintHelper;
import crazypants.enderio.paint.render.PaintRegistry;
import crazypants.enderio.paint.render.UVLock;
import crazypants.enderio.render.IBlockStateWrapper;
import crazypants.enderio.render.ICacheKey;
import crazypants.enderio.render.IRenderMapper;
import crazypants.enderio.render.ISmartRenderAwareBlock;
import crazypants.enderio.render.dummy.BlockMachineBase;
import crazypants.enderio.render.pipeline.BlockStateWrapperBase;
import crazypants.enderio.render.property.EnumRenderPart;
import crazypants.enderio.render.property.IOMode.EnumIOMode;
import crazypants.enderio.render.registry.SmartModelAttacher;
import crazypants.enderio.render.util.QuadCollector;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockPaintedStairs extends BlockStairs implements ITileEntityProvider, IPaintable.ITexturePaintableBlock, ISmartRenderAwareBlock,
    IRenderMapper.IBlockRenderMapper.IRenderLayerAware, IRenderMapper.IItemRenderMapper.IItemModelMapper {

  public static BlockPaintedStairs create() {
    BlockPaintedStairs woodStairs = new BlockPaintedStairs(Blocks.OAK_STAIRS, ModObject.blockPaintedStair.getUnlocalisedName());
    woodStairs.init();
    MachineRecipeRegistry.instance.registerRecipe(ModObject.blockPainter.getUnlocalisedName(), new BasicPainterTemplate<BlockPaintedStairs>(woodStairs,
        Blocks.OAK_STAIRS, Blocks.ACACIA_STAIRS, Blocks.SPRUCE_STAIRS, Blocks.BIRCH_STAIRS, Blocks.JUNGLE_STAIRS, Blocks.DARK_OAK_STAIRS));

    return woodStairs;
  }

  public static BlockPaintedStairs create_stone() {
    BlockPaintedStairs stoneStairs = new BlockPaintedStairs(Blocks.STONE_STAIRS, ModObject.blockPaintedStoneStair.getUnlocalisedName());
    stoneStairs.init();
    MachineRecipeRegistry.instance.registerRecipe(ModObject.blockPainter.getUnlocalisedName(), new BasicPainterTemplate<BlockPaintedStairs>(stoneStairs,
        Blocks.STONE_STAIRS, Blocks.BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS, Blocks.NETHER_BRICK_STAIRS, Blocks.SANDSTONE_STAIRS, Blocks.QUARTZ_STAIRS,
        Blocks.RED_SANDSTONE_STAIRS));

    return stoneStairs;
  }

  private final String name;

  public BlockPaintedStairs(Block model, String name) {
    super(model.getDefaultState());
    if (!(model instanceof BlockStairs)) {
      throw new RuntimeException();
    }
    this.setCreativeTab(null);
    this.name = name;
    setUnlocalizedName(name);
    setRegistryName(name);
  }

  private void init() {
    GameRegistry.register(this);
    GameRegistry.register(new BlockItemPaintedBlock(this, name));
    SmartModelAttacher.registerNoProps(this);
    PaintRegistry.registerModel("stairs", new ResourceLocation("minecraft", "block/oak_stairs"), PaintRegistry.PaintMode.ALL_TEXTURES);
    PaintRegistry.registerModel("outer_stairs", new ResourceLocation("minecraft", "block/oak_outer_stairs"), PaintRegistry.PaintMode.ALL_TEXTURES);
    PaintRegistry.registerModel("inner_stairs", new ResourceLocation("minecraft", "block/oak_inner_stairs"), PaintRegistry.PaintMode.ALL_TEXTURES);
  }

  @Override
  public TileEntity createNewTileEntity(World world, int metadata) {
    return new TileEntityPaintedBlock();
  }

  @Override
  public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
    setPaintSource(state, world, pos, PainterUtil2.getSourceBlock(stack));
    if (!world.isRemote) {
      world.notifyBlockUpdate(pos, state, state, 3);      
    }
  }

  @Override
  public boolean removedByPlayer(IBlockState bs, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
    if (willHarvest) {
      return true;
    }
    return super.removedByPlayer(bs, world, pos, player, willHarvest);
  }

  @Override
  public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, @Nullable ItemStack stack) {    
    super.harvestBlock(worldIn, player, pos, state, te, stack);
    super.removedByPlayer(state, worldIn, pos, player, true);
  }

  @Override
  public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    List<ItemStack> drops = super.getDrops(world, pos, state, fortune);
    for (ItemStack drop : drops) {
      PainterUtil2.setSourceBlock(drop, getPaintSource(state, world, pos));
    }
    return drops;
  }

  @Override
  public ItemStack getPickBlock(IBlockState bs, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
    final ItemStack pickBlock = super.getPickBlock(bs, target, world, pos, player);
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

  @Override
  public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
    if (state != null && world != null && pos != null) {
      IBlockStateWrapper blockStateWrapper = new BlockStateWrapperBase(state, world, pos, this);
      blockStateWrapper.addCacheKey(getPaintSource(state, world, pos)).addCacheKey(state.getValue(HALF)).addCacheKey(state.getValue(FACING))
          .addCacheKey(state.getValue(SHAPE));
      blockStateWrapper.bakeModel();
      return blockStateWrapper;
    } else {
      return state;
    }
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IItemRenderMapper getItemRenderMapper() {
    return this;
  }
  
  @SideOnly(Side.CLIENT)
  private IBakedModel mapRender(IBlockState state, @Nullable IBlockState paint) {

    final boolean top = state.getValue(HALF) == EnumHalf.TOP;
    int xRot = top ? 180 : 0;
    int yRot = top ? 90 : 0;

    switch (state.getValue(FACING)) {
    case EAST:
      yRot += 0;
      break;
    case SOUTH:
      yRot += 90;
      break;
    case WEST:
      yRot += 180;
      break;
    case NORTH:
      yRot += 270;
      break;
    default:
      return null;
    }

    String model;
    switch (state.getValue(SHAPE)) {
    case STRAIGHT:
      model = "stairs";
      yRot += top ? -90 : 0;
      break;
    case OUTER_RIGHT:
      model = "outer_stairs";
      break;
    case OUTER_LEFT:
      model = "outer_stairs";
      yRot += -90;
      break;
    case INNER_RIGHT:
      model = "inner_stairs";
      break;
    case INNER_LEFT:
      model = "inner_stairs";
      yRot += -90;
      break;
    default:
      return null;
    }

    return PaintRegistry.getModel(IBakedModel.class, model, paint, new UVLock(ModelRotation.getModelRotation(xRot, yRot)));
  }

  @Override
  @SideOnly(Side.CLIENT)
  public @Nonnull ICacheKey getCacheKey(@Nonnull Block block, @Nonnull ItemStack stack, @Nonnull ICacheKey cacheKey) {
    return cacheKey.addCacheKey(getPaintSource(block, stack));
  }

  @Override
  @SideOnly(Side.CLIENT)
  public List<IBakedModel> mapItemRender(Block block, ItemStack stack) {
    IBlockState paintSource = getPaintSource(block, stack);
    IBlockState stdOverlay = BlockMachineBase.block.getDefaultState().withProperty(EnumRenderPart.SUB, EnumRenderPart.PAINT_OVERLAY);    
    IBakedModel model1 = PaintRegistry.getModel(IBakedModel.class, "stairs", paintSource, new UVLock(null));
    IBakedModel model2 = PaintRegistry.getModel(IBakedModel.class, "stairs", stdOverlay, PaintRegistry.OVERLAY_TRANSFORMATION4);
    List<IBakedModel> list = new ArrayList<IBakedModel>();
    list.add(model1);
    list.add(model2);
    return list;
  }

  @Override
  public boolean canRenderInLayer(BlockRenderLayer layer) {
    return true;
  }

  @Override
  public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
    IBlockState bs = world.getBlockState(pos);
    return bs.getMaterial() == Material.WOOD ? 20 : super.getFlammability(world, pos, face);
  }

  @Override
  public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
    IBlockState bs = world.getBlockState(pos);
    return bs.getMaterial() == Material.WOOD ? 5 : super.getFireSpreadSpeed(world, pos, face);
  }

  @Override
  public boolean doesSideBlockRendering(IBlockState bs, IBlockAccess world, BlockPos pos, EnumFacing face) {
    return false;
  }

  @Deprecated
  @Override
  public boolean shouldSideBeRendered(IBlockState bs, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
    // disabled because it fails once the player uses a wrench to rotate the block
    // if (worldIn.getBlockState(pos.offset(side)).getBlock() instanceof BlockPaintedStairs
    // && getPaintSource(null, worldIn, pos) == getPaintSource(null, worldIn, pos)) {
    // return false;
    // }
    return super.shouldSideBeRendered(bs, worldIn, pos, side);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
    if (tab != null) {
      super.getSubBlocks(itemIn, tab, list);
    }
  }

  @Override
  @SideOnly(Side.CLIENT)
  public List<IBlockState> mapBlockRender(IBlockStateWrapper state, IBlockAccess world, BlockPos pos, BlockRenderLayer blockLayer,
      QuadCollector quadCollector) {
    IBlockState paintSource = getPaintSource(state, world, pos);
    if (PainterUtil2.canRenderInLayer(paintSource, blockLayer)) {
      quadCollector.addFriendlybakedModel(blockLayer, mapRender(state, paintSource), paintSource, MathHelper.getPositionRandom(pos));
    }
    return null;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public EnumMap<EnumFacing, EnumIOMode> mapOverlayLayer(IBlockStateWrapper state, IBlockAccess world, BlockPos pos, boolean isPainted) {
    return null;
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
