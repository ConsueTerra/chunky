/* Copyright (c) 2012-2014 Jesper Öqvist <jesper@llbit.se>
 *
 * This file is part of Chunky.
 *
 * Chunky is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chunky is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Chunky.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.llbit.chunky.map;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import se.llbit.chunky.main.Chunky;
import se.llbit.chunky.resources.MiscImages;
import se.llbit.chunky.world.Block;
import se.llbit.chunky.world.Chunk;
import se.llbit.chunky.world.ChunkPosition;
import se.llbit.chunky.world.ChunkSelectionTracker;
import se.llbit.chunky.world.ChunkView;
import se.llbit.chunky.world.World;
import se.llbit.math.QuickMath;
import se.llbit.math.Vector3d;

/**
 * @author Jesper Öqvist (jesper@llbit.se)
 */
public class WorldRenderer {

	public static int SELECTION_COLOR = 0x75FF0000;

	private boolean highlightEnabled;
	private Block hlBlock = Block.DIAMONDORE;
	private Color hlColor = Color.red;

	private boolean mapUpdated = false;

	private void renderEmpty(Graphics g, int width, int height) {
		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);
		g.setFont(new Font("Sans serif", Font.BOLD, 11)); //$NON-NLS-1$
	}

	int round = 0;
	int colors[] = {
			0xFF0000, 0x00FF00, 0x0000FF,
			0xFF3300, 0x00FF33, 0x3300FF,
			0xFF3333, 0x33FF33, 0x3333FF,
			0x990000, 0x009900, 0x000099,
			0x993300, 0x009933, 0x330099,
			0x993333, 0x339933, 0x333399,
			0x99AA33, 0x3399AA, 0xAA3399,
			0x99AAAA, 0xAA99AA, 0xAAAA99,
	};

	/**
	 * Render the map
	 * @param world
	 * @param buffer
	 * @param renderer
	 * @param selection
	 */
	public void render(World world, MapBuffer buffer,
			Chunk.Renderer renderer, ChunkSelectionTracker selection) {

		int width = buffer.getWidth();
		int height = buffer.getHeight();

		if (world.isEmptyWorld()) {
			Graphics g = buffer.getGraphics();
			renderEmpty(g, width, height);
			return;
		}

		ChunkView view = buffer.getView();

		for (ChunkPosition pos: buffer) {
			int x = pos.x;
			int z = pos.z;

			Chunk chunk = world.getChunk(pos);

			renderer.render(chunk, buffer, x, z);
			if (highlightEnabled) {
				chunk.renderHighlight(buffer, x, z,
						hlBlock, hlColor);
			}
			if (selection.isSelected(pos)) {
				buffer.fillRectAlpha(
						view.chunkScale * (x - view.px0),
						view.chunkScale * (z - view.pz0),
						view.chunkScale, view.chunkScale,
						SELECTION_COLOR);
			}

//			if (!chunk.isEmpty()) {
//				buffer.fillRect(
//						view.chunkScale * (x - view.px0),
//						view.chunkScale * (z - view.pz0),
//						view.chunkScale, view.chunkScale,
//						colors[round]);
//			}

		}
		round += 1;
		round %= colors.length;
	}

	/**
	 * Render overlay icons
	 * @param world
	 * @param chunky
	 * @param g
	 * @param renderBuffer
	 */
	public void renderHUD(World world, Chunky chunky,
			Graphics g, MapBuffer renderBuffer) {

		boolean loadIndicator = chunky.isLoading();
		Chunk.Renderer renderer = chunky.getChunkRenderer();

		ChunkView view = renderBuffer.getView();

		if (loadIndicator) {
			g.drawImage(MiscImages.clock, view.width-32, 0, 32, 32, null);
		}

		renderPlayer(world, g, view,
				renderer == Chunk.surfaceRenderer
				|| world.playerLocY() == world.currentLayer());

		renderSpawn(world, g, view,
				renderer == Chunk.surfaceRenderer
				|| world.spawnPosY() == world.currentLayer());
	}

	public void renderPlayer(World world, Graphics g, ChunkView view, boolean sameLayer) {
		double blockScale = view.scale / 16.;
		Vector3d playerPos = world.playerPos();
		if (playerPos == null) {
			return;
		}
		int px = (int) QuickMath.floor(playerPos.x * blockScale);
		int pz = (int) QuickMath.floor(playerPos.z * blockScale);
		int ppx = px - (int) QuickMath.floor(view.x0 * view.scale);
		int ppy = pz - (int) QuickMath.floor(view.z0 * view.scale);
		int pw = (int) QuickMath.max(8, QuickMath.min(16, blockScale * 2));
		ppx = Math.min(view.width-pw, Math.max(0, ppx-pw/2));
		ppy = Math.min(view.height-pw, Math.max(0, ppy-pw/2));

		if (sameLayer) {
			g.drawImage(MiscImages.face, ppx, ppy, pw, pw, null);
		} else {
			g.drawImage(MiscImages.face_t, ppx, ppy, pw, pw, null);
		}
	}

	public void renderSpawn(World world, Graphics g, ChunkView view, boolean sameLayer) {
		double blockScale = view.scale / 16.;
		if (!world.haveSpawnPos()) {
			return;
		}
		int px = (int) QuickMath.floor(world.spawnPosX() * blockScale);
		int pz = (int) QuickMath.floor(world.spawnPosZ() * blockScale);
		int ppx = px - (int) QuickMath.floor(view.x0 * view.scale);
		int ppy = pz - (int) QuickMath.floor(view.z0 * view.scale);
		int pw = (int) QuickMath.max(8, QuickMath.min(16, blockScale * 2));
		ppx = Math.min(view.width-pw, Math.max(0, ppx-pw/2));
		ppy = Math.min(view.height-pw, Math.max(0, ppy-pw/2));

		if (sameLayer) {
			g.drawImage(MiscImages.home, ppx, ppy, pw, pw, null);
		} else {
			g.drawImage(MiscImages.home_t, ppx, ppy, pw, pw, null);
		}
	}

	/**
	 * Set the highlight enable flag
	 * @param value
	 */
	public synchronized void setHighlightEnabled(boolean value) {
		highlightEnabled = value;
	}

	/**
	 * Set the highlight block type
	 * @param bt
	 */
	public synchronized void highlightBlock(Block bt) {
		hlBlock = bt;
	}

	/**
	 * Set the highlight color
	 * @param newColor
	 */
	public synchronized void setHighlightColor(Color newColor) {
		hlColor = newColor;
	}

	/**
	 * @return <code>true</code> if block highlighting is enabled
	 */
	public synchronized boolean isHighlightEnabled() {
		return highlightEnabled;
	}

	/**
	 * @return The current highlighted block type
	 */
	public synchronized Block getHighlightBlock() {
		return hlBlock;
	}

	/**
	 * @return The current highlight color
	 */
	public synchronized Color getHighlightColor() {
		return hlColor;
	}

	/**
	 * @return <code>true</code> if the map has been updated
	 */
	public synchronized boolean mapUpdated() {
		boolean res = mapUpdated;
		mapUpdated = false;
		return res;
	}
}
