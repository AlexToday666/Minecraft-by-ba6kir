package com.alextoday.game;

import com.alextoday.game.world.World;
import com.alextoday.game.world.BlockType;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.font.BitmapText;


public class Main extends SimpleApplication implements ActionListener {

    private World world;
    private BulletAppState bulletAppState;
    private CharacterControl player;
    private final Vector3f walkDirection = new Vector3f();
    private boolean forward;
    private boolean backward;
    private boolean left;
    private boolean right;

    private static final int VIEW_RADIUS_CHUNKS = 2;
    private static final float GROUND_HALF_SIZE = 256f;
    private static final float GROUND_HEIGHT = 3f;

    public static void main(String[] args) {
        Main app = new Main();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 720);
        settings.setTitle("MinecraftAlex");
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        initPhysics();
        initCamera();
        initLights();
        initWorld();
        initPlayer();
        initKeys();
        initCrosshair();

    }

    private void initCrosshair() {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");

        BitmapText cross = new BitmapText(guiFont, false);
        cross.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        cross.setText("+");
        cross.setColor(ColorRGBA.White);

        float x = settings.getWidth() / 2f - cross.getLineWidth() / 2f;
        float y = settings.getHeight() / 2f + cross.getLineHeight() / 2f;

        cross.setLocalTranslation(x, y, 0);

        guiNode.attachChild(cross);
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camDir = cam.getDirection().clone().setY(0).normalizeLocal();
        Vector3f camLeft = cam.getLeft().clone().setY(0).normalizeLocal();

        walkDirection.set(0, 0, 0);
        if (forward) {
            walkDirection.addLocal(camDir);
        }
        if (backward) {
            walkDirection.addLocal(camDir.negate());
        }
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }

        player.setWalkDirection(walkDirection.mult(0.5f));
        cam.setLocation(player.getPhysicsLocation().add(0, 1.6f, 0));

        Vector3f pos = player.getPhysicsLocation();
        world.updateVisibleChunks(pos.x, pos.z, VIEW_RADIUS_CHUNKS);
    }

    private void initPhysics() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
    }

    private void initCamera() {
        viewPort.setBackgroundColor(new ColorRGBA(0.6f, 0.8f, 1f, 1f));
        flyCam.setMoveSpeed(0f);
        flyCam.setRotationSpeed(2f);
    }

    private void initLights() {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-1, -2, -1).normalizeLocal());
        sun.setColor(ColorRGBA.White.mult(1.5f));
        rootNode.addLight(sun);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.4f));
        rootNode.addLight(ambient);
    }

    private void initWorld() {
        world = new World(rootNode, assetManager, bulletAppState.getPhysicsSpace());

        BoxCollisionShape groundShape =
                new BoxCollisionShape(new Vector3f(GROUND_HALF_SIZE, GROUND_HEIGHT / 2f, GROUND_HALF_SIZE));
        RigidBodyControl ground = new RigidBodyControl(groundShape, 0);
        ground.setPhysicsLocation(new Vector3f(0, GROUND_HEIGHT / 2f, 0));
        bulletAppState.getPhysicsSpace().add(ground);

        world.updateVisibleChunks(0f, 0f, VIEW_RADIUS_CHUNKS);
    }

    private void initPlayer() {
        CapsuleCollisionShape capsule = new CapsuleCollisionShape(0.5f, 1.8f, 1);
        player = new CharacterControl(capsule, 0.1f);
        player.setJumpSpeed(20f);
        player.setFallSpeed(30f);
        player.setGravity(30f);

        int terrainY = world.getTerrainHeightAt(0f, 0f);

        player.setPhysicsLocation(new Vector3f(0f, terrainY + 3f, 0f));

        bulletAppState.getPhysicsSpace().add(player);
    }

    private void initKeys() {
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));

        inputManager.addMapping("BreakBlock", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("PlaceBlock", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

        inputManager.addListener(this,
                "Forward", "Backward", "Left", "Right", "Jump",
                "BreakBlock", "PlaceBlock");
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if ("Forward".equals(name)) {
            forward = isPressed;
        } else if ("Backward".equals(name)) {
            backward = isPressed;
        } else if ("Left".equals(name)) {
            left = isPressed;
        } else if ("Right".equals(name)) {
            right = isPressed;
        } else if ("Jump".equals(name) && isPressed) {
            player.jump();
        } else if ("BreakBlock".equals(name) && isPressed) {
            handleBreakBlock();
        } else if ("PlaceBlock".equals(name) && isPressed) {
            handlePlaceBlock();
        }
    }

    private static class BlockRaycastResult {
        boolean hit;
        int hitX, hitY, hitZ;
        int placeX, placeY, placeZ;
        float distance;
    }

    private BlockRaycastResult raycastBlock(float maxDistance) {
        BlockRaycastResult result = new BlockRaycastResult();

        Vector3f origin = cam.getLocation().clone();
        Vector3f dir = cam.getDirection().normalize();

        float step = 0.1f;
        float dist = 0f;

        int lastAirX = 0, lastAirY = 0, lastAirZ = 0;
        boolean hasLastAir = false;

        while (dist <= maxDistance) {
            Vector3f p = origin.add(dir.mult(dist));

            int bx = (int) Math.floor(p.x);
            int by = (int) Math.floor(p.y);
            int bz = (int) Math.floor(p.z);

            BlockType type = world.getBlockType(bx, by, bz);

            if (type != null && type != BlockType.AIR) {
                result.hit = true;
                result.hitX = bx;
                result.hitY = by;
                result.hitZ = bz;

                if (hasLastAir) {
                    result.placeX = lastAirX;
                    result.placeY = lastAirY;
                    result.placeZ = lastAirZ;
                } else {
                    result.placeX = bx;
                    result.placeY = by;
                    result.placeZ = bz;
                }

                result.distance = dist;
                return result;
            } else {
                hasLastAir = true;
                lastAirX = bx;
                lastAirY = by;
                lastAirZ = bz;
            }

            dist += step;
        }

        return result;
    }

    private BlockType selectedBlockType = BlockType.DIRT;

    private void handleBreakBlock() {
        BlockRaycastResult hit = raycastBlock(6f);
        if (!hit.hit) return;

        world.setBlockType(hit.hitX, hit.hitY, hit.hitZ, BlockType.AIR);
        world.updateChunkAt(hit.hitX, hit.hitY, hit.hitZ);
    }

    private void handlePlaceBlock() {
        BlockRaycastResult hit = raycastBlock(6f);
        if (!hit.hit) return;

        BlockType existing = world.getBlockType(hit.placeX, hit.placeY, hit.placeZ);
        if (existing != null && existing != BlockType.AIR) return;

        Vector3f playerPos = player.getPhysicsLocation();

        float px = playerPos.x;
        float py = playerPos.y;
        float pz = playerPos.z;

        if (hit.placeX >= px - 0.5f && hit.placeX <= px + 0.5f &&
                hit.placeY >= py && hit.placeY <= py + 1.8f &&
                hit.placeZ >= pz - 0.5f && hit.placeZ <= pz + 0.5f) {
            return;
        }
        world.setBlockType(hit.placeX, hit.placeY, hit.placeZ, selectedBlockType);
        world.updateChunkAt(hit.placeX, hit.placeY, hit.placeZ);
    }

}
