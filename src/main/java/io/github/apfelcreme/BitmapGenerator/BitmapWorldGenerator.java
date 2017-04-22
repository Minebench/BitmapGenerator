package io.github.apfelcreme.BitmapGenerator;

import io.github.apfelcreme.BitmapGenerator.Populator.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.MaterialData;

import java.awt.image.BufferedImage;
import java.util.Arrays;
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
public class BitmapWorldGenerator extends ChunkGenerator {

    private WorldConfiguration worldConfiguration;
    private BufferedImage biomeMap;

    public BitmapWorldGenerator(WorldConfiguration worldConfiguration) {
        this.worldConfiguration = worldConfiguration;
        this.biomeMap = worldConfiguration.getBiomeMap();
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
                    BiomeDefinition biomeDefinition = worldConfiguration.getBiomeDefinition(imageCoordX, imageCoordZ);
                    if (biomeDefinition != null) {
                        biome.setBiome(cX, cZ, biomeDefinition.getBiome());
                        data.setBlock(cX, 0, cZ, Material.BEDROCK);
                        for (int cY = 1; cY < 256; cY++) {
                            int heighestBlock = worldConfiguration.getHeight(imageCoordX, imageCoordZ);

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
                                    && cY <= worldConfiguration.getWaterHeight()) {
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
                new TreePopulator(worldConfiguration),
                new SnowPopulator(worldConfiguration),
                new SchematicPopulator(worldConfiguration),
                new FloraPopulator(worldConfiguration),
                new OrePopulator(worldConfiguration)
        );
    }

}
