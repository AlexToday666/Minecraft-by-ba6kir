package com.alextoday.game.world;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class World {

    private final Node rootNode;
    private final AssetManager assetManager;

    private final Map<BlockType, Material> materials;
    private final Box cubeMesh;
    private final List<Chunk> chunks = new ArrayList<>();

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

    public void generateChunkGrid(int countX, int countZ) {
        chunks.clear();

        for (int cx = 0; cx < countX; cx++) {
            for (int cz = 0; cz < countZ; cz++) {
                Chunk chunk = new Chunk(cx, cz);

                for (int x = 0; x < Chunk.SIZE_X; x++) {
                    for (int z = 0; z < Chunk.SIZE_Z; z++) {
                        int worldX = cx * Chunk.SIZE_X + x;
                        int worldZ = cz * Chunk.SIZE_Z + z;
                        int h = 2 + (int) (Math.sin(worldX * 0.3) + Math.cos(worldZ * 0.3));
                        if (h < 1) h = 1;
                        if (h > Chunk.SIZE_Y) h = Chunk.SIZE_Y;
                        for (int y = 0; y < h; y++) {
                            if (y == h - 1) {
                                chunk.setBlock(x, y, z, BlockType.GRASS);
                            } else if (y >= h - 3) {
                                chunk.setBlock(x, y, z, BlockType.DIRT);
                            } else {
                                chunk.setBlock(x, y, z, BlockType.STONE);
                            }
                        }
                    }
                }


                chunk.buildGeometry(rootNode, cubeMesh, materials);
                chunks.add(chunk);
            }
        }
    }


    public List<Chunk> getChunks() {
        return chunks;
    }

    public void update(float tpf) {
        // пока пусто
    }
}
