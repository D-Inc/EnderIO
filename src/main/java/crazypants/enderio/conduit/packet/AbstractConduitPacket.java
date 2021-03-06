package crazypants.enderio.conduit.packet;

import java.util.UUID;

import com.enderio.core.common.BlockEnder;

import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.IConduitBundle;
import crazypants.enderio.conduit.TileConduitBundle;
import crazypants.enderio.conduit.registry.ConduitRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class AbstractConduitPacket<T extends IConduit> extends AbstractConduitBundlePacket {

  private UUID uuid;

  public AbstractConduitPacket() {
  }

  public AbstractConduitPacket(TileEntity tile, T conduit) {
    super(tile);
    this.uuid = ConduitRegistry.get(conduit).getNetworkUUID();
  }

  protected Class<? extends IConduit> getConType() {
    return ConduitRegistry.get(uuid).getBaseType();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    super.toBytes(buf);
    buf.writeLong(uuid.getMostSignificantBits());
    buf.writeLong(uuid.getLeastSignificantBits());
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    super.fromBytes(buf);
    uuid = new UUID(buf.readLong(), buf.readLong());
  }

  @SuppressWarnings("unchecked")
  protected T getTileCasted(MessageContext ctx) {
    World world = getWorld(ctx);
    if (world == null) {
      return null;
    }
    IConduitBundle te = BlockEnder.getAnyTileEntitySafe(world, getPos(), TileConduitBundle.class);
    if (te == null) {
      return null;
    }
    return (T) te.getConduit(getConType());
  }
}
