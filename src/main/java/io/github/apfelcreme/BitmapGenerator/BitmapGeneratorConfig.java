package io.github.apfelcreme.BitmapGenerator;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.schematic.SchematicFormat;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.material.MaterialData;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

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
    private List<File> biomeFiles;

    public BitmapGeneratorConfig(BitmapGenerator plugin) {
        this.plugin = plugin;
        this.biomeFiles = new ArrayList<>();
        plugin.saveResource("config.yml", false);
        if (!new File(plugin.getDataFolder() + "/schematics").exists()) {
            new File(plugin.getDataFolder() + "/schematics").mkdirs();
        }
        if (!new File(plugin.getDataFolder() + "/biomes").exists()) {
            new File(plugin.getDataFolder() + "/biomes").mkdirs();
        }


        // copy biome files
        try {
            List<String> biomeFileNames = getBiomeFiles();
            for (String biomeFileName : biomeFileNames) {
                plugin.saveResource(biomeFileName, false);
                biomeFiles.add(new File(plugin.getDataFolder() + "/" + biomeFileName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * returns a list of all biome yaml files in the resource folder
     * @return a list of all biome yaml files in the resource folder
     * @throws IOException
     */
    private List<String> getBiomeFiles() throws IOException {
        List<String> biomeFiles = new ArrayList<>();
            CodeSource src = BitmapGeneratorConfig.class.getProtectionDomain().getCodeSource();
            if (src != null) {
                URL jar = src.getLocation();
                ZipInputStream zip = new ZipInputStream(jar.openStream());
                while (true) {
                    ZipEntry e = zip.getNextEntry();
                    if (e == null)
                        break;
                    String name = e.getName();
                    if (name.startsWith("biomes/") && !name.equals("biomes/")) {
                        biomeFiles.add(name);
                    }
                }
            }
        return biomeFiles;
    }

    /**
     * returns a list of all defined biomes
     *
     * @return a list of all biomes
     */
    public List<BiomeDefinition> loadBiomes() {
        List<BiomeDefinition> biomes = new ArrayList<>();
        for (File biomeFile : biomeFiles) {
            String biomeName = biomeFile.getName().replace(".yml", "");
            YamlConfiguration biomeConfig = YamlConfiguration.loadConfiguration(biomeFile);
            int r = biomeConfig.getInt("r");
            int g = biomeConfig.getInt("g");
            int b = biomeConfig.getInt("b");
            Biome biome = Biome.valueOf(biomeConfig.getString("biome"));
            boolean snowfall = biomeConfig.getBoolean("snow");
            List<BiomeDefinition.BlockData> blocks = new ArrayList<>();
            if (biomeConfig.get("blocks") != null) {
                for (String materialName : biomeConfig.getConfigurationSection("blocks").getKeys(false)) {
                    Material block = Material.getMaterial(biomeConfig.getInt("blocks." + materialName + ".block"));
                    byte data = (byte) biomeConfig.getInt("blocks." + materialName + ".data");
                    double chance = biomeConfig.getDouble("blocks." + materialName + ".chance");
                    blocks.add(new BiomeDefinition.BlockData(new MaterialData(block, data), chance));
                }
            }
            int floraCount = biomeConfig.getInt("floraChance");
            List<BiomeDefinition.BlockData> floraTypes = new ArrayList<>();
            if (biomeConfig.get("floraTypes") != null) {
                for (String floraName : biomeConfig.getConfigurationSection("floraTypes").getKeys(false)) {
                    Material block = Material.getMaterial(biomeConfig.getInt("floraTypes." + floraName + ".block"));
                    byte data = (byte) biomeConfig.getInt("floraTypes." + floraName + ".data");
                    double chance = biomeConfig.getDouble("floraTypes." + floraName + ".chance");
                    floraTypes.add(new BiomeDefinition.BlockData(new MaterialData(block, data), chance));
                }
            }
            int treeCount = biomeConfig.getInt("treeChance");
            List<BiomeDefinition.TreeData> treeTypes = new ArrayList<>();
            if (biomeConfig.get("treeTypes") != null) {
                for (String treeName : biomeConfig.getConfigurationSection("treeTypes").getKeys(false)) {
                    String type = biomeConfig.getString("treeTypes." + treeName + ".type");
                    TreeType treeType = TreeType.valueOf(type);
                    double chance = biomeConfig.getDouble("treeTypes." + treeName + ".chance");
                    treeTypes.add(new BiomeDefinition.TreeData(treeType, chance));
                }
            }
            int veinCount = biomeConfig.getInt("veinChance");
            List<BiomeDefinition.OreVein> veinTypes = new ArrayList<>();
            if (biomeConfig.get("veinTypes") != null) {
                for (String veinName : biomeConfig.getConfigurationSection("veinTypes").getKeys(false)) {
                    Material block = Material.getMaterial(biomeConfig.getInt("veinTypes." + veinName + ".block"));
                    byte data = (byte) biomeConfig.getInt("veinTypes." + veinName + ".data");
                    double chance = biomeConfig.getDouble("veinTypes." + veinName + ".chance");
                    int length = biomeConfig.getInt("veinTypes." + veinName + ".length");
                    int stroke = biomeConfig.getInt("veinTypes." + veinName + ".stroke");
                    veinTypes.add(new BiomeDefinition.OreVein(new MaterialData(block, data), chance, length, stroke));
                }
            }
            int schematicCount = biomeConfig.getInt("schematicChance");
            List<BiomeDefinition.Schematic> schematics = new ArrayList<>();
            if (biomeConfig.get("schematics") != null) {
                for (String schematicName : biomeConfig.getConfigurationSection("schematics").getKeys(false)) {
                    String schematic = biomeConfig.getString("schematics." + schematicName + ".fileName");
                    int negativeOffset = biomeConfig.getInt("schematics." + schematicName + ".negativeOffset");
                    double chance = biomeConfig.getDouble("schematics." + schematicName + ".chance");
                    schematics.add(new BiomeDefinition.Schematic(schematic, getSchematic(schematicName), negativeOffset, chance));
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
            plugin.getLogger().info(" Loaded Biome: " + biomeDefinition.getName());
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
