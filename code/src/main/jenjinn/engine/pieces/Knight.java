/**
 * Written by Tom Ball 2017.
 *
 * This code is unlicensed but please don't plagiarize.
 */

package jenjinn.engine.pieces;

import jenjinn.engine.bitboarddatabase.BBDB;
import jenjinn.engine.enums.Side;

/**
 * A class representing a Knight to be used by the engine
 *
 * @author TB
 * @date 24 Jan 2017
 */
public class Knight extends ChessPiece
{
	Knight(final Side side)
	{
		super(PieceType.N, side);
	}

	/* (non-Javadoc)
	 *
	 * @see jenjinn.engine.pieces.ChessPiece#getAttackset(byte, long, long) */
	@Override
	public long getAttackset(final byte loc, final long occupiedSquares)
	{
		return BBDB.EBA[3][loc];
	}

	@Override
	public long getStartBitboard()
	{
		return 0b1000010L << 56 * (getSide().isWhite() ? 0 : 1);
	}
}