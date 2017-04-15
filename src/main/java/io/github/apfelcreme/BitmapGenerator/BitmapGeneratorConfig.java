package io.github.apfelcreme.BitmapGenerator;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.schematic.SchematicFormat;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.material.MaterialData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
public class BitmapGeneratorConfig {

    private BitmapGenerator plugin;

    public BitmapGeneratorConfig(BitmapGenerator plugin) {
        this.plugin = plugin;
        plugin.saveResource("config.yml", false);
        if (!new File(plugin.getDataFolder() + "/schematics").exists()) {
            new File(plugin.getDataFolder() + "/schematics").mkdirs();
        }
    }

    /**
     * returns a list of all defined biomes
     *
     * @return a list of all biomes
     */
    public List<BiomeDefinition> loadBiomes() {
        List<BiomeDefinition> biomes = new ArrayList<>();
        FileConfiguration config = plugin.getConfig();
        Set<String> biomeNames = config.getConfigurationSection("biomes").getKeys(false);
        for (String biomeName : biomeNames) {
            int r = config.getInt("biomes." + biomeName + ".r");
            int g = config.getInt("biomes." + biomeName + ".g");
            int b = config.getInt("biomes." + biomeName + ".b");
            Biome biome = Biome.valueOf(config.getString("biomes." + biomeName + ".biome"));
            boolean snowfall = config.getBoolean("biomes."+biomeName+".snow");
            List<BiomeDefinition.BlockData> blocks = new ArrayList<>();
            if (config.get("biomes." + biomeName + ".blocks") != null) {
                for (String materialName : config.getConfigurationSection("biomes." + biomeName + ".blocks").getKeys(false)) {
                    Material block = Material.getMaterial(config.getInt("biomes." + biomeName + ".blocks." + materialName + ".block"));
                    byte data = (byte) config.getInt("biomes." + biomeName + ".blocks." + materialName + ".data");
                    double chance = config.getDouble("biomes." + biomeName + ".blocks." + materialName + ".chance");
                    blocks.add(new BiomeDefinition.BlockData(new MaterialData(block, data), chance));
                }
            }
            int floraCount = config.getInt("biomes." + biomeName + ".floraCount");
            List<BiomeDefinition.BlockData> floraTypes = new ArrayList<>();
            if (config.get("biomes." + biomeName + ".floraTypes") != null) {
                for (String floraName : config.getConfigurationSection("biomes." + biomeName + ".floraTypes").getKeys(false)) {
                    Material block = Material.getMaterial(config.getInt("biomes." + biomeName + ".floraTypes." + floraName + ".block"));
                    byte data = (byte) config.getInt("biomes." + biomeName + ".floraTypes." + floraName + ".data");
                    double chance = config.getDouble("biomes." + biomeName + ".floraTypes." + floraName + ".chance");
                    floraTypes.add(new BiomeDefinition.BlockData(new MaterialData(block, data), chance));
                }
            }
            int treeCount = config.getInt("biomes." + biomeName + ".treeCount");
            List<BiomeDefinition.TreeData> treeTypes = new ArrayList<>();
            if (config.get("biomes." + biomeName + ".treeTypes") != null) {
                for (String treeName : config.getConfigurationSection("biomes." + biomeName + ".treeTypes").getKeys(false)) {
                    String type = config.getString("biomes." + biomeName + ".treeTypes." + treeName + ".type");
                    TreeType treeType = TreeType.valueOf(type);
                    double chance = config.getDouble("biomes." + biomeName + ".treeTypes." + treeName + ".chance");
                    treeTypes.add(new BiomeDefinition.TreeData(treeType, chance));
                }
            }
            int veinCount = config.getInt("biomes." + biomeName + ".veinCount");
            List<BiomeDefinition.OreVein> veinTypes = new ArrayList<>();
            if (config.get("biomes." + biomeName + ".veinTypes") != null) {
                for (String veinName : config.getConfigurationSection("biomes." + biomeName + ".veinTypes").getKeys(false)) {
                    Material block = Material.getMaterial(config.getInt("biomes." + biomeName + ".veinTypes." + veinName + ".block"));
                    byte data = (byte) config.getInt("biomes." + biomeName + ".veinTypes." + veinName + ".data");
                    double chance = config.getDouble("biomes." + biomeName + ".veinTypes." + veinName + ".chance");
                    int length = config.getInt("biomes." + biomeName + ".veinTypes." + veinName + ".length");
                    int stroke = config.getInt("biomes." + biomeName + ".veinTypes." + veinName + ".stroke");
                    veinTypes.add(new BiomeDefinition.OreVein(new MaterialData(block, data), chance, length, stroke));
                }
            }
            int schematicCount = config.getInt("biomes." + biomeName + ".schematicCount");
            List<BiomeDefinition.Schematic> schematics = new ArrayList<>();
            if (config.get("biomes." + biomeName + ".schematics") != null) {
                for (String schematicName : config.getConfigurationSection("biomes." + biomeName + ".schematics").getKeys(false)) {
                    String schematic = config.getString("biomes." + biomeName + ".schematics." + schematicName + ".fileName");
                    double chance = config.getDouble("biomes." + biomeName + ".schematics." + schematicName + ".chance");
                    schematics.add(new BiomeDefinition.Schematic(schematic, getSchematic(schematicName), chance));
                }
            }
            BiomeDefinition biomeDefinition =
                    new BiomeDefinition(
                            biomeName,
                            r, g, b,
                            biome,
                            snowfall,
                            blocks,
                            floraCount, floraTypes,
                            treeCount, treeTypes,
                            veinCount, veinTypes,
                            schematicCount, schematics);
            biomes.add(biomeDefinition);
            plugin.getLogger().info("Loaded Biome: " + biomeDefinition.getName());
        }
        return biomes;
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
     * returns the file name of the height map
     *
     * @return the file name of the height map
     */
    public String getHeightMapName() {
        return plugin.getConfig().getString("heightMap");
    }

    /**
     * returns the file name of the biome map
     *
     * @return the file name of the biome map
     */
    public String getBiomeMapName() {
        return plugin.getConfig().getString("biomeMap");
    }

    /**
     * returns the file name of the biome map
     *
     * @return the file name of the biome map
     */
    public int getWaterHeight() {
        return plugin.getConfig().getInt("waterHeight");
    }
}
