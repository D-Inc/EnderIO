package crazypants.enderio.teleport.telepad.gui;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.enderio.core.client.gui.widget.GuiToolTip;
import com.enderio.core.common.BlockEnder;
import com.enderio.core.common.util.BlockCoord;
import com.enderio.core.common.util.Util;

import crazypants.enderio.EnderIO;
import crazypants.enderio.gui.GuiContainerBaseEIO;
import crazypants.enderio.network.PacketHandler;
import crazypants.enderio.power.PowerDisplayUtil;
import crazypants.enderio.teleport.telepad.TelepadTarget;
import crazypants.enderio.teleport.telepad.TileDialingDevice;
import crazypants.enderio.teleport.telepad.TileTelePad;
import crazypants.enderio.teleport.telepad.packet.PacketSetTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

public class GuiDialingDevice extends GuiContainerBaseEIO {

  private static final int ID_TELEPORT_BUTTON = 96;

  GuiButton teleportButton;

  private final TileDialingDevice dialingDevice;
  private final TileTelePad telepad;

  private int powerX = 8;
  private int powerY = 9;
  private int powerScale = 120;

  private int progressX = 26;
  private int progressY = 110;
  private int progressScale = 124;

  private final GuiTargetList targetList;

  public GuiDialingDevice(InventoryPlayer playerInv, TileDialingDevice te) {
    super(new ContainerDialingDevice(playerInv, te), "dialingDevice");
    this.dialingDevice = te;
    telepad = findTelepad();
    
    ySize = 220;

    addToolTip(new GuiToolTip(new Rectangle(powerX, powerY, 10, powerScale), "") {
      @Override
      protected void updateText() {
        text.clear();
        updatePowerBarTooltip(text);
      }
    });

    addToolTip(new GuiToolTip(new Rectangle(progressX, progressY, progressScale, 10), "") {
      @Override
      protected void updateText() {
        text.clear();
        if (telepad != null) {
          text.add(Math.round(telepad.getProgress() * 100) + "%");
        }
      }
    });

    int w = 115;
    int h = 71;
    int x = 30;
    int y = 10;
    targetList = new GuiTargetList(w, h, x, y, te);
    targetList.setShowSelectionBox(true);
    targetList.setScrollButtonIds(100, 101);
    
    if(telepad != null) {
      targetList.setSelection(telepad.getTarget());
    }

    addToolTip(new GuiToolTip(new Rectangle(x, y, w, h), "") {
      @Override
      protected void updateText() {
        text.clear();
        TelepadTarget el = targetList.getElementAt(getLastMouseX() + getGuiLeft(), getLastMouseY());
        if (el != null) {
          Rectangle iconBnds = targetList.getIconBounds(0);
          if (iconBnds.contains(getLastMouseX() + getGuiLeft(), 1)) {
            text.add(TextFormatting.RED + "Delete");
          } else {
            text.add(TextFormatting.WHITE + el.getName());
            text.add(BlockCoord.chatString(el.getLocation()));
            text.add(el.getDimenionName());
          }
        }
      }
    });

  }

  private TileTelePad findTelepad() {
    
    BlockPos pos = dialingDevice.getPos();
    EnumFacing forward = dialingDevice.getFacing().getInputSide();
    EnumFacing up;
    EnumFacing side;
    if (forward.getFrontOffsetY() == 0) {
      up = EnumFacing.UP;
      side = forward.rotateY();
    } else { // look along y
      up = EnumFacing.NORTH;
      side = EnumFacing.EAST;
    }

    int range = 4;
    TileTelePad result = null;
    for (int i = 0; i < range*2; i++) {
      for(int j=0;j<range*2;j++) {
        for(int k=0;k<range*2;k++) {
          BlockPos check = pos.offset(forward, i + 1).offset(side,j-range).offset(up,k-range);
          result = BlockEnder.getAnyTileEntitySafe(dialingDevice.getWorld(), check, TileTelePad.class);
          if(result != null) {
            return result.getMaster();
          }
        }
      }
    }

    return result;
  }

  private String getPowerOutputLabel() {
    return I18n.format("enderio.gui.max");
  }

  protected int getPowerOutputValue() {
    return dialingDevice.getUsage();
  }

  protected void updatePowerBarTooltip(List<String> text) {
    text.add(getPowerOutputLabel() + " " + PowerDisplayUtil.formatPower(getPowerOutputValue()) + " " + PowerDisplayUtil.abrevation()
        + PowerDisplayUtil.perTickStr());
    text.add(PowerDisplayUtil.formatStoredPower(dialingDevice.getEnergyStored(), dialingDevice.getMaxEnergyStored(null)));
  }

  @Override
  public void initGui() {
    super.initGui();

    String text = EnderIO.lang.localize("gui.telepad.teleport");
    int width = getFontRenderer().getStringWidth(text) + 10;

    int x = guiLeft + (xSize / 2) - (width / 2);
    int y = guiTop + 85;

    teleportButton = new GuiButton(ID_TELEPORT_BUTTON, x, y, width, 20, text);
    addButton(teleportButton);

    ((ContainerDialingDevice) inventorySlots).createGhostSlots(getGhostSlots());

    targetList.onGuiInit(this);
  }

  @Override
  public void updateScreen() {
    super.updateScreen();
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    bindGuiTexture();
    int sx = (width - xSize) / 2;
    int sy = (height - ySize) / 2;

    drawTexturedModalRect(sx, sy, 0, 0, this.xSize, this.ySize);

    int powerScaled = dialingDevice.getPowerScaled(powerScale);
    drawTexturedModalRect(sx + powerX, sy + powerY + powerScale - powerScaled, xSize, 0, 10, powerScaled);

    targetList.drawScreen(mouseX, mouseY, partialTick);

    super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

    if (dialingDevice.getEnergyStored() < dialingDevice.getUsage()) {
      String txt = TextFormatting.DARK_RED + "No Power";
      renderInfoMessage(sx, sy, txt, 0x000000);
      return;
    }

    if (telepad == null) {
      String txt = TextFormatting.DARK_RED + "No Telepad";
      renderInfoMessage(sx, sy, txt, 0x000000);
      return;
    }
    if (telepad.getEnergyStored(null) <= 0) {
      String txt = TextFormatting.DARK_RED + "Telepad not powered";
      renderInfoMessage(sx, sy, txt, 0x000000);
      return;
    }
    if(targetList.getSelectedElement() == null) {
      String txt = TextFormatting.DARK_RED + "Enter Target";
      renderInfoMessage(sx, sy, txt, 0x000000);
      return;
    }

    bindGuiTexture();
    int progressScaled = Util.getProgressScaled(progressScale, telepad);
    drawTexturedModalRect(sx + progressX, sy + progressY, 0, ySize, progressScaled, 10);

    Entity e = telepad.getCurrentTarget();
    if (e != null) {
      String name = e.getName();
      renderInfoMessage(sx, sy, name, 0x000000);
    } else if (telepad.wasBlocked()) {
      String s = EnderIO.lang.localize("gui.telepad.blocked");
      renderInfoMessage(sx, sy, s, 0xAA0000);
    }

  }

  private void renderInfoMessage(int sx, int sy, String txt, int color) {
    FontRenderer fnt = Minecraft.getMinecraft().fontRendererObj;
    fnt.drawString(txt, sx + xSize / 2 - fnt.getStringWidth(txt) / 2, sy + progressY + fnt.FONT_HEIGHT + 6, color);
  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    super.actionPerformed(button);

    if (button.id == ID_TELEPORT_BUTTON) {
      TelepadTarget target = targetList.getSelectedElement();
      if (target != null && dialingDevice.getEnergyStored() > 0 && telepad != null) {
        telepad.setTarget(target);
        PacketHandler.INSTANCE.sendToServer(new PacketSetTarget(telepad, target));
        telepad.teleportAll();
      }
    }
  }
}
