package crazypants.enderio.conduit.power;

import java.util.List;

import javax.annotation.Nonnull;

import crazypants.enderio.EnderIO;
import crazypants.enderio.ModObject;
import crazypants.enderio.conduit.AbstractItemConduit;
import crazypants.enderio.conduit.ConduitDisplayMode;
import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.ItemConduitSubtype;
import crazypants.enderio.conduit.geom.Offset;
import crazypants.enderio.conduit.registry.ConduitRegistry;
import crazypants.enderio.gui.IconEIO;
import crazypants.enderio.power.PowerDisplayUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPowerConduit extends AbstractItemConduit {

  private static String PREFIX;
  private static String POSTFIX;

  static ItemConduitSubtype[] SUBTYPES = new ItemConduitSubtype[] {
      new ItemConduitSubtype(ModObject.itemPowerConduit.name(), "enderio:itemPowerConduit"),
      new ItemConduitSubtype(ModObject.itemPowerConduit.name() + "Enhanced", "enderio:itemPowerConduitEnhanced"),
      new ItemConduitSubtype(ModObject.itemPowerConduit.name() + "Ender", "enderio:itemPowerConduitEnder")
  };

  private final ConduitRegistry.ConduitInfo conduitInfo;

  public static ItemPowerConduit create() {
    ItemPowerConduit result = new ItemPowerConduit();
    result.init();
    return result;
  }

  protected ItemPowerConduit() {
    super(ModObject.itemPowerConduit, SUBTYPES);
    conduitInfo = new ConduitRegistry.ConduitInfo(getBaseConduitType(), Offset.DOWN, Offset.DOWN, Offset.SOUTH, Offset.DOWN);
    conduitInfo.addMember(PowerConduit.class);
    ConduitRegistry.register(conduitInfo);
    ConduitDisplayMode.registerDisplayMode(new ConduitDisplayMode(getBaseConduitType(), IconEIO.WRENCH_OVERLAY_POWER, IconEIO.WRENCH_OVERLAY_POWER_OFF));
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerRenderers() {
    super.registerRenderers();
    conduitInfo.addRenderer(new PowerConduitRenderer());
  }

  @Override
  public @Nonnull Class<? extends IConduit> getBaseConduitType() {
    return IPowerConduit.class;
  }

  @Override
  public IConduit createConduit(ItemStack stack, EntityPlayer player) {
    return new PowerConduit(stack.getItemDamage());
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack itemStack, EntityPlayer par2EntityPlayer, List<String> list, boolean par4) {
    if(PREFIX == null) {
      POSTFIX = " " + PowerDisplayUtil.abrevation() + PowerDisplayUtil.perTickStr();
      PREFIX = EnderIO.lang.localize("power.maxOutput") + " ";
    }
    super.addInformation(itemStack, par2EntityPlayer, list, par4);
    int cap = PowerConduit.getMaxEnergyIO(itemStack.getMetadata());
    list.add(PREFIX + PowerDisplayUtil.formatPower(cap) + POSTFIX);
  }
  
  @Override
  public boolean shouldHideFacades(ItemStack stack, EntityPlayer player) {
    return true;
  }
}
