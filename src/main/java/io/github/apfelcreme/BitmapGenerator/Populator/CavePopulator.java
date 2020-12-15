package io.github.apfelcreme.BitmapGenerator.Populator;

import io.github.apfelcreme.BitmapGenerator.BiomeDefinition;
import io.github.apfelcreme.BitmapGenerator.WorldConfiguration;
import org.bukkit.Material;
import org.bukkit.World;
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
public class CavePopulator implements ChunkPopulator {

    private WorldConfiguration worldConfiguration;

    public CavePopulator(WorldConfiguration worldConfiguration) {
        this.worldConfiguration = worldConfiguration;
    }

    @Override
    public synchronized void populate(World world, Random random, int chunkX, int chunkZ, ChunkGenerator.ChunkData chunk, BiomeDefinition biomeDefinition) {
        double radius = worldConfiguration.getCaveRadius();
        for (int x = 0; x < 16; x++) {
            int coordX = (chunkX << 4) + x;
            for (int z = 0; z < 16; z++) {
                int coordZ = (chunkZ << 4) + z;
                if (worldConfiguration.isCave(coordX, coordZ)) {
                    int coordY = worldConfiguration.getCaveHeight(coordX, coordZ);
                    if (coordY > 5) {
                        Material type = chunk.getType(x, coordY, coordZ);
                        if (!type.isAir()) {
                            // TODO: make this somehow nicer :(
                            for (int rX = (int) (coordX - radius); rX < coordX + radius; rX++) {
                                for (int rY = (int) (coordY - radius); rY < coordY + radius; rY++) {
                                    for (int rZ = (int) (coordZ - radius); rZ < coordZ + radius; rZ++) {
                                        if (type == Material.STONE) {
                                            chunk.setBlock(rX, rY, rZ, Material.AIR);
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
