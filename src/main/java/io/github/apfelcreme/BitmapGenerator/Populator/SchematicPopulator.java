package io.github.apfelcreme.BitmapGenerator.Populator;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.schematic.SchematicFormat;
import io.github.apfelcreme.BitmapGenerator.BiomeDefinition;
import io.github.apfelcreme.BitmapGenerator.BitmapGenerator;
import io.github.apfelcreme.BitmapGenerator.WorldGenerator;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.BlockPopulator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.logging.Level;

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
    private BitmapGenerator plugin;

    public SchematicPopulator(BitmapGenerator plugin, BufferedImage biomeMap) {
        this.plugin = plugin;
        this.biomeMap = biomeMap;
    }

    @Override
    public synchronized void populate(World world, Random random, Chunk chunk) {
        int minChunkX = -((biomeMap.getWidth() / 2) / 16);
        int minChunkZ = -((biomeMap.getHeight() / 2) / 16);
        int maxChunkX = ((biomeMap.getWidth() / 2) / 16) - 1;
        int maxChunkZ = ((biomeMap.getHeight() / 2) / 16) - 1;
        if (chunk.getX() >= minChunkX && chunk.getX() <= maxChunkX && chunk.getZ() >= minChunkZ && chunk.getZ() <= maxChunkZ) {
            for (BiomeDefinition biomeDefinition : plugin.getDistinctChunkBiomes(chunk)) {
                for (int i = 0; i < biomeDefinition.getSchematicCount(); i++) {
                    BiomeDefinition.Schematic schematic = biomeDefinition.nextSchematic();
                    int schematicX = (chunk.getX() << 4) + random.nextInt(16);
                    int schematicZ = (chunk.getZ() << 4) + random.nextInt(16);
                    int schematicY = world.getHighestBlockYAt(schematicX, schematicZ);

                    if (plugin.getBiomeDefinition(schematicX, schematicZ).equals(biomeDefinition)) {
                        if (biomeDefinition.isGroundBlock(world.getBlockAt(schematicX, schematicY - 1, schematicZ))) {
                            for (int x = 0; x < schematic.getClipboard().getWidth(); x++) {
                                for (int y = 0; y < schematic.getClipboard().getHeight(); y++) {
                                    for (int z = 0; z < schematic.getClipboard().getLength(); z++) {
                                        Block block = world.getBlockAt(
                                                schematicX + x - (schematic.getClipboard().getWidth() / 2),
                                                schematicY + y,
                                                schematicZ + z - (schematic.getClipboard().getLength() / 2));
                                        if (block.getType() == Material.AIR) {
                                            block.setTypeIdAndData(schematic.getClipboard().getBlock(new Vector(x, y, z)).getId(),
                                                    (byte) schematic.getClipboard().getBlock(new Vector(x, y, z)).getData(),
                                                    true);
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
