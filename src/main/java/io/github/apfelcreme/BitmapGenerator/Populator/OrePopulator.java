package io.github.apfelcreme.BitmapGenerator.Populator;

import io.github.apfelcreme.BitmapGenerator.BiomeDefinition;
import io.github.apfelcreme.BitmapGenerator.WorldConfiguration;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import java.awt.image.BufferedImage;
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

    private WorldConfiguration worldConfiguration;

    public OrePopulator(WorldConfiguration worldConfiguration) {
        this.worldConfiguration = worldConfiguration;
    }


    @Override
    public synchronized void populate(World world, Random random, Chunk chunk) {
        for (BiomeDefinition biomeDefinition : worldConfiguration.getDistinctChunkBiomes(chunk)) {
            double veinCount;
            if (biomeDefinition.getVeinChance() < 1) {
                veinCount = Math.random() <= biomeDefinition.getVeinChance() ? 1 : 0;
            } else {
                veinCount = (int) biomeDefinition.getVeinChance();
            }
            for (int i = 0; i < veinCount; i++) {
                BiomeDefinition.OreVein vein = biomeDefinition.nextVein();
                int startX = random.nextInt(10);
                int startY = 5 + random.nextInt((random.nextBoolean() ? vein.getMaxHeight() : Math.min(worldConfiguration.getWaterHeight(), vein.getMaxHeight())) - 5);
                int startZ = random.nextInt(10);
                double alpha = Math.toRadians(random.nextInt(90));
                double beta = Math.toRadians(random.nextInt(90));

                int endX = (int) (startX + (vein.getLength() * Math.cos(alpha)));
                int endY = (int) (startY + (vein.getLength() * Math.sin(alpha)));
                int endZ = (int) (startZ + (vein.getLength() * Math.cos(beta)));
                List<Point3D> path = bresenham(new Point3D(startX, startY, startZ), new Point3D(endX, endY, endZ));
                for (Point3D point : path) {
                    for (int sX = 0; sX < vein.getStroke(); sX++) {
                        for (int sY = 0; sY < vein.getStroke() && point.y + sY < vein.getMaxHeight(); sY++) {
                            for (int sZ = 0; sZ < vein.getStroke(); sZ++) {
                                if (chunk.getBlock(point.x + sX, point.y + sY, point.z + sZ).getType() == Material.STONE) {
                                    chunk.getBlock(point.x + sX, point.y + sY, point.z + sZ).setBlockData(vein.getOre(), false);
                                }
                            }
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

}
