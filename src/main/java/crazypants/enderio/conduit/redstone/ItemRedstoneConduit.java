package crazypants.enderio.conduit.redstone;

import javax.annotation.Nonnull;

import crazypants.enderio.ModObject;
import crazypants.enderio.conduit.AbstractItemConduit;
import crazypants.enderio.conduit.ConduitDisplayMode;
import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.ItemConduitSubtype;
import crazypants.enderio.conduit.geom.Offset;
import crazypants.enderio.conduit.registry.ConduitRegistry;
import crazypants.enderio.gui.IconEIO;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRedstoneConduit extends AbstractItemConduit {

  private static ItemConduitSubtype[] subtypes = new ItemConduitSubtype[] {
      new ItemConduitSubtype(ModObject.itemRedstoneConduit.name() + "Insulated", "enderio:itemRedstoneInsulatedConduit")
  };

  public static ItemRedstoneConduit create() {
    ItemRedstoneConduit result = new ItemRedstoneConduit();
    result.init();
    return result;
  }

  private final ConduitRegistry.ConduitInfo conduitInfo;

  protected ItemRedstoneConduit() {
    super(ModObject.itemRedstoneConduit, subtypes);
    conduitInfo = new ConduitRegistry.ConduitInfo(getBaseConduitType(), Offset.UP, Offset.UP, Offset.NORTH, Offset.UP);
    conduitInfo.addMember(InsulatedRedstoneConduit.class);
    conduitInfo.setCanConnectToAnything();
    ConduitRegistry.register(conduitInfo);
    ConduitDisplayMode.registerDisplayMode(new ConduitDisplayMode(getBaseConduitType(), IconEIO.WRENCH_OVERLAY_REDSTONE, IconEIO.WRENCH_OVERLAY_REDSTONE_OFF));
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerRenderers() {
    super.registerRenderers();
    conduitInfo.addRenderer(new InsulatedRedstoneConduitRenderer());
  }

  @Override
  public @Nonnull Class<? extends IConduit> getBaseConduitType() {
    return IRedstoneConduit.class;
  }

  @Override
  public IConduit createConduit(ItemStack stack, EntityPlayer player) {    
    return new InsulatedRedstoneConduit();          
  }

  @Override
  public boolean shouldHideFacades(ItemStack stack, EntityPlayer player) {
    return true;
  }
}
