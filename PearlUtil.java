package hw3;

import static api.State.EMPTY;
import static api.State.OPEN_GATE;
import static api.State.PEARL;
import static api.State.PORTAL;
import static api.State.isBoundary;
import static api.State.isMovable;

import api.Direction;
import api.MoveRecord;
import api.State;

/**
 * Utility class containing the key algorithms for moves in the Pearls game.
 * This class is completely stateless.
 * 
 * @author Rida Azam
 */
public class PearlUtil {
	public PearlUtil() {
		// does nothing
	}

	/**
	 * Replaces all PEARL states with EMPTY state between indices start and end,
	 * inclusive. If the records array is non-null, then all records corresponding
	 * to removed pearls are marked as disappeared.
	 * 
	 * @param states  any array of States
	 * @param records a parallel array of <code>MoveRecord</code>, consistent with
	 *                the state sequence (possibly null)
	 * @param start   starting index, inclusive
	 * @param end     ending index, inclusive
	 */
	public void collectPearls(State[] states, MoveRecord[] records, int start, int end) {
		for (int i = start; i <= end; i += 1) {
			if (states[i] == PEARL) {
				states[i] = (EMPTY);

				records[i].setDisappeared();
			}
		}
	}

	/**
	 * Returns the index of the rightmost movable block that is at or to the left of
	 * the given index <code>start</code>. Returns -1 if there is no movable block
	 * at <code>start</code> or to the left.
	 * 
	 * @param states array of State objects
	 * @param start  starting index for searching
	 * @return index of first movable block encountered when searching towards the
	 *         left, starting from the given starting index; returns -1 if there is
	 *         no movable block found
	 */
	public int findRightmostMovableBlock(State[] states, int start) {
		int i = start;
		while (i >= 0 && !State.isMovable(states[i])) {
			i--;
		}

		return i;
	}

	/**
	 * Determines whether the given state sequence is valid for the moveBlocks
	 * method. A state sequence is valid for moveBlocks if
	 * <ul>
	 * <li>its length is at least 2, and
	 * <li>the first state is EMPTY, OPEN_GATE, or PORTAL, and
	 * <li>it contains exactly one <em>boundary state</em>, which is the last
	 * element of the array
	 * </ul>
	 * Boundary states are defined by the method <code>State.isBoundary</code>, and
	 * are defined differently based on whether there is any movable block in the
	 * array.
	 * 
	 * @param states any array of States
	 * @return true if the array is a valid state sequence, false otherwise
	 */
	public static boolean isValidForMoveBlocks(State[] states) {
		if (states.length < 2) {
			return false;
		}
		State start = states[0];
		if (!(start == EMPTY || start == OPEN_GATE || start == PORTAL)) {
			return false;
		}
		boolean containsMovable = false;
		for (int i = 1; i < states.length; ++i) {
			State c = states[i];
			if (isMovable(c)) {
				containsMovable = true;
			}
			if (isBoundary(c, containsMovable)) {
				return i == states.length - 1;
			}
		}
		return false;
	}

	/**
	 * Updates the given state sequence to be consistent with shifting the "player"
	 * to the right as far as possible. The starting position of the player is
	 * always index 0. The state sequence is assumed to be <em>valid for
	 * movePlayer</em>, which means that the sequence could have been obtained by
	 * applying moveBlocks to a sequence that was valid for moveBlocks. That is, the
	 * validity condition is the same as for moveBlocks, except that
	 * <ul>
	 * <li>all movable blocks, if any, are as far to the right as possible, and
	 * <li>the last element may be OPEN_GATE or PORTAL, even if there are no movable
	 * blocks
	 * </ul>
	 * <p>
	 * The player's new index, returned by the method, will be one of the following:
	 * <ul>
	 * <li>if the array contains any movable blocks, the new index is just before
	 * the first movable block in the array;
	 * <li>otherwise, if the very last element of the array is SPIKES_ALL, the new
	 * index is the last position in the array;
	 * <li>otherwise, the new index is the next-to-last position of the array.
	 * </ul>
	 * Note the last state of the array is always treated as a boundary for the
	 * player, even if it is OPEN_GATE or PORTAL. All pearls in the sequence are
	 * changed to EMPTY and any open gates passed by the player are changed to
	 * CLOSED_GATE by this method. (If the player's new index is on an open gate,
	 * its state remains OPEN_GATE.)
	 *
	 * <p>
	 * If the records array is non-null, then each record is updated as follows:
	 * <ul>
	 * <li>The first record's <code>moveTo</code> attribute is set to the player's
	 * new index in the array
	 * <li>All pearls are marked as <code>disappeared</code>
	 * </ul>
	 * The states within each record are not modified. It should be assumed that the
	 * given records array is <em>consistent</em> with the states array (allowing
	 * for prior movement of movable blocks from a previous call to
	 * <code>moveBlocks</code>).
	 * 
	 * @param states  a valid state sequence in which movable blocks, if any, are at
	 *                the far right only
	 * @param records a parallel array of <code>MoveRecord</code>, consistent with
	 *                the state sequence (possibly null)
	 * @param dir     direction of the move, for the purpose of determining whether
	 *                spikes may be deadly
	 * @return the player's new index
	 */
	public int movePlayer(State[] states, MoveRecord[] records, Direction dir) {
		// first find the end where player will end up - this is always
		// the next to last cell, unless there is a movable that comes first
		// or it's deadly spikes
		int end = states.length - 2;
		for (int i = 0; i < states.length; ++i) {
			if (State.isMovable(states[i])) {
				end = i - 1;
				break;
			}
		}
		if (State.spikesAreDeadly(states[end + 1], dir)) {
			end = end + 1;
		}

		// now we know 'end' is the player's final cell,
		// just go through and collect pearls...
		collectPearls(states, records, 0, end);

		// ...and close the open gates
		// (Note that if player ends up on an open gate, it should not be closed)
		for (int i = 0; i < end; ++i) {
			if (states[i] == State.OPEN_GATE) {
				states[i] = (State.CLOSED_GATE);
				records[i].setClosed();
			}
		}

		records[0].setMovedToIndex(end);

		return end;
	}

	/**
	 * Updates the given state sequence to be consistent with shifting all movable
	 * blocks as far to the right as possible, replacing their previous positions
	 * with EMPTY. Adjacent movable blocks with opposite parity are "merged" from
	 * the right and removed. The given array is assumed to be <em>valid for
	 * moveBlocks</em> in the sense of the method <code>validForMoveBlocks</code>.
	 * If a movable block moves over a pearl (whether or not the block is
	 * subsequently removed due to merging with an adjacent block) then the pearl is
	 * also replaced with EMPTY.
	 * <p>
	 * If the given array of records is non-null, then the records are updated as
	 * follows:
	 * <ul>
	 * <li>all records for movable blocks must be updated with the new index of the
	 * block
	 * <li>records for movable blocks that either moved or disappeared are marked as
	 * disappeared
	 * <li>all pearls that were passed by movable blocks are marked as disappeared
	 * </ul>
	 * <em>Note that merging is done from the right.</em> For example, given a cell
	 * sequence represented by ".+-+#", the resulting cell sequence would be
	 * "...+#", but the records would show positions 2 and 3 as having moved to
	 * position 3 and disappeared, and position 1 as having moved to position 3. It
	 * should be assumed that the given records array is <em>consistent</em> with
	 * the states array.
	 * 
	 * @param states  a valid state sequence
	 * @param records parallel array of <code>Descriptors</code> exactly matching
	 *                the cell sequence, possibly null
	 */
	public void moveBlocks(State[] states, MoveRecord[] records) {
		// Here is our algorithm from the pdf, in pseudocode:
		//
		// let end = next to last cell
		// while not done
		// let nextMovable = index of rightmost movable block to left of end
		// if there isn't one
		// done = true
		// else
		// let prev = index of rightmost movable block to left of nextMovable
		// if there is one, and if they can merge
		// collect pearls that we move over
		// set both cells as empty
		// else
		// collect pearls that we move over
		// move the block at nextMovable to end
		// end = end - 1
		//

		if (!isValidForMoveBlocks(states)) {
			throw new IllegalArgumentException("Invalid state array for moveBlocks");
		}

		// the "end" is where the rightmost movable block will move
		// initially it's the last cell before the boundary, but will shift to left
		// as we put movable blocks there
		int end = states.length - 2;

		boolean done = false;

		while (!done) {
			int nextMovable = findRightmostMovableBlock(states, end);
			if (nextMovable < 0) {
				done = true;
			} else {
				// there is a movable cell at nextMovable, so we check if there
				// is another one to its left that will merge
				int prev = findRightmostMovableBlock(states, nextMovable - 1);
				if (prev >= 0 && State.canMerge(states[nextMovable], states[prev])) {
					// ok, the two blocks will merge

					// first, we collect pearls starting at prev
					collectPearls(states, records, prev + 1, end);

					// do the "merge" and disappear the two blocks
					states[nextMovable] = (State.EMPTY);
					states[prev] = (State.EMPTY);

					// update records
					records[nextMovable].setDisappeared();
					records[nextMovable].setMovedToIndex(end);

					records[prev].setDisappeared();
					records[prev].setMovedToIndex(end);

					// ('end' stays the same, since the blocks disappeared)

				} else {
					// can't merge, so shift block to end

					// first, we collect pearls
					collectPearls(states, records, nextMovable + 1, end);

					// move cells[nextMovable] up to end (unless it's already there)
					if (nextMovable < end) {
						states[end] = states[nextMovable];
						states[nextMovable] = (State.EMPTY);

						// update records
						records[nextMovable].setMovedToIndex(end);
					}

					// now 'end' shifts to left, since we put a movable block there
					end = end - 1;
				}
			}
		}
	}

}
