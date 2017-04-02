package io.github.apfelcreme.BitmapGenerator.Populator;

import io.github.apfelcreme.BitmapGenerator.BitmapGenerator;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.BlockPopulator;

import java.util.ArrayList;
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
public class OrePopulator extends BlockPopulator {

    private BitmapGenerator plugin;
    private PopulationSettings populationSettings;

    public OrePopulator(BitmapGenerator plugin) {
        this.plugin = plugin;
        this.populationSettings = new PopulationSettings();
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {

        for (int i = 0; i < plugin.getConfig().getInt("population.oreVeinsPerChunk", 6); i++) {
            PopulationSettings.Vein vein = populationSettings.getRandomOre();

            int startX = random.nextInt(10);
            int startY = 5 + random.nextInt(40);
            int startZ = random.nextInt(10);
            double alpha = Math.toRadians(random.nextInt(90));
            double beta = Math.toRadians(random.nextInt(90));

            int endX = (int) (startX + (vein.length * Math.cos(alpha)));
            int endY = (int) (startY + (vein.length * Math.sin(alpha)));
            int endZ = (int) (startZ + (vein.length * Math.cos(beta)));

            List<Point3D> path = bresenham(new Point3D(startX, startY, startZ), new Point3D(endX, endY, endZ));
            for (Point3D point : path) {
                for (int sX = 0; sX < vein.stroke; sX++) {
                    for (int sY = 0; sY < vein.stroke; sY++) {
                        for (int sZ = 0; sZ < vein.stroke; sZ++) {
                            chunk.getBlock(point.x + sX, point.y + sY, point.z + sZ).setType(vein.material);
                        }
                    }
                }
            }
        }
    }

    /**
     * returns a list of all points a line intersects
     *
     * @param start the start point
     * @param end   the end point
     * @return a list of all points a line intersects
     */
    public List<Point3D> bresenham(Point3D start, Point3D end) {
        List<Point3D> result = new ArrayList<>();

        int dx = end.x - start.x;
        int dy = end.y - start.y;
        int dz = end.z - start.z;

        int ax = Math.abs(dx) << 1;
        int ay = Math.abs(dy) << 1;
        int az = Math.abs(dz) << 1;

        int signx = (int) Math.signum(dx);
        int signy = (int) Math.signum(dy);
        int signz = (int) Math.signum(dz);

        int x = start.x;
        int y = start.y;
        int z = start.z;

        int deltax, deltay, deltaz;
        if (ax >= Math.max(ay, az)) /* x dominant */ {
            deltay = ay - (ax >> 1);
            deltaz = az - (ax >> 1);
            while (true) {
                result.add(new Point3D(x, y, z));
                if (x == end.x) {
                    return result;
                }

                if (deltay >= 0) {
                    y += signy;
                    deltay -= ax;
                }

                if (deltaz >= 0) {
                    z += signz;
                    deltaz -= ax;
                }

                x += signx;
                deltay += ay;
                deltaz += az;
            }
        } else if (ay >= Math.max(ax, az)) /* y dominant */ {
            deltax = ax - (ay >> 1);
            deltaz = az - (ay >> 1);
            while (true) {
                result.add(new Point3D(x, y, z));
                if (y == end.y) {
                    return result;
                }

                if (deltax >= 0) {
                    x += signx;
                    deltax -= ay;
                }

                if (deltaz >= 0) {
                    z += signz;
                    deltaz -= ay;
                }

                y += signy;
                deltax += ax;
                deltaz += az;
            }
        } else if (az >= Math.max(ax, ay)) /* z dominant */ {
            deltax = ax - (az >> 1);
            deltay = ay - (az >> 1);
            while (true) {
                result.add(new Point3D(x, y, z));
                if (z == end.z) {
                    return result;
                }

                if (deltax >= 0) {
                    x += signx;
                    deltax -= az;
                }

                if (deltay >= 0) {
                    y += signy;
                    deltay -= az;
                }

                z += signz;
                deltax += ax;
                deltay += ay;
            }
        }
        return result;
    }

    /**
     * a simple class to represent a point in a room
     */
    public class Point3D {

        private int x;
        private int y;
        private int z;

        public Point3D(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public class PopulationSettings {

        private List<Vein> availableVeins;

        public PopulationSettings() {
            availableVeins = new ArrayList<>();
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("population.ore");
            for (String oreName : section.getKeys(false)) {
                Material material = Material.getMaterial(oreName);
                if (material != null) {
                    availableVeins.add(new Vein(material,
                            section.getDouble(material.name() + ".chance"),
                            section.getInt(material.name() + ".length"),
                            section.getInt(material.name() + ".stroke")
                    ));
                }
            }
        }

        /**
         * returns a random ore
         * @return a random ore
         */
        public Vein getRandomOre() {
            int totalSum = 0;
            Random random = new Random();
            for (Vein vein : availableVeins) {
                totalSum += vein.chance * 100;
            }
            int index = random.nextInt(totalSum);
            int sum = 0;
            int i = 0;
            while (sum < index) {
                sum = sum + (int) (availableVeins.get(i++).chance * 100);
            }
            return availableVeins.get(Math.max(0, i - 1));
        }

        public class Vein {
            private Material material;
            private double chance;
            private int length;
            private int stroke;

            public Vein(Material material, double chance, int length, int stroke) {
                this.material = material;
                this.chance = chance;
                this.length = length;
                this.stroke = stroke;
            }
        }

    }

}
