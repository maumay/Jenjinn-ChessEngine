package jenjinn.engine.evaluation;

import jenjinn.engine.bitboarddatabase.Bitboards;
import jenjinn.engine.boardstate.BoardState;
import jenjinn.engine.enums.Side;
import jenjinn.engine.misc.EngineUtils;
import jenjinn.engine.pieces.ChessPiece;

/**
 * Static exchange evaluator, implemented using pseudo c code on
 * chessprogramming
 * 
 * @author t
 */
public final class StaticExchangeEvaluator
{
	private static final int[] ASCENDING_PVALUES = { 0, 2, 1, 3, 4, 5, 6, 8, 7, 9, 10, 11 };

	public StaticExchangeEvaluator()
	{
	}

	private long targBB, fromset, occ, attadef, potenxray;

	public int eval(final byte targ, final byte from, final BoardState state, final short[] pieceValues)
	{
		// Make sure all instance variables set correctly first
		fromset = 1L << from;
		targBB = 1L << targ;
		occ = state.getOccupiedSquares();
		generateAttackDefenseInfo(state);

		final long knightLocs = state.getPieceLocations(2) | state.getPieceLocations(8);
		Side fromSide = state.getFriendlySide();

		int d = 0;
		final int[] gain = new int[32];
		gain[d] = pieceValues[state.getPieceAt(targ).index() % 6];
		ChessPiece attPiece = state.getPieceFromBB(fromset);

		do {
			d++;
			fromSide = fromSide.otherSide();

			gain[d] = pieceValues[attPiece.index() % 6] - gain[d - 1];

			if (Math.max(-gain[d - 1], gain[d]) < 0) {
				break;
			}

			attadef ^= fromset;
			occ ^= fromset;

			if ((fromset & knightLocs) == 0) {
				updateXrays(state);
			}
			fromset = getLeastValuablePiece(state.getPieceLocationsCopy(), fromSide);
			attPiece = state.getPieceFromBB(fromset);
		} while (fromset != 0);

		while (--d > 0) {
			gain[d - 1] = -Math.max(-gain[d - 1], gain[d]);
		}
		return gain[0];
	}

	public boolean isGoodExchange(final byte targ, final byte from, final BoardState state, final short[] pieceValues)
	{
		return eval(targ, from, state, pieceValues) >= 0;
	}

	private void updateXrays(final BoardState state)
	{
		for (final byte loc : EngineUtils.getSetBits(potenxray)) {
			final ChessPiece p = state.getPieceAt(loc);
			if ((p.getAttackset(loc, occ) & targBB) != 0) {
				final long locBB = 1L << loc;
				potenxray ^= locBB;
				attadef ^= locBB;
			}
		}
	}

	private void generateAttackDefenseInfo(final BoardState state)
	{
		attadef = 0L;
		potenxray = 0L;
		int ctr = 0;
		for (final long locs : state.getPieceLocationsCopy()) {
			final ChessPiece p = ChessPiece.get(ctr++);
			final boolean canXray = p.canXray();

			for (final byte loc : EngineUtils.getSetBits(locs)) {
				final long atts = p.getAttackset(loc, occ);

				if ((atts & targBB) != 0) {
					attadef |= (1L << loc);
				}
				else if (canXray && (Bitboards.EBA[(p.index() % 6) + 1][loc] & targBB) != 0) {
					potenxray |= (1L << loc);
				}
			}
		}
	}

	private long getLeastValuablePiece(final long[] pieceLocs, final Side fromSide)
	{
		final int startIdx = fromSide.index(), endIdx = startIdx + 6;
		for (int i = startIdx; i < endIdx; i++) {
			final long subset = attadef & pieceLocs[ASCENDING_PVALUES[i]];
			if (subset != 0) {
				return (subset & -subset);
			}
		}
		return 0L;
	}
}