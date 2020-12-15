package io.github.apfelcreme.BitmapGenerator.Populator;

import io.github.apfelcreme.BitmapGenerator.BiomeDefinition;
import io.github.apfelcreme.BitmapGenerator.Util;
import io.github.apfelcreme.BitmapGenerator.WorldConfiguration;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
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
public class TreePopulator extends BlockPopulator {

    private WorldConfiguration worldConfiguration;

    public TreePopulator(WorldConfiguration worldConfiguration) {
        this.worldConfiguration = worldConfiguration;
    }

    @Override
    public synchronized void populate(World world, Random random, Chunk chunk) {
        for (BiomeDefinition biomeDefinition : worldConfiguration.getDistinctChunkBiomes(chunk)) {
            double treeCount;
            if (biomeDefinition.getTreeChance() < 1) {
                treeCount = random.nextDouble() <= biomeDefinition.getTreeChance() ? 1 : 0;
            } else {
                treeCount = (int) biomeDefinition.getTreeChance();
            }
            for (int i = 0; i < treeCount; i++) {
                int treeX = (chunk.getX() << 4) + random.nextInt(16);
                int treeZ = (chunk.getZ() << 4) + random.nextInt(16);
                int treeY = Util.getHighestBlock(world, treeX, treeZ) + 1;
                if (biomeDefinition.isGroundBlock(world.getBlockAt(treeX, treeY - 1, treeZ))) {
                    if (worldConfiguration.getBiomeDefinition(treeX, treeZ).equals(biomeDefinition)) {
                        BiomeDefinition.TreeData treeData = biomeDefinition.nextTree();
                        if (treeData != null) {
                            world.generateTree(new Location(world, treeX, treeY, treeZ), treeData.getType());
                        }
                    }
                }
            }
        }
    }

}
