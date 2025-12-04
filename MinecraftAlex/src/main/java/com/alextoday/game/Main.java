package com.alextoday.game;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;

public class Main extends SimpleApplication{
    public static void main(String[] args){
        Main app = new Main();

        AppSettings settings = new AppSettings(true);
        settings.setTitle("MinecraftAlex - Step 1");
        settings.setResolution(1280, 720);
        app.setSettings(settings);
        app.setShowSettings(false);

        app.start();
    }

    public void simpleInitApp() {

        Box box = new Box(0.5f, 0.5f, 0.5f);
        Geometry cube = new Geometry("Cube", box);

        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Green);

        cube.setMaterial(mat);

        rootNode.attachChild(cube);

        cam.setLocation(cam.getLocation().add(0, 0, 3));
    }
}