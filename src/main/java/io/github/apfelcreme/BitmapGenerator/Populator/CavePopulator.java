package io.github.apfelcreme.BitmapGenerator.Populator;

import io.github.apfelcreme.BitmapGenerator.WorldConfiguration;
import org.bukkit.Chunk;
import org.bukkit.Material;
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
public class CavePopulator extends BlockPopulator {

    private final BufferedImage biomeMap;
    private WorldConfiguration worldConfiguration;

    public CavePopulator(WorldConfiguration worldConfiguration) {
        this.worldConfiguration = worldConfiguration;
        this.biomeMap = worldConfiguration.getBiomeMap();
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {

        double radius = worldConfiguration.getCaveRadius();

        int minChunkX = -((biomeMap.getWidth() / 2) / 16);
        int minChunkZ = -((biomeMap.getHeight() / 2) / 16);
        int maxChunkX = ((biomeMap.getWidth() / 2) / 16) - 1;
        int maxChunkZ = ((biomeMap.getHeight() / 2) / 16) - 1;

        if (chunk.getX() >= minChunkX && chunk.getX() <= maxChunkX && chunk.getZ() >= minChunkZ && chunk.getZ() <= maxChunkZ) {

            for (int x = 0; x < 16; x++) {
                int coordX = (chunk.getX() << 4) + x;
                for (int z = 0; z < 16; z++) {
                    int coordZ = (chunk.getZ() << 4) + z;
                    if (worldConfiguration.isCave(coordX, coordZ)) {
                        int coordY = worldConfiguration.getCaveHeight(coordX, coordZ);
                        if (coordY > 5) {
                            Block block = world.getBlockAt(coordX, coordY, coordZ);
                            if (block.getType() != Material.AIR ) {
                                // TODO: make this somehow nicer :(
                                for (int rX = (int) (coordX - radius); rX < coordX + radius; rX++) {
                                    for (int rY = (int) (coordY - radius); rY < coordY + radius; rY++) {
                                        for (int rZ = (int) (coordZ - radius); rZ < coordZ + radius; rZ++) {
                                            if (block.getType() == Material.STONE
                                                    && block.getLocation().distance(world.getBlockAt(rX, rY, rZ).getLocation()) <= radius) {
                                                world.getBlockAt(rX, rY, rZ).setTypeIdAndData(Material.AIR.getId(), (byte) 0, false);
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
}
