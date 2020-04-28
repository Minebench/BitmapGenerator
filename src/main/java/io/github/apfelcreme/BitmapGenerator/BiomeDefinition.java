package io.github.apfelcreme.BitmapGenerator;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import org.bukkit.TreeType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.List;
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
public class BiomeDefinition {

    private String name;
    private int rgb;
    private Biome biome;
    private int surfaceLayerHeight;
    private boolean snowfall;
    private List<BlockChance> blocks;
    private double floraChance;
    private List<BlockChance> floraTypes;
    private double treeChance;
    private List<TreeData> treeTypes;
    private double veinChance;
    private List<OreVein> veinTypes;
    private double schematicChance;
    private List<Schematic> schematics;


    public BiomeDefinition(String name, int r, int g, int b, Biome biome, int surfaceLayerHeight, boolean snowfall,
                           List<BlockChance> blocks, double floraChance, List<BlockChance> floraTypes, double treeChance,
                           List<TreeData> treeTypes, double veinChance, List<OreVein> veinTypes, double schematicChance,
                           List<Schematic> schematics) {
        this.name = name;
        this.rgb = (255 << 24)
                | (r << 16)
                | (g << 8)
                | (b);
        this.biome = biome;
        this.surfaceLayerHeight = surfaceLayerHeight;
        this.snowfall = snowfall;
        this.blocks = blocks;
        this.floraChance = floraChance;
        this.floraTypes = floraTypes;
        this.treeChance = treeChance;
        this.treeTypes = treeTypes;
        this.veinChance = veinChance;
        this.veinTypes = veinTypes;
        this.schematicChance = schematicChance;
        this.schematics = schematics;
    }


    /**
     * returns the name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * red
     *
     * @return red
     */
    public int getR() {
        return (rgb >> 16) & 0x000000FF;
    }

    /**
     * green
     *
     * @return green
     */
    public int getG() {
        return (rgb >> 8) & 0x000000FF;
    }

    /**
     * green
     *
     * @return green
     */
    public int getB() {
        return rgb & 0x000000FF;
    }

    /**
     * Get the RGB in hexadecimal form
     *
     * @return The RGB in hexadecimal form
     */
    public int getRGB() {
        return rgb;
    }

    /**
     * the biome object
     *
     * @return the biome object
     */
    public Biome getBiome() {
        return biome;
    }

    /**
     * returns the height of the block layer on top of the stone layer
     *
     * @return the height of the block layer on top of the stone layer
     */
    public int getSurfaceLayerHeight() {
        return surfaceLayerHeight;
    }

    /**
     * should snow be placed on top of the terrain?
     *
     * @return true if so, false otherwise
     */
    public boolean isSnowfall() {
        return snowfall;
    }

    /**
     * returns the amount of flowers/grass pasted per chunk
     *
     * @return the amount of flowers/grass pasted per chunk
     */
    public double getFloraChance() {
        return floraChance;
    }

    /**
     * returns the amount of schematics pasted per chunk
     *
     * @return the amount of schematics pasted per chunk
     */
    public double getSchematicChance() {
        return schematicChance;
    }

    /**
     * returns the amount of trees pasted per chunk
     *
     * @return the amount of trees pasted per chunk
     */
    public double getTreeChance() {
        return treeChance;
    }


    /**
     * returns the amount of veins generated per chunk
     *
     * @return the amount of veins generated per chunk
     */
    public double getVeinChance() {
        return veinChance;
    }

    /**
     * checks if a given block ore is one of the ground blocks defined in the config
     *
     * @param block a block found (e.g. when populating flora)
     * @return true, if the block found is one of the defined blocks,
     * false if not (random block when populating is for example leaves from a generated tree. you dont want to put grass on that)
     */
    public boolean isGroundBlock(Block block) {
        boolean isGround = false;
        for (BlockChance blockChance : blocks) {
            if (block.getBlockData().matches(blockChance.blockData)) {
                isGround = true;
            }
        }
        return isGround;
    }

    /**
     * returns a random blockData matching the quota
     *
     * @return a random blockData
     */
    public BlockData nextBlock() {
        int totalSum = 0;
        Random random = new Random();
        for (BlockChance blockChance : blocks) {
            totalSum += blockChance.chance * 100;
        }
        int index = random.nextInt(totalSum);
        int sum = 0;
        int i = 0;
        while (sum < index) {
            sum = sum + (int) (blocks.get(i++).chance * 100);
        }
        return blocks.get(Math.max(0, i - 1)).blockData;
    }

    /**
     * returns a random flora matching the quota
     *
     * @return a random flora
     */
    public BlockData nextFloraData() {
        int totalSum = 0;
        Random random = new Random();
        for (BlockChance flora : floraTypes) {
            totalSum += flora.chance * 100;
        }
        int index = random.nextInt(totalSum);
        int sum = 0;
        int i = 0;
        while (sum < index) {
            sum = sum + (int) (floraTypes.get(i++).chance * 100);
        }
        return floraTypes.get(Math.max(0, i - 1)).blockData;
    }

    /**
     * returns a random tree matching the quota
     *
     * @return a random tree
     */
    public TreeData nextTree() {
        int totalSum = 0;
        Random random = new Random();
        for (TreeData treeType : treeTypes) {
            totalSum += treeType.chance * 100;
        }
        int index = random.nextInt(totalSum);
        int sum = 0;
        int i = 0;
        while (sum < index) {
            sum = sum + (int) (treeTypes.get(i++).chance * 100);
        }
        return treeTypes.get(Math.max(0, i - 1));
    }

    /**
     * returns a random ore matching the quota
     *
     * @return a random ore
     */
    public OreVein nextVein() {
        int totalSum = 0;
        Random random = new Random();
        for (OreVein veinType : veinTypes) {
            totalSum += veinType.chance * 100;
        }
        int index = random.nextInt(totalSum);
        int sum = 0;
        int i = 0;
        while (sum < index) {
            sum = sum + (int) (veinTypes.get(i++).chance * 100);
        }
        return veinTypes.get(Math.max(0, i - 1));
    }

    /**
     * returns a random schematic matching the quota
     *
     * @return a random schematic
     */
    public Schematic nextSchematic() {
        int totalSum = 0;
        Random random = new Random();
        for (Schematic schematic : schematics) {
            totalSum += schematic.chance * 100;
        }
        int index = random.nextInt(totalSum);
        int sum = 0;
        int i = 0;
        while (sum < index) {
            sum = sum + (int) (schematics.get(i++).chance * 100);
        }
        return schematics.get(Math.max(0, i - 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BiomeDefinition that = (BiomeDefinition) o;

        return !(name != null ? !name.equals(that.name) : that.name != null);
    }

    /**
     * a class to represent a block type
     */
    public static class BlockChance {
        private BlockData blockData;
        private double chance;

        public BlockChance(BlockData blockData, double chance) {
            this.blockData = blockData;
            this.chance = chance;
        }

        @Override
        public String toString() {
            return blockData.getAsString() + "*" + chance;
        }
    }

    /**
     * a class to represent a tree
     */
    public static class TreeData {
        private TreeType type;
        private double chance;

        public TreeData(TreeType type, double chance) {
            this.type = type;
            this.chance = chance;
        }

        public TreeType getType() {
            return type;
        }

        @Override
        public String toString() {
            return type + "*" + chance;
        }
    }

    /**
     * a class to represent an ore vein
     */
    public static class OreVein {
        private BlockData ore;
        private double chance;
        private int length;
        private int stroke;
        private int maxHeight;

        public OreVein(BlockData ore, double chance, int length, int stroke, int maxHeight) {
            this.ore = ore;
            this.chance = chance;
            this.length = length;
            this.stroke = stroke;
            this.maxHeight = maxHeight;
        }

        public BlockData getOre() {
            return ore;
        }

        public int getLength() {
            return length;
        }

        public int getStroke() {
            return stroke;
        }

        public int getMaxHeight() {
            return maxHeight;
        }

        @Override
        public String toString() {
            return ore.getAsString() + "*" + chance + "<" + maxHeight;
        }
    }

    /**
     * a class to represent a schematic and its probability to be pasted
     */
    public static class Schematic {
        private String name;
        private Clipboard clipboard;
        private int yOffset;
        private double chance;

        public Schematic(String name, Clipboard clipboard, int yOffset, double chance) {
            this.name = name;
            this.clipboard = clipboard;
            this.yOffset = yOffset;
            this.chance = chance;
        }

        public Clipboard getClipboard() {
            return clipboard;
        }

        public int getYOffset() {
            return yOffset;
        }

        @Override
        public String toString() {
            return name + "*" + chance;
        }
    }


}
