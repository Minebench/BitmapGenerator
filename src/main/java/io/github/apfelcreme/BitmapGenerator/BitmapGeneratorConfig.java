package io.github.apfelcreme.BitmapGenerator;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import java.io.File;
import java.util.regex.Pattern;

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
     * returns the material type assigned to a color in config
     *
     * @param r red
     * @param g green
     * @param b blue
     * @return the block assigned to that color
     */
    public MaterialData getBlock(int r, int g, int b) {
        MaterialData data = null;
        String s = plugin.getConfig().getString("surface." + r + "," + g + "," + b);
        if (s.contains(Pattern.quote(":"))) {
            Material material = Material.getMaterial(Integer.valueOf(s.split(Pattern.quote(":"))[0]));
            if (material != null) {
                byte blockData = Byte.valueOf(s.split(Pattern.quote(":"))[1]);
                data = new MaterialData(material, blockData);
            }
        } else {
            Material material = Material.getMaterial(Integer.valueOf(s));
            if (material != null) {
                data = new MaterialData(material);
            }
        }
        return data != null ? data : new MaterialData(Material.WATER);
    }

    public String getHeightmapName() {
        return plugin.getConfig().getString("heightMap");
    }

    public String getBlockmapName() {
        return plugin.getConfig().getString("blockMap");
    }
}
