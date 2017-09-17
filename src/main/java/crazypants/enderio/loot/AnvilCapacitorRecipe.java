package crazypants.enderio.loot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import crazypants.enderio.EnderIO;
import crazypants.enderio.Log;
import crazypants.enderio.capacitor.CapacitorHelper;
import crazypants.enderio.capacitor.CapacitorHelper.SetType;
import crazypants.util.NbtValue;
import crazypants.util.Prep;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static crazypants.enderio.ModObject.itemBasicCapacitor;

public class AnvilCapacitorRecipe {

  public static void create() {
    MinecraftForge.EVENT_BUS.register(AnvilCapacitorRecipe.class);
  }

  @SubscribeEvent
  public static void handleAnvilEvent(AnvilUpdateEvent evt) {
    ItemStack left = evt.getLeft();
    ItemStack right = evt.getRight();

    if (Prep.isInvalid(left) || Prep.isInvalid(right) || left.getItem() != itemBasicCapacitor.getItem() || right.getItem() != itemBasicCapacitor.getItem()
        || left.getMetadata() != 3 || right.getMetadata() != 3) {
      return;
    }

    List<Pair<String, Float>> dataLeft = CapacitorHelper.getCapDataRaw(left);
    List<Pair<String, Float>> dataRight = CapacitorHelper.getCapDataRaw(right);

    if (dataLeft == null || dataRight == null || dataLeft.isEmpty() || dataRight.isEmpty()) {
      return;
    }

    Map<WeightedUpgrade, Pair<Float, Float>> data = new HashMap<WeightedUpgrade, Pair<Float, Float>>();
    float seed = 0f;

    for (Pair<String, Float> pair : dataLeft) {
      WeightedUpgrade weightedUpgrade = WeightedUpgrade.getByRawString(pair.getKey());
      if (weightedUpgrade == null) {
        return;
      }
      data.put(weightedUpgrade, Pair.of(pair.getValue(), (Float) null));
      seed += pair.getValue();
    }

    for (Pair<String, Float> pair : dataRight) {
      WeightedUpgrade weightedUpgrade = WeightedUpgrade.getByRawString(pair.getKey());
      if (weightedUpgrade == null) {
        return;
      }
      if (data.containsKey(weightedUpgrade)) {
        data.put(weightedUpgrade, Pair.of(pair.getValue(), data.get(weightedUpgrade).getKey()));
      } else {
        data.put(weightedUpgrade, Pair.of(pair.getValue(), (Float) null));
      }
      seed += pair.getValue();
    }

    Random rand = new Random((long) (seed * 1000));

    Map<WeightedUpgrade, Float> result = new HashMap<WeightedUpgrade, Float>();

    for (WeightedUpgrade wa : WeightedUpgrade.values()) {
      if (data.containsKey(wa)) {
        final float combine = combine(rand, data.get(wa));
        result.put(wa, combine);
        Log.debug("Combining " + wa + " " + data.get(wa).getKey() + " and " + data.get(wa).getValue() + " to " + combine);
      }
    }

    int baselevel = Math.max(CapacitorHelper.getCapLevelRaw(left), CapacitorHelper.getCapLevelRaw(right));
    if (baselevel < 5 && rand.nextFloat() < .5f) {
      baselevel++;
    }

    ItemStack stack = left.copy();

    String name = LootSelector.buildBaseName(EnderIO.lang.localize("itemBasicCapacitor.name"), baselevel);
    stack = CapacitorHelper.addCapData(stack, SetType.LEVEL, null, baselevel);

    for (Entry<WeightedUpgrade, Float> entry : result.entrySet()) {
      stack = CapacitorHelper.addCapData(stack, entry.getKey().setType, entry.getKey().capacitorKey, entry.getValue());
      name = LootSelector.buildName(EnderIO.lang.localize(entry.getKey().langKey, name), entry.getValue());
    }

    NbtValue.CAPNAME.setString(stack, name);

    evt.setOutput(stack);
    evt.setCost(baselevel * baselevel);
  }

  static private float combine(Random rand, Pair<Float, Float> pair) {
    if (pair.getRight() == null) {
      return pair.getLeft();
    }
    return combine(rand, pair.getLeft(), pair.getRight());
  }

  static private float combine(Random rand, float a, float b) {
    float min = a < b ? a : b;
    float center = a < b ? b : a;
    float offsetLow = center - min;
    float max = Math.min(center + offsetLow, 4.75f);
    float offsetHigh = max - center;

    float gaussian = (float) rand.nextGaussian();
    if (gaussian <= 0) {
      return Math.max(min, center + gaussian * offsetLow);
    } else {
      return Math.min(max, center + gaussian * offsetHigh);
    }
  }

}
