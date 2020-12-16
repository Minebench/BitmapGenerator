package io.github.apfelcreme.BitmapGenerator;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Attachable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

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
public class Util {

    /**
     * copies a file from the jar file to the plugin data path
     *
     * @param fileName  the name of the resource file
     * @param worldName the name of the world the file belongs to
     */
    public synchronized static void saveResource(BitmapGeneratorPlugin plugin, String fileName, String worldName) throws IOException {
        File out = new File(plugin.getDataFolder() + "/" + worldName + "/" + fileName);
        if (!out.exists()) {
            out.getParentFile().mkdirs();
            out.createNewFile();
            InputStream in = plugin.getClass().getResourceAsStream("/" + fileName);
            FileOutputStream fOut = new FileOutputStream(out);
            plugin.getLogger().info("Extracting '" + fileName + "' to '" + out.getAbsolutePath() + "' (" + in.available() + " byte)");
            int i;
            while ((i = in.read()) != -1) {
                fOut.write(i);
            }
            fOut.close();
            in.close();
        }
    }

    /**
     * returns the highest block at a position which isn't air or leaves or any other non-occluding block
     *
     * @param world the world
     * @param x     the x coordinate
     * @param z     the z coordinate
     * @return the highest block at a position which isn't air or leaves or any other non-occluding block
     */
    public static int getHighestBlock(World world, int x, int z) {
        for (int y = world.getMaxHeight(); y > 0; y--) {
            Block b = world.getBlockAt(x, y, z);
            if (b.getType().isOccluding()) {
                return y;
            }
        }
        return 1;
    }

    /**
     * returns the highest block at a position which isn't air or leaves or any other non-occluding block
     *
     * @param world     the world
     * @param chunkData The chunk data to search in
     * @param x         the chunk x coordinate
     * @param z         the chunk z coordinate
     * @return the highest block at a position which isn't air or leaves or any other non-occluding block
     */
    public static int getHighestBlock(World world, ChunkGenerator.ChunkData chunkData, int x, int z) {
        for (int y = world.getMaxHeight(); y > 0; y--) {
            if (chunkData.getType(x, y, z).isOccluding()) {
                return y;
            }
        }
        return 1;
    }

    /**
     * Get the blocks from a clipboard
     * @param clipboard
     * @return
     */
    public static BlockData[][][] getBlocks(Clipboard clipboard) {
        int diffX = clipboard.getMaximumPoint().getX() - clipboard.getMinimumPoint().getX();
        int diffY = clipboard.getMaximumPoint().getY() - clipboard.getMinimumPoint().getY();
        int diffZ = clipboard.getMaximumPoint().getZ() - clipboard.getMinimumPoint().getZ();
        BlockData[][][] blocks = new BlockData[diffX][diffY][diffZ];
        for (int x = 0; x < diffX; x++) {
            for (int y = 0; y < diffY; y++) {
                for (int z = 0; z < diffZ; z++) {
                    blocks[x][y][z] = BukkitAdapter.adapt(clipboard.getFullBlock(clipboard.getMinimumPoint().add(x, y, z)));
                }
            }
        }
        return blocks;
    }

    public static BlockData rotateBlock(BlockData block, int rotation) {
        if (rotation == 0) {
            return block;
        }
        if (block instanceof Directional) {
            BlockFace rotated = BlockFace.values()[((Directional) block).getFacing().ordinal() + rotation % 4];
            if (((Directional) block).getFaces().contains(rotated)) {
                ((Directional) block).setFacing(rotated);
            }
        }
        return block;
    }
}
