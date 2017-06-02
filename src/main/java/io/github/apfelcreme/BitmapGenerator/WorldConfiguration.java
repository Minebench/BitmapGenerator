package io.github.apfelcreme.BitmapGenerator;

import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.util.io.Closer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.material.MaterialData;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
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
public class WorldConfiguration {

    private BitmapGeneratorPlugin plugin;
    private String worldName;
    private File worldFolder;
    private List<File> biomeFiles;
    private Map<Integer, BiomeDefinition> biomes;
    private String prefix;

    private YamlConfiguration worldConfig;
    private BufferedImage biomeMap = null;
    private BufferedImage heightMap = null;
    private BufferedImage riverMap = null;

    private double caveRadius;
    private double noise;
    private double snowNoise;

    private Perlin noiseHeight;
    private Perlin noiseMap;
    private Perlin snowHeight;
    private int waterHeight;
    private int riverDepth;
    private World world;

    /**
     * creates a new instance if all is fine, or null if an error occurs during the loading process
     *
     * @param plugin     the plugin instance
     * @param worldName  the world name
     * @param caveSeed   the cave seed
     * @param heightSeed the cave height seed
     * @param snowSeed   the snow height seed
     * @return a new instance or null
     */
    public static WorldConfiguration newInstance(BitmapGeneratorPlugin plugin, String worldName, long caveSeed, long heightSeed, long snowSeed) {
        // create a new WorldConfiguration object
        //  - create folder structure & extract all files
        WorldConfiguration worldConfiguration = new WorldConfiguration(plugin, worldName, caveSeed, heightSeed, snowSeed);

        // Load the biomes
        //  - load the image files (biomemap.png and heightmap.png)
        //  - check their size
        //  - load all biome definitions for this world from their .yml files in /BitmapGenerator/<world>/biomes/
        //  - check all pixels on the map and try to assign the color to one of the loaded biome definitions
        boolean biomesCorrectlyLoaded = worldConfiguration.loadBiomes();

        // Everything fine?
        return biomesCorrectlyLoaded ? worldConfiguration : null;
    }

    /**
     * constructor
     *
     * @param plugin     the plugin instance
     * @param worldName  the world name
     * @param caveSeed   the world seed do determine if a block should be set to air to generate a cave
     * @param heightSeed the seed to determine the cave height at a location
     */
    private WorldConfiguration(BitmapGeneratorPlugin plugin, String worldName, long caveSeed, long heightSeed, long snowSeed) {
        this.plugin = plugin;
        this.worldName = worldName;

        this.worldFolder = new File(plugin.getDataFolder(), worldName);
        this.prefix = "[" + worldName + "] ";
        this.biomeFiles = new ArrayList<>();
        try {

            //extract all resources if they dont exist yet (world.yml, all biome.yml-files and all schematics)
            Util.saveResource(plugin, "world.yml", worldName);
            worldConfig = YamlConfiguration.loadConfiguration(new File(worldFolder, "world.yml"));

            // Load the world seeds and initiate the Perlin-Noise for cave generation
            if (worldConfig.get("caveSeed") == null) {
                worldConfig.set("caveSeed", caveSeed);
                worldConfig.save(new File(worldFolder, "world.yml"));
            }
            if (worldConfig.get("heightSeed") == null) {
                worldConfig.set("heightSeed", heightSeed);
                worldConfig.save(new File(worldFolder, "world.yml"));
            }
            if (worldConfig.get("snowSeed") == null) {
                worldConfig.set("snowSeed", snowSeed);
                worldConfig.save(new File(worldFolder, "world.yml"));
            }
            caveSeed = worldConfig.getLong("caveSeed");
            heightSeed = worldConfig.getLong("heightSeed");
            snowSeed = worldConfig.getLong("snowSeed");
            this.caveRadius = worldConfig.getDouble("caveRadius", 3.9d);
            this.noise = worldConfig.getDouble("noise", 48);
            this.snowNoise = worldConfig.getDouble("snowNoise", 24);
            this.waterHeight = worldConfig.getInt("waterHeight");
            this.riverDepth = worldConfig.getInt("riverDepth", 4);
            this.noiseMap = new Perlin(caveSeed);
            this.noiseHeight = new Perlin(heightSeed);
            this.snowHeight = new Perlin(snowSeed);

            plugin.getLogger().info("Cave-Generation: " + caveSeed + ", Cave-Height: " + heightSeed + ", Snow-Height: " + snowSeed);

            // create some directories
            if (!new File(worldFolder, "schematics").exists()) {
                new File(worldFolder, "schematics").mkdirs();
            }
            if (!new File(worldFolder, "biomes").exists()) {
                new File(worldFolder, "biomes").mkdirs();
            }

            // extract the biome files
            for (String biomeFileName : getBiomeResourceFiles()) {
                Util.saveResource(plugin, biomeFileName, worldName);
            }

            // extract the schematic files
            for (String schematicName : getSchematicFiles()) {
                Util.saveResource(plugin, schematicName, worldName);
            }


            File folder = new File(worldFolder, "biomes");
            if (folder.exists() && folder.isDirectory()) {
                Collections.addAll(biomeFiles, folder.listFiles());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * returns a list of all resource files in the resource folder
     *
     * @return a list of all resource files in the resource folder
     * @throws IOException
     */
    private List<String> getBiomeResourceFiles() throws IOException {
        List<String> biomeResourceFiles = new ArrayList<>();
        CodeSource src = WorldConfiguration.class.getProtectionDomain().getCodeSource();
        if (src != null) {
            URL jar = src.getLocation();
            ZipInputStream zip = new ZipInputStream(jar.openStream());
            while (true) {
                ZipEntry e = zip.getNextEntry();
                if (e == null)
                    break;
                String name = e.getName();
                if (name.startsWith("biomes/") && !name.equals("biomes/")) {
                    biomeResourceFiles.add(name);
                }
            }
        }
        return biomeResourceFiles;
    }

    /**
     * returns a list of all schematic files found for this world
     *
     * @return a list of all schematic file names
     * @throws IOException
     */
    private List<String> getSchematicFiles() throws IOException {
        List<String> schematicsFiles = new ArrayList<>();
        CodeSource src = WorldConfiguration.class.getProtectionDomain().getCodeSource();
        if (src != null) {
            URL jar = src.getLocation();
            ZipInputStream zip = new ZipInputStream(jar.openStream());
            while (true) {
                ZipEntry e = zip.getNextEntry();
                if (e == null)
                    break;
                String name = e.getName();
                if (name.startsWith("schematics/") && !name.equals("schematics/")) {
                    schematicsFiles.add(name);
                }
            }
        }
        return schematicsFiles;
    }

    /**
     * loads all defined biomes and checks if the image contains invalid colors, to which no biome definition
     * is available.
     *
     * @return true of correctly loaded, false otherwise
     */
    private boolean loadBiomes() {
        plugin.getLogger().info(prefix + "Loading Biome-Map...");
        try {
            File biomeMapFile = new File(worldFolder, getBiomeMapName());
            if (biomeMapFile.exists()) {
                biomeMap = ImageIO.read(biomeMapFile);
                File heightMapFile = new File(worldFolder, getHeightMapName());
                if (heightMapFile.exists()) {
                    plugin.getLogger().info(prefix + "Loading Height-Map...");
                    heightMap = ImageIO.read(heightMapFile);
                }
                File riverMapFile = new File(worldFolder, getRiverMapName());
                if (riverMapFile.exists()) {
                    plugin.getLogger().info(prefix + "Loading River-Map...");
                    riverMap = ImageIO.read(riverMapFile);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (biomeMap == null) {
            plugin.getLogger().severe(prefix + "BiomeMap was not found! " +
                    "Place a biomeMap-File to /plugins/BitmapGeneratorPlugin/" + worldName + "/" + getBiomeMapName() +
                    "! Then rerun the command");
            return false;
        }
        if (heightMap == null) {
            plugin.getLogger().severe(prefix + "HeightMap was not found! " +
                    "Place a heightMap-File to /plugins/BitmapGeneratorPlugin/" + worldName + "/" + getHeightMapName() +
                    "! Then rerun the command");
            return false;
        }
        if (biomeMap.getWidth() != heightMap.getWidth()) {
            plugin.getLogger().severe(prefix + "BiomeMap width does not equal HeightMap width! " +
                    "Adjust the images, then rerun the command");
            return false;
        }
        if (biomeMap.getHeight() != heightMap.getHeight()) {
            plugin.getLogger().severe(prefix + "BiomeMap height does not equal HeightMap height! " +
                    "Adjust the images, then rerun the command");
            return false;
        }

        // Load biomes from the world.yml config file
        plugin.getLogger().info(prefix + "Loading biomes...");
        biomes = loadBiomeDefinition();
        plugin.getLogger().info(prefix + biomes.size() + " biomes were loaded!");

        // Find all colors used in the biome map file
        plugin.getLogger().info(prefix + "Checking biome validity...");
        Set<Integer> rgbValues = new HashSet<>();
        for (int x = 0; x < biomeMap.getWidth(); x++) {
            for (int y = 0; y < biomeMap.getHeight(); y++) {
                rgbValues.add(biomeMap.getRGB(x, y));
            }
        }

        // try to match all found colors with their biomes
        plugin.getLogger().info(prefix + "Colors found: ");
        boolean valid = true;
        for (Integer rgbValue : rgbValues) {
            Color color = new Color(rgbValue);
            BiomeDefinition foundBiome = biomes.get(color.getRGB());
            if (foundBiome != null) {
                plugin.getLogger().info(prefix + " [" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "] -> " + foundBiome.getName());
            } else {
                plugin.getLogger().info(prefix + " [" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "] could not be assigned to any biome!");
                valid = false;
            }
        }

        if (!valid) {
            plugin.getLogger().info(prefix + "Invalid colors were found in " + getBiomeMapName() + "!");
            return false;
        } else {
            plugin.getLogger().info(prefix + "All colors found could be assigned to a biome!");
            return true;
        }
    }

    /**
     * returns the world name
     *
     * @return the world name
     */
    public String getWorldName() {
        return worldName;
    }

    /**
     * returns a list of all defined biomes
     *
     * @return a list of all biomes
     */
    public Map<Integer, BiomeDefinition> loadBiomeDefinition() {
        Map<Integer, BiomeDefinition> biomes = new HashMap<>();
        for (File biomeFile : biomeFiles) {
            String biomeName = biomeFile.getName().replace(".yml", "");
            YamlConfiguration biomeConfig = YamlConfiguration.loadConfiguration(biomeFile);
            int r = biomeConfig.getInt("r");
            int g = biomeConfig.getInt("g");
            int b = biomeConfig.getInt("b");
            Biome biome = Biome.valueOf(biomeConfig.getString("biome"));
            int surfaceLayerHeight = biomeConfig.getInt("surfaceLayerHeight");
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
            double floraCount = biomeConfig.getDouble("floraChance");
            List<BiomeDefinition.BlockData> floraTypes = new ArrayList<>();
            if (biomeConfig.get("floraTypes") != null) {
                for (String floraName : biomeConfig.getConfigurationSection("floraTypes").getKeys(false)) {
                    Material block = Material.getMaterial(biomeConfig.getInt("floraTypes." + floraName + ".block"));
                    byte data = (byte) biomeConfig.getInt("floraTypes." + floraName + ".data");
                    double chance = biomeConfig.getDouble("floraTypes." + floraName + ".chance");
                    floraTypes.add(new BiomeDefinition.BlockData(new MaterialData(block, data), chance));
                }
            }
            double treeCount = biomeConfig.getDouble("treeChance");
            List<BiomeDefinition.TreeData> treeTypes = new ArrayList<>();
            if (biomeConfig.get("treeTypes") != null) {
                for (String treeName : biomeConfig.getConfigurationSection("treeTypes").getKeys(false)) {
                    String type = biomeConfig.getString("treeTypes." + treeName + ".type");
                    TreeType treeType = TreeType.valueOf(type);
                    double chance = biomeConfig.getDouble("treeTypes." + treeName + ".chance");
                    treeTypes.add(new BiomeDefinition.TreeData(treeType, chance));
                }
            }
            double veinCount = biomeConfig.getDouble("veinChance");
            List<BiomeDefinition.OreVein> veinTypes = new ArrayList<>();
            if (biomeConfig.get("veinTypes") != null) {
                for (String veinName : biomeConfig.getConfigurationSection("veinTypes").getKeys(false)) {
                    Material block = Material.getMaterial(biomeConfig.getInt("veinTypes." + veinName + ".block", worldConfig.getInt("veinTypes." + veinName + ".block")));
                    byte data = (byte) biomeConfig.getInt("veinTypes." + veinName + ".data", worldConfig.getInt("veinTypes." + veinName + ".data"));
                    double chance = biomeConfig.getDouble("veinTypes." + veinName + ".chance", worldConfig.getInt("veinTypes." + veinName + ".chance"));
                    int length = biomeConfig.getInt("veinTypes." + veinName + ".length", worldConfig.getInt("veinTypes." + veinName + ".length"));
                    int stroke = biomeConfig.getInt("veinTypes." + veinName + ".stroke", worldConfig.getInt("veinTypes." + veinName + ".stroke"));
                    int maxHeight = biomeConfig.getInt("veinTypes." + veinName + ".max-height", worldConfig.getInt("veinTypes." + veinName + ".max-height", 255));
                    veinTypes.add(new BiomeDefinition.OreVein(new MaterialData(block, data), chance, length, stroke, maxHeight));
                }
            }
            double schematicCount = biomeConfig.getDouble("schematicChance");
            List<BiomeDefinition.Schematic> schematics = new ArrayList<>();
            if (biomeConfig.get("schematics") != null) {
                for (String schematicName : biomeConfig.getConfigurationSection("schematics").getKeys(false)) {
                    String schematic = biomeConfig.getString("schematics." + schematicName + ".fileName");
                    int yOffset = biomeConfig.getInt("schematics." + schematicName + ".yOffset");
                    double chance = biomeConfig.getDouble("schematics." + schematicName + ".chance");
                    schematics.add(new BiomeDefinition.Schematic(schematic, getSchematic(schematic), yOffset, chance));
                }
            }
            BiomeDefinition biomeDefinition =
                    new BiomeDefinition(
                            biomeName,
                            r, g, b,
                            biome,
                            surfaceLayerHeight,
                            snowfall,
                            blocks,
                            floraCount, floraTypes,
                            treeCount, treeTypes,
                            veinCount, veinTypes,
                            schematicCount, schematics);
            biomes.put(biomeDefinition.getRGB(), biomeDefinition);
        }
        return biomes;
    }

    /**
     * returns a schematic with the given file name
     *
     * @param filename a file name
     * @return the clipboard of the schematic
     */
    public Clipboard getSchematic(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }

        if (!filename.endsWith(".schematic")) {
            filename += ".schematic";
        }

        File file = new File(worldFolder, "schematics/" + filename);
        if (!file.exists()) {
            plugin.getLogger().log(Level.SEVERE, prefix + "No schematic found with the name " + filename + "!");
            return null;
        }
        ClipboardFormat schemFormat = ClipboardFormat.findByFile(file);
        if (schemFormat == null) {
            plugin.getLogger().log(Level.SEVERE, prefix + "Could not load schematic format from file " + file.getAbsolutePath() + "!");
            return null;
        }
        Closer closer = Closer.create();
        try {
            FileInputStream fis = closer.register(new FileInputStream(file));
            BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
            ClipboardReader reader = schemFormat.getReader(bis);
            return reader.read(BukkitUtil.getLocalWorld(world).getWorldData());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, prefix + "Error loading file " + file.getAbsolutePath(), e);
            return null;
        }
    }

    public BufferedImage getBiomeMap() {
        return biomeMap;
    }

    /**
     * returns the file name of the height map
     *
     * @return the file name of the height map
     */
    public String getHeightMapName() {
        return worldConfig.getString("heightMap");
    }

    /**
     * returns the file name of the biome map
     *
     * @return the file name of the biome map
     */
    public String getBiomeMapName() {
        return worldConfig.getString("biomeMap");
    }

    /**
     * returns the file name of the river map
     *
     * @return the file name of the river map
     */
    public String getRiverMapName() {
        return worldConfig.getString("riverMap");
    }

    /**
     * returns the file name of the biome map
     *
     * @return the file name of the biome map
     */
    public int getWaterHeight() {
        return waterHeight;
    }

    /**
     * returns the biome defined by the biomeMap at the given point
     *
     * @param blockX the x position of the block (not the chunk coordinate)
     * @param blockZ the z position of the block (not the chunk coordinate)
     * @return the biome defined by the biomeMap at the given point
     */
    public BiomeDefinition getBiomeDefinition(int blockX, int blockZ) {
        int imageX = addOffset(blockX,  heightMap.getWidth());
        int imageZ = addOffset(blockZ,  heightMap.getHeight());

        return biomes.get(biomeMap.getRGB(imageX, imageZ));
    }

    /**
     * returns the height at a location
     *
     * @param blockX the x coordinate of the block
     * @param blockZ the z coordinate of the block
     * @return the height of the location referenced in the height map
     */
    public int getHeight(int blockX, int blockZ) {
        int imageX = addOffset(blockX,  heightMap.getWidth());
        int imageZ = addOffset(blockZ,  heightMap.getHeight());

        return (heightMap.getRGB(imageX, imageZ) >> 16) & 0x000000FF;
    }

    /**
     * returns the river depth at a location
     *
     * @param blockX the x coordinate of the block
     * @param blockZ the z coordinate of the block
     * @return the river depth of the location referenced in the river depth map
     */
    public int getRiverDepth(int blockX, int blockZ) {
        if (riverMap == null) {
            return 0;
        }
        int imageX = addOffset(blockX,  riverMap.getWidth());
        int imageZ = addOffset(blockZ,  riverMap.getHeight());

        return (int) ((((riverMap.getRGB(imageX, imageZ) >>> 24) & 0x000000FF) / 255.0) * riverDepth);
    }

    private int addOffset(int number, int size) {
        int result = size / 2 + number;
        if (result < 0) {
            result = 0;
        } else if (result >= size) {
            result = size - 1;
        }
        return result;
    }

    /**
     * returns the y coordinate of a cave at a certain coordinate
     *
     * @param blockX the x coordinate
     * @param blockZ the z coordinate
     * @return the y coordinate of a cave at a location
     */
    public int getCaveHeight(int blockX, int blockZ) {
        int offsetX = ((biomeMap.getWidth() / 2));
        int offsetZ = ((biomeMap.getHeight() / 2));

        int imageX = blockX + offsetX;
        int imageZ = blockZ + offsetZ;

        double val = noiseHeight.noise(imageX / noise, imageZ / noise, 0);

        return (0x010101 * (int) ((val + 1) * 40)) & 0x000000FF;
    }

    /**
     * checks if a cave should be generated at the given coordinate
     *
     * @param blockX the x coordinate
     * @param blockZ the z coordinate
     * @return true or false
     */
    public boolean isCave(int blockX, int blockZ) {
        int offsetX = ((biomeMap.getWidth() / 2));
        int offsetZ = ((biomeMap.getHeight() / 2));

        int imageX = blockX + offsetX;
        int imageZ = blockZ + offsetZ;

        double val = noiseMap.noise(imageX / noise, 60 / noise, imageZ / noise);

        return (val > 0 && val < 0.1);
    }

    public byte getSnowHeight(int snowX, int snowZ) {
        int offsetX = ((biomeMap.getWidth() / 2));
        int offsetZ = ((biomeMap.getHeight() / 2));

        int imageX = snowX + offsetX;
        int imageZ = snowZ + offsetZ;

        double val = snowHeight.noise(imageX / snowNoise, imageZ / snowNoise, 0);

        return (byte)((0x010101 * (int) ((val + 1) * 4.5)) & 0x000000FF);
    }

    /**
     * returns the noise value (near 0 -> very noisy, ~30 -> very calm)
     *
     * @return the noise value
     */
    public double getNoise() {
        return noise;
    }

    public double getCaveRadius() {
        return caveRadius;
    }

    /**
     * returns a list of all biome definitions found in the chunk
     *
     * @param chunk the chunk
     * @return a list of biomes
     */
    public List<BiomeDefinition> getDistinctChunkBiomes(Chunk chunk) {
        List<BiomeDefinition> biomeDefinitions = new ArrayList<>();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BiomeDefinition biomeDefinition = getBiomeDefinition((chunk.getX() * 16) + x, (chunk.getZ() * 16) + z);
                if (biomeDefinition != null && !biomeDefinitions.contains(biomeDefinition)) {
                    biomeDefinitions.add(biomeDefinition);
                }
            }
        }
        return biomeDefinitions;
    }
}
