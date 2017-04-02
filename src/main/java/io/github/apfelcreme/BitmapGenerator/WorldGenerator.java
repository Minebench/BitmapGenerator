package io.github.apfelcreme.BitmapGenerator;

import io.github.apfelcreme.BitmapGenerator.Populator.FloraPopulator;
import io.github.apfelcreme.BitmapGenerator.Populator.OrePopulator;
import io.github.apfelcreme.BitmapGenerator.Populator.SchematicPopulator;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.generator.NormalChunkGenerator;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.MaterialData;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

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
public class WorldGenerator extends ChunkGenerator {

    private BitmapGenerator plugin;
    private BufferedImage blockMap;
    private BufferedImage heightMap;

    private ChunkProviderServer chunkProvider = null;

    public WorldGenerator(BitmapGenerator plugin, BufferedImage blockMap, BufferedImage heightMap) {
        this.plugin = plugin;
        this.blockMap = blockMap;
        this.heightMap = heightMap;
    }

    @Override
    public synchronized ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        ChunkData data = createChunkData(world);
        int minChunkX = -((blockMap.getWidth() / 2) / 16);
        int minChunkZ = -((blockMap.getHeight() / 2) / 16);
        int maxChunkX = ((blockMap.getWidth() / 2) / 16) - 1;
        int maxChunkZ = ((blockMap.getHeight() / 2) / 16) - 1;

        int diffX = x - minChunkX;
        int diffZ = z - minChunkZ;

        int surfaceLayerHeight = 4;

        if (x >= minChunkX && x <= maxChunkX && z >= minChunkZ && z <= maxChunkZ) {
            for (int cX = 0; cX < 16; cX++) {
                for (int cZ = 0; cZ < 16; cZ++) {
                    biome.setBiome(cX, cZ, Biome.BEACHES);
                    data.setBlock(cX, 0, cZ, Material.BEDROCK);
                    for (int cY = 1; cY < 256; cY++) {
                        int heighestBlock = (heightMap.getRGB((diffX * 16) + cX, (diffZ * 16) + cZ)  >> 16) & 0x000000FF;

                        // fill with the destined block
                        if (cY <= heighestBlock && cY > (heighestBlock - surfaceLayerHeight)) {
                            int blockR = (blockMap.getRGB((diffX * 16) + cX, (diffZ * 16) + cZ) >> 16) & 0x000000FF;
                            int blockG = (blockMap.getRGB((diffX * 16) + cX, (diffZ * 16) + cZ) >> 8) & 0x000000FF;
                            int blockB = (blockMap.getRGB((diffX * 16) + cX, (diffZ * 16) + cZ)) & 0x000000FF;
                            MaterialData material = plugin.getBitmapGeneratorConfig().getBlock(blockR, blockG, blockB);
                            if (material != null) {
                                data.setBlock(cX, cY, cZ, material);
                            }
                        } else if (cY <= (heighestBlock - surfaceLayerHeight)) { // everything under the surface layer
//                            data.setBlock(cX, cY, cZ, naturalChunkData[cX][cY][cZ]);
                            data.setBlock(cX, cY, cZ, Material.STONE);
                            // ores will be populated later
                        } else if (cY >= heighestBlock) { // everything above the highest block -> air
                            data.setBlock(cX, cY, cZ, new MaterialData(Material.AIR));
                        }

                        // Fill everything under level 61 with water
                        if ((data.getType(cX, cY, cZ) == null || data.getType(cX, cY, cZ) == Material.AIR) && cY <= 60) {
                            data.setBlock(cX, cY, cZ, new MaterialData(Material.WATER));
                        }
                    }
                }
            }
        } else {
            // No image-data for this chunk: fill with water
            for (int cX = 0; cX < 16; cX++) {
                for (int cZ = 0; cZ < 16; cZ++) {
                    data.setBlock(cX, 0, cZ, Material.BEDROCK);
                    for (int cY = 1; cY <= 60; cY++) {
                        data.setBlock(cX, cY, cZ, Material.WATER);
                    }
                }
            }
        }
        return data;
    }

    /**
     * pregenerates a chunk with the default world generator
     * @param w the world
     * @param x x coordinate of the chunk
     * @param z z coordinate of the chunk
     * @return the block the default generator would paste
     */
    private MaterialData[][][] getNaturalChunk(World w, int x, int z) {
        if (chunkProvider == null) {
            CraftWorld cw = (CraftWorld) w;
            WorldServer ws = cw.getHandle();
            IChunkLoader loader = ws.getDataManager().createChunkLoader(ws.worldProvider);
            NormalChunkGenerator _gen = new NormalChunkGenerator(ws, w.getSeed());
            chunkProvider = new ChunkProviderServer(ws, loader, _gen);
        }

        MaterialData[][][] naturalData = new MaterialData[16][256][16];


        Chunk chunk = chunkProvider.getChunkAt(x, z);
        chunk.p();

        for (int bX = 0; bX < 16; bX++) {
            for (int bY = 0; bY < 256; bY++) {
                for (int bZ = 0; bZ < 16; bZ++) {
                    naturalData[bX][bY][bZ] = new MaterialData(Material.getMaterial(
                            Block.getId(chunk.getBlockData(new BlockPosition(bX, bY, bZ)).getBlock())));
                }
            }
        }
        return naturalData;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Arrays.asList(
                new FloraPopulator(),
                new SchematicPopulator(plugin),
                new OrePopulator(plugin)
        );
    }

}
