/**
 * Copyright � 2017 Lhasa Limited
 * File created: 19 Sep 2017 by ThomasB
 * Creator : ThomasB
 * Version : $Id$
 */
package jenjinn.testingengine.pieces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jenjinn.engine.enums.Direction;
import jenjinn.engine.enums.Side;
import jenjinn.engine.enums.Sq;
import jenjinn.engine.misc.EngineUtils;
import jenjinn.engine.pieces.ChessPiece;
import jenjinn.engine.pieces.PieceType;

/**
 * @author ThomasB
 * @since 19 Sep 2017
 */
public abstract class TChessPiece extends ChessPiece
{
	/**
	 * For the testing engine we use a much slower but more intuitive way
	 * of generating possible moves.
	 */
	final List<Direction> movementDirections;

	/**
	 *
	 */
	public TChessPiece(final PieceType type, final Side side, final List<Direction> moveDirs)
	{
		super(type, side);
		this.movementDirections = Collections.unmodifiableList(moveDirs);
	}

	/**
	 * Given the location of the piece, the location of all friendly pieces
	 * and the location of all enemy pieces this method returns the attackset
	 * in bitboard form.
	 */
	@Override
	public long getAttackset(final byte loc, final long occupiedSquares)
	{
		final List<Sq> attackSq = new ArrayList<>();
		final Sq start = Sq.getSq(loc);

		for (final Direction d : movementDirections)
		{
			Sq next = start;
			while ((next = next.getNextSqInDirection(d)) != null)
			{
				attackSq.add(next);
				if ((next.getAsBB() & occupiedSquares) != 0)
				{
					break;
				}
			}
		}
		return bbFromSqs(attackSq);
	}

	protected final long bbFromSqs(final List<Sq> sqs)
	{
		return EngineUtils.multipleOr(sqs.stream()
				.mapToLong(x -> x.getAsBB())
				.toArray());
	}
}

/* ---------------------------------------------------------------------*
 * This software is the confidential and proprietary
 * information of Lhasa Limited
 * Granary Wharf House, 2 Canal Wharf, Leeds, LS11 5PS
 * ---
 * No part of this confidential information shall be disclosed
 * and it shall be used only in accordance with the terms of a
 * written license agreement entered into by holder of the information
 * with LHASA Ltd.
 * --------------------------------------------------------------------- */