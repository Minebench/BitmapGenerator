package io.github.apfelcreme.BitmapGenerator.Populator;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.schematic.SchematicFormat;
import io.github.apfelcreme.BitmapGenerator.BitmapGenerator;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.BlockPopulator;

import java.io.File;
import java.util.*;
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

    private BitmapGenerator plugin;
    private PopulationSettings populationSettings;

    public SchematicPopulator(BitmapGenerator plugin) {
        this.plugin = plugin;
        this.populationSettings = new PopulationSettings();
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        for (String foundationBlockId : plugin.getConfig().getConfigurationSection("population.schematics").getKeys(false)) {
            Material foundation = Material.getMaterial(Integer.valueOf(foundationBlockId));
            for (int c = 0; c < plugin.getConfig().getInt("population.schematics."+foundationBlockId+".count"); c++) {
                PopulationSettings.Schematic schematic = populationSettings.getRandomSchematic(Integer.valueOf(foundationBlockId));
                if (schematic != null) {
                    int centerX = (chunk.getX() << 4) + random.nextInt(16);
                    int centerZ = (chunk.getZ() << 4) + random.nextInt(16);
                    int centerY = world.getHighestBlockYAt(centerX, centerZ);
                    if (world.getBlockAt(centerX, centerY - 1, centerZ).getType() == foundation) {
                        for (int x = 0; x < schematic.clipboard.getWidth(); x++) {
                            for (int y = 0; y < schematic.clipboard.getHeight(); y++) {
                                for (int z = 0; z < schematic.clipboard.getLength(); z++) {
                                    if (world.getBlockAt(centerX + x, centerY + y, centerZ + z).getType() == Material.AIR) {
                                        world.getBlockAt(centerX + x, centerY + y, centerZ + z).setTypeIdAndData(
                                                schematic.clipboard.getBlock(new Vector(x, y, z)).getId(),
                                                (byte) schematic.clipboard.getBlock(new Vector(x, y, z)).getData(),
                                                true);
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    public class PopulationSettings {

        private Map<Integer, List<Schematic>> availableSchematics;

        public PopulationSettings() {
            availableSchematics = new HashMap<>();
            Set<String> foundationBlocks = plugin.getConfig().getConfigurationSection("population.schematics").getKeys(false);
            for (String foundationBlockId : foundationBlocks) {
                Set<String> schematicNames = plugin.getConfig().getConfigurationSection("population.schematics." + foundationBlockId + ".schematic").getKeys(false);
                List<Schematic> schematics = new ArrayList<>();
                for (String schematicName : schematicNames) {
                    schematics.add(new Schematic(
                            getSchematic(schematicName),
                            plugin.getConfig().getDouble("population.schematics." + foundationBlockId + ".schematic." + schematicName + ".chance"))
                    );
                }
                availableSchematics.put(Integer.valueOf(foundationBlockId), schematics);
            }
        }

        /**
         * returns a schematic with the given file name
         *
         * @param filename a file name
         * @return the clipboard of the schematic
         */
        public CuboidClipboard getSchematic(String filename) {
            if (filename == null || filename.isEmpty()) {
                return null;
            }

            if (!filename.endsWith(".schematic")) {
                filename += ".schematic";
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

        /**
         * returns a random schematic based on the block it shall be pasted on
         * @param foundationBlockId the id of the block the schemtic is pasted on
         * @return a randomly selected schematic
         */
        public Schematic getRandomSchematic(int foundationBlockId) {
            int totalSum = 0;
            Random random = new Random();
            if (availableSchematics.get(foundationBlockId) == null) {
                return null;
            }
            for (Schematic schematic : availableSchematics.get(foundationBlockId)) {
                totalSum += schematic.chance * 100;
            }
            int index = random.nextInt(totalSum);
            int sum = 0;
            int i = 0;
            while (sum < index) {
                sum = sum + (int) (availableSchematics.get(foundationBlockId).get(i++).chance * 100);
            }
            return availableSchematics.get(foundationBlockId).get(Math.max(0, i - 1));
        }

        /**
         * a class to represent a schematic and its probability to be pasted
         */
        public class Schematic {
            private CuboidClipboard clipboard;
            private double chance;

            public Schematic(CuboidClipboard clipboard, double chance) {
                this.clipboard = clipboard;
                this.chance = chance;
            }
        }
    }
}
