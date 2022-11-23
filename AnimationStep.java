package ui;

import api.Cell;
import api.Direction;

/**
 * Data for one segment of an animation.
 * The overall duration of the animation is based on the number
 * of cells traversed by the player.  The total duration is represented in units
 * of index * animationMultiplier, where 'index' is normally the index of a cell
 * within the descriptor list returned from the game.  Each animation step has a 
 * start time and end time, measured in these units.  AnimationSteps can be used
 * for actual motion, but can also be used to perform other action based on 
 * a percentage of completion from start time to end time.  The flags
 * for apparate and disapparate are provided to allow a UI to detect whether
 * to use the AnimationStep for this purpose.  It also makes sense to use
 * an AnimationStep whose start time and end time are the same, since
 * there are flags to indicate that the cell should "appear" (invisible before
 * start time) or "disappear" (invisible after end time).
 * @author smkautz
 *
 */
public class AnimationStep
{

  private Cell cell;
  private int startRow;
  private int startCol;
  private int endRow;
  private int endCol;
  private int start;
  private int end;
  private int duration;
  private boolean disappear;
  private boolean appear;
  private double currentRow;
  private double currentCol;
  private double rowStep;
  private double colStep;
  private boolean disapparating;
  private boolean apparating;
  
  /**
   * Constructor.  
   * @param cell 
   *   the Cell being moved
   * @param sr
   *   starting row
   * @param sc
   *   starting column
   * @param er
   *   ending row
   * @param ec
   *   ending column
   * @param start
   *   start time, in units of index * animationMultiplier
   * @param end
   *   end time, in units of index * animationMultiplier
   * @param disappear
   *   if true, cell becomes invisible after end time
   * @param appear
   *   if true, cell is invisible before end time
   * @param edgeOut
   *   if true, add an additional time slot to move off the grid at end
   * @param edgeIn
   *   if true, add an additional time slot to move into the grid at beginning
   * @param dir
   *   direction of the move (needed only for edgeOut or edgeIn calculation)
   * @param multiplier
   *   scale for animation units
   */
  public AnimationStep(Cell cell, int sr, int sc, int er, int ec, int start, int end, boolean disappear, boolean appear, boolean edgeOut, boolean edgeIn, Direction dir, int multiplier)
  {
    this.cell = cell;
    // account for going off edge
    if (edgeOut)
    {
      end += multiplier;
      switch (dir)
      {
        case LEFT: ec -= 1; break;
        case RIGHT: ec += 1; break;
        case UP: er -= 1; break;
        case DOWN: er += 1; break;
      }
    }
    if (edgeIn)
    {
      start -= multiplier;
      switch (dir)
      {
        case LEFT: sc += 1; break;
        case RIGHT: sc -= 1; break;
        case UP: sr += 1; break;
        case DOWN: sr -= 1; break;
      }
    }
    
    //convert to pixels
    startRow = sr * GamePanel.CELL_SIZE;
    startCol = sc * GamePanel.CELL_SIZE;
    endRow = er * GamePanel.CELL_SIZE;
    endCol = ec * GamePanel.CELL_SIZE;
    currentRow = startRow;
    currentCol = startCol;
    this.start = start;
    this.end = end;
    duration = end - start;  // units of animation count
    this.disappear = disappear;
    this.appear = appear;
    
    rowStep = 0;
    colStep = 0;
    if (startRow != endRow)
    {
      rowStep = duration == 0 ? 0 : ((double) endRow - startRow) / duration;
    }
    if (startCol != endCol)
    {
      colStep = duration == 0 ? 0 : ((double) endCol - startCol) / duration;
    }
  }
  
  public Cell getCell()
  {
    return cell;
  }
  
  public int getStartRow()
  {
    return startRow / GamePanel.CELL_SIZE;
  }
  
  public int getStartCol()
  {
    return startCol / GamePanel.CELL_SIZE;
  }
  
  public int getCurrentRow()
  {
    return (int) Math.round(currentRow);
  }
  
  public int getCurrentCol()
  {
    return (int) Math.round(currentCol);
  }
  
  public boolean isVisible(int animationCount)
  {
    return !(animationCount >= end && disappear || animationCount < start && appear);
  }
  
  public void step(int animationCount)
  {   
    if (animationCount > end)
    {
      currentCol = endCol;
      currentRow = endRow;
    }
    else if (animationCount >= start)
    {
      double fraction = ((double) animationCount - start) / duration;
      currentCol = startCol + fraction * colStep * duration;
      currentRow = startRow + fraction * rowStep * duration;
     }

  }
  
  public double percentDone(int animationCount)
  {
    if (animationCount > end)
    {
      return 1;
    }
    else if (animationCount >= start)
    {
      return ((double) animationCount - start) / duration;
    }
    else
    {
      return 0;
    }    
  }
  
  public boolean done(int animationCount)
  {
    return animationCount >= end;
  }
  
  public void setDisapparating()
  {
    disapparating = true;
  }
  
  public void setApparating()
  {
    apparating = true;
  }
  
  public boolean disapparating()
  {
    return disapparating;
  }
  
  public boolean apparating()
  {
    return apparating;
  }

}
