package io.github.apfelcreme.BitmapGenerator;

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
public class PseudoWorldGenerator extends ChunkGenerator {

    @Override
    public synchronized ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        // Whenever Multiverse gets a list of generators, this will be called instead of the actual Bitmap-Generator
        // to not create any useless files. (Apparently to get a list of generators and to check their viability,
        // Multiverse tries to create a world with it)
        return createChunkData(world);
    }
}
