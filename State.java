package api;


/**
 * Possible cell states for the Pearls game.
 */
public enum State
{ 
  // WARNING: if we change these, be sure to update the TEXT array too!
  EMPTY, 
  WALL, 
  PEARL, 
  OPEN_GATE, 
  CLOSED_GATE, 
  MOVABLE_POS, 
  MOVABLE_NEG, 
  SPIKES_LEFT, 
  SPIKES_RIGHT, 
  SPIKES_DOWN, 
  SPIKES_UP, 
  SPIKES_ALL, 
  PORTAL;
  

  
  /**
   * Returns true if the given state represents a movable block.
   * @param s
   *   a State value
   * @return
   *   true if the given state is one of the two movable block types
   */
  public static boolean isMovable(State s)
  {
    return s == MOVABLE_POS || s == MOVABLE_NEG;
  }
  
  /**
   * Returns true if both given states are movable and have opposite parity.
   * @param s1
   *   a State value
   * @param s2
   *   a State value
   * @return
   *   true if both given states are movable and have opposite parity
   */
  public static boolean canMerge(State s1, State s2)
  {
    return s1 == MOVABLE_POS && s2 == MOVABLE_NEG ||
           s2 == MOVABLE_POS && s1 == MOVABLE_NEG;
  }
  
  /**
   * Returns true if the given state is some form of spikes.
   * @param s
   *   a State
   * @return
   *   true if the given state is one of the five types of spikes
   */
  public static boolean isSpikes(State s)
  {
    return s == SPIKES_LEFT || 
        s == SPIKES_RIGHT ||
        s == SPIKES_DOWN ||
        s == SPIKES_UP ||
        s == SPIKES_ALL;
  }
  
  
  /**
   * Returns true if the given state is spikes pointing in
   * the direction opposite the given <code>Direction</code>,
   * or if the given state is SPIKES_ALL.
   * @param s
   *   a State value
   * @param dir
   *   a <code>Direction</code>
   * @return
   *   true if player moving in given direction would be impaled
   *   on the given state
   */ 
  public static boolean spikesAreDeadly(State s, Direction dir)
  {
    return s == SPIKES_ALL ||
        s == SPIKES_DOWN && dir == Direction.UP ||
        s == SPIKES_LEFT && dir == Direction.RIGHT ||
        s == SPIKES_RIGHT && dir == Direction.LEFT ||
        s == SPIKES_UP && dir == Direction.DOWN;
  }

  /**
   * Determines whether the given state should be considered a boundary
   * when constructing a state sequence for a move.  The "boundary" 
   * is the last cell in the sequence, beyond which nothing will move.
   * For motion of the player, the possible boundaries are CLOSED_GATE, WALL, 
   * or any form of spikes.  However, movable blocks cannot pass through gates or 
   * portals, so if there were any movable blocks encountered previously
   * in constructing the sequence, then OPEN_GATE and PORTAL would also be 
   * considered boundary states.
   * @param s
   *   any State value
   * @param containsMovable
   *   true if OPEN_GATE and PORTAL should also be treated as boundary states, 
   *   false otherwise
   * @return
   *   true if the given state is a boundary when constructing a state sequence
   */
  public static boolean isBoundary(State s, boolean containsMovable)
  {
    if (!containsMovable)
    {
      return s == CLOSED_GATE ||
          s == SPIKES_LEFT ||
          s == SPIKES_RIGHT ||
          s == SPIKES_DOWN ||
          s == SPIKES_UP ||
          s == SPIKES_ALL ||
          s == WALL;
    }
    else
    {
      return s == CLOSED_GATE ||
          s == SPIKES_LEFT ||
          s == SPIKES_RIGHT ||
          s == SPIKES_DOWN ||
          s == SPIKES_UP ||
          s == SPIKES_ALL ||
          s == WALL ||

          // movable blocks can't go through gates or portals,
          // so these also count as boundary states when
          // there is a movable block
          s == OPEN_GATE ||
          s == PORTAL;
    }
  }

}
