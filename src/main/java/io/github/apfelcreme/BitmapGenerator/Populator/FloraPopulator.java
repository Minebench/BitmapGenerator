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
import org.bukkit.material.MaterialData;

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
public class FloraPopulator extends BlockPopulator {

    private WorldConfiguration worldConfiguration;

    public FloraPopulator(WorldConfiguration worldConfiguration) {
        this.worldConfiguration = worldConfiguration;
    }



    @Override
    public synchronized void populate(World world, Random random, Chunk chunk) {
        for (BiomeDefinition biomeDefinition : worldConfiguration.getDistinctChunkBiomes(chunk)) {
            double floraCount;
            if (biomeDefinition.getFloraChance() < 1) {
                floraCount = Math.random() <= biomeDefinition.getFloraChance() ? 1 : 0;
            } else {
                floraCount = (int) biomeDefinition.getFloraChance();
            }
            for (int i = 0; i < floraCount; i++) {
                int floraX = (chunk.getX() << 4) + random.nextInt(16);
                int floraZ = (chunk.getZ() << 4) + random.nextInt(16);
                int floraY = Util.getHighestBlock(world, floraX, floraZ) + 1;
                if (worldConfiguration.getBiomeDefinition(floraX, floraZ).equals(biomeDefinition)) {
                    MaterialData floraData = biomeDefinition.nextFloraData();
                    if (floraData != null) {
                        if (biomeDefinition.isGroundBlock(world.getBlockAt(floraX, floraY - 1, floraZ))) {
                            if (canBePlanted(floraData, world.getBlockAt(floraX, floraY - 1, floraZ))) {
                                world.getBlockAt(floraX, floraY, floraZ).setTypeIdAndData(floraData.getItemType().getId(), floraData.getData(), true);
                            }
                        }
                    }
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
    private boolean canBePlanted(MaterialData flora, Block groundBlock) {
        if (flora.getItemType() == Material.LONG_GRASS) {
            if (groundBlock.getType() != Material.GRASS && groundBlock.getType() != Material.DIRT) {
                return false;
            }
        }
        return groundBlock.getRelative(BlockFace.UP).isEmpty();
    }
}
