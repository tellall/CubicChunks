/*
 *  This file is part of Cubic Chunks Mod, licensed under the MIT License (MIT).
 *
 *  Copyright (c) 2015 contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package io.github.opencubicchunks.cubicchunks.core.asm.mixin.core.common;

import static io.github.opencubicchunks.cubicchunks.api.util.Coords.blockToCube;
import static io.github.opencubicchunks.cubicchunks.api.util.Coords.blockToLocal;

import io.github.opencubicchunks.cubicchunks.core.lighting.LightingManager;
import io.github.opencubicchunks.cubicchunks.core.world.ICubeProviderInternal;
import io.github.opencubicchunks.cubicchunks.core.world.cube.Cube;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.core.lighting.LightingManager;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.util.IntRange;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.core.world.ICubeProviderInternal;
import io.github.opencubicchunks.cubicchunks.core.asm.mixin.ICubicWorldInternal;
import io.github.opencubicchunks.cubicchunks.core.asm.mixin.ICubicWorldSettings;
import io.github.opencubicchunks.cubicchunks.api.util.NotCubicChunksWorldException;
import io.github.opencubicchunks.cubicchunks.core.world.cube.Cube;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Contains implementation of {@link ICubicWorld} interface.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mixin(World.class)
@Implements(@Interface(iface = ICubicWorld.class, prefix = "world$"))
public abstract class MixinWorld implements ICubicWorldInternal {

    // these have to be here because of mixin limitation, they are used by MixinWorldServer
    @Shadow public abstract ISaveHandler getSaveHandler();
    @Shadow public abstract boolean isAreaLoaded(BlockPos blockpos1, BlockPos blockpos2);

    @Shadow protected IChunkProvider chunkProvider;
    @Shadow @Final @Mutable public WorldProvider provider;
    @Shadow @Final public Random rand;
    @Shadow @Final public boolean isRemote;
    @Shadow @Final public Profiler theProfiler;
    @Shadow @Final @Mutable protected ISaveHandler saveHandler;
    @Shadow protected boolean findingSpawnPoint;
    @Shadow protected WorldInfo worldInfo;
    @Shadow protected int updateLCG;

    @Shadow protected abstract boolean isChunkLoaded(int i, int i1, boolean allowEmpty);

    @Nullable private LightingManager lightingManager;
    protected boolean isCubicWorld;
    protected int minHeight = 0, maxHeight = 256;
    private int minGenerationHeight = 0, maxGenerationHeight = 256;

    @Shadow public abstract boolean isValid(BlockPos pos);

    protected void initCubicWorld(IntRange heightRange, IntRange generationRange) {
        ((ICubicWorldSettings) worldInfo).setCubic(true);
        // Set the world height boundaries to their highest and lowest values respectively
        this.minHeight = heightRange.getMin();
        this.maxHeight = heightRange.getMax();

        this.minGenerationHeight = generationRange.getMin();
        this.maxGenerationHeight = generationRange.getMax();

        //has to be created early so that creating BlankCube won't crash
        this.lightingManager = new LightingManager((World) (Object) this);
    }

    @Override public boolean isCubicWorld() {
        return this.isCubicWorld;
    }

    @Override public int getMinHeight() {
        return this.minHeight;
    }

    @Override public int getMaxHeight() {
        return this.maxHeight;
    }

    @Override public int getMinGenerationHeight() {
        return this.minGenerationHeight;
    }

    @Override public int getMaxGenerationHeight() {
        return this.maxGenerationHeight;
    }

    @Override public ICubeProviderInternal getCubeCache() {
        if (!this.isCubicWorld()) {
            throw new NotCubicChunksWorldException();
        }
        return (ICubeProviderInternal) this.chunkProvider;
    }

    @Override public LightingManager getLightingManager() {
        if (!this.isCubicWorld()) {
            throw new NotCubicChunksWorldException();
        }
        assert this.lightingManager != null;
        return this.lightingManager;
    }

    @Override
    public boolean testForCubes(CubePos start, CubePos end, Predicate<? super ICube> cubeAllowed) {
        // convert block bounds to chunk bounds
        int minCubeX = start.getX();
        int minCubeY = start.getY();
        int minCubeZ = start.getZ();
        int maxCubeX = end.getX();
        int maxCubeY = end.getY();
        int maxCubeZ = end.getZ();

        for (int cubeX = minCubeX; cubeX <= maxCubeX; cubeX++) {
            for (int cubeY = minCubeY; cubeY <= maxCubeY; cubeY++) {
                for (int cubeZ = minCubeZ; cubeZ <= maxCubeZ; cubeZ++) {
                    Cube cube = this.getCubeCache().getLoadedCube(cubeX, cubeY, cubeZ);
                    if (!cubeAllowed.test(cube)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override public Cube getCubeFromCubeCoords(int cubeX, int cubeY, int cubeZ) {
        return this.getCubeCache().getCube(cubeX, cubeY, cubeZ);
    }

    @Override public Cube getCubeFromBlockCoords(BlockPos pos) {
        return this.getCubeFromCubeCoords(blockToCube(pos.getX()), blockToCube(pos.getY()), blockToCube(pos.getZ()));
    }

    @Override public int getEffectiveHeight(int blockX, int blockZ) {
        return this.chunkProvider.provideChunk(blockToCube(blockX), blockToCube(blockZ))
                .getHeightValue(blockToLocal(blockX), blockToLocal(blockZ));
    }

    // suppress mixin warning when running with -Dmixin.checks.interfaces=true
    @Override public void tickCubicWorld() {
        // pretend this method doesn't exist
        throw new NoSuchMethodError("World.tickCubicWorld: Classes extending World need to implement tickCubicWorld in CubicChunks");
    }

    //vanilla field accessors

    /**
     * @author Foghrye4
     * @reason Original {@link World#markChunkDirty(BlockPos, TileEntity)}
     *         called by TileEntities whenever they need to force Chunk to save
     *         valuable info they changed. Because now we store TileEntities in
     *         Cubes instead of Chunks, it will be quite reasonable to force
     *         Cubes to save themselves.
     */
    @Inject(method = "markChunkDirty", at = @At("HEAD"), cancellable = true)
    public void onMarkChunkDirty(BlockPos pos, TileEntity unusedTileEntity, CallbackInfo ci) {
        if (this.isCubicWorld()) {
            Cube cube = this.getCubeCache().getLoadedCube(CubePos.fromBlockCoords(pos));
            if (cube != null) {
                cube.markDirty();
            }
            ci.cancel();
        }
    }
    
    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    public void getBlockState(BlockPos pos, CallbackInfoReturnable<IBlockState> ci) {
        if (this.isCubicWorld()) {
            if (this.isValid(pos))
                ci.setReturnValue(this.getCubeCache().getCube(CubePos.fromBlockCoords(pos)).getBlockState(pos));
            else
                ci.setReturnValue(Blocks.AIR.getDefaultState());
            ci.cancel();
        }
    }

    @Override public boolean isBlockColumnLoaded(BlockPos pos) {
        return isBlockColumnLoaded(pos, true);
    }

    @Override public boolean isBlockColumnLoaded(BlockPos pos, boolean allowEmpty) {
        return this.isChunkLoaded(blockToCube(pos.getX()), blockToCube(pos.getZ()), allowEmpty);
    }
}
