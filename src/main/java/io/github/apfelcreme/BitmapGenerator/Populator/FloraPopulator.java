package io.github.apfelcreme.BitmapGenerator.Populator;

import io.github.apfelcreme.BitmapGenerator.BiomeDefinition;
import io.github.apfelcreme.BitmapGenerator.Util;
import io.github.apfelcreme.BitmapGenerator.WorldConfiguration;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
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
public class FloraPopulator implements ChunkPopulator {

    private WorldConfiguration worldConfiguration;

    public FloraPopulator(WorldConfiguration worldConfiguration) {
        this.worldConfiguration = worldConfiguration;
    }



    @Override
    public synchronized void populate(World world, Random random, int chunkX, int chunkZ, ChunkGenerator.ChunkData chunk, BiomeDefinition biomeDefinition) {
        double floraCount;
        if (biomeDefinition.getFloraChance() < 1) {
            floraCount = random.nextDouble() <= biomeDefinition.getFloraChance() ? 1 : 0;
        } else {
            floraCount = biomeDefinition.getFloraChance() / 2 + random.nextInt((int) biomeDefinition.getFloraChance());
        }
        for (int i = 0; i < floraCount; i++) {
            int floraX = random.nextInt(16);
            int floraZ = random.nextInt(16);
            int floraY = Util.getHighestBlock(world, chunk, floraX, floraZ) + 1;
            if (worldConfiguration.getBiomeDefinition((chunkX << 4) + floraX, (chunkX << 4) + floraZ).equals(biomeDefinition)) {
                BlockData floraData = biomeDefinition.nextFloraData(random);
                if (floraData != null
                        && chunk.getType(floraX, floraY, floraZ) == Material.AIR
                        && biomeDefinition.isGroundBlock(chunk.getBlockData(floraX, floraY - 1, floraZ))
                        && canBePlanted(floraData, chunk.getType(floraX, floraY - 1, floraZ))
                        && chunk.getType(floraX, floraY + 1, floraZ).isAir()) {
                    chunk.setBlock(floraX, floraY, floraZ, floraData);
                }
            }
        }
    }

    /**
     * checks if a flora type can be planted on the given block
     *
     * @param flora       the flora
     * @param groundBlock the block
     * @return true if it can be planted there, false otherwise
     */
    private boolean canBePlanted(BlockData flora, Material groundBlock) {
        if (Tag.FLOWERS.isTagged(flora.getMaterial())) {
            if (groundBlock != Material.GRASS && groundBlock != Material.DIRT) {
                return false;
            }
        }
        return true;
    }
}
