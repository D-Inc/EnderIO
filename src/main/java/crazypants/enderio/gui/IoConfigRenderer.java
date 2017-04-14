package crazypants.enderio.gui;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import com.enderio.core.client.render.BoundingBox;
import com.enderio.core.client.render.ColorUtil;
import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.util.BlockCoord;
import com.enderio.core.common.vecmath.Camera;
import com.enderio.core.common.vecmath.Matrix4d;
import com.enderio.core.common.vecmath.VecmathUtil;
import com.enderio.core.common.vecmath.Vector3d;
import com.enderio.core.common.vecmath.Vector4f;
import com.enderio.core.common.vecmath.Vertex;

import crazypants.enderio.machine.IIoConfigurable;
import crazypants.enderio.machine.IoMode;
import crazypants.enderio.machine.PacketIoMode;
import crazypants.enderio.machine.alloy.BlockAlloySmelter;
import crazypants.enderio.network.PacketHandler;
import crazypants.enderio.teleport.TravelController;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;

public class IoConfigRenderer {

  // protected static final RenderBlocks RB = new RenderBlocks();

  private boolean dragging = false;
  private float pitch = 0;
  private float yaw = 0;
  private double distance;
  private long initTime;

  private Minecraft mc = Minecraft.getMinecraft();
  private World world = mc.thePlayer.worldObj;

  private final Vector3d origin = new Vector3d();
  private final Vector3d eye = new Vector3d();
  private final Camera camera = new Camera();
  private final Matrix4d pitchRot = new Matrix4d();
  private final Matrix4d yawRot = new Matrix4d();

  public BlockCoord originBC;

  private List<BlockCoord> configurables = new ArrayList<BlockCoord>();
  private List<BlockCoord> neighbours = new ArrayList<BlockCoord>();

  private SelectedFace selection;

  private boolean renderNeighbours = true;
  private boolean inNeigButBounds = false;

  public IoConfigRenderer(IIoConfigurable configuarble) {
    this(Collections.singletonList(configuarble.getLocation()));
  }

  public IoConfigRenderer(List<BlockCoord> configurables) {
    this.configurables.addAll(configurables);

    Vector3d c;
    Vector3d size;
    if (configurables.size() == 1) {
      BlockCoord bc = configurables.get(0);
      c = new Vector3d(bc.x + 0.5, bc.y + 0.5, bc.z + 0.5);
      size = new Vector3d(1, 1, 1);
    } else {
      Vector3d min = new Vector3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
      Vector3d max = new Vector3d(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
      for (BlockCoord bc : configurables) {
        min.set(Math.min(bc.x, min.x), Math.min(bc.y, min.y), Math.min(bc.z, min.z));
        max.set(Math.max(bc.x, max.x), Math.max(bc.y, max.y), Math.max(bc.z, max.z));
      }
      size = new Vector3d(max);
      size.sub(min);
      size.scale(0.5);
      c = new Vector3d(min.x + size.x, min.y + size.y, min.z + size.z);
      size.scale(2);
    }

    originBC = new BlockCoord((int) c.x, (int) c.y, (int) c.z);
    origin.set(c);
    pitchRot.setIdentity();
    yawRot.setIdentity();

    pitch = -mc.thePlayer.rotationPitch;
    yaw = 180 - mc.thePlayer.rotationYaw;

    distance = Math.max(Math.max(size.x, size.y), size.z) + 4;

    for (BlockCoord bc : configurables) {
      for (EnumFacing dir : EnumFacing.VALUES) {
        BlockCoord loc = bc.getLocation(dir);
        if (!configurables.contains(loc)) {
          neighbours.add(loc);
        }
      }
    }

    world = mc.thePlayer.worldObj;
  }

  public void init() {
    initTime = System.currentTimeMillis();
  }

  public SelectedFace getSelection() {
    return selection;
  }

  public void handleMouseInput() {

    if (Mouse.getEventButton() == 0) {
      dragging = Mouse.getEventButtonState();
    }

    if (dragging) {

      double dx = (Mouse.getEventDX() / (double) mc.displayWidth);
      double dy = (Mouse.getEventDY() / (double) mc.displayHeight);
      if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
        distance -= dy * 15;
      } else {
        yaw -= 4 * dx * 180;
        pitch += 2 * dy * 180;
        pitch = (float) VecmathUtil.clamp(pitch, -80, 80);
      }
    }

    distance -= Mouse.getEventDWheel() * 0.01;
    distance = VecmathUtil.clamp(distance, 0.01, 200);

    long elapsed = System.currentTimeMillis() - initTime;

    //Mouse Over
    int x = Mouse.getEventX();
    int y = Mouse.getEventY();
    Vector3d start = new Vector3d();
    Vector3d end = new Vector3d();
    if (camera.getRayForPixel(x, y, start, end)) {
      end.scale(distance * 2);
      end.add(start);
      updateSelection(start, end);
    }

    //Mouse pressed on configurable side
    if (!Mouse.getEventButtonState() && camera.isValid() && elapsed > 500) {
      if (Mouse.getEventButton() == 1) {
        if (selection != null) {
          selection.config.toggleIoModeForFace(selection.face);
          PacketHandler.INSTANCE.sendToServer(new PacketIoMode(selection.config, selection.face));
        }
      } else if (Mouse.getEventButton() == 0 && inNeigButBounds) {
        renderNeighbours = !renderNeighbours;
      }
    }
  }

  private void updateSelection(Vector3d start, Vector3d end) {
    start.add(origin);
    end.add(origin);
    List<RayTraceResult> hits = new ArrayList<RayTraceResult>();

    for (BlockCoord bc : configurables) {
      IBlockState bs = world.getBlockState(bc.getBlockPos());
      Block block = bs.getBlock();
      if (block != null) {        
        RayTraceResult hit = bs.collisionRayTrace(world, bc.getBlockPos(), new Vec3d(start.x, start.y, start.z), new Vec3d(end.x, end.y, end.z));
        if (hit != null) {
          hits.add(hit);
        }
      }
    }
    selection = null;
    RayTraceResult hit = getClosestHit(new Vec3d(start.x, start.y, start.z), hits);
    if (hit != null) {
      TileEntity te = world.getTileEntity(hit.getBlockPos());
      if (te instanceof IIoConfigurable) {
        IIoConfigurable configuarble = (IIoConfigurable) te;
        EnumFacing face = hit.sideHit;
        selection = new SelectedFace(configuarble, face);
      }
    }
  }

  public static RayTraceResult getClosestHit(Vec3d origin, Collection<RayTraceResult> candidates) {
    double minLengthSquared = Double.POSITIVE_INFINITY;
    RayTraceResult closest = null;

    for (RayTraceResult hit : candidates) {
      if (hit != null) {
        double lengthSquared = hit.hitVec.squareDistanceTo(origin);
        if (lengthSquared < minLengthSquared) {
          minLengthSquared = lengthSquared;
          closest = hit;
        }
      }
    }
    return closest;
  }

  public void drawScreen(int par1, int par2, float partialTick, Rectangle vp, Rectangle parentBounds) {

    if (!updateCamera(partialTick, vp.x, vp.y, vp.width, vp.height)) {
      return;
    }

    applyCamera(partialTick);
    TravelController.instance.setSelectionEnabled(false);
    renderScene();
    TravelController.instance.setSelectionEnabled(true);
    renderSelection();
    renderOverlay(par1, par2);
  }

  private void renderSelection() {
    if (selection == null) {
      return;
    }

    BoundingBox bb = new BoundingBox(selection.config.getLocation());

    TextureAtlasSprite icon = BlockAlloySmelter.selectedFaceIcon.get(TextureAtlasSprite.class);
    List<Vertex> corners = bb.getCornersWithUvForFace(selection.face, icon.getMinU(), icon.getMaxU(), icon.getMinV(), icon.getMaxV());

    GlStateManager.disableDepth();
    GlStateManager.disableLighting();
    
    RenderUtil.bindBlockTexture();    
    VertexBuffer tes = Tessellator.getInstance().getBuffer();

    GlStateManager.color(1, 1, 1);
    Vector3d trans = new Vector3d((-origin.x) + eye.x, (-origin.y) + eye.y, (-origin.z) + eye.z);
    tes.setTranslation(trans.x, trans.y, trans.z);
    RenderUtil.addVerticesToTessellator(corners, DefaultVertexFormats.POSITION_TEX, true);
    Tessellator.getInstance().draw();
    tes.setTranslation(0, 0, 0);

  }

  private void renderOverlay(int mx, int my) {
    Rectangle vp = camera.getViewport();
    ScaledResolution scaledresolution = new ScaledResolution(mc);

    int vpx = vp.x / scaledresolution.getScaleFactor();
    int vph = vp.height / scaledresolution.getScaleFactor();
    int vpw = vp.width / scaledresolution.getScaleFactor();
    int vpy = (int) ((float) (vp.y + vp.height - 4) / (float) scaledresolution.getScaleFactor());

    GL11.glViewport(0, 0, mc.displayWidth, mc.displayHeight);
    GL11.glMatrixMode(GL11.GL_PROJECTION);
    GL11.glLoadIdentity();
    GL11.glOrtho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
    GL11.glMatrixMode(GL11.GL_MODELVIEW);
    GL11.glLoadIdentity();
    GL11.glTranslatef(vpx, vpy, -2000.0F);

    GlStateManager.disableLighting();

    int x = vpw - 17;
    int y = vph - 18;

    mx -= vpx;
    my -= vpy;

    if (mx >= x && mx <= x + IconEIO.IO_WHATSIT.width && my >= y && my <= y + IconEIO.IO_WHATSIT.height) {
      GlStateManager.enableBlend();
      RenderUtil.renderQuad2D(x, y, 0, IconEIO.IO_WHATSIT.width, IconEIO.IO_WHATSIT.height, new Vector4f(0.4f, 0.4f, 0.4f, 0.6f));
      GlStateManager.disableBlend();
      inNeigButBounds = true;
    } else {
      inNeigButBounds = false;
    }

    GL11.glColor3f(1, 1, 1);
    IconEIO.map.render(IconEIO.IO_WHATSIT, x, y, true);

    if (selection != null) {
      IconEIO ioIcon = null;
      int yOffset = 0;
      // INPUT
      IoMode mode = selection.config.getIoMode(selection.face);
      if (mode == IoMode.PULL) {
        ioIcon = IconEIO.INPUT;
      } else if (mode == IoMode.PUSH) {
        ioIcon = IconEIO.OUTPUT;
      } else if (mode == IoMode.PUSH_PULL) {
        ioIcon = IconEIO.INPUT_OUTPUT;
      } else if (mode == IoMode.DISABLED) {
        ioIcon = IconEIO.DISABLED;
        yOffset = 5;
      }

      y = vph - mc.fontRendererObj.FONT_HEIGHT - 2;
      mc.fontRendererObj.drawString(getLabelForMode(mode), 4, y, ColorUtil.getRGB(Color.white));
      if (ioIcon != null) {
        int w = mc.fontRendererObj.getStringWidth(mode.getLocalisedName());
        double xd = (w - ioIcon.width) / 2;
        xd = Math.max(0, w);
        xd /= 2;
        xd += 4;
        xd /= scaledresolution.getScaleFactor();
        ioIcon.getMap().render(ioIcon, xd, y - mc.fontRendererObj.FONT_HEIGHT - 2 - yOffset, true);
      }
    }
  }

  protected String getLabelForMode(IoMode mode) {
    return mode.getLocalisedName();
  }

  private void renderScene() {
    
    GlStateManager.enableCull();
    GlStateManager.enableRescaleNormal();
    
    RenderHelper.disableStandardItemLighting();
    mc.entityRenderer.disableLightmap();
    RenderUtil.bindBlockTexture();
    
    GlStateManager.disableLighting();
    GlStateManager.enableTexture2D();
    GlStateManager.enableAlpha();

    Vector3d trans = new Vector3d((-origin.x) + eye.x, (-origin.y) + eye.y, (-origin.z) + eye.z);
    
    BlockRenderLayer oldRenderLayer = MinecraftForgeClient.getRenderLayer();
    try {
      for (BlockRenderLayer layer : BlockRenderLayer.values()) {
        ForgeHooksClient.setRenderLayer(layer);
        setGlStateForPass(layer, false);
        doWorldRenderPass(trans, configurables, layer);
      }

      if (renderNeighbours) {
        for (BlockRenderLayer layer : BlockRenderLayer.values()) {
          ForgeHooksClient.setRenderLayer(layer);
          setGlStateForPass(layer, true);
          doWorldRenderPass(trans, neighbours, layer);
        }
      }
    } finally {
      ForgeHooksClient.setRenderLayer(oldRenderLayer);
    }
    
    RenderHelper.enableStandardItemLighting();
    GlStateManager.enableLighting();
    TileEntityRendererDispatcher.instance.entityX = origin.x - eye.x;
    TileEntityRendererDispatcher.instance.entityY = origin.y - eye.y;
    TileEntityRendererDispatcher.instance.entityZ = origin.z - eye.z;
    TileEntityRendererDispatcher.staticPlayerX = origin.x - eye.x;
    TileEntityRendererDispatcher.staticPlayerY = origin.y - eye.y;
    TileEntityRendererDispatcher.staticPlayerZ = origin.z - eye.z;

    for (int pass = 0; pass < 2; pass++) {
      ForgeHooksClient.setRenderPass(pass);
      setGlStateForPass(pass, false);
      doTileEntityRenderPass(configurables, pass);
      if (renderNeighbours) {
        setGlStateForPass(pass, true);
        doTileEntityRenderPass(neighbours, pass);
      }
    }
    ForgeHooksClient.setRenderPass(-1);
    setGlStateForPass(0, false);
  }

  private void doTileEntityRenderPass(List<BlockCoord> blocks, int pass) {
    for (BlockCoord bc : blocks) {
      TileEntity tile = world.getTileEntity(bc.getBlockPos());
      if (tile != null) {
        if (tile.shouldRenderInPass(pass)) {
          Vector3d at = new Vector3d(eye.x, eye.y, eye.z);
          at.x += bc.x - origin.x;
          at.y += bc.y - origin.y;
          at.z += bc.z - origin.z;
          if(tile.getClass() == TileEntityChest.class) {
            TileEntityChest chest = (TileEntityChest)tile;     
            if(chest.adjacentChestXNeg != null) {
              tile = chest.adjacentChestXNeg;
              at.x--;
            }  else if(chest.adjacentChestZNeg != null) {
              tile = chest.adjacentChestZNeg;
              at.z--;
            }
          }
          TileEntityRendererDispatcher.instance.renderTileEntityAt(tile, at.x, at.y, at.z, 0);
        }
      }
    }    
  }

  private void doWorldRenderPass(Vector3d trans, List<BlockCoord> blocks, BlockRenderLayer layer) {

    VertexBuffer wr = Tessellator.getInstance().getBuffer();
    wr.begin(7, DefaultVertexFormats.BLOCK);

    Tessellator.getInstance().getBuffer().setTranslation(trans.x, trans.y, trans.z);

    for (BlockCoord bc : blocks) {
      
      IBlockState bs = world.getBlockState(bc.getBlockPos());
      Block block = bs.getBlock();
      bs = bs.getActualState(world, bc.getBlockPos());                        
      if (block.canRenderInLayer(bs, layer)) {
        renderBlock(bs, bc.getBlockPos(), world, Tessellator.getInstance().getBuffer());
      }
    }

    Tessellator.getInstance().draw();
    Tessellator.getInstance().getBuffer().setTranslation(0, 0, 0);    
  }

  public void renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, VertexBuffer worldRendererIn) {

    try {
      BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();      
      EnumBlockRenderType type = state.getRenderType();
      if (type != EnumBlockRenderType.MODEL) {
        blockrendererdispatcher.renderBlock(state, pos, blockAccess, worldRendererIn);
        return;
      }      

      // We only want to change one param here, the check sides
      IBakedModel ibakedmodel = blockrendererdispatcher.getModelForState(state);
      state = state.getBlock().getExtendedState(state, world, pos);
      blockrendererdispatcher.getBlockModelRenderer().renderModel(blockAccess, ibakedmodel, state, pos, worldRendererIn, false);
      
    } catch (Throwable throwable) {
      // Just bury a render issue here, it is only the IO screen
    }
  }

  private void setGlStateForPass(BlockRenderLayer layer, boolean isNeighbour) {
    int pass = layer == BlockRenderLayer.TRANSLUCENT ? 1 : 0;
    setGlStateForPass(pass, isNeighbour);
  }
  
  private void setGlStateForPass(int layer, boolean isNeighbour) {

    GlStateManager.color(1, 1, 1);
    if (isNeighbour) {
      
      GlStateManager.enableDepth();
      GlStateManager.enableBlend();
      float alpha = 1f;
      float col = 1f;
            
      GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_CONSTANT_COLOR);
      GL14.glBlendColor(col, col, col, alpha);      
      return;
    }

    if (layer == 0) {
      GlStateManager.enableDepth();
      GlStateManager.disableBlend();
      GlStateManager.depthMask(true);
    } else {
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      GlStateManager.depthMask(false);
      
    }

  }

  private boolean updateCamera(float partialTick, int vpx, int vpy, int vpw, int vph) {
    if (vpw <= 0 || vph <= 0) {
      return false;
    }
    camera.setViewport(vpx, vpy, vpw, vph);
    camera.setProjectionMatrixAsPerspective(30, 0.05, 50, vpw, vph);
    eye.set(0, 0, distance);
    pitchRot.makeRotationX(Math.toRadians(pitch));
    yawRot.makeRotationY(Math.toRadians(yaw));
    pitchRot.transform(eye);
    yawRot.transform(eye);
    camera.setViewMatrixAsLookAt(eye, RenderUtil.ZERO_V, RenderUtil.UP_V);
    return camera.isValid();
  }

  private void applyCamera(float partialTick) {
    Rectangle vp = camera.getViewport();
    GL11.glViewport(vp.x, vp.y, vp.width, vp.height);
    GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
    GL11.glMatrixMode(GL11.GL_PROJECTION);
    RenderUtil.loadMatrix(camera.getTransposeProjectionMatrix());
    GL11.glMatrixMode(GL11.GL_MODELVIEW);
    RenderUtil.loadMatrix(camera.getTransposeViewMatrix());
    GL11.glTranslatef(-(float) eye.x, -(float) eye.y, -(float) eye.z);
  }

  public static class SelectedFace {

    public IIoConfigurable config;
    public EnumFacing face;

    public SelectedFace(IIoConfigurable config, EnumFacing face) {
      super();
      this.config = config;
      this.face = face;
    }

    @Override
    public String toString() {
      return "SelectedFace [config=" + config + ", face=" + face + "]";
    }

  }

}
