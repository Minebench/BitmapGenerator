package io.github.apfelcreme.BitmapGenerator.Populator;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.schematic.SchematicFormat;
import io.github.apfelcreme.BitmapGenerator.BitmapGenerator;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

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
public class SchematicPopulator extends BlockPopulator {

    BitmapGenerator plugin;

    public SchematicPopulator(BitmapGenerator plugin) {
        this.plugin = plugin;
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        List<String> schematicNames = plugin.getConfig().getStringList("schematics");
        if (schematicNames != null && !schematicNames.isEmpty()) {
            for (int i = 0; i < 3; i++) {
                int centerX = (chunk.getX() << 4) + random.nextInt(16);
                int centerZ = (chunk.getZ() << 4) + random.nextInt(16);
                int centerY = world.getHighestBlockYAt(centerX, centerZ);

                if (world.getBlockAt(centerX, centerY - 1, centerZ).getType() == Material.GRASS) {
                    String schematicFileName = schematicNames.get(new Random().nextInt(schematicNames.size()));
                    CuboidClipboard clipboard = getSchematic(schematicFileName);
                    centerX = centerX - (clipboard.getWidth() / 2);
                    centerZ = centerZ - (clipboard.getLength() / 2);
                    for (int x = 0; x < clipboard.getWidth(); x++) {
                        for (int y = 0; y < clipboard.getHeight(); y++) {
                            for (int z = 0; z < clipboard.getLength(); z++) {
                                try {
                                    if (world.getBlockAt(centerX + x, centerY + y, centerZ + z).getType() == Material.AIR) {
                                        world.getBlockAt(centerX + x, centerY + y, centerZ + z).setTypeIdAndData(
                                                clipboard.getBlock(new Vector(x, y, z)).getId(),
                                                (byte) clipboard.getBlock(new Vector(x, y, z)).getData(),
                                                true);
                                    }
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    System.out.println("aioob: " + x + " " + y + " " + z);
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public CuboidClipboard getSchematic(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }

        File file = new File(plugin.getDataFolder() + "/schematics/" + filename);
        if (!file.exists()) {
            plugin.getLogger().log(Level.SEVERE, "No schematic found with the name " + filename + "!");
            return null;
        }
        SchematicFormat schemFormat = SchematicFormat.getFormat(file);
        if (schemFormat == null) {
            plugin.getLogger().log(Level.SEVERE, "Could not load schematic format from file " + file.getAbsolutePath() + "!");
            return null;
        }
        try {
            return schemFormat.load(file);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading file " + file.getAbsolutePath(), e);
            return null;
        }
    }
}
