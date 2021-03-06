package crazypants.enderio.machine.obelisk.relocator;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

import crazypants.enderio.ModObject;
import crazypants.enderio.machine.SlotDefinition;
import crazypants.enderio.machine.farm.PacketFarmAction;
import crazypants.enderio.machine.obelisk.spawn.TileEntityAbstractSpawningObelisk;
import crazypants.enderio.network.PacketHandler;
import info.loenwind.autosave.annotations.Storable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import static crazypants.enderio.capacitor.CapacitorKey.AVERSION_POWER_BUFFER;
import static crazypants.enderio.capacitor.CapacitorKey.AVERSION_POWER_INTAKE;
import static crazypants.enderio.capacitor.CapacitorKey.AVERSION_POWER_USE;

@Storable
public class TileRelocatorObelisk extends TileEntityAbstractSpawningObelisk {

  private final Map<EntityLivingBase, Integer> relocationQueue = new WeakHashMap<EntityLivingBase, Integer>();
  private final Random rand = new Random();

  public TileRelocatorObelisk() {
    super(new SlotDefinition(12, 0), AVERSION_POWER_INTAKE, AVERSION_POWER_BUFFER, AVERSION_POWER_USE);
  }
  
  @Override
  public @Nonnull String getMachineName() {
    return ModObject.blockSpawnRelocator.getUnlocalisedName();
  }

  @Override
  public Result isSpawnPrevented(EntityLivingBase mob) {
    if (redstoneCheckPassed && hasPower() && isMobInRange(mob) && isMobInFilter(mob)) {
      relocationQueue.put(mob, null);
      return Result.DONE;
    } else {
      return Result.NEXT;
    }
  }

  @Override
  protected boolean processTasks(boolean redstoneCheck) {
    if (!relocationQueue.isEmpty()) {
      AxisAlignedBB targetBB = new AxisAlignedBB(getPos(), getPos().add(1, 1, 1)).expand(4, 1, 4);
      Iterator<EntityLivingBase> iterator = relocationQueue.keySet().iterator();
      while (iterator.hasNext()) {
        EntityLivingBase mob = iterator.next();
        if (mob == null || mob.isDead || worldObj.getEntityByID(mob.getEntityId()) == null || mob.ticksExisted > 2 * 60 * 20 || relocationQueue.size() > 100) {
          iterator.remove();
        } else if (hasPower() && rand.nextFloat() < .025f) {
          AxisAlignedBB mobbb = mob.getEntityBoundingBox();
          if (targetBB.intersectsWith(mobbb)) {
            iterator.remove();
          } else {
            double x = getPos().getX() + .5 + Math.random() * 8d - 4.0;
            double y = getPos().getY() + .5 + Math.random() * 3d - 1.5;
            double z = getPos().getZ() + .5 + Math.random() * 8d - 4.0;
            double dx = mobbb.maxX - mobbb.minX;
            double dy = mobbb.maxY - mobbb.minY;
            double dz = mobbb.maxZ - mobbb.minZ;
            AxisAlignedBB bb = new AxisAlignedBB(x - dx / 2, y, z - dz / 2, x + dx / 2, y + dy, z + dz / 2);

            boolean spaceClear = worldObj.checkNoEntityCollision(bb, mob) && worldObj.getCollisionBoxes(mob, bb).isEmpty()
                && (worldObj.containsAnyLiquid(bb) == mob.isCreatureType(EnumCreatureType.WATER_CREATURE, false));

            if (spaceClear) {
              PacketHandler.INSTANCE.sendToAllAround(new PacketFarmAction(new BlockPos(mob.posX, mob.posY, mob.posZ)),
                  new TargetPoint(worldObj.provider.getDimension(), mob.posX, mob.posY, mob.posZ, 64));
              mob.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
              mob.setPositionAndUpdate(x - dx / 2, y, z - dz / 2);
              mob.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
              PacketHandler.INSTANCE.sendToAllAround(new PacketFarmAction(new BlockPos(mob.posX, mob.posY, mob.posZ)),
                  new TargetPoint(worldObj.provider.getDimension(), mob.posX, mob.posY, mob.posZ, 64));
              iterator.remove();
            }
          }
        }
      }
    }
    return super.processTasks(redstoneCheck);
  }

  @Override
  public SpawnObeliskAction getSpawnObeliskAction() {
    return SpawnObeliskAction.RELOCATE;
  }

}
