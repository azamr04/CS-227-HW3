package hw3;

import java.util.ArrayList;

import api.Cell;
import api.Direction;
import api.MoveRecord;
import api.State;
import api.StringUtil;

/**
 * Basic game state and operations for a the puzzle game "Pearls", which is a
 * simplified version of "Quell".
 * 
 * @author smkautz
 * @author Rida Azam
 */
public class Pearls {
	/**
	 * Two-dimensional array of Cell objects representing the grid on which the game
	 * is played.
	 */
	private Cell[][] grid;

	/**
	 * Instance of PearlUtil to be used with this game.
	 */
	private PearlUtil util;

	/**
	 * Number of moves made this game.
	 */
	private int moves;

	/**
	 * Score (number of pearls collected) for this game.
	 */
	private int score;

	/**
	 * Constructs a game from the given string description. The conventions for
	 * representing cell states as characters can be found in
	 * <code>StringUtil</code>.
	 * 
	 * @param init      string array describing initial cell states
	 * @param givenUtil PearlUtil instance to use in the <code>move</code> method
	 */
	public Pearls(String[] init, PearlUtil givenUtil) {
		grid = StringUtil.createFromStringArray(init);
		util = givenUtil;
		moves = 0;
		score = 0;
	}

	/**
	 * Returns the number of columns in the grid.
	 * 
	 * @return width of the grid
	 */
	public int getColumns() {
		return grid[0].length;
	}

	/**
	 * Returns the number of rows in the grid.
	 * 
	 * @return height of the grid
	 */
	public int getRows() {
		return grid.length;
	}

	/**
	 * Returns the cell at the given row and column.
	 * 
	 * @param row row index for the cell
	 * @param col column index for the cell
	 * @return cell at given row and column
	 */
	public Cell getCell(int row, int col) {
		return grid[row][col];
	}

	/**
	 * Returns true if the game is over, false otherwise. The game ends when all
	 * pearls are removed from the grid or when the player lands on a cell with
	 * spikes.
	 * 
	 * @return true if the game is over, false otherwise
	 */
	public boolean isOver() {
		
		// Checks if player died on spike
		boolean diedOnSpike = State.isSpikes(grid[getCurrentRow()][getCurrentColumn()].getState());
		
		return countPearls() == 0 || diedOnSpike;
	}

	/**
	 * Performs a move along a state sequence in the given direction, updating the
	 * score, the move count, and all affected cells in the grid. The method returns
	 * an array of MoveRecord objects representing the states in original state
	 * sequence before modification, with their <code>movedTo</code> and
	 * <code>disappeared</code> status set to indicate the cell states' new
	 * locations after modification.
	 * 
	 * @param dir direction of the move
	 * @return array of MoveRecord objects describing modified cells
	 */
	public MoveRecord[] move(Direction dir) {
		int pearlsBefore = countPearls();

		State[] stateSequence = getStateSequence(dir);
		MoveRecord[] records = new MoveRecord[stateSequence.length];

		for (int i = 0; i < records.length; i++) {
			records[i] = new MoveRecord(stateSequence[i], i);
		}

		util.moveBlocks(stateSequence, records);
		int index = util.movePlayer(stateSequence, records, dir);

		setStateSequence(stateSequence, dir, index);

		this.score += pearlsBefore - countPearls();
		this.moves++;
		return records;
	}

	/**
	 * Returns the row index for the players current location.
	 * 
	 * @return player's current row index
	 */
	public int getCurrentRow() {
		int currentRow = 0;
		for (int i = 0; i < getRows() - 1; i++) {
			for (int j = 0; j < getColumns(); j++) {
				if (grid[i][j].isPlayerPresent()) {
					currentRow = i;
				}
			}
		}
		
		return currentRow;
	}

	/**
	 * Returns the column index for the player's current location.
	 * 
	 * @return player's current column index
	 */
	public int getCurrentColumn() {
		int currentColumn = 0;

		for (int i = 0; i < getRows() - 1; i++) {
			for (int j = 0; j < getColumns(); j++) {
				if (grid[i][j].isPlayerPresent()) {
					currentColumn = j;
				}
			}
		}

		return currentColumn;
	}

	/**
	 * Returns true if the game is over and the player did not die on spikes.
	 * 
	 * @return true if the player won, false otherwise
	 */
	public boolean won() {

		// Checks if player's current location is a spike (player died on spike)
		boolean diedOnSpike = State.isSpikes(grid[getCurrentRow()][getCurrentColumn()].getState());
		
		return isOver() && (!diedOnSpike);
	}

	/**
	 * Returns the number of moves made in this current game
	 * 
	 * @return number of moves made
	 */
	public int getMoves() {
		return moves;
	}

	/**
	 * Returns current score (number of pearls disappeared) for the current game.
	 * 
	 * @return current score
	 */
	public int getScore() {
		return score;
	}

	/**
	 * Finds a valid state sequence in the given direction starting with the
	 * player's current position and ending with a boundary cell as defined by the
	 * method State.isBoundary. The actual cell locations are obtained by following
	 * getNextRow and getNextColumn in the given direction, and the sequence ends
	 * when a boundary state is found. A boundary state is defined by the method
	 * State.isBoundary and is different depending on whether a movable block has
	 * been encountered so far in the state sequence (the player can move through
	 * open gates and portals, but the movable blocks cannot). It can be assumed
	 * that there will eventually be a boundary state (i.e., the grid has no
	 * infinite loops). The first element of the returned array corresponds to the
	 * cell containing the player, and the last element of the array is the boundary
	 * state. This method does not modify the grid or any aspect of the game state.
	 * 
	 * @param dir direction of the sequence
	 * @return array of states in the sequence
	 */
	public State[] getStateSequence(Direction dir) {
		// Create an ArrayList to hold states
		ArrayList<State> states = new ArrayList<State>();

		// Get player position
		int row = getCurrentRow();
		int col = getCurrentColumn();

		// Add first state (player's current position)
		states.add(grid[row][col].getState());

		// Get next row index and next state
		int temp = row;
		row = getNextRow(row, col, dir, false);
		col = getNextColumn(temp, col, dir, false);
		State nextState = grid[row][col].getState();

		// Create boolean for if movable block has been encountered
		boolean foundMoveBlock = nextState == State.MOVABLE_NEG || nextState == State.MOVABLE_POS;

		// Create boolean for if need to portal jump
		boolean doPortalJump = nextState == State.PORTAL;

		// Loop while next state is not boundary
		while (!State.isBoundary(nextState, foundMoveBlock)) {

			// Add next state
			states.add(nextState);

			// Execute normally if no portal jump
			if (!doPortalJump) {

				// Get new row/column values and next state
				temp = row;
				row = getNextRow(row, col, dir, false);
				col = getNextColumn(temp, col, dir, false);
				nextState = grid[row][col].getState();

				// Check for portals
				if (nextState == State.PORTAL) {
					doPortalJump = true;
				}

				// Check for movable blocks
				if (nextState == State.MOVABLE_NEG || nextState == State.MOVABLE_POS) {
					foundMoveBlock = true;
				}
			}

			// Execute if portal jump
			else {

				// Get new column and row values by adding portal offset
				temp = row;
				row = getNextRow(row, col, dir, doPortalJump);
				col = getNextColumn(temp, col, dir, doPortalJump);

				// Create next state based on new values, set portal jump to false
				nextState = grid[row][col].getState();
				doPortalJump = false;
			}
		}

		// Add boundary state
		states.add(nextState);

		// Create State array, and iterate through to fill in values
		State[] stateSequence = new State[states.size()];
		for (int i = 0; i < states.size(); i++) {
			stateSequence[i] = states.get(i);
		}
		return stateSequence;
	}

	/**
	 * Sets the given state sequence and updates the player position. This method
	 * effectively retraces the steps for creating a state sequence in the given
	 * direction, starting with the player's current position, and updates the grid
	 * with the new states. The given state sequence can be assumed to be
	 * structurally consistent with the existing grid, e.g., no portal or wall cells
	 * are moved.
	 * 
	 * @param states      updated states to replace existing ones in the sequence
	 * @param dir         direction of the state sequence
	 * @param playerIndex index within the array of the player's location
	 */
	public void setStateSequence(State[] states, Direction dir, int playerIndex) {
		// Get current position
		int row = getCurrentRow();
		int col = getCurrentColumn();

		// Set player present to false (player has moved)
		grid[row][col].setPlayerPresent(false);

		// Get next row and next state
		int temp = row;
		row = getNextRow(row, col, dir, false);
		col = getNextColumn(temp, col, dir, false);
		State nextState = grid[row][col].getState();

		// Create booleans for if encountered movable blocks
		boolean foundMoveBlock = nextState == State.MOVABLE_NEG || nextState == State.MOVABLE_POS;

		// Create boolean for if need to do portal jump
		boolean doPortalJump = nextState == State.PORTAL;

		// Create count to iterate through states
		int count = 0;

		// Loop while state is not boundary
		while (!State.isBoundary(nextState, foundMoveBlock)) {

			// Increment count and update grid
			count++;
			grid[row][col].setState(states[count]);

			// Check if reached player position, set if true
			if (count == playerIndex) {
				grid[row][col].setPlayerPresent(true);
			}

			// Iterate normally if there's no portal jump
			if (!doPortalJump) {
				
				// Get new row/column values and next state
				temp = row;
				row = getNextRow(row, col, dir, doPortalJump);
				col = getNextColumn(temp, col, dir, doPortalJump);
				nextState = grid[row][col].getState();
				
				// Check for portals
				if (nextState == State.PORTAL) {
					doPortalJump = true;
				}

				// Check for movable blocks
				if (nextState == State.MOVABLE_NEG || nextState == State.MOVABLE_POS) {
					foundMoveBlock = true;
				}
			}
			
			// Execute if portal jump
			else {
				
				// Get new row and portal values by adding portal offset
				temp = row;
				row = getNextRow(row, col, dir, doPortalJump);
				col = getNextColumn(temp, col, dir, doPortalJump);

				// Get next state and set doPortalJump to false
				nextState = grid[row][col].getState();
				doPortalJump = false;
			}

		}
		// In case player dies on spikes
		if (count < playerIndex) {
			grid[row][col].setPlayerPresent(true);
		}

	}

	/**
	 * Helper method returns the next row for a state sequence in the given
	 * direction, possibly wrapping around. If the flag doPortalJump is true, then
	 * the next row will be obtained by adding the cell's row offset.
	 * 
	 * @param row          row for current cell
	 * @param col          column for current cell
	 * @param dir          direction
	 * @param doPortalJump true if the next cell should be based on a portal offset
	 *                     in case the current cell is a portal
	 * @return
	 */
	public int getNextRow(int row, int col, Direction dir, boolean doPortalJump) {
		int nextRow = 0;

		// If current cell is portal, update row by offset
		if (doPortalJump) {
			Cell c = getCell(row, col);
			nextRow = row + c.getRowOffset();
			return nextRow;
		}

		// Get row below if direction is down
		if (dir == Direction.DOWN) {
			nextRow = (row + 1) % getRows();
		}

		// Otherwise get row above if direction is up
		else if (dir == Direction.UP) {
			nextRow = ((row - 1) + getRows()) % getRows();
		}
		
		// Otherwise return row
		else {
			return row;
		}

		return nextRow;
	}

	/**
	 * Helper method returns the next column for a state sequence in the given
	 * direction, possibly wrapping around. If the flag doPortalJump is true, then
	 * the next column will be obtained by adding the cell's column offset.
	 * 
	 * @param row          row for current cell
	 * @param col          column for current cell
	 * @param dir          direction
	 * @param doPortalJump true if the next cell should be based on a portal offset
	 *                     in case the current cell is a portal
	 * @return
	 */
	public int getNextColumn(int row, int col, Direction dir, boolean doPortalJump) {
		int nextCol = 0;

		// If current cell is portal, update column by offset
		if (doPortalJump) {
			Cell c = getCell(row, col);
			nextCol = col + c.getColumnOffset();
			return nextCol;
		}

		// Get column to right if direction is right
		if (dir == Direction.RIGHT) {
			nextCol = (col + 1) % getColumns();
		}

		// Otherwise get column to the left
		else if (dir == Direction.LEFT) {
			nextCol = ((col - 1) + getColumns()) % getColumns();
		} 
		
		// Otherwise return column
		else {
			return col;
		}
		
		return nextCol;
	}

	/**
	 * Returns the number of pearls left in the grid
	 * 
	 * @return pearls remaining
	 */
	public int countPearls() {
		int pearlsLeft = 0;
		for (int i = 0; i < getRows() - 1; i++) {
			for (int j = 0; j < getColumns() - 1; j++) {
				if (grid[i][j].getState() == State.PEARL) {
					pearlsLeft++;
				}
			}
		}
		return pearlsLeft;
	}

}
