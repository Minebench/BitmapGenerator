package io.github.apfelcreme.BitmapGenerator.Populator;

import io.github.apfelcreme.BitmapGenerator.BiomeDefinition;
import io.github.apfelcreme.BitmapGenerator.Util;
import io.github.apfelcreme.BitmapGenerator.WorldConfiguration;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
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
public class SchematicPopulator implements ChunkPopulator {

    private WorldConfiguration worldConfiguration;

    public SchematicPopulator(WorldConfiguration worldConfiguration) {
        this.worldConfiguration = worldConfiguration;
    }

    @Override
    public synchronized void populate(World world, Random random, int chunkX, int chunkZ, ChunkGenerator.ChunkData chunk, BiomeDefinition biomeDefinition) {
        int searchRadius = (int) Math.ceil(biomeDefinition.getMaxSchematicSize() / 16d);

        List<SchematicConfig> schemCoords = new ArrayList<>();

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int z = -searchRadius; z <= searchRadius; z++) {
                long chunkSeed = world.getSeed();
                chunkSeed = 37 * chunkSeed + chunkX + x;
                chunkSeed = 37 * chunkSeed + chunkZ + z;
                Random chunkRandom = new Random(chunkSeed);

                double schematicCount;
                if (biomeDefinition.getSchematicChance() < 1) {
                    schematicCount = chunkRandom.nextDouble() <= biomeDefinition.getSchematicChance() ? 1 : 0;
                } else {
                    schematicCount = biomeDefinition.getSchematicChance() / 2 + chunkRandom.nextInt((int) biomeDefinition.getSchematicChance());
                    double maxInChunk = (schematicCount * schematicCount) / (biomeDefinition.getMaxSchematicSize() * biomeDefinition.getMaxSchematicSize());
                    if (schematicCount > maxInChunk) {
                        schematicCount = maxInChunk;
                    }
                }

                for (int i = 0; i < schematicCount; i++) {
                    schemCoords.add(new SchematicConfig(x * 16 + chunkRandom.nextInt(16), z * 16 + chunkRandom.nextInt(16), chunkRandom.nextInt(4)));
                }
            }
        }

        for (SchematicConfig schematicConf : schemCoords) {
            int schematicX = schematicConf.getX();
            int schematicZ = schematicConf.getZ();

            if (worldConfiguration.getBiomeDefinition((chunkX << 4) + schematicX, (chunkZ << 4) + schematicZ).equals(biomeDefinition)) {
                BiomeDefinition.Schematic schematic = biomeDefinition.nextSchematic(random);

                if (biomeDefinition.isRotateSchematics()) {
                    schematic = schematic.rotate(schematicConf.getRotation());
                }

                int schematicWidth = schematic.getDimensions().getBlockX();
                int schematicHeight = schematic.getDimensions().getBlockY();
                int schematicLength = schematic.getDimensions().getBlockZ();

                int startX = schematicX - schematicWidth / 2;
                int startZ = schematicZ - schematicLength / 2;

                // Get center height
                int schematicY = worldConfiguration.getHeight((chunkX << 4) + schematicX, (chunkZ << 4) + schematicZ) + 1;
                // Check actual chunk highest block if the full schematic is in the chunk
                int schematicOffset = schematic.getYOffset();
                if (startX >= 0 && startX + schematicWidth < 16 && startZ >= 0 && startZ + schematicLength < 16) {
                    schematicY = Util.getHighestBlock(world, chunk, schematicX, schematicZ);
                    // Check if valid ground block, continue if not
                    if (!biomeDefinition.isGroundBlock(chunk.getBlockData(schematicX, schematicY - 1, schematicZ))) {
                        continue;
                    }
                    // Try putting schematic on floor
                    boolean foundSolid = false;
                    for (int testedY = 0; testedY < schematicHeight && !foundSolid; testedY++) {
                        for (int x = 0; x < schematicWidth; x++) {
                            for (int z = 0; z < schematicLength; z++) {
                                // Create the rotated vector
                                BlockData b = schematic.getBlock(x, testedY, z);
                                if (b != null && !b.getMaterial().isAir()) {
                                    for (int offset = schematicOffset; offset > 0; offset--) {
                                        Material type = chunk.getType(
                                                schematicX + x - (schematicWidth / 2),
                                                schematicY + offset - 1,
                                                schematicZ + z - (schematicLength / 2));
                                        if (type.isOccluding()) {
                                            schematicOffset = offset;
                                            foundSolid = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                for (int x = 0; x < schematicWidth; x++) {
                    for (int z = 0; z < schematicLength; z++) {
                        int schemX = startX + x;
                        int schemZ = startZ + z;
                        if (schemZ >= 0 && schemZ < 16 && schemX >= 0 && schemX < 16) {
                            for (int y = 0; y < schematicHeight; y++) {
                                BlockData block = schematic.getBlock(x, y, z);
                                if (block != null && !block.getMaterial().isAir()
                                        && !chunk.getType(schemX, schematicY + schematicOffset + y, schemZ).isSolid()) {
                                    chunk.setBlock(schemX, schematicY + schematicOffset + y, schemZ, block);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private class SchematicConfig {
        private final int x;
        private final int z;
        private final int rotation;

        private SchematicConfig(int x, int z, int rotation) {
            this.x = x;
            this.z = z;
            this.rotation = rotation;
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        public int getRotation() {
            return rotation;
        }
    }
}
