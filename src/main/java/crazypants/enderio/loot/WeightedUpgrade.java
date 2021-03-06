package crazypants.enderio.loot;

import java.util.ArrayList;
import java.util.List;

import crazypants.enderio.capacitor.CapacitorHelper.SetType;
import crazypants.enderio.capacitor.CapacitorKey;
import net.minecraft.util.WeightedRandom;

public enum WeightedUpgrade {
  SMELTING(SetType.NAME, CapacitorKey.ALLOY_SMELTER_POWER_USE, "smelting", 10),
  INTAKE(SetType.TYPE, CapacitorKey.ALLOY_SMELTER_POWER_INTAKE, "intake", 20),
  BUFFER(SetType.TYPE, CapacitorKey.ALLOY_SMELTER_POWER_BUFFER, "buffer", 20),
  CRAFTING(SetType.NAME, CapacitorKey.CRAFTER_TICKS, "crafting", 10),
  AREA(SetType.TYPE, CapacitorKey.ATTRACTOR_RANGE, "area", 5),
  GREEN(SetType.NAME, CapacitorKey.FARM_BONUS_SIZE, "green", 10),
  RED(SetType.NAME, CapacitorKey.STIRLING_POWER_GEN, "red", 10),
  MOBBY(SetType.NAME, CapacitorKey.SPAWNER_SPEEDUP, "mobby", 5),
  CRUSHED(SetType.NAME, CapacitorKey.SAG_MILL_POWER_USE, "crushed", 15),
  CLEANCUT(SetType.NAME, CapacitorKey.SLICE_POWER_USE, "cleancut", 5),
  TIGHT(SetType.NAME, CapacitorKey.SOUL_BINDER_POWER_USE, "tight", 5),
  AA(SetType.NAME, CapacitorKey.PAINTER_POWER_USE, "aa", 10),

  ;

  public final SetType setType;
  public final CapacitorKey capacitorKey;
  public final String langKey;

  private WeightedUpgrade(SetType setType, CapacitorKey capacitorKey, String langKey, int weight) {
    this.setType = setType;
    this.capacitorKey = capacitorKey;
    this.langKey = "loot.capacitor." + langKey;
    WeightedUpgradeImpl.weightedUpgrades.add(new WeightedUpgradeImpl(weight, this));
  }

  public static List<WeightedUpgrade.WeightedUpgradeImpl> getWeightedupgrades() {
    return WeightedUpgradeImpl.weightedUpgrades;
  }

  public static class WeightedUpgradeImpl extends WeightedRandom.Item {
    private static final List<WeightedUpgrade.WeightedUpgradeImpl> weightedUpgrades = new ArrayList<WeightedUpgrade.WeightedUpgradeImpl>();
    private final WeightedUpgrade upgrade;

    private WeightedUpgradeImpl(int weight, WeightedUpgrade upgrade) {
      super(weight);
      this.upgrade = upgrade;
    }

    public WeightedUpgrade getUpgrade() {
      return upgrade;
    }

  }

  public static WeightedUpgrade getByRawString(String raw) {
    for (WeightedUpgrade wa : values()) {
      switch (wa.setType) {
      case NAME:
        if (wa.capacitorKey.getName().equals(raw)) {
          return wa;
        }
        break;
      case TYPE:
        if (wa.capacitorKey.getValueType().getName().equals(raw)) {
          return wa;
        }
        break;
      default:
        break;
      }
    }
    return null;
  }

}