package io.github.apfelcreme.BitmapGenerator.Populator;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockData;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.world.registry.WorldData;
import io.github.apfelcreme.BitmapGenerator.BiomeDefinition;
import io.github.apfelcreme.BitmapGenerator.Util;
import io.github.apfelcreme.BitmapGenerator.WorldConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.awt.image.BufferedImage;
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
public class SchematicPopulator extends BlockPopulator {

    private final BufferedImage biomeMap;
    private WorldConfiguration worldConfiguration;

    public SchematicPopulator(WorldConfiguration worldConfiguration) {
        this.worldConfiguration = worldConfiguration;
        this.biomeMap = worldConfiguration.getBiomeMap();
    }

    @Override
    public synchronized void populate(World world, Random random, Chunk chunk) {
        int minChunkX = -((biomeMap.getWidth() / 2) / 16);
        int minChunkZ = -((biomeMap.getHeight() / 2) / 16);
        int maxChunkX = ((biomeMap.getWidth() / 2) / 16) - 1;
        int maxChunkZ = ((biomeMap.getHeight() / 2) / 16) - 1;
        if (chunk.getX() >= minChunkX && chunk.getX() <= maxChunkX && chunk.getZ() >= minChunkZ && chunk.getZ() <= maxChunkZ) {
            WorldData worldData = BukkitUtil.getLocalWorld(world).getWorldData();
            for (BiomeDefinition biomeDefinition : worldConfiguration.getDistinctChunkBiomes(chunk)) {
                double schematicCount;
                if (biomeDefinition.getSchematicChance() < 1) {
                    schematicCount = Math.random() <= biomeDefinition.getSchematicChance() ? 1 : 0;
                } else {
                    schematicCount = (int) biomeDefinition.getSchematicChance();
                }
                for (int i = 0; i < schematicCount; i++) {

                    int schematicX = (chunk.getX() << 4) + random.nextInt(16);
                    int schematicZ = (chunk.getZ() << 4) + random.nextInt(16);
                    int schematicY = Util.getHighestBlock(world, schematicX, schematicZ) + 1;

                    if (worldConfiguration.getBiomeDefinition(schematicX, schematicZ).equals(biomeDefinition)) {
                        if (biomeDefinition.isGroundBlock(world.getBlockAt(schematicX, schematicY - 1, schematicZ))) {
                            BiomeDefinition.Schematic schematic = biomeDefinition.nextSchematic();

                            // initialize the values needed to rotate the schematic
                            int rotation = random.nextInt(4);
                            // Whether or not the schematic points into north or south direction
                            boolean northSouth = rotation % 2 == 0;
                            int xMod;
                            int zMod;
                            int xStart = schematic.getClipboard().getOrigin().getBlockX();
                            int zStart = schematic.getClipboard().getOrigin().getBlockZ();
                            if (rotation < 2) {
                                xMod = 1;
                            } else {
                                xMod = -1;
                                xStart += schematic.getClipboard().getDimensions().getBlockX();
                            }
                            if (rotation > 0 && rotation < 3) {
                                zMod = -1;
                                zStart += schematic.getClipboard().getDimensions().getBlockZ();
                            } else {
                                zMod = 1;
                            }

                            int schematicWidth = northSouth
                                    ? schematic.getClipboard().getDimensions().getBlockX()
                                    : schematic.getClipboard().getDimensions().getBlockZ();
                            int schematicHeight = schematic.getClipboard().getDimensions().getBlockY();
                            int schematicLength = northSouth
                                    ? schematic.getClipboard().getDimensions().getBlockZ()
                                    : schematic.getClipboard().getDimensions().getBlockX();
                            for (int x = 0; x < schematicWidth; x++) {
                                for (int y = 0; y < schematicHeight; y++) {
                                    for (int z = 0; z < schematicLength; z++) {
                                        try {
                                            Block block = world.getBlockAt(
                                                    schematicX + x - (schematicWidth / 2),
                                                    schematicY + y + schematic.getYOffset(),
                                                    schematicZ + z - (schematicLength / 2));
                                            // Create the rotated vector
                                            Vector rotatedVector = new Vector(xStart + xMod * (northSouth ? x : z), y, zStart + zMod * (northSouth ? z : x));
                                            BaseBlock b = schematic.getClipboard().getBlock(rotatedVector);
                                            if (b != null && !b.isAir()) {
                                                int blockData = b.getData();
                                                // Rotate the actual block
                                                if (rotation < 3) {
                                                    for (int rot = 0; rot < rotation; rot++) {
                                                        blockData = BlockData.rotate90(b.getId(), blockData);
                                                    }
                                                } else {
                                                    blockData = BlockData.rotate90Reverse(b.getId(), blockData);
                                                }
                                                block.setTypeIdAndData(b.getId(), (byte) blockData, true);
                                            }
                                        } catch (ArrayIndexOutOfBoundsException e) {
                                            Bukkit.getServer().getLogger().severe(
                                                    "Error: too fast (?) at " + schematicX + "," + schematicY + "," + schematicZ);
                                            e.printStackTrace();
                                            // TODO: Happens sometimes for no apparent reason ?
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
