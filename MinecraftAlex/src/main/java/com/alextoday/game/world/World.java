package com.alextoday.game.world;

import com.jme3.asset.AssetManager;
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

    public World(Node rootNode, AssetManager assetManager) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.materials = new EnumMap<>(BlockType.class);
        this.cubeMesh = new Box(0.5f, 0.5f, 0.5f);
        initMaterials();
    }

    private void initMaterials() {
        for (BlockType type : BlockType.values()) {
            if (!type.isSolid()) {
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

    private String chunkKey(int cx, int cz) {
        return cx + "," + cz;
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
                chunk.generateFlat(3);
                chunk.buildGeometry(rootNode, cubeMesh, materials);
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
}
