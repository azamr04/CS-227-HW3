package ui;

import api.Cell;
import api.State;
import api.MoveRecord;
import api.StringUtil;

/**
 * Descriptor for a Cell in a cell sequence that includes the 
 * actual grid row and column.
 * @author smkautz
 *
 */
public class ExtendedDescriptor
{
  private State state;
  //private Cell cell;
  private int index;
  private int newIndex;
  private boolean disappeared; 
  private int oldCol;
  private int newCol;
  private int oldRow;
  private int newRow;

  public ExtendedDescriptor(MoveRecord d)
  {
    //cell = d.getCell();
    state = d.getState();
    index = d.getIndex();
    newIndex = d.getMovedTo();
    disappeared = d.isDisappeared();
  }
  
//  public Cell getCell()
//  {
//    return cell;
//  }
  public State getState()
  {
    return state;
  }
  
  public int getIndex()
  {
    return index;
  }

  public int getNewIndex()
  {
    return newIndex;
  }

  public boolean isDisappeared()
  {
    return disappeared;
  }
  
  public void setDisappeared()
  {
    disappeared = true;
  }
  
  public int getOldRow()
  {
    return oldRow;
  }

  public int getOldCol()
  {
    return oldCol;
  }

  public int getNewRow()
  {
    return newRow;
  }
  
  public int getNewCol()
  {
    return newCol;
  }

  public void setOldRowCol(int oldRow, int oldCol)
  {
    this.oldRow = oldRow;
    this.oldCol = oldCol;
  }

  public void setNewRowCol(int newRow, int newCol)
  {
    this.newRow = newRow;
    this.newCol = newCol;
  }

  public String toString()
  {
    //String player = cell.isPlayerPresent() ? "$" : "";
    String ret = "[";
    //ret += cell.getState().toString() + player + ",";
    ret += StringUtil.getChar(state) + ",";
    ret += " old (" + oldRow + ", " + oldCol + ") ";
    ret += " new (" + newRow + ", " + newCol + ")]";
        return ret;
  }
}
