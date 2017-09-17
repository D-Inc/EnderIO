package crazypants.enderio.conduit.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import crazypants.enderio.Log;
import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.geom.Offset;
import crazypants.enderio.conduit.geom.Offsets;
import crazypants.enderio.conduit.render.ConduitRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConduitRegistry {

  public static class ConduitInfo {
    private final @Nonnull UUID networkUUID;
    private final @Nonnull Class<? extends IConduit> baseType;
    private final @Nonnull Offset none, x, y, z;
    private boolean canConnectToAnything;
    @SideOnly(Side.CLIENT)
    private @Nullable Collection<ConduitRenderer> renderers;
    private @Nonnull Collection<Class<? extends IConduit>> members = new ArrayList<Class<? extends IConduit>>();

    /**
     * Constructs a new ConduitInfo object.
     * 
     * @param baseType
     *          An interface that identifies this type of conduit.
     * @param none
     *          The preferred location of the conduit node in the bundle.
     * @param x
     *          The preferred location of the conduit arm in the bundle on the X
     *          axis.
     * @param y
     *          The preferred location of the conduit arm in the bundle on the Y
     *          axis.
     * @param z
     *          The preferred location of the conduit arm in the bundle on the Z
     *          axis.
     */
    public ConduitInfo(@Nonnull Class<? extends IConduit> baseType, @Nonnull Offset none, @Nonnull Offset x, @Nonnull Offset y, @Nonnull Offset z) {
      this.baseType = baseType;
      this.none = none;
      this.x = x;
      this.y = y;
      this.z = z;
      this.networkUUID = UUID.nameUUIDFromBytes(baseType.getName().getBytes());
    }

    /**
     * Returns the UUID that identifies the conduit type.
     */
    public @Nonnull UUID getNetworkUUID() {
      return networkUUID;
    }

    /**
     * Returns the interface that identifies the conduit type.
     */
    public @Nonnull Class<? extends IConduit> getBaseType() {
      return baseType;
    }

    /**
     * Conduits that can connect to any block type (but AIR) need to have this
     * enabled so the GUI selector knows to show GUIs for unconnected sides.
     */
    public void setCanConnectToAnything() {
      this.canConnectToAnything = true;
    }

    /**
     * Conduits that can connect to any block type (but AIR) need to have this
     * enabled so the GUI selector knows to show GUIs for unconnected sides.
     */
    public boolean canConnectToAnything() {
      return canConnectToAnything;
    }

    /**
     * Adds a renderer to the list of conduit renderers. This can called
     * multiple times, and the registered renderers are not linked to this
     * conduit type.
     * <p>
     * <em>CLIENT only!</em>
     */
    @SideOnly(Side.CLIENT)
    public void addRenderer(@Nonnull ConduitRenderer renderer) {
      if (renderers == null) {
        renderers = new ArrayList<ConduitRenderer>();
      }
      renderers.add(renderer);
    }

    /**
     * Returns the registered renderers for this conduit type. Can be an empty
     * list.
     * <p>
     * <em>CLIENT only!</em>
     */
    @SideOnly(Side.CLIENT)
    public @Nonnull Collection<ConduitRenderer> getRenderers() {
      if (renderers == null) {
        return Collections.<ConduitRenderer> emptyList();
      }
      return renderers;
    }

    /**
     * Adds a conduit implementation class for this conduit type. The given
     * class <em>must</em> implement the base type (the interface that
     * identifies the conduit type). This must be called at least once for any
     * conduit type.
     */
    public void addMember(@Nonnull Class<? extends IConduit> member) {
      members.add(member);
    }

    /**
     * Returns a collection of registered conduit implementation classes.
     */
    public @Nonnull Collection<Class<? extends IConduit>> getMembers() {
      return members;
    }

    // internal use only
    protected @Nonnull Offset getNone() {
      return none;
    }

    // internal use only
    protected @Nonnull Offset getX() {
      return x;
    }

    // internal use only
    protected @Nonnull Offset getY() {
      return y;
    }

    // internal use only
    protected @Nonnull Offset getZ() {
      return z;
    }

  }

  private static final List<ConduitInfo> conduitInfos = new ArrayList<ConduitInfo>();
  private static final Map<Class<? extends IConduit>, ConduitInfo> conduitCLassMap = new IdentityHashMap<Class<? extends IConduit>, ConduitInfo>();
  private static final Map<UUID, ConduitInfo> conduitUUIDMap = new HashMap<UUID, ConduitInfo>();

  private static final Map<Class<? extends IConduit>, UUID> conduitMemberMapF = new IdentityHashMap<Class<? extends IConduit>, UUID>();
  private static final Map<UUID, Class<? extends IConduit>> conduitMemberMapR = new HashMap<UUID, Class<? extends IConduit>>();

  /**
   * Register an old name for a conduit member class after renaming it. Allows
   * conduits of that type to be read from the save game. The aliasName should
   * be the ".getName()" of the old class.
   */
  public static void registerAlias(Class<? extends IConduit> member, String aliasName) {
    final UUID uuid = UUID.nameUUIDFromBytes(aliasName.getBytes());
    conduitMemberMapR.put(uuid, member);
  }

  /**
   * Register an old name for a conduit identity interface after renaming it.
   * Allows conduits of that type to be read from the save game. The aliasName
   * should be the ".getName()" of the old interface. The given ConduitInfo must
   * already be registered.
   */
  public static void registerAlias(ConduitInfo info, String aliasName) {
    final UUID uuid = UUID.nameUUIDFromBytes(aliasName.getBytes());
    conduitUUIDMap.put(uuid, info);
  }

  /**
   * Register a new conduit type.
   * <p>
   * Will throw a RuntimeException if no location in the bundle could be found
   * for the conduit.
   */
  public static void register(ConduitInfo info) {
    conduitInfos.add(info);
    Collections.sort(conduitInfos, UUID_COMPERATOR);
    conduitUUIDMap.put(info.getNetworkUUID(), info);
    conduitCLassMap.put(info.getBaseType(), info);
    for (Class<? extends IConduit> member : info.getMembers()) {
      conduitCLassMap.put(member, info);
      final UUID uuid = UUID.nameUUIDFromBytes(member.getName().getBytes());
      conduitMemberMapF.put(member, uuid);
      conduitMemberMapR.put(uuid, member);
    }

    Offset none = info.getNone(), x = info.getX(), y = info.getY(), z = info.getZ();
    while (!Offsets.registerOffsets(info.getBaseType(), none, x, y, z)) {
      z = z.next();
      if (z == null) {
        z = Offset.first();
      }
      if (z == info.getZ()) {
        y = y.next();
        if (y == null) {
          y = Offset.first();
        }
        if (y == info.getY()) {
          x = x.next();
          if (x == null) {
            x = Offset.first();
          }
          if (x == info.getX()) {
            none = none.next();
            if (none == null) {
              none = Offset.first();
            }
          }
        }
      }
      if (z == info.getZ() && y == info.getY() && x == info.getX() && none == info.getNone()) {
        throw new RuntimeException("Failed to find free offsets for " + info.getBaseType());
      }
    }
  }

  /**
   * Add a member to an already registered conduit type. The given 'info' MUST already be registered, you can access all registered types with getAll().
   * <p>
   * Please be advised that is is considered a 'hack' and may or may not work. Especially conduit types where the members need to interact with each other will
   * not magically work.
   **/
  public static void injectMember(ConduitInfo info, Class<? extends IConduit> member) {
    if (!conduitInfos.contains(info)) {
      throw new IllegalArgumentException("The specified ConduitInfo has not been added yet");
    }
    conduitCLassMap.put(member, info);
    final UUID uuid = UUID.nameUUIDFromBytes(member.getName().getBytes());
    conduitMemberMapF.put(member, uuid);
    conduitMemberMapR.put(uuid, member);
  }

  /**
   * Returns the ConduitInfo for the given conduit instance (member).
   */
  public static ConduitInfo get(IConduit conduit) {
    return conduitCLassMap.get(conduit.getClass());
  }

  /**
   * Returns the ConduitInfo for the given conduit interface UUID.
   */
  public static ConduitInfo get(UUID uuid) {
    return conduitUUIDMap.get(uuid);
  }

  /**
   * Returns all registered ConduitInfos. This list is always sorted the same
   * way.
   */
  public static Collection<ConduitInfo> getAll() {
    return conduitInfos;
  }

  /**
   * Returns a new conduit instance (member) for the given member UUID (
   * <em>not</em> interface UUID).
   */
  public static IConduit getInstance(UUID uuid) {
    final Class<? extends IConduit> clazz = conduitMemberMapR.get(uuid);
    if (clazz == null) {
      Log.warn("Ignoring unregistered conduit type " + uuid);
      return null;
    }
    try {
      return clazz.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Could not create an instance of the conduit of type " + uuid, e);
    }
  }

  /**
   * Returns the member UUID for the given conduit instance (member). This is
   * not the interface/network UUID!
   */
  public static UUID getInstanceUUID(IConduit conduit) {
    return conduitMemberMapF.get(conduit.getClass());
  }

  private static boolean sortingSupported = true;

  public static void sort(List<IConduit> conduits) {
    if (sortingSupported) {
      try {
        Collections.sort(conduits, CONDUIT_COMPERATOR);
      } catch (UnsupportedOperationException e) {
        // On older versions of Java this is not supported. We don't care, the list is only sorted to optimize our model cache.
        sortingSupported = false;
      }
    }
  }

  private static final Comparator<ConduitInfo> UUID_COMPERATOR = new Comparator<ConduitInfo>() {

    @Override
    public int compare(ConduitInfo o1, ConduitInfo o2) {
      return o1.getNetworkUUID().compareTo(o2.getNetworkUUID());
    }

  };

  private static final Comparator<IConduit> CONDUIT_COMPERATOR = new Comparator<IConduit>() {

    @Override
    public int compare(IConduit o1, IConduit o2) {
      return get(o1).getNetworkUUID().compareTo(get(o2).getNetworkUUID());
    }

  };

}
