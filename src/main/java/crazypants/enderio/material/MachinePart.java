package crazypants.enderio.material;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

public enum MachinePart {

  MACHINE_CHASSI("machineChassi"),
  BASIC_GEAR("basicGear", "gearStone");

  public final @Nonnull String baseName;
  public final @Nonnull String unlocalisedName;
  public final @Nonnull String iconKey;
  public final @Nonnull String oreDict;

  public static List<ResourceLocation> resources() {
    List<ResourceLocation> res = new ArrayList<ResourceLocation>(values().length);
    for(MachinePart c : values()) {
      res.add(new ResourceLocation(c.iconKey));
    }
    return res;
  }
  
  private MachinePart(@Nonnull String baseName) {
    this(baseName, "item" + StringUtils.capitalize(baseName));
  }

  private MachinePart(@Nonnull String baseName, @Nonnull String oreDict) {
    this.baseName = baseName;
    this.unlocalisedName = "enderio." + baseName;
    this.iconKey = "enderio:" + baseName;
    this.oreDict = oreDict;
  }
  
  public static void registerOres(@Nonnull Item item) {
    for (MachinePart m : values()) {
      OreDictionary.registerOre(m.oreDict, new ItemStack(item, 1, m.ordinal()));
    }
  }
}
