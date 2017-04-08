package io.github.apfelcreme.BitmapGenerator;

import io.github.apfelcreme.BitmapGenerator.Populator.FloraPopulator;
import io.github.apfelcreme.BitmapGenerator.Populator.OrePopulator;
import io.github.apfelcreme.BitmapGenerator.Populator.SchematicPopulator;
import io.github.apfelcreme.BitmapGenerator.Populator.TreePopulator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.MaterialData;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
    private BufferedImage biomeMap;
    private BufferedImage heightMap;

    public WorldGenerator(BitmapGenerator plugin, BufferedImage blockMap, BufferedImage heightMap) {
        this.plugin = plugin;
        this.biomeMap = blockMap;
        this.heightMap = heightMap;
    }

    @Override
    public synchronized ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        ChunkData data = createChunkData(world);
        int minChunkX = -((biomeMap.getWidth() / 2) / 16);
        int minChunkZ = -((biomeMap.getHeight() / 2) / 16);
        int maxChunkX = ((biomeMap.getWidth() / 2) / 16) - 1;
        int maxChunkZ = ((biomeMap.getHeight() / 2) / 16) - 1;

        int surfaceLayerHeight = 4;

        if (x >= minChunkX && x <= maxChunkX && z >= minChunkZ && z <= maxChunkZ) {
            for (int cX = 0; cX < 16; cX++) {
                for (int cZ = 0; cZ < 16; cZ++) {
                    int imageCoordX = x * 16 + cX;
                    int imageCoordZ = z * 16 + cZ;
                    BiomeDefinition biomeDefinition = plugin.getBiomeDefinition(imageCoordX, imageCoordZ);
                    if (biomeDefinition != null) {
                        biome.setBiome(cX, cZ, biomeDefinition.getBiome());
                        data.setBlock(cX, 0, cZ, Material.BEDROCK);
                        for (int cY = 1; cY < 256; cY++) {
                            int heighestBlock = plugin.getHeight(imageCoordX, imageCoordZ);

                            // fill with the destined block
                            if (cY <= heighestBlock && cY > (heighestBlock - surfaceLayerHeight)) {
                                data.setBlock(cX, cY, cZ, biomeDefinition.nextBlock());
                            } else if (cY <= (heighestBlock - surfaceLayerHeight)) { // everything under the surface layer
//                            data.setBlock(cX, cY, cZ, naturalChunkData[cX][cY][cZ]);
                                data.setBlock(cX, cY, cZ, Material.STONE);
                                // ores will be populated later
                            } else if (cY >= heighestBlock) { // everything above the highest block -> air
                                data.setBlock(cX, cY, cZ, new MaterialData(Material.AIR));
                            }

                            // Fill everything under the waterHeight-level with water
                            if ((data.getType(cX, cY, cZ) == null || data.getType(cX, cY, cZ) == Material.AIR)
                                    && cY <= plugin.getBitmapGeneratorConfig().getWaterHeight()) {
                                data.setBlock(cX, cY, cZ, new MaterialData(Material.WATER));
                            }
                        }
                    }
                }
            }
        } else {
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
        return Arrays.asList(
                new SchematicPopulator(plugin, biomeMap),
                new TreePopulator(plugin, biomeMap),
                new FloraPopulator(plugin, biomeMap),
                new OrePopulator(plugin, biomeMap)
        );
    }

}
