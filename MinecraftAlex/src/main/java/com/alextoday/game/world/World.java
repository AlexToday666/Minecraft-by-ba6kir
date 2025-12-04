package com.alextoday.game.world;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

import java.util.EnumMap;
import java.util.Map;

public class World {

    private final Node rootNode;
    private final AssetManager assetManager;

    private final Map<BlockType, Material> materials;
    private final Box cubeMesh;

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

    public void generateSpawnArea(int size) {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {

                BlockType type;
                if ((x < 3 || x > size - 4) || (z < 3 || z > size - 4)) {
                    type = BlockType.STONE;
                } else if ((x + z) % 2 == 0) {
                    type = BlockType.GRASS;
                } else {
                    type = BlockType.DIRT;
                }

                if (!type.isSolid()) {
                    continue;
                }

                Geometry blockGeom = new Geometry("block_" + x + "_" + z, cubeMesh);
                blockGeom.setMaterial(materials.get(type));
                blockGeom.setLocalTranslation(x, 0, z);

                rootNode.attachChild(blockGeom);
            }
        }
    }

    public void update(float tpf) {
        // пока пусто, сюда позже будет логика мира
    }
}
