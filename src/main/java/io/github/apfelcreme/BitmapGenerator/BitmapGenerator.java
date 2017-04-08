package io.github.apfelcreme.BitmapGenerator;

import org.bukkit.Chunk;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private BufferedImage biomeMap = null;
    private BufferedImage heightMap = null;
    private List<BiomeDefinition> biomes;

    public void onEnable() {
        this.bitmapGeneratorConfig = new BitmapGeneratorConfig(this);

        try {
            File biomeMapFile = new File(getDataFolder() + "/" + bitmapGeneratorConfig.getBiomeMapName());
            if (biomeMapFile.exists()) {
                biomeMap = ImageIO.read(biomeMapFile);
            }

            File heightMapFile = new File(getDataFolder() + "/" + bitmapGeneratorConfig.getHeightMapName());
            if (heightMapFile.exists()) {
                heightMap = ImageIO.read(heightMapFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (biomeMap == null) {
            getLogger().severe("BiomeMap was not found!");
            getServer().getPluginManager().disablePlugin(this);
        }
        if (heightMap == null) {
            getLogger().severe("HeightMap was not found!");
            getServer().getPluginManager().disablePlugin(this);
        }
        if (biomeMap.getWidth() != heightMap.getWidth()) {
            getLogger().severe("BiomeMap width does not equal HeightMap width!");
            getServer().getPluginManager().disablePlugin(this);
        }
        if (biomeMap.getHeight() != heightMap.getHeight()) {
            getLogger().severe("BiomeMap height does not equal HeightMap height!");
            getServer().getPluginManager().disablePlugin(this);
        }
        biomes = bitmapGeneratorConfig.loadBiomes();
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new WorldGenerator(this, biomeMap, heightMap);
    }

    /**
     * returns the configuration object
     *
     * @return the configuration object
     */
    public BitmapGeneratorConfig getBitmapGeneratorConfig() {
        return bitmapGeneratorConfig;
    }

    /**
     * returns the biome defined by the biomeMap at the given point
     *
     * @param blockX the x position of the block (not the chunk coordinate)
     * @param blockZ the z position of the block (not the chunk coordinate)
     * @return the biome defined by the biomeMap at the given point
     */
    public BiomeDefinition getBiomeDefinition(int blockX, int blockZ) {

        int offsetX = ((biomeMap.getWidth() / 2));
        int offsetZ = ((biomeMap.getHeight() / 2));

        int imageX = blockX + offsetX;
        int imageZ = blockZ + offsetZ;

        int biomeR = (biomeMap.getRGB(imageX, imageZ) >> 16) & 0x000000FF;
        int biomeG = (biomeMap.getRGB(imageX, imageZ) >> 8) & 0x000000FF;
        int biomeB = (biomeMap.getRGB(imageX, imageZ)) & 0x000000FF;
        for (BiomeDefinition biomeDefinition : biomes) {
            if (biomeDefinition.getR() == biomeR && biomeDefinition.getG() == biomeG && biomeDefinition.getB() == biomeB) {
                return biomeDefinition;
            }
        }
        return null;
    }

    /**
     * returns the height at location
     * @param blockX the x coordinate of the block
     * @param blockZ the z coordinate of the block
     * @return the height of the location referenced in the height map
     */
    public int getHeight(int blockX, int blockZ) {

        int offsetX = ((heightMap.getWidth() / 2));
        int offsetZ = ((heightMap.getHeight() / 2));

        int imageX = blockX + offsetX;
        int imageZ = blockZ + offsetZ;

        return (heightMap.getRGB(imageX, imageZ) >> 16) & 0x000000FF;
    }

    /**
     * returns a list of all biome definitions found in the chunk
     *
     * @param chunk the chunk
     * @return a list of biomes
     */
    public List<BiomeDefinition> getDistinctChunkBiomes(Chunk chunk) {
        List<BiomeDefinition> biomeDefinitions = new ArrayList<>();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BiomeDefinition biomeDefinition = getBiomeDefinition((chunk.getX() * 16) + x, (chunk.getZ() * 16) + z);
                if (!biomeDefinitions.contains(biomeDefinition)) {
                    biomeDefinitions.add(biomeDefinition);
                }
            }
        }
        return biomeDefinitions;
    }

    /**
     * joins a number of strings and places a seperator between them
     * -> taken from StringUtils to reduce the number of dependencies
     *
     * @param list      the array of strings
     * @param separator the seperator
     * @return a joined string
     */
    public static String join(List<?> list, String separator) {
        if (list == null) {
            return null;
        } else {
            if (separator == null) {
                separator = "";
            }

            int noOfItems = list.size();
            if (noOfItems <= 0) {
                return "";
            } else {
                StringBuilder buf = new StringBuilder(noOfItems * 16);

                for (int i = 0; i < list.size(); ++i) {
                    if (i > 0) {
                        buf.append(separator);
                    }

                    if (list.get(i) != null) {
                        buf.append(list.get(i));
                    }
                }

                return buf.toString();
            }
        }
    }
}
