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

        List<ChunkCoord> schemCoords = new ArrayList<>();

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int z = -searchRadius; z <= searchRadius; z++) {
                long chunkSeed = world.getSeed();
                chunkSeed = 37 * chunkSeed + x;
                chunkSeed = 37 * chunkSeed + z;
                Random chunkRandom = new Random(chunkSeed);

                double schematicCount;
                if (biomeDefinition.getSchematicChance() < 1) {
                    schematicCount = chunkRandom.nextDouble() <= biomeDefinition.getSchematicChance() ? 1 : 0;
                } else {
                    schematicCount = (int) biomeDefinition.getSchematicChance();
                    double maxInChunk = (schematicCount * schematicCount) / (biomeDefinition.getMaxSchematicSize() * biomeDefinition.getMaxSchematicSize());
                    if (schematicCount > maxInChunk) {
                        schematicCount = maxInChunk;
                    }
                }

                for (int i = 0; i < schematicCount; i++) {
                    schemCoords.add(new ChunkCoord(chunkRandom.nextInt(16), chunkRandom.nextInt(16)));
                }
            }
        }

        for (ChunkCoord coord : schemCoords) {
            int schematicX = coord.getX();
            int schematicZ = coord.getZ();
            int inChunkX = Math.min(Math.max(schematicX, 0), 15);
            int inChunkZ = Math.min(Math.max(schematicZ, 0), 15);

            if (worldConfiguration.getBiomeDefinition((chunkX << 4) + schematicX, (chunkZ << 4) + schematicZ).equals(biomeDefinition)) {
                BiomeDefinition.Schematic schematic = biomeDefinition.nextSchematic(random);

                // initialize the values needed to rotate the schematic
                int rotation = random.nextInt(4);
                // Whether or not the schematic points into north or south direction
                boolean northSouth = rotation % 2 == 0;
                int xMod;
                int zMod;
                if (rotation < 2) {
                    xMod = 1;
                } else {
                    xMod = -1;
                }
                if (rotation > 0 && rotation < 3) {
                    zMod = -1;
                } else {
                    zMod = 1;
                }

                int schematicWidth = northSouth
                        ? schematic.getDimensions().getBlockX()
                        : schematic.getDimensions().getBlockZ();
                int schematicHeight = schematic.getDimensions().getBlockY();
                int schematicLength = northSouth
                        ? schematic.getDimensions().getBlockZ()
                        : schematic.getDimensions().getBlockX();

                int startX = schematicX - schematicWidth / 2;
                int startZ = schematicZ - schematicLength / 2;

                // Get center height
                int schematicY = worldConfiguration.getHeight((chunkX << 4) + schematicX, (chunkZ << 4) + schematicZ) + 1;
                // Check actual chunk highest block if the full schematic is in the chunk
                int schematicOffset = schematic.getYOffset();
                if (startX >= 0 && startX + schematicWidth < 16 && startZ >= 0 && startZ + schematicLength < 16) {
                    schematicY = Util.getHighestBlock(world, chunk, schematicX, schematicZ);
                    // Check if valid ground block, continue if not
                    if (!biomeDefinition.isGroundBlock(chunk.getBlockData(inChunkX, schematicY - 1, inChunkZ))) {
                        continue;
                    }
                    // Try putting schematic on floor
                    boolean foundSolid = false;
                    for (int testedY = 0; testedY < schematicHeight && !foundSolid; testedY++) {
                        for (int x = 0; x < schematicWidth; x++) {
                            for (int z = 0; z < schematicLength; z++) {
                                // Create the rotated vector
                                BlockData b = schematic.getBlock(xMod * (northSouth ? x : z), testedY, zMod * (northSouth ? z : x));
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
                        int schemX = schematicX - xMod * schematicWidth / 2 + xMod * (northSouth ? x : z);
                        int schemZ = schematicZ - xMod * schematicLength / 2 + zMod * (northSouth ? z : x);
                        if (schemZ >= 0 && schemZ < 16 && schemX >= 0 && schemX < 16) {
                            for (int y = 0; y < schematicHeight; y++) {
                                BlockData block = schematic.getBlock(x, y, z);
                                if (block != null && !block.getMaterial().isAir()) {
                                    chunk.setBlock(schemX, schematicY + schematicOffset + y, schemZ, Util.rotateBlock(block, rotation));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private class ChunkCoord {
        private final int x;
        private final int z;

        private ChunkCoord(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }
    }
}
