package io.github.apfelcreme.BitmapGenerator.Populator;

import io.github.apfelcreme.BitmapGenerator.WorldConfiguration;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

import java.util.ArrayDeque;
import java.util.Deque;
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

    private WorldConfiguration worldConfiguration;

    public CavePopulator(WorldConfiguration worldConfiguration) {
        this.worldConfiguration = worldConfiguration;
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        double radius = worldConfiguration.getCaveRadius();
        for (int x = chunk.getX() << 4; x < (chunk.getX() << 4) + 16; x++) {
            for (int z = chunk.getZ() << 4; z < (chunk.getZ() << 4) + 16; z++) {
                if (worldConfiguration.useAdvancedCaveGenerator()) {
                    for (int y = (int) radius + 1; y < 128; y++) {
                        if (worldConfiguration.isCave(x, y, z)) {
                            drawSphere(world, x, y, z, radius);
                        }
                    }
                } else {
                    if (worldConfiguration.isCave(x, z)) {
                        int y = worldConfiguration.getCaveHeight(x, z);
                        if (y > radius + 1 && y < 128) {
                            drawSphere(world, x, y, z, radius);
                        }
                    }
                }
            }
        }
    }
    
    private void drawSphere(World world, int coordX, int coordY, int coordZ, double radius) {
        Block block = world.getBlockAt(coordX, coordY, coordZ);
        if (block.getType() == Material.STONE) {
            // TODO: make this somehow nicer :(
            int chunkX = coordX >> 4;
            int chunkZ = coordZ >> 4;
    
            int caveHeight = worldConfiguration.getCaveHeight(coordX, coordZ);
            radius = (caveHeight / 50) * radius;
            double squaredRadius = Math.pow(radius, 2);
            
            for (int rX = (int) (coordX - radius); rX < coordX + radius; rX++) {
                for (int rZ = (int) (coordZ - radius); rZ < coordZ + radius; rZ++) {
                    if ((rX >> 4 == chunkX && rZ >> 4 == chunkZ) || world.isChunkLoaded(rX >> 4, rZ >> 4)) {
                        for (int rY = (int) (coordY - radius); rY < coordY + radius; rY++) {
                            if (rY > 1 && block.getLocation().distanceSquared(new Location(world, rX, rY, rZ)) <= squaredRadius) {
                                Block b = world.getBlockAt(rX, rY, rZ);
                                if (b.getType().isSolid()) {
                                    b.setType(rY > 10 ? Material.AIR : Material.LAVA, false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
}
