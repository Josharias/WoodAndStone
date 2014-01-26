/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.was.generator;

import org.terasology.anotherWorld.PerlinLandscapeGenerator;
import org.terasology.anotherWorld.PluggableWorldGenerator;
import org.terasology.anotherWorld.coreBiome.AlpineBiome;
import org.terasology.anotherWorld.coreBiome.DesertBiome;
import org.terasology.anotherWorld.coreBiome.ForestBiome;
import org.terasology.anotherWorld.coreBiome.PlainsBiome;
import org.terasology.anotherWorld.coreBiome.TaigaBiome;
import org.terasology.anotherWorld.coreBiome.TundraBiome;
import org.terasology.anotherWorld.decorator.BeachDecorator;
import org.terasology.anotherWorld.decorator.BlockCollectionFilter;
import org.terasology.anotherWorld.decorator.BlockFilter;
import org.terasology.anotherWorld.decorator.CaveDecorator;
import org.terasology.anotherWorld.decorator.layering.DefaultLayersDefinition;
import org.terasology.anotherWorld.decorator.layering.LayeringDecorator;
import org.terasology.anotherWorld.decorator.ore.OreDecorator;
import org.terasology.anotherWorld.util.PDist;
import org.terasology.anotherWorld.util.alpha.IdentityAlphaFunction;
import org.terasology.anotherWorld.util.alpha.MinMaxAlphaFunction;
import org.terasology.anotherWorld.util.alpha.PowerAlphaFunction;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.SimpleUri;
import org.terasology.growingFlora.BlockFloraDefinition;
import org.terasology.growingFlora.FloraDecorator;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.RegisterWorldGenerator;
import org.terasology.world.liquid.LiquidType;

import java.util.Arrays;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterWorldGenerator(id = "overTheAges", displayName = "Over the Ages", description = "Generates the world for playing 'Over the Ages' content mods.")
public class WoodAndStoneWorldGenerator extends PluggableWorldGenerator {
    private BlockManager blockManager;

    public WoodAndStoneWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    protected void setupGenerator() {
        setSeaLevel(50);

        // Make flat land a bit more prevalent than hills or mountains
        setTerrainFunction(
                new PowerAlphaFunction(IdentityAlphaFunction.singleton, 1.5f));
        // Make sure that area on the sea level is not dry, this will prevent deserts spawning next to sea
        setHumidityFunction(
                new MinMaxAlphaFunction(IdentityAlphaFunction.singleton, 0.3f, 1f));
        // Make sure that area on the sea level in not too cold, so that colder areas (with snow) will
        // only exist in higher Y-levels
        setTemperatureFunction(
                new MinMaxAlphaFunction(IdentityAlphaFunction.singleton, 0.4f, 1f));

        blockManager = CoreRegistry.get(BlockManager.class);

        final Block mantle = blockManager.getBlock("Core:MantleStone");
        final Block stone = blockManager.getBlock("Core:Stone");
        final Block water = blockManager.getBlock("Core:Water");
        final Block sand = blockManager.getBlock("Core:Sand");
        final Block dirt = blockManager.getBlock("Core:Dirt");
        final Block grass = blockManager.getBlock("Core:Grass");
        final Block snow = blockManager.getBlock("Core:Snow");
        final Block ice = blockManager.getBlock("Core:Ice");

        // Make the lowlands a bit more common than higher areas (using PowerAlphaFunction)
        setLandscapeGenerator(
                new PerlinLandscapeGenerator(0.3f, mantle, stone, water, LiquidType.WATER,
                        new PowerAlphaFunction(IdentityAlphaFunction.singleton, 1.3f)));

        // Replace stone with sand on the sea shores
        addChunkDecorator(
                new BeachDecorator(new BlockCollectionFilter(stone), sand, 2, 5));

        // Setup biome terrain layers
        setupLayers(stone, sand, dirt, grass, snow, ice);

        // Setup ore spawning
        setupOreGenerator(stone);

        // Dig some caves in the terrain
        addChunkDecorator(
                new CaveDecorator(new BlockFilter() {
                    @Override
                    public boolean accepts(Chunk chunk, int x, int y, int z) {
                        return true;
                    }
                }, new PDist(0.1f, 0f), new PDist(5f, 1f), new PDist(70f, 60f), new PDist(70f, 10f), new PDist(2f, 0.5f))
        );

        // Setup flora growing in the world
        setupFlora(grass, sand, snow);
    }

    private void setupFlora(Block grass, Block sand, Block snow) {
        BlockFilter normalTreesGround = new BlockCollectionFilter(Arrays.asList(grass, snow));

        Block oakSapling = blockManager.getBlock("GrowingFlora:OakSaplingGenerated");
        Block pineSapling = blockManager.getBlock("GrowingFlora:PineSaplingGenerated");

        FloraDecorator floraDecorator = new FloraDecorator(new PDist(2f, 0.4f), new PDist(20f, 0.6f), new PDist(160f, 40f));

        // Forest
        floraDecorator.addTreeDefinition(ForestBiome.ID,
                new BlockFloraDefinition(0.9f, 0.8f, oakSapling, normalTreesGround));
        floraDecorator.addTreeDefinition(ForestBiome.ID,
                new BlockFloraDefinition(0.1f, 0.8f, pineSapling, normalTreesGround));

        // Plains
        floraDecorator.addTreeDefinition(PlainsBiome.ID,
                new BlockFloraDefinition(1f, 0.3f, oakSapling, normalTreesGround));

        // Tundra
        floraDecorator.addTreeDefinition(TundraBiome.ID,
                new BlockFloraDefinition(0.1f, 0.1f, oakSapling, normalTreesGround));
        floraDecorator.addTreeDefinition(TundraBiome.ID,
                new BlockFloraDefinition(0.9f, 0.7f, pineSapling, normalTreesGround));

        addChunkDecorator(floraDecorator);
    }


    private void setupOreGenerator(Block stone) {
        BlockFilter replacedBlocks = new BlockCollectionFilter(stone);
        OreDecorator oreDecorator = new OreDecorator(replacedBlocks);

        // Use plugin mechanism to setup required ores for the modules, by default WoodAndStone requires no
        // ores

        addChunkDecorator(oreDecorator);
    }

    private void setupLayers(Block stone, Block sand, Block dirt, Block grass, Block snow, Block ice) {
        BlockFilter replacedBlocks = new BlockCollectionFilter(stone);

        LayeringDecorator layering = new LayeringDecorator();

        DefaultLayersDefinition desertDef = new DefaultLayersDefinition(DesertBiome.ID);
        desertDef.addLayerDefinition(new PDist(3, 1), replacedBlocks, sand, false);
        desertDef.addLayerDefinition(new PDist(4, 2), replacedBlocks, dirt, true);
        layering.addBiomeLayers(desertDef);

        DefaultLayersDefinition forestDef = new DefaultLayersDefinition(ForestBiome.ID);
        DefaultLayersDefinition plainsDef = new DefaultLayersDefinition(PlainsBiome.ID);
        forestDef.addLayerDefinition(new PDist(1, 0), replacedBlocks, grass, false);
        plainsDef.addLayerDefinition(new PDist(1, 0), replacedBlocks, grass, false);
        forestDef.addLayerDefinition(new PDist(4, 2), replacedBlocks, dirt, true);
        plainsDef.addLayerDefinition(new PDist(4, 2), replacedBlocks, dirt, true);
        layering.addBiomeLayers(forestDef);
        layering.addBiomeLayers(plainsDef);

        DefaultLayersDefinition tundraDef = new DefaultLayersDefinition(TundraBiome.ID);
        DefaultLayersDefinition taigaDef = new DefaultLayersDefinition(TaigaBiome.ID);
        tundraDef.addLayerDefinition(new PDist(1, 0), replacedBlocks, snow, false);
        taigaDef.addLayerDefinition(new PDist(1, 0), replacedBlocks, snow, false);
        tundraDef.addLayerDefinition(new PDist(4, 2), replacedBlocks, dirt, true);
        taigaDef.addLayerDefinition(new PDist(4, 2), replacedBlocks, dirt, true);
        layering.addBiomeLayers(tundraDef);
        layering.addBiomeLayers(taigaDef);

        DefaultLayersDefinition alpineDef = new DefaultLayersDefinition(AlpineBiome.ID);
        alpineDef.addLayerDefinition(new PDist(2f, 1f), replacedBlocks, ice, false);
        alpineDef.addLayerDefinition(new PDist(1f, 0f), replacedBlocks, snow, false);
        layering.addBiomeLayers(alpineDef);

        addChunkDecorator(layering);
    }
}