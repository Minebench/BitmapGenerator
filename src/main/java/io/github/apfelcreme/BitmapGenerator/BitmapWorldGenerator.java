package io.github.apfelcreme.BitmapGenerator;

import io.github.apfelcreme.BitmapGenerator.Populator.ChunkPopulator;
import io.github.apfelcreme.BitmapGenerator.Populator.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
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

    private List<ChunkPopulator> chunkPopulators = new ArrayList<>();

    public BitmapWorldGenerator(WorldConfiguration worldConfiguration) {
        this.worldConfiguration = worldConfiguration;
        if (!worldConfiguration.isGeneratingVanillaCaves()) {
            chunkPopulators.add(new CavePopulator(worldConfiguration));
        }
        chunkPopulators.add(new OrePopulator(worldConfiguration));
        chunkPopulators.add(new SchematicPopulator(worldConfiguration));
        chunkPopulators.add(new FloraPopulator(worldConfiguration));
        chunkPopulators.add(new SnowPopulator(worldConfiguration));
    }

    @Override
    public synchronized ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        ChunkData data = createChunkData(world);

        List<BiomeDefinition> biomes = new ArrayList<>();

        for (int cX = 0; cX < 16; cX++) {
            for (int cZ = 0; cZ < 16; cZ++) {
                int imageCoordX = x * 16 + cX;
                int imageCoordZ = z * 16 + cZ;
                BiomeDefinition biomeDefinition = worldConfiguration.getBiomeDefinition(imageCoordX, imageCoordZ);
                if (biomeDefinition != null) {
                    biomes.add(biomeDefinition);
                    data.setBlock(cX, 0, cZ, Material.BEDROCK);
                    int highestBlock = worldConfiguration.getHeight(imageCoordX, imageCoordZ);
                    int riverDepth = worldConfiguration.getRiverDepth(imageCoordX, imageCoordZ);
                    for (int cY = 1; cY <= Math.max(highestBlock, worldConfiguration.getWaterHeight()); cY++) {

                        if (riverDepth > 0 && cY <= highestBlock && cY > highestBlock - riverDepth) {
                            // fill with water (if there is a river)
                            if (biomeDefinition.getBiome() != Biome.FROZEN_RIVER && biomeDefinition.getBiome() != Biome.RIVER) {
                                biome.setBiome(cX, cY, cZ, Biome.RIVER);
                            }
                            if (cY < highestBlock) { // Make river water one block below surface
                                data.setBlock(cX, cY, cZ, Material.WATER);
                            }
                        } else {
                            // fill with the destined block
                            if (cY <= highestBlock && cY > (highestBlock - biomeDefinition.getSurfaceLayerHeight())) {
                                // surface layer
                                data.setBlock(cX, cY, cZ, biomeDefinition.nextBlock(random));
                            } else if (cY <= (highestBlock - biomeDefinition.getSurfaceLayerHeight())) {
                                // everything under the surface layer, ores and caves will be populated later
                                data.setBlock(cX, cY, cZ, Material.STONE);
                            }
                        }

                        // Fill everything under the waterHeight-level with water
                        if ((data.getType(cX, cY, cZ) == null || data.getType(cX, cY, cZ) == Material.AIR)
                                && cY <= worldConfiguration.getWaterHeight()) {
                            data.setBlock(cX, cY, cZ, Material.WATER);
                        }
                    }
                    for (int cY = 0; cY < world.getMaxHeight(); cY++) {
                        biome.setBiome(cX, cY, cZ, biomeDefinition.getBiome());
                    }
                } else {
                    // No image-data for this chunk: fill with water
                    data.setBlock(cX, 0, cZ, Material.BEDROCK);
                    for (int cY = 1; cY <= worldConfiguration.getWaterHeight(); cY++) {
                        data.setBlock(cX, cY, cZ, Material.WATER);
                    }
                }

            }
        }

        if (!biomes.isEmpty()) {
            for (ChunkPopulator chunkPopulator : chunkPopulators) {
                for (BiomeDefinition biomeDefinition : biomes) {
                    chunkPopulator.populate(world, random, x, z, data, biomeDefinition);
                }
            }
        }

        return data;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return worldConfiguration.isGeneratingVanillaCaves();
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return worldConfiguration.isGeneratingVanillaDecorations();
    }

    @Override
    public boolean shouldGenerateMobs() {
        return worldConfiguration.isGeneratingVanillaMobs();
    }

    @Override
    public boolean shouldGenerateStructures() {
        return worldConfiguration.isGeneratingVanillaStructures();
    }

    /**
     * calls all populators in a given order
     *
     * @param world -
     * @return -
     */
    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        List<BlockPopulator> blockPopulators = new ArrayList<>();
        //blockPopulators.add(new TreePopulator(worldConfiguration));
        return blockPopulators;
    }

}
