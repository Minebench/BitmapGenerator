package io.github.apfelcreme.BitmapGenerator;

import com.sk89q.worldedit.CuboidClipboard;
import org.bukkit.TreeType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;

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
    private int r;
    private int g;
    private int b;
    private Biome biome;
    private boolean snowfall;
    private List<BlockData> blocks;
    private double floraChance;
    private List<BlockData> floraTypes;
    private double treeChance;
    private List<TreeData> treeTypes;
    private double veinChance;
    private List<OreVein> veinTypes;
    private double schematicChance;
    private List<Schematic> schematics;


    public BiomeDefinition(String name, int r, int g, int b, Biome biome, boolean snowfall, List<BlockData> blocks, int floraChance, List<BlockData> floraTypes, int treeChance,
                           List<TreeData> treeTypes, int veinChance, List<OreVein> veinTypes, int schematicChance, List<Schematic> schematics) {
        this.name = name;
        this.r = r;
        this.g = g;
        this.b = b;
        this.biome = biome;
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
        return r;
    }

    /**
     * green
     *
     * @return green
     */
    public int getG() {
        return g;
    }

    /**
     * green
     *
     * @return green
     */
    public int getB() {
        return b;
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
        for (BlockData blockData : blocks) {
            if (blockData.materialData.getItemType() == block.getType()
                    && blockData.materialData.getData() == block.getData()) {
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
    public MaterialData nextBlock() {
        int totalSum = 0;
        Random random = new Random();
        for (BlockData blockData : blocks) {
            totalSum += blockData.chance * 100;
        }
        int index = random.nextInt(totalSum);
        int sum = 0;
        int i = 0;
        while (sum < index) {
            sum = sum + (int) (blocks.get(i++).chance * 100);
        }
        return blocks.get(Math.max(0, i - 1)).materialData;
    }

    /**
     * returns a random flora matching the quota
     *
     * @return a random flora
     */
    public MaterialData nextFloraData() {
        int totalSum = 0;
        Random random = new Random();
        for (BlockData flora : floraTypes) {
            totalSum += flora.chance * 100;
        }
        int index = random.nextInt(totalSum);
        int sum = 0;
        int i = 0;
        while (sum < index) {
            sum = sum + (int) (floraTypes.get(i++).chance * 100);
        }
        return floraTypes.get(Math.max(0, i - 1)).materialData;
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
        Schematic schematic = schematics.get(Math.max(0, i - 1));
        schematic.getClipboard().rotate2D(random.nextInt(4) * 90);
        return schematic;
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
    public static class BlockData {
        private MaterialData materialData;
        private double chance;

        public BlockData(MaterialData materialData, double chance) {
            this.materialData = materialData;
            this.chance = chance;
        }

        @Override
        public String toString() {
            return materialData.getItemType().name() + ":" + materialData.getData() + "*" + chance;
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
        private MaterialData ore;
        private double chance;
        private int length;
        private int stroke;

        public OreVein(MaterialData ore, double chance, int length, int stroke) {
            this.ore = ore;
            this.chance = chance;
            this.length = length;
            this.stroke = stroke;
        }

        public MaterialData getOre() {
            return ore;
        }

        public int getLength() {
            return length;
        }

        public int getStroke() {
            return stroke;
        }

        @Override
        public String toString() {
            return ore.getItemType().name() + ":" + ore.getData() + "*" + chance;
        }
    }

    /**
     * a class to represent a schematic and its probability to be pasted
     */
    public static class Schematic {
        private String name;
        private CuboidClipboard clipboard;
        private int yOffset;
        private double chance;

        public Schematic(String name, CuboidClipboard clipboard, int yOffset, double chance) {
            this.name = name;
            this.clipboard = clipboard;
            this.yOffset = yOffset;
            this.chance = chance;
        }

        public CuboidClipboard getClipboard() {
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
