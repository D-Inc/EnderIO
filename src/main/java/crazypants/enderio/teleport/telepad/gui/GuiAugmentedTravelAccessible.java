package crazypants.enderio.teleport.telepad.gui;

import crazypants.enderio.EnderIO;
import crazypants.enderio.GuiID;
import crazypants.enderio.gui.IconEIO;
import crazypants.enderio.network.PacketHandler;
import crazypants.enderio.teleport.GuiTravelAccessable;
import crazypants.enderio.teleport.telepad.TileTelePad;
import crazypants.enderio.teleport.telepad.packet.PacketOpenServerGui;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;

public class GuiAugmentedTravelAccessible extends GuiTravelAccessable<TileTelePad> implements IToggleableGui {

  private static final int ID_SWITCH_BUTTON = 99;

  ToggleTravelButton switchButton;

  public GuiAugmentedTravelAccessible(InventoryPlayer playerInv, TileTelePad te, World world) {
    super(playerInv, te, world);
    switchButton = new ToggleTravelButton(this, ID_SWITCH_BUTTON, GuiTelePad.SWITCH_X, GuiTelePad.SWITCH_Y, IconEIO.IO_WHATSIT);
    switchButton.setToolTip(EnderIO.lang.localize("gui.telepad.configure.telepad"));
  }
  
  @Override
  public void initGui() {
    super.initGui();
    switchButton.onGuiInit();
  }

  @Override
  public void switchGui() {
    GuiID.GUI_ID_TELEPAD.openClientGui(world, te.getPos(), mc.thePlayer, null);
    PacketHandler.INSTANCE.sendToServer(new PacketOpenServerGui(te, GuiID.GUI_ID_TELEPAD));
  }
}
