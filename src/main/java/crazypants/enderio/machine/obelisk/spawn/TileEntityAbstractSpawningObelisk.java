package crazypants.enderio.machine.obelisk.spawn;

import javax.annotation.Nonnull;

import static crazypants.enderio.capacitor.CapacitorKey.AVERSION_RANGE;

import crazypants.enderio.ModObject;
import crazypants.enderio.capacitor.ICapacitorKey;
import crazypants.enderio.machine.SlotDefinition;
import info.loenwind.autosave.annotations.Storable;
import net.minecraft.entity.EntityLivingBase;

@Storable
public abstract class TileEntityAbstractSpawningObelisk extends AbstractMobObelisk implements ISpawnCallback {

  private boolean registered = false;

  @Override
  public abstract Result isSpawnPrevented(EntityLivingBase mob);

  @Override
  public abstract @Nonnull String getMachineName();
  

  public TileEntityAbstractSpawningObelisk(SlotDefinition slotDefinition, ICapacitorKey maxEnergyRecieved, ICapacitorKey maxEnergyStored,
      ICapacitorKey maxEnergyUsed) {
    super(slotDefinition, maxEnergyRecieved, maxEnergyStored, maxEnergyUsed);
  }

  public TileEntityAbstractSpawningObelisk(SlotDefinition slotDefinition, ModObject modObject) {
    super(slotDefinition, modObject);
  }

  @Override
  public void invalidate() {
    super.invalidate();    
    SpawningObeliskController.instance.deregisterGuard(this);
    registered = false;
  }

  @Override
  public float getRange() {
    return AVERSION_RANGE.getFloat(getCapacitorData());
  }

  @Override
  protected boolean processTasks(boolean redstoneCheck) {
    if (redstoneCheck && hasPower()) {
      if(!registered) {
        SpawningObeliskController.instance.registerGuard(this);
        registered = true;
      }
      usePower();
    } 
    return false;    
  }

}