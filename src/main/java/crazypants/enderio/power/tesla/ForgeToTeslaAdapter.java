package crazypants.enderio.power.tesla;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.IEnergyStorage;

public class ForgeToTeslaAdapter implements ITeslaConsumer, ITeslaHolder, ITeslaProducer, ICapabilityProvider {

  private IEnergyStorage delegate;
  
  public ForgeToTeslaAdapter(IEnergyStorage delegate) {
    this.delegate = delegate;
  }

  @Override
  public long takePower(long power, boolean simulated) {
    if(delegate.canExtract()) {
      return delegate.extractEnergy((int)power, simulated);
    }
    return 0;
  }

  @Override
  public long getStoredPower() {
    return delegate.getEnergyStored();
  }

  @Override
  public long getCapacity() {
    return delegate.getMaxEnergyStored();
  }

  @Override
  public long givePower(long power, boolean simulated) {
    if(delegate.canReceive()) {
      return delegate.receiveEnergy((int)power, simulated);
    }
    return 0;
  }

  @Override
  public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
    if(capability == TeslaCapabilities.CAPABILITY_HOLDER) {
      return true;
    }
    if(capability == TeslaCapabilities.CAPABILITY_CONSUMER && delegate.canReceive()) {
      return true;
    }
    if(capability == TeslaCapabilities.CAPABILITY_PRODUCER && delegate.canExtract()) {
      return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
    if(hasCapability(capability, facing)) {
      return (T)this;
    }
    return null;
  }
  
}
