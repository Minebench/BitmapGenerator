package io.github.apfelcreme.BitmapGenerator.Populator;

import io.github.apfelcreme.BitmapGenerator.BiomeDefinition;
import io.github.apfelcreme.BitmapGenerator.Util;
import io.github.apfelcreme.BitmapGenerator.WorldConfiguration;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
public class SnowPopulator extends BlockPopulator {

    private WorldConfiguration worldConfiguration;
    private final BufferedImage biomeMap;

    public SnowPopulator(WorldConfiguration worldConfiguration) {
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
                if (biomeDefinition.isSnowfall()) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int snowX = (chunk.getX() << 4) + x;
                            int snowZ = (chunk.getZ() << 4) + z;
                            if (worldConfiguration.getBiomeDefinition(snowX, snowZ).equals(biomeDefinition)) {
                                for (int y = worldConfiguration.getHeight(snowX, snowZ); y < 255; y++) {
                                    Block block = world.getBlockAt(snowX, y, snowZ);
                                    if (block.getType() == Material.AIR
                                            && block.getRelative(BlockFace.DOWN).getType() != Material.SNOW
                                            && block.getRelative(BlockFace.DOWN).getType() != Material.AIR) {
                                        block.setTypeIdAndData(Material.SNOW.getId(), worldConfiguration.getSnowHeight(snowX, snowZ), true);
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
