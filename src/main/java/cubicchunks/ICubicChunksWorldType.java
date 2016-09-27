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
package cubicchunks;

import cubicchunks.util.AddressTools;
import cubicchunks.world.ICubicWorldServer;
import cubicchunks.worldgen.ColumnGenerator;
import cubicchunks.worldgen.ICubicChunkGenerator;

public interface ICubicChunksWorldType {
	/**
	 * Returns Y position of the bottom block in the world
	 */
	default int getMinimumPossibleHeight() {
		return AddressTools.MIN_BLOCK_Y;
	}

	/**
	 * Returns Y position of block above the top block in the world,
	 */
	default int getMaximumPossibleHeight() {
		return AddressTools.MAX_BLOCK_Y + 1;
	}

	ICubicChunkGenerator createCubeGenerator(ICubicWorldServer world);

	//TODO: change return type to IChunkProvider
	ColumnGenerator createColumnGenerator(ICubicWorldServer world);
}
