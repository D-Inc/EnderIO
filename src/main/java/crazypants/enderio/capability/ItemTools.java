package crazypants.enderio.capability;

import javax.annotation.Nullable;

import crazypants.enderio.machine.AbstractMachineEntity;
import crazypants.enderio.machine.IoMode;
import crazypants.util.Prep;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class ItemTools {

  @CapabilityInject(IItemHandler.class)
  public static Capability<IItemHandler> ITEM_HANDLER_CAPABILITY = null;

  private ItemTools() {
  }

  public static boolean doPush(IBlockAccess world, BlockPos pos) {
    boolean result = false;
    for (EnumFacing facing : EnumFacing.values()) {
      MoveResult moveResult = move(NO_LIMIT, world, pos, facing, pos.offset(facing), facing.getOpposite());
      if (moveResult == MoveResult.SOURCE_EMPTY) {
        return false;
      } else if (moveResult == MoveResult.MOVED) {
        result = true;
      }
    }
    return result;
  }

  public static boolean doPull(IBlockAccess world, BlockPos pos) {
    boolean result = false;
    for (EnumFacing facing : EnumFacing.values()) {
      MoveResult moveResult = move(NO_LIMIT, world, pos.offset(facing), facing.getOpposite(), pos, facing);
      if (moveResult == MoveResult.TARGET_FULL) {
        return false;
      } else if (moveResult == MoveResult.MOVED) {
        result = true;
      }
    }
    return result;
  }

  public static enum MoveResult {
    NO_ACTION,
    LIMITED,
    MOVED,
    TARGET_FULL,
    SOURCE_EMPTY;
  }

  public static MoveResult move(Limit limit, IBlockAccess world, BlockPos sourcePos, EnumFacing sourceFacing, BlockPos targetPos, EnumFacing targetFacing) {
    if (!limit.canWork()) {
      return MoveResult.LIMITED;
    }
    boolean movedSomething = false;
    TileEntity source = world.getTileEntity(sourcePos);
    if (source != null && source.hasWorldObj() && !source.getWorld().isRemote && canPullFrom(source, sourceFacing)) {
      TileEntity target = world.getTileEntity(targetPos);
      if (target != null && target.hasWorldObj() && canPutInto(target, targetFacing)) {
        IItemHandler sourceHandler = getExternalInventory(world, sourcePos, sourceFacing);
        if (sourceHandler != null && hasItems(sourceHandler)) {
          IItemHandler targetHandler = getExternalInventory(world, targetPos, targetFacing);
          if (targetHandler != null && hasFreeSpace(targetHandler)) {
            for (int i = 0; i < sourceHandler.getSlots(); i++) {
              ItemStack removable = sourceHandler.extractItem(i, limit.getItems(), true);
              if (Prep.isValid(removable) && removable.stackSize > 0) {
                ItemStack unacceptable = ItemHandlerHelper.insertItemStacked(targetHandler, removable, true);
                int movable = removable.stackSize - (Prep.isInvalid(unacceptable) ? 0 : unacceptable.stackSize);
                if (movable > 0) {
                  ItemStack removed = sourceHandler.extractItem(i, movable, false);
                  if (Prep.isValid(removed) && removed.stackSize > 0) {
                    ItemStack targetRejected = ItemHandlerHelper.insertItemStacked(targetHandler, removed, false);
                    if (Prep.isValid(targetRejected) && targetRejected.stackSize > 0) {
                      ItemStack sourceRejected = ItemHandlerHelper.insertItemStacked(sourceHandler, targetRejected, false);
                      if (Prep.isValid(sourceRejected) && sourceRejected.stackSize > 0) {
                        EntityItem drop = new EntityItem(source.getWorld(), sourcePos.getX() + 0.5, sourcePos.getY() + 0.5, sourcePos.getZ() + 0.5,
                            sourceRejected);
                        source.getWorld().spawnEntityInWorld(drop);
                        return MoveResult.MOVED;
                      }
                    }
                  }
                  movedSomething = true;
                  limit.useItems(movable);
                  if (!limit.canWork()) {
                    return MoveResult.MOVED;
                  }
                }
              }
            }
          } else {
            return MoveResult.TARGET_FULL;
          }
        } else {
          return MoveResult.SOURCE_EMPTY;
        }
      } else {
        return MoveResult.TARGET_FULL;
      }
    } else {
      return MoveResult.SOURCE_EMPTY;
    }
    return movedSomething ? MoveResult.MOVED : MoveResult.NO_ACTION;
  }
  
  /**
   * 
   * @param inventory
   * @param item
   * @return the number inserted
   */
  public static int doInsertItem(IItemHandler inventory, ItemStack item) {
    if (inventory == null || Prep.isInvalid(item)) {
      return 0;
    }
    int startSize = item.stackSize;
    ItemStack res = ItemHandlerHelper.insertItemStacked(inventory, item.copy(), false);
    int val = Prep.isInvalid(res) ? startSize : startSize - res.stackSize;
    return val;
  }

  public static boolean hasFreeSpace(IItemHandler handler) {
    for (int i = 0; i < handler.getSlots(); i++) {
      ItemStack stack = handler.getStackInSlot(i);
      if (Prep.isInvalid(stack) || (stack.isStackable() == stack.stackSize < stack.getMaxStackSize())) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasItems(IItemHandler handler) {
    for (int i = 0; i < handler.getSlots(); i++) {
      ItemStack stack = handler.getStackInSlot(i);
      if (Prep.isValid(stack) && stack.stackSize > 0) {
        return true;
      }
    }
    return false;
  }

  public static int countItems(IItemHandler handler, ItemStack template) {
    int count = 0;
    for (int i = 0; i < handler.getSlots(); i++) {
      ItemStack stack = handler.getStackInSlot(i);
      if (com.enderio.core.common.util.ItemUtil.areStacksEqual(template, stack)) {
        count += stack.stackSize;
      }
    }
    return count;
  }

  /**
   * Determines how many items can be inserted into an inventory given that the number of items is limited by "limit".
   * 
   * @param handler
   *          The target inventory
   * @param template
   *          The item to insert
   * @param limit
   *          The limit, meaning the maximum number of items that are allowed in the inventory.
   * @return The number of items that can be inserted without violating the limit.
   */
  public static int getInsertLimit(IItemHandler handler, ItemStack template, int limit) {
    int count = 0;
    for (int i = 0; i < handler.getSlots(); i++) {
      ItemStack stack = handler.getStackInSlot(i);
      if (com.enderio.core.common.util.ItemUtil.areStacksEqual(template, stack)) {
        count += stack.stackSize;
      }
      if (count >= limit) {
        return 0;
      }
    }
    return limit - count;
  }

  public static boolean hasAtLeast(IItemHandler handler, ItemStack template, int limit) {
    int count = 0;
    for (int i = 0; i < handler.getSlots(); i++) {
      ItemStack stack = handler.getStackInSlot(i);
      if (com.enderio.core.common.util.ItemUtil.areStacksEqual(template, stack)) {
        count += stack.stackSize;
      }
      if (count >= limit) {
        return true;
      }
    }
    return false;
  }

  public static boolean canPutInto(TileEntity tileEntity, EnumFacing facing) {
    if (tileEntity instanceof AbstractMachineEntity) {
      IoMode ioMode = ((AbstractMachineEntity) tileEntity).getIoMode(facing);
      return ioMode != IoMode.DISABLED && ioMode != IoMode.PUSH;
    }
    return true;
  }

  public static boolean canPullFrom(TileEntity tileEntity, EnumFacing facing) {
    if (tileEntity instanceof AbstractMachineEntity) {
      IoMode ioMode = ((AbstractMachineEntity) tileEntity).getIoMode(facing);
      return ioMode != IoMode.DISABLED && ioMode != IoMode.PULL;
    }
    return true;
  }

  public static final Limit NO_LIMIT = new Limit(Integer.MAX_VALUE, Integer.MAX_VALUE);

  public static class Limit {
    private int stacks, items;

    public Limit(int stacks, int items) {
      this.stacks = stacks;
      this.items = items;
    }

    public Limit(int items) {
      this.stacks = Integer.MAX_VALUE;
      this.items = items;
    }

    public int getStacks() {
      return stacks;
    }

    public int getItems() {
      return items;
    }

    public void useItems(int count) {
      stacks--;
      items -= count;
    }

    public boolean canWork() {
      return stacks > 0 && items > 0;
    }

    public Limit copy() {
      return new Limit(stacks,items);
    }

  }

  public static @Nullable IItemHandler getExternalInventory(IBlockAccess world, BlockPos pos, EnumFacing face) {
    if (world == null || pos == null || face == null) {
      return null;
    }
    TileEntity te = world.getTileEntity(pos);
    if (te != null && te.hasCapability(ITEM_HANDLER_CAPABILITY, face)) {
      return te.getCapability(ITEM_HANDLER_CAPABILITY, face);
    }
    if (te instanceof ISidedInventory) {
      // Log.info("ItemConduit.getExternalInventory: Found non-capability sided inv at " + pos);
      return new SidedInvWrapper((ISidedInventory) te, face);
    }
    if (te instanceof IInventory) {
      // Log.info("ItemConduit.getExternalInventory: Found non-capability inv at " + pos);
      return new InvWrapper((IInventory) te);
    }
    return null;
  }

}
