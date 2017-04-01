package io.github.apfelcreme.BitmapGenerator;

import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Copyright (C) 2017 Lord36 aka Apfelcreme
 * <p>
 * This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *
 * @author Lord36 aka Apfelcreme
 */
public class BitmapGenerator extends JavaPlugin {

    private BitmapGeneratorConfig bitmapGeneratorConfig;
    private BufferedImage blockMap = null;
    private BufferedImage heightMap = null;

    public void onEnable() {
        this.bitmapGeneratorConfig = new BitmapGeneratorConfig(this);

        try {
            File blockMapFile = new File(getDataFolder() + "/" + bitmapGeneratorConfig.getBlockmapName());
            if (blockMapFile.exists()) {
                blockMap = ImageIO.read(blockMapFile);
            }

            File heightMapFile = new File(getDataFolder() + "/" + bitmapGeneratorConfig.getHeightmapName());
            if (heightMapFile.exists()) {
                heightMap = ImageIO.read(heightMapFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if ((blockMap == null || heightMap == null)
                || blockMap.getWidth() != heightMap.getWidth()
                || blockMap.getHeight() != heightMap.getHeight()
                || (blockMap.getWidth() % 16) != 0
                || (blockMap.getHeight() % 16) != 0
                || (heightMap.getWidth() % 16) != 0
                || (heightMap.getHeight() % 16) != 0) {
            getLogger().severe("BlockMap == null? " + (blockMap == null));
            getLogger().severe("HeightMap == null? " + (heightMap == null));
            if (blockMap != null && heightMap != null) {
                getLogger().severe("HeightMap != BlockMap? " + (blockMap.getWidth() == heightMap.getWidth() && blockMap.getHeight() == heightMap.getHeight()));
            }
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
            return new WorldGenerator(this, blockMap, heightMap);
    }

    /**
     * returns the configuration object
     *
     * @return the configuration object
     */
    public BitmapGeneratorConfig getBitmapGeneratorConfig() {
        return bitmapGeneratorConfig;
    }

}
