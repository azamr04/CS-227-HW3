package api;

/**
 * Represents one cell of the Pearls game grid.  A cell always
 * has a state, represented by one of the <code>State</code>
 * constants, and has an attribute indicating whether or not the
 * player is present in the cell.  There are also two attributes,
 * a row offset and a column offset, that are only meaningful when
 * the Cell's state is PORTAL.  (The offsets give the vertical and
 * horizontal shift to the companion portal.)
 * @author smkautz
 */
public class Cell
{
  /**
   * State of this cell.
   */
  private State state;
  
  /**
   * In case of a PORTAL cell, the amount to be added to the
   * column index to reach the companion portal.  If the 
   * state is not PORTAL, this value is meaningless.
   */
  private int colOffset; 
  
  /**
   * In case of a PORTAL cell, the amount to be added to the
   * row index to reach the companion portal.  If the 
   * state is not PORTAL, this value is meaningless.
   */
  private int rowOffset;
 
  /**
   * Indicates whether the player is currently present in this cell.
   */
  private boolean playerPresent;
  
  /**
   * Constructs a Cell with the given state.  Initially the
   * row and column offsets are zero and player is not present.
   * @param givenState
   *   state for this Cell
   */
  public Cell(State givenState)
  {
    state = givenState;
  }
  
  /**
   * Constructs a copy of the given Cell.
   * @param existingCell
   *   original to be copied
   */
  public Cell(Cell existingCell)
  {
    state = existingCell.state;
    colOffset = existingCell.colOffset;
    rowOffset = existingCell.rowOffset;
    playerPresent = existingCell.playerPresent;
  }
  
  /**
   * Returns the state for this cell.
   * @return
   *   state for this cell
   */
  public State getState()
  {
    return state;
  }

  /**
   * Sets the state for this cell.
   * @param givenState
   *   new state for this cell
   */
  public void setState(State givenState)
  {
    state = givenState;
  }
  
  /**
   * Returns the column offset for this cell.
   * @return
   *   column offset for this cell
   */
  public int getColumnOffset()
  {
    return colOffset;
  }
  
  /**
   * Returns the row offset for this cell.
   * @return
   *   row offset for this cell
   */
  public int getRowOffset()
  {
    return rowOffset;
  }
  
  /**
   * Sets the row and column offsets for this cell.
   * @param rowOffset
   *   given row offset
   * @param colOffset
   *   given column offset
   */
  public void setOffsets(int rowOffset, int colOffset)
  {
    this.colOffset = colOffset;
    this.rowOffset = rowOffset;
  }
  
  /**
   * Determines whether or not the player is present in this cell.
   * @return
   *   true if player is present, false otherwise
   */
  public boolean isPlayerPresent()
  {
    return playerPresent;
  }
  
  /**
   * Sets whether or not the player is present in this cell.
   * @param isPresent
   *   true if player is present, false otherwise
   */
  public void setPlayerPresent(boolean isPresent)
  {
    playerPresent = isPresent;
  }
  
  /**
   * Returns a string representation of this Cell.
   */
  public String toString()
  {
    String player = isPlayerPresent() ? " ($)" : "";
    String offsets = "";
    if (rowOffset != 0 || colOffset != 0)
    {
      offsets = " offsets (" + rowOffset + ", " + colOffset + ")";
    }
    String ret = "[";
    ret += StringUtil.getChar(state) + player + offsets + "]";
    return ret;
  }
}
