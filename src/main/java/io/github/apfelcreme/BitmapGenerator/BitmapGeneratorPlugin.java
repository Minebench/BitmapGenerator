package io.github.apfelcreme.BitmapGenerator;

import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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
public class BitmapGeneratorPlugin extends JavaPlugin {

    private Map<String, WorldConfiguration> worldConfigurations;

    public void onEnable() {
        worldConfigurations = new HashMap<>();
    }

    /**
     * loads the correct World Configuration for the given world
     *
     * @param worldName the world name
     * @param id        the id (generator settings?)
     * @return a chunk generator for the world creation process
     */
    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        if (worldName.equals("test") || worldName.equals("world")) {
            getLogger().info("Multiverse created a map named '"+worldName+"' as it always does! Redirecting to a pseudo chunk generator! Ignore this...");
            return new PseudoWorldGenerator();
        } else {
            WorldConfiguration worldConfiguration = worldConfigurations.get(worldName);
            if (worldConfiguration == null) {
                worldConfiguration = WorldConfiguration.newInstance(this, worldName);
                if (worldConfiguration != null) {
                    worldConfigurations.put(worldName, worldConfiguration);
                } else {
                    getLogger().severe("Something is missing! Ignore the MultiVerse-Error, we failed on purpose to prevent a corrupted world from being generated!");
                    getLogger().severe("You may have to delete the old world first, though. '/mvdelete " + worldName + "' should do.");
                    return null;
                }
            }
            return new BitmapWorldGenerator(worldConfiguration);
        }
    }

    /**
     * copies a file from the jar file to the plugin data path
     *
     * @param fileName  the name of the resource file
     * @param worldName the name of the world the file belongs to
     */
    public synchronized void saveResource(String fileName, String worldName) throws IOException {
        File out = new File(getDataFolder() + "/" + worldName + "/" + fileName);
        if (!out.exists()) {
            out.getParentFile().mkdirs();
            out.createNewFile();
            InputStream in = getClass().getResourceAsStream("/" + fileName);
            FileOutputStream fOut = new FileOutputStream(out);
            getLogger().info("Extracting '" + fileName + "' to '" + out.getAbsolutePath() + "' (" + in.available() + " byte)");
            int i;
            while ((i = in.read()) != -1) {
                fOut.write(i);
            }
            fOut.close();
            in.close();
        }
    }
}
