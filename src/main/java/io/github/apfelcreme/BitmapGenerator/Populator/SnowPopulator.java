package io.github.apfelcreme.BitmapGenerator.Populator;

import io.github.apfelcreme.BitmapGenerator.BiomeDefinition;
import io.github.apfelcreme.BitmapGenerator.WorldConfiguration;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.data.type.Snow;
import org.bukkit.generator.ChunkGenerator;

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
public class SnowPopulator implements ChunkPopulator {

    private WorldConfiguration worldConfiguration;

    public SnowPopulator(WorldConfiguration worldConfiguration) {
        this.worldConfiguration = worldConfiguration;
    }

    @Override
    public synchronized void populate(World world, Random random, int chunkX, int chunkZ, ChunkGenerator.ChunkData chunk, BiomeDefinition biomeDefinition) {
        if (biomeDefinition.isSnowfall()) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int snowX = (chunkX << 4) + x;
                    int snowZ = (chunkZ << 4) + z;
                    if (worldConfiguration.getBiomeDefinition(snowX, snowZ).equals(biomeDefinition)) {
                        for (int y = worldConfiguration.getHeight(snowX, snowZ); y < world.getMaxHeight(); y++) {
                            byte snowHeight = worldConfiguration.getSnowHeight(snowX, snowZ);
                            Material below = chunk.getType(x, y - 1, z);
                            if (below.isSolid()) {
                                Material type = chunk.getType(x, y, z);
                                if (type.isAir()) {
                                    Snow snow = (Snow) Material.SNOW.createBlockData();
                                    snow.setLayers(snowHeight > 0 ? snowHeight : 1);
                                    chunk.setBlock(x, y, z, snow);
                                }
                                if (snowHeight > 0 && !type.isSolid() && biomeDefinition.isGroundBlock(below.createBlockData())) {
                                    chunk.setBlock(x, y - 1, z, Material.SNOW_BLOCK);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
