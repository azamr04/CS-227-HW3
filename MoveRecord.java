
package api;

/**
 * A MoveRecord encapsulates a State along with information about how it might
 * have been shifted or modified during a move in the game.
 * @author smkautz
 */
public class MoveRecord
{
  /**
   * Original state to be described by this MoveRecord.
   */
  private final State state;
  
  /**
   * Original index for the state within a state sequence array.
   */
  private final int index;
  
  /**
   * Updated index for the state if it was moved.  (Note: when the original
   * index is zero, this value is always the new index of the player, not 
   * of the state at index 0.)
   */
  private int movedTo;
  
  /**
   * Indicates whether the state was removed by the move.
   */
  private boolean disappeared;

  /**
   * Indicates whether the state was an open gate that was closed by the move.
   */
  private boolean closed;

  /**
   * Constructs a MoveRecord from the given State and index.  Normally the 
   * index is the same as the cell's array index within a valid state sequence.
   * The movedTo index is initially the same as the given index and the
   * disappeared status is false.  Index 0 is always the player's initial
   * location.
   * @param givenState
   *   State for this MoveRecord
   * @param currentIndex
   *   index for this MoveRecord
   */
  public MoveRecord(State givenState, int currentIndex)
  {
    state = givenState;
    index = currentIndex;
    movedTo = index;
    disappeared = false;
  }
  
  /**
   * Returns the state in this MoveRecord.
   * @return
   *   state in this MoveRecord
   */
  public State getState()
  {
    return state;
  }

  /**
   * Returns the index in this MoveRecord.
   * @return
   *   index in this MoveRecord
   */
  public int getIndex()
  {
    return index;
  }

  /**
   * Sets the new index to indicate a state moved to a new location within 
   * a state sequence.  In the case of a cell containing the player, the
   * new index indicates the new location of the player in the sequence.
   * @param newIndex
   *   new index for the current cell (or player)
   */
  public void setMovedToIndex(int newIndex)
  {
    this.movedTo = newIndex;
  }
  
  /**
   * Returns the (possibly) new index for this MoveRecord's cell.
   * @return
   *   new index for this MoveRecord's cell
   */
  public int getMovedTo()
  {
    return movedTo;
  }

  /**
   * Determines whether this cell has been given a new index.
   * @return
   *   true if the new index differs from the original index
   */
  public boolean isMoved()
  {
    return movedTo != index;
  }
  
  /**
   * Determines whether this state was deleted.
   * @return
   *   true if this state was deleted, false otherwise
   */
  public boolean isDisappeared()
  {
    return disappeared;
  }
  
  /**
   * Sets the disappeared status of this MoveRecord.
   */
  public void setDisappeared()
  {
    disappeared = true;
  }

  /**
   * Determines whether this state is an open gate that was closed by a move.
   * @return
   *   true if this state was closed
   */
  public boolean isClosed()
  {
    return closed;
  }
  
  /**
   * Sets the closed status of this MoveRecord.
   */
  public void setClosed()
  {
    closed = true;
  }
  
  /**
   * Returns a string representation of this MoveRecord.
   */
  public String toString()
  {
    String moved = index != movedTo ? " moved to " + movedTo : "";
    String invisible = disappeared ? " disappeared" : "";
    String closedGate = (state == State.OPEN_GATE && closed) ? " closed" : "";
    String ret = "[";
    ret += StringUtil.getChar(state) + moved + invisible + closedGate + "]";
    return ret;
  }
  
}
