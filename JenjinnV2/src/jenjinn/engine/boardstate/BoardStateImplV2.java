/**
 * Copyright � 2017 Lhasa Limited
 * File created: 19 Jul 2017 by ThomasB
 * Creator : ThomasB
 * Version : $Id$
 */
package jenjinn.engine.boardstate;

import java.util.List;

import jenjinn.engine.bitboarddatabase.BBDB;
import jenjinn.engine.enums.Side;
import jenjinn.engine.enums.TerminationType;
import jenjinn.engine.misc.EngineUtils;
import jenjinn.engine.moves.ChessMove;
import jenjinn.engine.pieces.ChessPiece;
import jenjinn.engine.zobristhashing.ZobristHasher;

/**
 * @author ThomasB
 * @since 19 Jul 2017
 */
public class BoardStateImplV2 implements BoardState
{
	public static ZobristHasher HASHER = ZobristHasher.getDefault();

	public static void setHasher(final ZobristHasher hASHER)
	{
		HASHER = hASHER;
	}

	/**
	 * An array of four most recent board hashings (including the
	 * hash of this state) for determining repetition draws.
	 */
	private final long[] recentHashings;

	/**
	 * Representation of the side to move, i.e the friendly side.
	 * 0 for white and 1 for black.
	 */
	private final byte friendlySide;
	private final byte castleRights;
	private final byte castleStatus;
	private final byte enPassantSq;
	private final long devStatus; // For simplicity use a long

	/**
	 * Think its worth keeping this stored, we need to calculate it
	 * for a states terminal status anyway and it allows us to remove
	 * illegal king moves during move retrieval which could save
	 * significant time during searching. TODO actually for termination status we need all friendly attacks too
	 *
	 * So if we keep all friendly attacks then we basically achieve the same thing because illegal king moves
	 * we induce a terminal boardstate straight away and save on searching, although why store it then? It
	 * will just be used once.
	 */
	// private final long friendlyAttacks;

	private final long[] pieceLocations;

	public BoardStateImplV2(final long[] recentHashings,
			final byte friendlySide,
			final byte castleRights,
			final byte castleStatus,
			final byte enPassantSq,
			final long devStatus,
			final long[] pieceLocations)
	{
		this.recentHashings = recentHashings;
		this.friendlySide = friendlySide;
		this.castleRights = castleRights;
		this.castleStatus = castleStatus;
		this.enPassantSq = enPassantSq;
		this.devStatus = devStatus;
		this.pieceLocations = pieceLocations;
	}

	@Override
	public List<ChessMove> getMoves()
	{
		// TODO Auto-generated method stub
		return null;
	}

	// @Override
	// public ChessMove generateMove(final AlgebraicCommand com)
	// {
	// // TODO Auto-generated method stub
	// return null;
	// }

	@Override
	public long zobristHash()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ChessPiece getPieceAt(final byte loc)
	{
		for (byte index = 0; index < 12; index++)
		{
			if ((BBDB.SOB[loc] & pieceLocations[index]) != 0)
			{
				return ChessPiece.PIECES[index];
			}
		}
		return null;
	}

	@Override
	public long getPieceLocations(final int pieceIndex)
	{
		return pieceLocations[pieceIndex];
	}

	@Override
	public long getAttackedSquares(final Side side)
	{
		// TODO - Could perform optimisation on pawn attacks
		final long occupiedSquares = getOccupiedSquares();
		long attackedSquares = 0L;

		for (byte i = side.getId(); i < side.getId() + 6; i++)
		{
			final byte[] locs = EngineUtils.getSetBits(pieceLocations[i]);
			final ChessPiece p = ChessPiece.get(i);

			for (final byte loc : locs)
			{
				attackedSquares |= p.getAttackset(loc, occupiedSquares);
			}
		}
		return attackedSquares;
	}

	@Override
	public byte getCastleStatus()
	{
		return castleStatus;
	}

	@Override
	public byte getCastleRights()
	{
		return castleRights;
	}

	@Override
	public long getDevelopmentStatus()
	{
		return devStatus;
	}

	@Override
	public long getHashing()
	{
		return recentHashings[0];
	}

	@Override
	public byte getEnPassantSq()
	{
		return enPassantSq;
	}

	@Override
	public long[] getRecentHashings()
	{
		return recentHashings;
	}

	@Override
	public Side getFriendlySide()
	{
		return friendlySide == 0 ? Side.W : Side.B;
	}

	@Override
	public Side getEnemySide()
	{
		return friendlySide == 0 ? Side.B : Side.W;
	}

	@Override
	public TerminationType getTerminationState()
	{
		// First check for taking of king
		final Side friendlySide = getFriendlySide();

		if ((getAttackedSquares(friendlySide) & pieceLocations[friendlySide.otherSide().getId() + 6]) != 0)
		{
			return friendlySide == Side.W ? TerminationType.WHITE_WIN : TerminationType.BLACK_WIN;
		}

		return null;
	}

	@Override
	public long getSideLocations(final Side s)
	{
		long locs = 0L;
		for (byte index = s.getId(); index < s.getId() + 6; index++)
		{
			locs |= pieceLocations[index];
		}
		return locs;
	}

	@Override
	public long getOccupiedSquares()
	{
		return EngineUtils.multipleOr(pieceLocations);
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