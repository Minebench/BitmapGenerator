package io.github.apfelcreme.BitmapGenerator;

import io.github.apfelcreme.BitmapGenerator.Populator.FloraPopulator;
import io.github.apfelcreme.BitmapGenerator.Populator.SchematicPopulator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.MaterialData;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
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
public class WorldGenerator extends ChunkGenerator {

    private BitmapGenerator plugin;
    private BufferedImage blockMap;
    private BufferedImage heightMap;

    public WorldGenerator(BitmapGenerator plugin, BufferedImage blockMap, BufferedImage heightMap) {
        this.plugin = plugin;
        this.blockMap = blockMap;
        this.heightMap = heightMap;
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        ChunkData data = createChunkData(world);
        int minChunkX = -((blockMap.getWidth() / 2) / 16);
        int minChunkZ = -((blockMap.getHeight() / 2) / 16);
        int maxChunkX = ((blockMap.getWidth() / 2) / 16) - 1;
        int maxChunkZ = ((blockMap.getHeight() / 2) / 16) - 1;

        int diffX = x - minChunkX;
        int diffZ = z - minChunkZ;

        System.out.println(maxChunkX + " " + minChunkZ + " " + maxChunkX + " " + maxChunkZ);
        System.out.println(diffX + " " + diffZ);
//        System.out.println(blockMap.getWidth()+ "x"+blockMap.getHeight());
//        System.out.println(heightMap.getWidth()+ "x"+heightMap.getHeight());


        if (x >= minChunkX && x <= maxChunkX && z >= minChunkZ && z <= maxChunkZ) {
            MaterialData[][][] pasteData = new MaterialData[16][256][16];
            for (int cX = 0; cX < 16; cX++) {
                for (int cZ = 0; cZ < 16; cZ++) {
                    pasteData[cX][0][cZ] = new MaterialData(Material.BEDROCK);
                    for (int cY = 1; cY < 256; cY++) {
                        Color color = new Color(heightMap.getRGB((diffX * 16) + cX, (diffZ * 16) + cZ));
                        int heighestBlock = color.getRed();
                        // fill with the destined block
                        if (cY <= heighestBlock) {
                            int blockR = (blockMap.getRGB((diffX * 16) + cX, (diffZ * 16) + cZ) >> 16) & 0x000000FF;
                            int blockG = (blockMap.getRGB((diffX * 16) + cX, (diffZ * 16) + cZ) >> 8) & 0x000000FF;
                            int blockB = (blockMap.getRGB((diffX * 16) + cX, (diffZ * 16) + cZ)) & 0x000000FF;
                            MaterialData material = plugin.getBitmapGeneratorConfig().getBlock(blockR, blockG, blockB);
                            if (material != null) {
                                pasteData[cX][cY][cZ] = material;
                            }
                        }

                        // Fill everything under level 61 with water
                        if (pasteData[cX][cY][cZ] == null && cY <= 60) {
                            pasteData[cX][cY][cZ] = new MaterialData(Material.WATER);
                        }
                    }
                }
            }
            for (int cX = 0; cX < 16; cX++) {
                for (int cY = 0; cY < 256; cY++) {
                    for (int cZ = 0; cZ < 16; cZ++) {
//                        System.out.println(cX + " " + cY + " " + cZ + " -> " + pasteData[cX][cY][cZ]);
                        if (pasteData[cX][cY][cZ] != null) {
                            data.setBlock(cX, cY, cZ, pasteData[cX][cY][cZ]);
                        }
                    }
                }
            }
        } else{
            // No image-data for this chunk: fill with water
            for (int cX = 0; cX < 16; cX++) {
                for (int cZ = 0; cZ < 16; cZ++) {
                    data.setBlock(cX, 0, cZ, Material.BEDROCK);
                    for (int cY = 1; cY <= 60; cY++) {
                        data.setBlock(cX, cY, cZ, Material.WATER);
                    }
                }
            }
        }

        return data;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Arrays.asList(new FloraPopulator(), new SchematicPopulator(plugin));
    }

}
