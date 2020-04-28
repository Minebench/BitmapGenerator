package io.github.apfelcreme.BitmapGenerator.Populator;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import io.github.apfelcreme.BitmapGenerator.BiomeDefinition;
import io.github.apfelcreme.BitmapGenerator.Util;
import io.github.apfelcreme.BitmapGenerator.WorldConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

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
public class SchematicPopulator extends BlockPopulator {

    private WorldConfiguration worldConfiguration;

    public SchematicPopulator(WorldConfiguration worldConfiguration) {
        this.worldConfiguration = worldConfiguration;
    }

    @Override
    public synchronized void populate(World world, Random random, Chunk chunk) {
        for (BiomeDefinition biomeDefinition : worldConfiguration.getDistinctChunkBiomes(chunk)) {
            double schematicCount;
            if (biomeDefinition.getSchematicChance() < 1) {
                schematicCount = Math.random() <= biomeDefinition.getSchematicChance() ? 1 : 0;
            } else {
                schematicCount = (int) biomeDefinition.getSchematicChance();
            }
            for (int i = 0; i < schematicCount; i++) {

                int schematicX = (chunk.getX() << 4) + random.nextInt(16);
                int schematicZ = (chunk.getZ() << 4) + random.nextInt(16);
                int schematicY = Util.getHighestBlock(world, schematicX, schematicZ) + 1;

                if (worldConfiguration.getBiomeDefinition(schematicX, schematicZ).equals(biomeDefinition)) {
                    if (biomeDefinition.isGroundBlock(world.getBlockAt(schematicX, schematicY - 1, schematicZ))) {
                        BiomeDefinition.Schematic schematic = biomeDefinition.nextSchematic();

                        // initialize the values needed to rotate the schematic
                        int rotation = random.nextInt(4);
                        // Whether or not the schematic points into north or south direction
                        boolean northSouth = rotation % 2 == 0;
                        int xMod;
                        int zMod;
                        int xStart = schematic.getClipboard().getMinimumPoint().getBlockX();
                        int yStart = schematic.getClipboard().getMinimumPoint().getBlockY();
                        int zStart = schematic.getClipboard().getMinimumPoint().getBlockZ();
                        if (rotation < 2) {
                            xMod = 1;
                        } else {
                            xMod = -1;
                            xStart += schematic.getClipboard().getDimensions().getBlockX() - 1;
                        }
                        if (rotation > 0 && rotation < 3) {
                            zMod = -1;
                            zStart += schematic.getClipboard().getDimensions().getBlockZ() - 1;
                        } else {
                            zMod = 1;
                        }

                        int schematicWidth = northSouth
                                ? schematic.getClipboard().getDimensions().getBlockX()
                                : schematic.getClipboard().getDimensions().getBlockZ();
                        int schematicHeight = schematic.getClipboard().getDimensions().getBlockY();
                        int schematicLength = northSouth
                                ? schematic.getClipboard().getDimensions().getBlockZ()
                                : schematic.getClipboard().getDimensions().getBlockX();
                        // Try putting schematic on floor
                        int schematicOffset = schematic.getYOffset();
                        boolean foundSolid = false;
                        for (int testedY = 0; testedY < schematicHeight && !foundSolid; testedY++) {
                            for (int x = 0; x < schematicWidth; x++) {
                                for (int z = 0; z < schematicLength; z++) {
                                    // Create the rotated vector
                                    BlockVector3 rotatedVector = BlockVector3.at(xStart + xMod * (northSouth ? x : z), yStart + testedY, zStart + zMod * (northSouth ? z : x));
                                    BlockState b = schematic.getClipboard().getBlock(rotatedVector);
                                    if (b != null && !b.getBlockType().getMaterial().isAir()) {
                                        for (int offset = schematicOffset; yStart + offset > 0; offset--) {
                                            Block block = world.getBlockAt(
                                                    schematicX + x - (schematicWidth / 2),
                                                    schematicY + offset - 1,
                                                    schematicZ + z - (schematicLength / 2));
                                            if (block.getType().isOccluding()) {
                                                schematicOffset = offset;
                                                foundSolid = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        try {
                            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(world), Integer.MAX_VALUE);
                            LocalSession session = new LocalSession();
                            session.setClipboard(new ClipboardHolder(schematic.getClipboard()));
                            session.getClipboard().setTransform(new AffineTransform().rotateY(rotation * 90));
                            Operation operation = session.getClipboard()
                                    .createPaste(editSession)
                                    .ignoreAirBlocks(true)
                                    .to(BlockVector3.at(schematicX - schematicWidth / 2, schematicY + schematicOffset, schematicZ - schematicLength / 2))
                                    .build();
                            Operations.complete(operation);
                            editSession.flushSession();
                        } catch (WorldEditException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
