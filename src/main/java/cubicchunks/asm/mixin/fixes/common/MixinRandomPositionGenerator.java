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
package cubicchunks.asm.mixin.fixes.common;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Mixin(RandomPositionGenerator.class)
public class MixinRandomPositionGenerator {
	// TODO: Find where it is in 1.10.2
    /*

    /**
     * @author Barteks2x
     * @reason original function does not check if area is loaded when iterate thru vertical block positions.
     * /
	@Overwrite
	private static BlockPos moveAboveSolid(BlockPos pos, EntityCreature entity) {
		if (!entity.world.getBlockState(pos).getMaterial().isSolid()) {
			return pos;
		}
		BlockPos currentPos = pos.up();

		while (currentPos.getY() < entity.world.getHeight() && entity.world.isBlockLoaded(currentPos) && entity.world.getBlockState(currentPos).getMaterial().isSolid()) {
			currentPos = currentPos.up();
		}

		if (!entity.world.isBlockLoaded(currentPos)) {
			return pos; // didn't find better position :(
		}

		return currentPos;
	}
	*/
}
