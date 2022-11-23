package api;

/**
 * Stores information about a portal for use when creating
 * a Pearls grid from a string representation.  
 * See <code>StringUtil#createFromStringArray</code>.  Portals are
 * identified in a string representation by a pair of capital 
 * letters that subsequently have to be matched to calculate the
 * offsets to the companion portal.
 * @author smkautz
 */
public class PortalInfo
{
  /**
   * Character (in the range 'A' through 'Z') identifying a portal
   */
  private final char id;
  
  /**
   * Row index for the portal.
   */
  private final int row;
  
  /**
   * Column index for the portal.
   */
  private final int col;
  
  /**
   * Constructs a PortalInfo object.
   * @param id
   *   given id
   * @param row
   *   given row index
   * @param col
   *   given column index
   */
  public PortalInfo(char id, int row, int col)
  {
    this.id = id;
    this.row = row;
    this.col = col;
  }
  
  /**
   * Returns the character identifying the portal.
   * @return
   *   id for the portal
   */
  public char getId()
  {
    return id;
  }
  
  /**
   * Returns the row for the portal.
   * @return
   *   row for the portal
   */
  public int getRow()
  {
    return row;
  }
  
  /**
   * Returns the column for the portal.
   *   column for the portal
   * @return
   *   column for the portal
   */
  public int getCol()
  {
    return col;
  }
}
