package io.github.apfelcreme.BitmapGenerator.Populator;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
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
            for (BiomeDefinition biomeDefinition : worldConfiguration.getDistinctChunkBiomes(chunk)) {
                double schematicCount;
                if (biomeDefinition.getSchematicChance() < 1) {
                    schematicCount = Math.random() <= biomeDefinition.getSchematicChance() ? 1 : 0;
                } else {
                    schematicCount = (int) biomeDefinition.getSchematicChance();
                }
                for (int i = 0; i < schematicCount; i++) {
                    BiomeDefinition.Schematic schematic = biomeDefinition.nextSchematic();
                    int schematicX = (chunk.getX() << 4) + random.nextInt(16);
                    int schematicZ = (chunk.getZ() << 4) + random.nextInt(16);
                    int schematicY = Util.getHighestBlock(world, schematicX, schematicZ) + 1;

                    if (worldConfiguration.getBiomeDefinition(schematicX, schematicZ).equals(biomeDefinition)) {
                        if (biomeDefinition.isGroundBlock(world.getBlockAt(schematicX, schematicY - 1, schematicZ))) {
                            int schematicWidth = schematic.getClipboard().getWidth();
                            int schematicHeight = schematic.getClipboard().getHeight();
                            int schematicLength = schematic.getClipboard().getLength();
                            for (int x = 0; x < schematicWidth; x++) {
                                for (int y = 0; y < schematicHeight; y++) {
                                    for (int z = 0; z < schematicLength; z++) {
                                        try {
                                            Block block = world.getBlockAt(
                                                    schematicX + x - (schematicWidth / 2),
                                                    schematicY + y + schematic.getYOffset(),
                                                    schematicZ + z - (schematicLength / 2));
                                            BaseBlock b = schematic.getClipboard().getBlock(new Vector(x, y, z));
                                            if (b != null && !b.isAir()) {
                                                block.setTypeIdAndData(b.getId(), (byte) b.getData(), true);
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
