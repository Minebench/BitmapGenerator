package io.github.apfelcreme.BitmapGenerator.Populator;

/*
 * BitmapGenerator
 * Copyright (c) 2020 Max Lee aka Phoenix616 (max@themoep.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import io.github.apfelcreme.BitmapGenerator.BiomeDefinition;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public interface ChunkPopulator {
    void populate(World world, Random random, int chunkX, int chunkZ, ChunkGenerator.ChunkData chunk, BiomeDefinition biomeDefinition);
}
