package com.alextoday.game.world;

import com.jme3.math.ColorRGBA;

public enum BlockType {
    AIR(false, null),
    GRASS(true, new ColorRGBA(0.3f, 0.8f, 0.3f, 1f)),
    DIRT(true, new ColorRGBA(0.59f, 0.29f, 0.0f, 1f)),
    STONE(true, ColorRGBA.Gray);

    private final boolean solid;
    public final ColorRGBA color;

    BlockType(boolean solid, ColorRGBA color) {
        this.solid = solid;
        this.color = color;
    }

    public boolean isSolid() {
        return solid;
    }
}
