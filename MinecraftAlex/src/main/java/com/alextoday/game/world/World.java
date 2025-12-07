package com.alextoday.game.world;

import com.alextoday.game.world.BlockType;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class World {

    private final Node rootNode;
    private final AssetManager assetManager;

    private final Map<BlockType, Material> materials;
    private final Box cubeMesh;

    private final Map<String, Chunk> chunks = new HashMap<>();

    private final PerlinNoise heightNoise;
    private final PhysicsSpace physicsSpace;

    public World(Node rootNode, AssetManager assetManager, PhysicsSpace physicsSpace) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.materials = new EnumMap<>(BlockType.class);
        this.cubeMesh = new Box(0.5f, 0.5f, 0.5f);

        this.heightNoise = new PerlinNoise(12345L);
        this.physicsSpace = physicsSpace;

        initMaterials();
    }

    private void initMaterials() {
        for (BlockType type : BlockType.values()) {
            if (type.color == null) {
                continue;
            }

            Material mat = new Material(assetManager,
                    "Common/MatDefs/Light/Lighting.j3md");
            mat.setBoolean("UseMaterialColors", true);
            mat.setColor("Diffuse", type.color);
            mat.setColor("Ambient", type.color.mult(0.7f));
            mat.setColor("Specular", ColorRGBA.White);
            mat.setFloat("Shininess", 16f);

            materials.put(type, mat);
        }
    }

    public int getTerrainHeightAt(float worldX, float worldZ) {
        double scale = 0.05;
        double n = heightNoise.noise(worldX * scale, worldZ * scale);

        int baseHeight = 8;
        int amplitude = 6;

        int height = (int) Math.round(baseHeight + n * amplitude);

        if (height < 1) height = 1;
        if (height > Chunk.SIZE_Y) height = Chunk.SIZE_Y;

        return height;
    }


    private String chunkKey(int cx, int cz) {
        return cx + "," + cz;
    }

    private Chunk getChunk(int cx, int cz) {
        return chunks.get(chunkKey(cx, cz));
    }

    private Chunk getChunkByWorldCoords(int x, int z) {
        int cx = Math.floorDiv(x, Chunk.SIZE_X);
        int cz = Math.floorDiv(z, Chunk.SIZE_Z);
        return getChunk(cx, cz);
    }


    public void updateVisibleChunks(float playerX, float playerZ, int radiusChunks) {
        ensureChunksAround(playerX, playerZ, radiusChunks);
        unloadFarChunks(playerX, playerZ, radiusChunks);
    }

    private void ensureChunksAround(float playerX, float playerZ, int radiusChunks) {
        int centerCx = (int) Math.floor(playerX / Chunk.SIZE_X);
        int centerCz = (int) Math.floor(playerZ / Chunk.SIZE_Z);

        for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
            for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
                int cx = centerCx + dx;
                int cz = centerCz + dz;

                String key = chunkKey(cx, cz);
                if (chunks.containsKey(key)) {
                    continue;
                }

                Chunk chunk = new Chunk(cx, cz);

                chunk.generateTerrainWithNoise(heightNoise, 8, 6);

                chunk.buildGeometry(rootNode, cubeMesh, materials);

                CollisionShape shape = CollisionShapeFactory.createMeshShape(chunk.getNode());
                RigidBodyControl terrainBody = new RigidBodyControl(shape, 0);
                chunk.getNode().addControl(terrainBody);
                physicsSpace.add(terrainBody);
                chunk.setTerrainBody(terrainBody);

                chunks.put(key, chunk);
            }
        }
    }

    private void unloadFarChunks(float playerX, float playerZ, int radiusChunks) {
        int centerCx = (int) Math.floor(playerX / Chunk.SIZE_X);
        int centerCz = (int) Math.floor(playerZ / Chunk.SIZE_Z);

        Iterator<Map.Entry<String, Chunk>> it = chunks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Chunk> entry = it.next();
            Chunk chunk = entry.getValue();

            int cx = chunk.getChunkX();
            int cz = chunk.getChunkZ();

            int dx = Math.abs(cx - centerCx);
            int dz = Math.abs(cz - centerCz);

            if (dx > radiusChunks || dz > radiusChunks) {
                RigidBodyControl body = chunk.getTerrainBody();
                if (body != null) {
                    physicsSpace.remove(body);
                    chunk.getNode().removeControl(body);
                }

                chunk.getNode().removeFromParent();
                it.remove();
            }
        }
    }

    public Map<String, Chunk> getChunks() {
        return chunks;
    }

    public void update(float tpf) {
    }

    public BlockType getBlockType(int x, int y, int z) {
        if (y < 0 || y >= Chunk.SIZE_Y) {
            return BlockType.AIR;
        }

        Chunk chunk = getChunkByWorldCoords(x, z);
        if (chunk == null) {
            return BlockType.AIR;
        }

        int localX = Math.floorMod(x, Chunk.SIZE_X);
        int localZ = Math.floorMod(z, Chunk.SIZE_Z);

        BlockType type = chunk.getBlock(localX, y, localZ);
        return (type != null) ? type : BlockType.AIR;
    }

    public void setBlockType(int x, int y, int z, BlockType type) {
        if (y < 0 || y >= Chunk.SIZE_Y) {
            return;
        }

        Chunk chunk = getChunkByWorldCoords(x, z);
        if (chunk == null) {
            return;
        }

        int localX = Math.floorMod(x, Chunk.SIZE_X);
        int localZ = Math.floorMod(z, Chunk.SIZE_Z);

        chunk.setBlock(localX, y, localZ, type);

        updateChunkAt(x, y, z);
    }

    public void updateChunkAt(int x, int y, int z) {
        Chunk chunk = getChunkByWorldCoords(x, z);
        if (chunk == null) {
            return;
        }

        RigidBodyControl oldBody = chunk.getTerrainBody();
        if (oldBody != null) {
            physicsSpace.remove(oldBody);
            chunk.getNode().removeControl(oldBody);
        }

        chunk.getNode().detachAllChildren();
        chunk.buildGeometry(rootNode, cubeMesh, materials);

        CollisionShape shape = CollisionShapeFactory.createMeshShape(chunk.getNode());
        RigidBodyControl terrainBody = new RigidBodyControl(shape, 0);
        chunk.getNode().addControl(terrainBody);
        physicsSpace.add(terrainBody);
        chunk.setTerrainBody(terrainBody);
    }

}
