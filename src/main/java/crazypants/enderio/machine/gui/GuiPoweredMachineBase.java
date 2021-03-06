package crazypants.enderio.machine.gui;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.enderio.core.client.gui.widget.GuiToolTip;
import com.enderio.core.client.render.RenderUtil;

import crazypants.enderio.EnderIO;
import crazypants.enderio.machine.AbstractPoweredMachineEntity;
import crazypants.enderio.power.PowerDisplayUtil;
import info.loenwind.scheduler.Celeb;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;

public abstract class GuiPoweredMachineBase<T extends AbstractPoweredMachineEntity> extends GuiMachineBase<T> {

  protected static final int POWER_Y = 14;
  protected final int POWER_X = 15;
  protected static final int POWER_WIDTH = 10;
  protected static final int POWER_HEIGHT = 42;
  protected static final int BOTTOM_POWER_Y = POWER_Y + POWER_HEIGHT;

  public GuiPoweredMachineBase(T machine, Container container, String... guiTexture) {
    super(machine, container, guiTexture);
    if(renderPowerBar()) {
      addToolTip(new GuiToolTip(new Rectangle(getPowerX(), getPowerY(), getPowerWidth(), getPowerHeight()), "") {

        @Override
        protected void updateText() {
          text.clear();
          if(renderPowerBar()) {
            updatePowerBarTooltip(text);
          }
        }

      });
    }
  }

  protected String getPowerOutputLabel() {
    return I18n.format("enderio.gui.max");
  }
  
  protected int getPowerOutputValue() {
    return getTileEntity().getPowerUsePerTick();
  }
  
  protected void updatePowerBarTooltip(List<String> text) {
    if (Celeb.C24.isOn()) {
      int frame = (int) ((EnderIO.proxy.getTickCount() / 30) % (lang.size() - 1));
      if (frame == 0) {
        Collections.shuffle(lang);
      }
      text.add(lang.get(frame));
    }
    text.add(getPowerOutputLabel() + " " + PowerDisplayUtil.formatPower(getPowerOutputValue()) + " " + PowerDisplayUtil.abrevation()
        + PowerDisplayUtil.perTickStr());
    text.add(PowerDisplayUtil.formatStoredPower(getTileEntity().getEnergyStored(), getTileEntity().getMaxEnergyStored()));
  }

  public void renderPowerBar(int k, int l) {
    if(renderPowerBar()) {
      int i1 = getTileEntity().getEnergyStoredScaled(getPowerHeight());
      drawTexturedModalRect(k + getPowerX(), l + (getPowerY() + getPowerHeight()) - i1, getPowerU(), getPowerV(), getPowerWidth(), i1);
      if (Celeb.C24.isOn()) {
        i1 = getPowerHeight();
        int frame = (int) ((EnderIO.proxy.getTickCount() / 3) % 25) * 10;
        RenderUtil.bindTexture(EnderIO.DOMAIN + ":textures/gui/overlay.png");
        drawTexturedModalRect(k + getPowerX(), l + (getPowerY() + getPowerHeight()) - i1, frame, 0, getPowerWidth(), i1);
        bindGuiTexture();
      }
    }
  }

  protected int getPowerX() {
    return POWER_X;
  }

  protected int getPowerY() {
    return POWER_Y;
  }

  protected int getPowerWidth() {
    return POWER_WIDTH;
  }

  protected int getPowerHeight() {
    return POWER_HEIGHT;
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    int k = (width - xSize) / 2;
    int l = (height - ySize) / 2;
    renderPowerBar(k, l);

    super.drawGuiContainerBackgroundLayer(par1, par2, par3);
  }

  protected int getPowerV() {
    return 31;
  }

  protected int getPowerU() {
    return 176;
  }

  protected boolean renderPowerBar() {
    return true;
  }

  private static final List<String> lang = Arrays.asList(new String[] { "Krismasi Njema", "UKhisimusi omuhle", "Moni Wa Chikondwelero Cha Kristmasi",
      "\u06A9\u0631\u0633\u0645\u0633 \u0645\u0628\u0627\u0631\u06A9", "G\u00EBzuar Krishtlindjen",
      "\u0639\u064A\u062F \u0645\u064A\u0644\u0627\u062F \u0645\u062C\u064A\u062F",
      "\u0547\u0576\u0578\u0580\u0570\u0561\u057E\u0578\u0580 \u0531\u0574\u0561\u0576\u0578\u0580 \u0587 \u054D\u0578\u0582\u0580\u0562 \u053E\u0576\u0578\u0582\u0576\u0564",
      "Vrolijk Kerstfeest", "djoyeus Noy\u00E9", "'vesela 'koleda", "\u5723\u8BDE\u5FEB\u4E50", "\u8056\u8A95\u5FEB\u6A02", "Sretan Bo\u017Ei\u0107",
      "Prejeme Vam Vesele Vanoce", "Gl\u00E6delig Jul", "Mbotama Malamu", "Feli\u0109an Kristnaskon", "R\u00F5\u00F5msaid J\u00F5ulup\u00FChi",
      "Hyv\u00E4\u00E4 Joulua", "Joyeux No\u00EBl", "Nedeleg Laouen", "Bon Natale", "Frohe Weihnachten", "Ni ti Burunya Chou", "Afishapa",
      "\u039A\u03B1\u03BB\u03AC \u03A7\u03C1\u03B9\u03C3\u03C4\u03BF\u03CD\u03B3\u03B5\u03BD\u03BD\u03B1",
      "\u10D2\u10D8\u10DA\u10DD\u10EA\u10D0\u10D5 \u10E8\u10DD\u10D1\u10D0-\u10D0\u10EE\u10D0\u10DA \u10EC\u10D4\u10DA\u10E1", "Juullimi Pilluarit",
      "Mele Kalikimaka", "Prettig Kerstfeest", "Kellemes kar\u00E1csonyi \u00FCnnepeket", "Gle\u00F0ileg j\u00F3l",
      "\u0936\u0941\u092D \u0915\u094D\u0930\u093F\u0938\u092E\u0938", "\u06A9\u0631\u0633\u0645\u0633", "Krismasasya shubhkaamnaa",
      "\u0A86\u0AA8\u0A82\u0AA6\u0AC0 \u0AA8\u0ABE\u0AA4\u0ABE\u0AB2", "\u09B6\u09C1\u09AD \u09AC\u09DC\u09A6\u09BF\u09A8",
      "\u0B95\u0BBF\u0BB1\u0BBF\u0BB8\u0BCD\u0BA4\u0BC1\u0BAE\u0BB8\u0BCD \u0BB5\u0BBE\u0BB4\u0BCD\u0BA4\u0BCD\u0BA4\u0BC1\u0B95\u0BCD\u0B95\u0BB3\u0BCD",
      "Khushal Borit Natala",
      "\u0C95\u0CCD\u0CB0\u0CBF\u0CB8\u0CCD \u0CAE\u0CB8\u0CCD \u0CB9\u0CAC\u0CCD\u0CAC\u0CA6 \u0CB6\u0CC1\u0CAD\u0CBE\u0CB7\u0CAF\u0C97\u0CB3\u0CC1",
      "Krismas Chibai", "\u0936\u0941\u092D \u0928\u093E\u0924\u093E\u0933",
      "\u0A15\u0A30\u0A3F\u0A38\u0A2E \u0A24\u0A47 \u0A28\u0A35\u0A3E\u0A70 \u0A38\u0A3E\u0A32 \u0A16\u0A41\u0A38\u0A3C\u0A3F\u0A2F\u0A3E\u0A70\u0A35\u0A3E\u0A32\u0A3E \u0A39\u0A4B\u0A35\u0A47",
      "Christmas inte mangalaashamsakal", "Christmas Subhakankshalu", "Selamat Natal", "Christmas MobArak", "Kir\u00EEsmes u ser sala we p\u00EEroz be",
      "Nollaig Shona Dhuit", "\u05D7\u05D2 \u05DE\u05D5\u05DC\u05D3 \u05E9\u05DE\u05D7", "Buon Natale", "Bon Natali",
      "\u3081\u308A\u30FC\u304F\u308A\u3059\u307E\u3059", "\uBA54\uB9AC \uD06C\uB9AC\uC2A4\uB9C8\uC2A4", "Priec\u00EFgus Ziemassv\u00BAtkus",
      "Linksm\u0173 Kal\u0117d\u0173", "\u0421\u0440\u0435\u045C\u0435\u043D \u0411\u043E\u0436\u0438\u043A", "Tratra ny Noely", "Il-Milied it-Tajjeb",
      "Selamat Hari Natal", "Puthuvalsara Aashamsakal", "Gozhqq Keshmish", "Nizhonigo Keshmish", "Quvianagli Anaiyyuniqpaliqsi", "Alussistuakeggtaarmek",
      "\u0915\u094D\u0930\u0938\u094D\u092E\u0938\u0915\u094B \u0936\u0941\u092D\u0915\u093E\u092E\u0928\u093E", "Meri Kirihimete",
      "barka d\u00E0 Kirs\u00ECmat\u00EC", "E ku odun", "Jabbama be salla Kirismati", "E keresimesi Oma", "Iselogbe", "Idara ukapade isua", "God Jul",
      "Maligayang Pasko", "Naragsak Nga Paskua", "Malipayon nga Pascua", "Maayong Pasko", "Maugmang Pasko", "Maabig ya pasko",
      "Weso\u0142ych \u015Awi\u0105t Bo\u017Cego Narodzenia", "Feliz Natal", "Cr\u1EB7ciun Fericit",
      "C \u0440\u043E\u0436\u0434\u0435\u0441\u0442\u0432\u043E\u043C", "Noheli nziza", "Manuia Le Kerisimasi", "Blithe Yule", "Nollaig Chridheil",
      "\u0425\u0440\u0438\u0441\u0442\u043E\u0441 \u0441\u0435 \u0440\u043E\u0434\u0438", "Vesele Vianoce", "Vesel Bo\u017Ei\u010D", "Kirismas Wacan",
      "Gese\u00EBnde Kersfees", "Feliz Navidad", "Bon Nadal", "Bo Nadal", "Eguberri on", "Sch\u00F6ni Wiehnachte", "Suk sarn warn Christmas", "Mutlu Noeller",
      "Seku Kulu",
      "\u0412\u0435\u0441\u0435\u043B\u043E\u0433\u043E \u0420\u0456\u0437\u0434\u0432\u0430 \u0456 \u0437 \u041D\u043E\u0432\u0438\u043C \u0420\u043E\u043A\u043E\u043C",
      "Chu\u0107 M\u01B0\u01F9g Gia\u0144g Sinh", "Nadolig Llawen", "Muve neKisimusi", "Izilokotho Ezihle Zamaholdeni", "toDwI'ma' qoS yItIvqu'",
      "Alass\u00EB a Hristomerend\u00EB", "Mereth Veren e-Doled Eruion", "Politically Correct Season Greetings" });

}
