package com.alextoday.game.world;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

import java.util.Map;

public class Chunk {

    public static final int SIZE_X = 16;
    public static final int SIZE_Y = 16;
    public static final int SIZE_Z = 16;

    private final int chunkX;
    private final int chunkZ;

    private final BlockType[][][] blocks;
    private final Node node;

    private RigidBodyControl terrainBody;

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blocks = new BlockType[SIZE_X][SIZE_Y][SIZE_Z];
        this.node = new Node("chunk_" + chunkX + "_" + chunkZ);
        initAir();
    }

    private void initAir() {
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                for (int z = 0; z < SIZE_Z; z++) {
                    blocks[x][y][z] = BlockType.AIR;
                }
            }
        }
    }

    public void generateFlat(int groundHeight) {
        if (groundHeight < 1) {
            groundHeight = 1;
        }
        if (groundHeight > SIZE_Y) {
            groundHeight = SIZE_Y;
        }

        for (int x = 0; x < SIZE_X; x++) {
            for (int z = 0; z < SIZE_Z; z++) {
                for (int y = 0; y < groundHeight; y++) {
                    if (y == groundHeight - 1) {
                        blocks[x][y][z] = BlockType.GRASS;
                    } else if (y >= groundHeight - 3) {
                        blocks[x][y][z] = BlockType.DIRT;
                    } else {
                        blocks[x][y][z] = BlockType.STONE;
                    }
                }
            }
        }
    }

    public void generateTerrainWithNoise(PerlinNoise noise,
                                         int baseHeight,
                                         int amplitude) {

        int waterSurfaceY = 6;

        for (int x = 0; x < SIZE_X; x++) {
            for (int z = 0; z < SIZE_Z; z++) {

                int worldX = chunkX * SIZE_X + x;
                int worldZ = chunkZ * SIZE_Z + z;

                double scale = 0.05;
                double n = noise.noise(worldX * scale, worldZ * scale);

                int height = (int) Math.round(baseHeight + n * amplitude);

                if (height < 1) height = 1;
                if (height > SIZE_Y) height = SIZE_Y;

                for (int y = 0; y < height; y++) {
                    if (y == height - 1) {
                        setBlock(x, y, z, BlockType.GRASS);
                    } else if (y >= height - 3) {
                        setBlock(x, y, z, BlockType.DIRT);
                    } else {
                        setBlock(x, y, z, BlockType.STONE);
                    }
                }

                int topGroundY = height - 1;
                if (topGroundY <= waterSurfaceY) {
                    int maxWaterY = Math.min(waterSurfaceY, SIZE_Y - 1);
                    for (int y = height; y <= maxWaterY; y++) {
                        setBlock(x, y, z, BlockType.WATER);
                    }
                }
            }
        }
    }

    public void buildGeometry(Node rootNode,
                              Box cubeMesh,
                              Map<BlockType, Material> materials) {

        int baseX = chunkX * SIZE_X;
        int baseZ = chunkZ * SIZE_Z;

        node.detachAllChildren();

        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                for (int z = 0; z < SIZE_Z; z++) {

                    BlockType type = blocks[x][y][z];
                    if (type == null) {
                        continue;
                    }

                    if (!type.isSolid() && type != BlockType.WATER) {
                        continue;
                    }


                    Material mat = materials.get(type);
                    if (mat == null) {
                        continue;
                    }

                    Geometry geom = new Geometry(
                            "block_" + chunkX + "_" + chunkZ + "_" + x + "_" + y + "_" + z,
                            cubeMesh
                    );
                    geom.setMaterial(mat);

                    float worldX = baseX + x;
                    float worldY = y;
                    float worldZ = baseZ + z;

                    geom.setLocalTranslation(worldX, worldY, worldZ);

                    node.attachChild(geom);
                }
            }
        }

        if (node.getParent() == null) {
            rootNode.attachChild(node);
        }
    }

    public BlockType getBlock(int x, int y, int z) {
        return blocks[x][y][z];
    }

    public void setBlock(int x, int y, int z, BlockType type) {
        blocks[x][y][z] = type;
    }

    public Node getNode() {
        return node;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public RigidBodyControl getTerrainBody() {
        return terrainBody;
    }

    public void setTerrainBody(RigidBodyControl terrainBody) {
        this.terrainBody = terrainBody;
    }
}
