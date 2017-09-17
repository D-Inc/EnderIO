package crazypants.enderio.machine.buffer;

import javax.annotation.Nullable;

import com.enderio.core.common.transform.EnderCoreMethods.IOverlayRenderAware;

import crazypants.enderio.capacitor.DefaultCapacitorData;
import crazypants.enderio.config.Config;
import crazypants.enderio.item.PowerBarOverlayRenderHelper;
import crazypants.enderio.power.AbstractPoweredBlockItem;
import crazypants.enderio.power.ItemPowerCapabilityBackend;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import static crazypants.enderio.ModObject.blockBuffer;
import static crazypants.enderio.capacitor.CapacitorKey.BUFFER_POWER_BUFFER;

public class BlockItemBuffer extends AbstractPoweredBlockItem implements IOverlayRenderAware {

  public BlockItemBuffer(Block block, String name) {
    super(block, 0, 0 ,0);
    setHasSubtypes(true);
    setMaxDamage(0);
    setRegistryName(name);
  }

  @Override
  public int getMetadata(int damage) {
    return damage;
  }

  @Override
  public String getUnlocalizedName(ItemStack stack) {
    return BufferType.values()[stack.getItemDamage()].getUnlocalizedName();
  }

  @Override
  public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ,
      IBlockState newState) {
    super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);

    if (newState.getBlock() == block) {
      TileEntity te = world.getTileEntity(pos);
      if (te instanceof TileBuffer) {
        TileBuffer buffer = ((TileBuffer) te);
        BufferType t = BufferType.values()[block.getMetaFromState(newState)];
        buffer.setHasInventory(t.hasInventory);
        buffer.setHasPower(t.hasPower);
        buffer.setCreative(t.isCreative);
        buffer.markDirty();
      }
    }
    return true;
  }

  @Override
  public void renderItemOverlayIntoGUI(ItemStack stack, int xPosition, int yPosition) {
    if (stack.stackSize == 1 && blockBuffer.getBlock().getStateFromMeta(stack.getMetadata()).getValue(BufferType.TYPE).hasPower) {
      PowerBarOverlayRenderHelper.instance.render(stack, xPosition, yPosition);
    }
  }

  @Override
  public boolean hasEffect(ItemStack stack) {
    return blockBuffer.getBlock().getStateFromMeta(stack.getMetadata()).getValue(BufferType.TYPE).isCreative || super.hasEffect(stack);
  }

  @Override
  public int getMaxEnergyStored(ItemStack stack) {
    BufferType type = blockBuffer.getBlock().getStateFromMeta(stack.getMetadata()).getValue(BufferType.TYPE);
    return type.hasPower ? BUFFER_POWER_BUFFER.get(DefaultCapacitorData.BASIC_CAPACITOR) : 0;
  }

  @Override
  public int getMaxInput(ItemStack container) {
    BufferType type = blockBuffer.getBlock().getStateFromMeta(container.getMetadata()).getValue(BufferType.TYPE);
    return type.hasPower ? Config.powerConduitTierThreeRF / 20 : 0;
  }

  @Override
  public int getMaxOutput(ItemStack container) {
    return getMaxInput(container);
  }
  
  @Override
  public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
    return new InnerProv(stack);
  }

  private class InnerProv implements ICapabilityProvider {

    private final ItemStack container;
    private final ItemPowerCapabilityBackend backend;
    
    public InnerProv(ItemStack container) {
      this.container = container;
      this.backend = new ItemPowerCapabilityBackend(container);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
      return backend.hasCapability(capability, facing) && blockBuffer.getBlock().getStateFromMeta(container.getMetadata()).getValue(BufferType.TYPE).hasPower;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
      if (!hasCapability(capability, facing)) {
        return null;
      }
      BufferType type = blockBuffer.getBlock().getStateFromMeta(container.getMetadata()).getValue(BufferType.TYPE);
      if(!type.hasPower || container.stackSize > 1) {
        return null;
      }
      if(type.isCreative) {
        return null; // TODO (T)new CreativePowerCap(container);
      }
      return backend.getCapability(capability, facing);
    }
  }
  
  // private class CreativePowerCap extends InternalPoweredItemWrapper {
  //
  // public CreativePowerCap (ItemStack container) {
  // super(container, BlockItemBuffer.this);
  // }
  //
  // @Override
  // public int receiveEnergy(int maxReceive, boolean simulate) {
  // return maxReceive;
  // }
  //
  // @Override
  // public int extractEnergy(int maxExtract, boolean simulate) {
  // return maxExtract;
  // }
  // }

}
