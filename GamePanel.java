
package ui;

import static api.State.CLOSED_GATE;
import static api.State.OPEN_GATE;
import static api.State.PEARL;
import static api.State.PORTAL;
import static api.State.isSpikes;
import static api.Direction.DOWN;
import static api.Direction.LEFT;
import static api.Direction.RIGHT;
import static api.Direction.UP;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import api.Cell;
import api.State;
import api.MoveRecord;
import api.Direction;
import api.StringUtil;
import hw3.Pearls;

/**
 * UI for a Pearls game. 
 */
public class GamePanel extends JPanel
{
  /**
   * Size in pixels of the cells for the grid.
   */
  public static final int CELL_SIZE = 40;
  
  /**
   * Most cells will be drawn to a size that is CELL_SIZE - 2 * PADDING
   */
  public static final int PADDING = 4;
  
  /**
   * Font size for text on grid cells.
   */
  public static final int FONT_SIZE = 20;
  
  /**
   * Indicates whether to print log messages.
   */
  public static final boolean VERBOSE = true;
  
  /**
   * Format string for pearl count.
   */
  private static final String pearlFormat = "Pearls: %2d";
  
  /**
   * Format string for move count.
   */
  private static final String moveFormat = "Moves: %2d";

  // Colors for everything
  private static final Color BACKGROUND = new Color(224, 224, 213);
  private static final Color BACKGROUND_OVER = Color.LIGHT_GRAY;
  private static final Color PLAYER_COLOR = new Color(44, 144, 249);
  private static final Color SPIKES_COLOR = new Color(255, 46, 0);
  private static final Color PEARL_COLOR = new Color(135, 249, 255);
  private static final Color PEARL_OUTLINE = new Color(0, 199, 206);
  private static final Color[] COLORS = {
    BACKGROUND, //  EMPTY, 
    Color.GRAY, //  WALL, 
    PEARL_COLOR, //  PEARL, 
    new Color(103, 209, 33), //  OPEN_GATE, 
    new Color(59, 130, 11), //  CLOSED_GATE, 
    new Color(173, 144, 38), //  MOVABLE_POS, 
    new Color(173, 144, 38), //  MOVABLE_NEG, 
    SPIKES_COLOR, //  SPIKES_LEFT, 
    SPIKES_COLOR, //  SPIKES_RIGHT, 
    SPIKES_COLOR, //  SPIKES_DOWN, 
    SPIKES_COLOR, //  SPIKES_UP, 
    SPIKES_COLOR, //  SPIKES_ALL, 
    Color.ORANGE, //  PORTAL;
  };
  
  /**
   * Indicates whether animation is currently in progress.
   */
  private boolean animating = false;
  
  /**
   * Descriptors for cells involved in a move being animated
   */
  private ExtendedDescriptor[] descriptors = null;
  
  /**
   * Actual animation segments.
   */
  private ArrayList<AnimationStep> animations = null; 
  
  /**
   * Scale up index values in descriptor list so we can interpolate easily between them.
   */
  private int animationMultiplier = 100;
  
  /**
   * The "current time" during an animation that triggers the start and end 
   * of the AnimationStep objects.
   */
  private int animationCount;
  
  /**
   * How much the animation count increases per frame, should divide
   * the animationMultiplier.
   */
  private int countPerFrame = 10;  // count per frame, should divide multiplier
  
  /**
   * Timer delay between frames during animation.
   */
  private int frameRate = 20;      // timer delay per frame
  
  /**
   * Current pearl count kept during animation as pearls are consumed.
   */
  private int currentPearls;
  
  /**
   * The game to be displayed by this panel.
   */
  private Pearls game;
  
  /**
   * Tracks whether key has already been pressed to avoid processing key repeats.
   */
  private boolean keyDown = false;
  
  // Swing components
  private JPanel scorePanel;
  private JLabel moveLabel;
  private JLabel pearlLabel;
  private Timer timer;
  
  /**
   * Constructs the component.
   */
  public GamePanel(Pearls game, JPanel scorePanel)
  {
    this.game = game;
    this.addKeyListener(new MyKeyListener());   
    this.scorePanel = scorePanel;
    
    // components for score panel  
    pearlLabel = new JLabel(String.format(pearlFormat, 0));
    pearlLabel.setOpaque(true);
    pearlLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
    scorePanel.add(pearlLabel);

    moveLabel = new JLabel(String.format(moveFormat, 0));
    moveLabel.setOpaque(true);
    moveLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
    scorePanel.add(moveLabel);
    
    // timer
    TimerCallback cb = new TimerCallback();
    timer = new Timer(frameRate, cb);
  }


  @Override
  public void paintComponent(Graphics g)
  {
    // clear background
    g.setColor(BACKGROUND);
    g.fillRect(0, 0, getWidth(), getHeight());
    
    // change backgrounds when game ends
    if (!animating && game.isOver())
    {
      g.setColor(BACKGROUND_OVER);
      g.fillRect(0, 0, getWidth(), getHeight());

      if (game.won())
      {
        scorePanel.setBackground(PEARL_COLOR);
        moveLabel.setBackground(PEARL_COLOR);
        pearlLabel.setBackground(PEARL_COLOR);
      }
      else
      {
        scorePanel.setBackground(SPIKES_COLOR);
        moveLabel.setBackground(SPIKES_COLOR);
        pearlLabel.setBackground(SPIKES_COLOR);    
      }
    }
    
    // pearl count
    if (animating)
    {
      pearlLabel.setText(String.format(pearlFormat, currentPearls));      
    }
    else
    {
      pearlLabel.setText(String.format(pearlFormat, game.getScore()));
    }
    
    // move label
    moveLabel.setText(String.format(moveFormat, game.getMoves()));
    
    // draw cells
    for (int row = 0; row < game.getRows(); ++row)
    {
      for (int col = 0; col < game.getColumns(); ++col)
      {
        Cell c = game.getCell(row, col);     
        drawCell(g, row, col, c);
      }
    }

    if (animating && animations != null && animations.size() > 0)
    {
      // first erase everything except portals, these will be filled in with animation steps
      // only do the last one if player is landing on spikes there
      State last = descriptors[descriptors.length - 1].getState();
      boolean playerDead = isSpikes(last) && descriptors[0].getNewIndex() == descriptors.length - 1;
          
      for (int i = 0; i < descriptors.length; ++i)
      {
        ExtendedDescriptor ed = descriptors[i];
        State s = ed.getState();
        if (s != PORTAL && (i < descriptors.length - 1 || playerDead))
        {
          int row = ed.getOldRow();
          int col = ed.getOldCol();
          g.setColor(BACKGROUND);
          g.fillRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
      }
      
      // then draw everything in the animation
      for (AnimationStep a : animations)
      {
        if (a != null)
        {
          Cell c = a.getCell();
          int row = a.getCurrentRow();        // this is in pixels
          int col = a.getCurrentCol();
           
          if (a.isVisible(animationCount))
          {
            if (a.disapparating())
            {
              double percent = a.percentDone(animationCount);
              drawCellPixelCoords(g, row, col, c, percent);
            }
            else if (a.apparating())
            {
              double percent = a.percentDone(animationCount);
              drawCellPixelCoords(g, row, col, c, 1 - percent);           
            }
            else
            {
              drawCellPixelCoords(g, row, col, c);
            }
          }
        }
      }      
    }
    
//    // draw grid overlay
//    g.setColor(Color.WHITE);
//    for (int row = 0; row < game.getRows(); ++row)
//    {
//      for (int col = 0; col < game.getColumns(); ++col)
//      {
//        g.drawRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
//      }
//    }

  }

  /**
   * Draws one cell, with row and column given in grid indices.
   * @param g
   * @param row
   * @param col
   * @param c
   */
  private void drawCell(Graphics g, int row, int col, Cell c)
  {
    drawCellPixelCoords(g, row * CELL_SIZE, col * CELL_SIZE, c);
  }
  
  /**
   * Draws one cell, with row and column given in pixels.
   * @param g
   * @param row
   * @param col
   * @param c
   */
  private void drawCellPixelCoords(Graphics g, int row, int col, Cell c)
  {
    drawCellPixelCoords(g, row, col, c, 0.0);
  }
  
  /**
   * Draws one cell, with row and column given in pixels.
   * @param g
   *   current graphics context
   * @param row
   *   row in pixels of upper left corner
   * @param col
   *   column in pixels of upper left corner
   * @param c
   *   Cell type
   * @param percent
   *   completion percentage for the animation segment, used for shrinking and growing
   *   the cell for apparition/disapparition
   */
  private void drawCellPixelCoords(Graphics g, int row, int col, Cell c, double percent)
  {
    // fill the cell 
    State s = c.getState();
    int padding = PADDING;
    if (s == PEARL || s == OPEN_GATE)
    {
      // pearls are smaller
      padding = PADDING * 2;
    }
    else if (c.isPlayerPresent())
    {
      // player is bigger
      padding = padding - 2;
    }
    
    // compute shrinkage
    int maxPadding = CELL_SIZE / 2;
    int actualPadding = (int) (padding + percent * (maxPadding - padding));
    
    // get the basic color
    Color color = COLORS[s.ordinal()];
    if (!animating && game.isOver() && s == State.EMPTY)
    {
      color = BACKGROUND_OVER;
    }
    g.setColor(color);
    
    // deal with special cases...
    if (c.isPlayerPresent() && s != PORTAL) 
    {
      g.setColor(PLAYER_COLOR);
      g.fillOval(col + actualPadding, row + actualPadding, CELL_SIZE - 2 * actualPadding, CELL_SIZE - 2 * actualPadding);
      ((Graphics2D) g).setStroke(new BasicStroke(1));
      g.setColor(Color.RED);
      g.drawOval(col + actualPadding, row + actualPadding, CELL_SIZE - 2 * actualPadding, CELL_SIZE - 2 * actualPadding);
    }
    else if (s == PORTAL)
    {
      // this is special because we might have to draw player in portal
      ((Graphics2D) g).setStroke(new BasicStroke(4));
      g.drawRoundRect(col + padding - 2, row + padding - 2, CELL_SIZE - 2 * padding + 4, CELL_SIZE - 2 * padding + 4, 10, 10);    
      g.setColor(Color.BLACK);
      g.fillRoundRect(col + actualPadding, row + actualPadding, CELL_SIZE - 2 * actualPadding, CELL_SIZE - 2 * actualPadding, 10, 10);
      if (c.isPlayerPresent() && !animating) 
      {
        g.setColor(PLAYER_COLOR);
        g.fillOval(col + actualPadding, row + actualPadding, CELL_SIZE - 2 * actualPadding, CELL_SIZE - 2 * actualPadding);
        ((Graphics2D) g).setStroke(new BasicStroke(1));
        g.setColor(Color.RED);
        g.drawOval(col + actualPadding, row + actualPadding, CELL_SIZE - 2 * actualPadding, CELL_SIZE - 2 * actualPadding);
      }
    }   
    else if (s == PEARL)
    {
       g.fillOval(col + actualPadding, row + actualPadding, CELL_SIZE - 2 * actualPadding, CELL_SIZE - 2 * actualPadding);
       g.setColor(PEARL_OUTLINE);
       ((Graphics2D) g).setStroke(new BasicStroke(1));
       g.drawOval(col + actualPadding, row + actualPadding, CELL_SIZE - 2 * actualPadding, CELL_SIZE - 2 * actualPadding);
    }
    else
    {
      g.fillRoundRect(col + actualPadding, row + actualPadding, CELL_SIZE - 2 * actualPadding, CELL_SIZE - 2 * actualPadding, 10, 10);
    }
    

    
    // possibly draw some text
    if (State.isSpikes(s) || State.isMovable(s))
    {
      g.setColor(Color.BLACK);
      String text = StringUtil.toString(new Cell[]{c}).trim();
      drawTextPixelCoords(g, row, col, text);
    }
  }
  
  /**
   * Draws the given string at the center of the cell at row, column given
   * as grid indices.
   * @param g
   * @param row
   * @param col
   * @param text
   */
  private void drawText(Graphics g, int row, int col, String text)
  {
    Font f = new Font(Font.SANS_SERIF, Font.PLAIN, FONT_SIZE);
    g.setFont(f);
    FontMetrics fm = g.getFontMetrics(f);
    // String text = "" + c.getCount();
    int h = fm.getHeight();
    int w = fm.stringWidth(text);
    int x = col * CELL_SIZE + CELL_SIZE / 2 - (w / 2);
    int y = row * CELL_SIZE + CELL_SIZE / 2 + (h / 2) - 2;
    g.drawString(text, x, y);
  }

  /**
   * Draws the given string at the center of the cell at row, column given
   * in pixel coordinates.
   * @param g
   * @param row
   * @param col
   * @param text
   */
  private void drawTextPixelCoords(Graphics g, int row, int col, String text)
  {
    Font f = new Font(Font.SANS_SERIF, Font.PLAIN, FONT_SIZE);
    g.setFont(f);
    FontMetrics fm = g.getFontMetrics(f);
    // String text = "" + c.getCount();
    int h = fm.getHeight();
    int w = fm.stringWidth(text);
    int x = col + CELL_SIZE / 2 - (w / 2);
    int y = row + CELL_SIZE / 2 + (h / 2) - 2;
    g.drawString(text, x, y);
  }


  /**
   * Print a string to the console if in verbose mode.
   * @param msg
   */
  private void log(String msg)
  {
    if (VERBOSE)
    {
      System.out.println(msg);
    }
  }
 
  /**
   * Attempts to figure out the actual row/column of cells from the descriptors provided by the game.
   * @param startingRow
   * @param startingCol
   * @param d
   * @param descriptors
   * @return
   */
  private ExtendedDescriptor[] setCellSequence(int startingRow, int startingCol, Direction d, MoveRecord[] descriptors)
  {
    ExtendedDescriptor[] ret = new ExtendedDescriptor[descriptors.length];
    for (int i = 0; i < descriptors.length; ++i)
    {
      ret[i] = new ExtendedDescriptor(descriptors[i]);
    }
    int row = startingRow;
    int col = startingCol;
    boolean doPortalJump = false;
    for (int i = 0; i < ret.length; ++i)
    {
      // follow the cell sequence again, but 
      // put actual row/column into each descriptor
        ret[i].setOldRowCol(row, col);
        if (ret[i].getIndex() == ret[i].getNewIndex())
        {
          ret[i].setNewRowCol(row, col);
        }
        else
        {
          // nothing other than player can go through a portal, so 
          // only one of the coordinates can change in each case
          // (note diff is always positive)
          int diff = ret[i].getNewIndex() - ret[i].getIndex();
          int rows = game.getRows();
          int cols = game.getColumns();
          switch (d)
          {
            case RIGHT: ret[i].setNewRowCol(row, (col + diff) % cols); break;
            case LEFT: ret[i].setNewRowCol(row, (col - diff + cols) % cols); break;
            case DOWN: ret[i].setNewRowCol((row + diff) % rows, col); break;
            case UP: ret[i].setNewRowCol((row - diff + rows) % rows, col); break;
          }
        }

      // if it's the first of two portals
      doPortalJump = ret[i].getState() == PORTAL && !doPortalJump;  
      int nextRow = findRow(row, col, d, doPortalJump);
      int nextCol = findCol(row, col, d, doPortalJump);
      row = nextRow;
      col = nextCol;
    }
    return ret;
  }

  /**
   * Finds the next row in cell sequence.
   * @param row
   * @param col
   * @param d
   * @param portalJump
   * @return
   */
  private int findRow(int row, int col, Direction d, boolean portalJump)
  {
    if (!portalJump)
    {
      int rows = game.getRows();
      switch(d)
      {
        case LEFT: return row;
        case RIGHT: return row;
        case UP: return (row - 1 + rows) % rows;
        case DOWN: return (row + 1) % rows;
      }
    }
    else
    {
      return row +  game.getCell(row, col).getRowOffset();
    }
    return -1;
  }
  
  /**
   * Finds the next column in cell sequence.
   * @param row
   * @param col
   * @param d
   * @param portalJump
   * @return
   */
  private int findCol(int row, int col, Direction d, boolean portalJump)
  {
    if (!portalJump)
    {
      int cols = game.getColumns();
      switch(d)
      {
        case LEFT: return (col - 1 + cols) % cols;
        case RIGHT: return (col + 1) % cols;
        case UP: return col;
        case DOWN: return col;
      }
    }
    else
    {
      return col +  game.getCell(row, col).getColumnOffset();
    }
    return -1;
  }

  /**
   * Attempts to determine whether it is possible to perform an animation based
   * on the given descriptors.
   * @param predescriptors
   * @param beforeGrid
   * @param startingRow
   * @param startingCol
   * @param dir
   * @return
   */
  private boolean sanityCheck(MoveRecord[] predescriptors, Cell[][] beforeGrid, int startingRow, int startingCol, Direction dir)
  {
    // if we can't set up animation due to errors in moves, revert to 
    // doing it without animation
    boolean animationSetupOk = true;
    
    descriptors = setCellSequence(startingRow, startingCol, dir, predescriptors);
    
//    for (ExtendedDescriptor pd : descriptors)
//    {
//      System.out.println(pd);
//      System.out.println(" " + pd.isDisappeared());
//      //System.out.println(" " + game.getCell(pd.getOldRow(), pd.getOldCol()).getState());
//    }
    
    // Descriptors consistent with before grid
    for (int i = 0; i < descriptors.length; ++i)
    {
      ExtendedDescriptor d = descriptors[i];
      State found = d.getState();
      Cell expected = beforeGrid[d.getOldRow()][d.getOldCol()];
      if (found != expected.getState())
      {
        String msg = "Descriptor at index " + i + " appears to be inconsistent with grid before move. ";
        String msg2 = "Cell sequence in direction " + dir + " starting at (" + startingRow + ", " + startingCol + 
            ") should have cell (" + d.getOldRow() + ", " + d.getOldCol() + ") with state " + expected.getState() + " at index " +  i +
            ",  found " + found + " in descriptor. ";
        log("ERROR: " + msg + msg2);
        return false;
      }
    }
    
    // Descriptor moves consistent with after grid,
    // at least for moved, non-disappeared items
    // start at 1 to avoid player
    for (int i = 1; i < descriptors.length; ++i)
    {
      ExtendedDescriptor d = descriptors[i];
      State expected = d.getState();
      int newIndex = d.getNewIndex(); // movedTo
      int newRow = descriptors[newIndex].getOldRow();
      int newCol = descriptors[newIndex].getOldCol();
      if (descriptors[i].getNewRow() != newRow ||descriptors[i].getNewCol() != newCol)
      {
        log("WARNING: internal inconsistency in sanity check at index " + i);
      }
      // if the cell has not disappeared, we should find it at the new location
      // with same state, unless it was a gate that closed
      if (i != newIndex && !d.isDisappeared())
      {
        Cell found = game.getCell(newRow, newCol);
        if (expected != found.getState() )
        {
          if (!(expected == OPEN_GATE && found.getState() == CLOSED_GATE))
          {
            String msg = "Descriptor at index " + i + " appears to be inconsistent with grid after move. ";
            String msg1 = "Descriptor shows new movedTo index " + newIndex + ". ";
            String msg2 = "Cell sequence in direction " + dir + " starting at (" + startingRow + ", " + startingCol + 
                ") should have cell (" + d.getOldRow() + ", " + d.getOldCol() + ") with state " + expected + " at index " +  i +
               " moved to (" + newRow + ", " + newCol + ") with state " + expected + " at index " +  newIndex + ", " +
                "but found " + found.getState() + ". ";

            log("ERROR: " + msg + msg1 + msg2);
            return false;
          }
        }
      }
    }
    
    // Check new player position in grid
    int newPlayerPosition = predescriptors[0].getMovedTo();
    int newPlayerRow = descriptors[newPlayerPosition].getOldRow();
    int newPlayerCol = descriptors[newPlayerPosition].getOldCol();
    int currentPlayerRow = game.getCurrentRow();
    int currentPlayerCol = game.getCurrentColumn();
    if (newPlayerRow != currentPlayerRow || newPlayerCol != currentPlayerCol)
    {
      log("ERROR: current player row/column seems to be inconsistent with cell sequence." + 
          "New player position according to getMovedTo is index " + newPlayerPosition + 
          ", which should be (" + newPlayerRow + ", " + newPlayerCol + "). getCurrentRow/Column " +
          "returns (" + currentPlayerRow + ", " + currentPlayerCol + "). "         
          );
      return false;
    }
    Cell currentPlayerCell = game.getCell(currentPlayerRow, currentPlayerCol);
    
    if (!currentPlayerCell.isPlayerPresent())
    {
      log("ERROR: current player row/column seems to be inconsistent with grid. " + 
          "getCurrentRow/Column " +
          "returns (" + currentPlayerRow + ", " + currentPlayerCol + "), but"
              + "player is not there. ");
       return false;
    }    
    
    // validate that portal transitions are ok
    // validate that there are no movable cells before the new player index
    // as reported by descriptors
    int i = 0;
    while (i < newPlayerPosition)
    {
      if (predescriptors[i].getState() == PORTAL)
      {
        if (i >= newPlayerPosition || predescriptors[i + 1].getState() != PORTAL)
        {
          log("ERROR: Portal at index " + i + " in Descriptor array is not followed by the other portal.");
          animationSetupOk = false;
          break;
        }
        else
        {
          i += 1;
        }
      }
      if (State.isMovable(predescriptors[i].getState()))
      {
        int movedTo = predescriptors[i].getMovedTo();
        if (movedTo < newPlayerPosition)
        {
          log("ERROR: Found movable cell in Descriptor array with movedTo index " + movedTo + 
              "  before new player index " + newPlayerPosition + ". ");
          animationSetupOk = false;
          break;   
        }
      }
     i += 1;
    }
    
    // make sure there are no portals after player new position, unless it's the boundary
    while (i < predescriptors.length)
    {
      if (predescriptors[i].getState() == PORTAL && i < predescriptors.length - 1)
      {
        log("ERROR: Portal at index " + i + " in Descriptor array is after new player index but does not appear to be the boundary " + newPlayerPosition + ". ");
        animationSetupOk = false;        
      }
      i += 1;
    }
    
    return animationSetupOk;
  }
  
  /**
   * God-awful complicated nonsense for setting up the animation segments.  This includes
   * player motion, disapparating and re-apparating when going through portals, motion
   * of movable blocks, possible disapparation of merged movable blocks, disappearance
   * of pearls, change of gates from open to closed, and possibly landing the player on
   * spikes. 
   * @param predescriptors
   * @param startingRow
   * @param startingCol
   * @param dir
   */
  private void setUpAnimation(MoveRecord[] predescriptors, int startingRow, int startingCol, Direction dir)
  {
    int newPlayerPosition = predescriptors[0].getMovedTo();
    animations = new ArrayList<AnimationStep>(); 

    // first, identify portal and edge transitions for the whole array

    // first index of each pair of portals, remember the sanity
    // check verified that portals occur in pairs
    ArrayList<Integer> portalTransitions = new ArrayList<>();

    // indices where we go off the edge
    ArrayList<Integer> edgeTransitions = new ArrayList<>();

    int i = 0;
    while (i < descriptors.length - 1)
    {
      // have already validated that there will be a second portal
      // and that no portals occur after player new position
      if (descriptors[i].getState() == PORTAL)
      {
        portalTransitions.add(i);
        i += 1;  // then skip the second portal
      }
      else
      {
        // check for edge transitions, note this can't happen at last cell
        int currentRow = descriptors[i].getOldRow();
        int currentCol = descriptors[i].getOldCol();
        int nextRow = descriptors[i + 1].getOldRow();
        int nextCol = descriptors[i + 1].getOldCol();
        if (dir == LEFT && nextCol > currentCol ||
            dir == RIGHT && nextCol < currentCol ||
            dir == UP && nextRow > currentRow || 
            dir == DOWN && nextRow < currentRow)
        {
          edgeTransitions.add(i);
        }
      }
      i += 1;
    }

    // ok, now have the transitions marked, make the animation steps
    // for the player.  Each portal/edge transition is the end of an animation step, 
    // and also the last cell is the end of the last sequence.  It is also possible
    // that the very first cell is a portal, so it could be both the 
    // beginning and end of an animation step. Likewise for the last.
    i = 0;
    int startIndex = 0;
    while (i <= newPlayerPosition)
    {
      if (portalTransitions.contains(i) || edgeTransitions.contains(i) || i == newPlayerPosition)
      {
        ExtendedDescriptor pd = descriptors[startIndex];
        ExtendedDescriptor pd2 = descriptors[i];
        int startRow = pd.getOldRow();
        int startCol = pd.getOldCol();
        int endRow = pd2.getOldRow();
        int endCol = pd2.getOldCol();
        int start = startIndex * animationMultiplier;
        int end = i * animationMultiplier;
        Cell c = new Cell(State.EMPTY);
        c.setPlayerPresent(true);
        boolean isEdgeOut = edgeTransitions.contains(i) && i < newPlayerPosition;
        boolean isEdgeIn = edgeTransitions.contains(i - 1);
        AnimationStep step = new AnimationStep(c, startRow, startCol, endRow, endCol, start, end, true, true, isEdgeOut, isEdgeIn, dir, animationMultiplier);
        animations.add(step);
        startIndex = i + 1;

        // account for apparating/disapparating at portals
        if (portalTransitions.contains(i))
        {
          // disapparate in first half
          start = i * animationMultiplier;
          end = i * animationMultiplier + animationMultiplier / 2;
          step = new AnimationStep(c, endRow, endCol, endRow, endCol, start, end, true, true, false, false, dir, animationMultiplier);
          step.setDisapparating();
          animations.add(step);

          // apparate in second half
          start = (i + 1) * animationMultiplier - animationMultiplier / 2;
          end = (i + 1) * animationMultiplier;
          ExtendedDescriptor pd3 = descriptors[i + 1];
          startRow = pd3.getOldRow();
          startCol = pd3.getOldCol();
          step = new AnimationStep(c, startRow, startCol, startRow, startCol, start, end, true, true, false, false, dir, animationMultiplier);
          step.setApparating();
          animations.add(step);
        }
      }
      i += 1;
    }

    // do the same for each of the movable blocks that has moved, remember
    // last cell is always a boundary
    // end time for movable block is always when the player stops
    // i.e. newPlayerPosition * animationMultiplier
    // start time for movable block is cell number - number of movable blocks
    // to left (includes self)

    // counts movable blocks to left
    int mblockCount = 0;  
    
    for (int j = 0; j < descriptors.length - 1; ++j)
    {
      ExtendedDescriptor exd = descriptors[j];

      // Note: we need to draw the movable blocks that aren't moving too!
      if (State.isMovable(exd.getState())) // && exd.getNewIndex() != exd.getIndex())
      {
        // in first segment where we found the movable block,
        // should be visible until moved
        boolean first = true;  

        State blockState = exd.getState();
        mblockCount += 1;
        int lastIndex = exd.getNewIndex();
        i = exd.getIndex();
        if (i != j) System.out.println("*****");
        startIndex = i;
        while (i <= lastIndex )
        {
          if (edgeTransitions.contains(i) || i == lastIndex)
          {
            ExtendedDescriptor pd = descriptors[startIndex];
            ExtendedDescriptor pd2 = descriptors[i];
            int startRow = pd.getOldRow();
            int startCol = pd.getOldCol();
            int endRow = pd2.getOldRow();
            int endCol = pd2.getOldCol();
            // movable cells should be one step ahead of player
            int start = (startIndex - mblockCount) * animationMultiplier;
            int end = (i - mblockCount) * animationMultiplier;
            Cell c = new Cell(blockState);
            boolean doAppear = !first;
            boolean doDisappear = exd.isDisappeared() || i < lastIndex;
            boolean isEdgeOut = edgeTransitions.contains(i) && i < lastIndex;
            boolean isEdgeIn = edgeTransitions.contains(i - 1);
            AnimationStep step = new AnimationStep(c, startRow, startCol, endRow, endCol, start, end, doDisappear, doAppear, isEdgeOut, isEdgeIn, dir, animationMultiplier);
            animations.add(step);
            startIndex = i + 1;
            first = false;

            if (i == lastIndex && exd.isDisappeared())
            {
              // put in a disapparation step when it reaches the end
              start = end;
              end = end + animationMultiplier / 2;
              startRow = endRow;
              startCol = endCol;
              doAppear = true;  
              doDisappear = true;
              isEdgeOut = false;
              isEdgeIn = false;
              step = new AnimationStep(c, startRow, startCol, endRow, endCol, start, end, doDisappear, doAppear, isEdgeOut, isEdgeIn, dir, animationMultiplier);
              step.setDisapparating();
              animations.add(step);    
            }
          }
          i += 1;
        }
      }
    }

    // Add an animation for each pearl so it disappears when player
    // (or movable block) gets to it
    // also change gates to closed while we are at it
    mblockCount = 0;  
    for (int j = 0; j < descriptors.length - 1; ++j)
    {
      ExtendedDescriptor exd = descriptors[j];

      if (exd.getState() == PEARL)
      {
        // make an animation step that will just become non visible after end time
        int startRow = exd.getOldRow();
        int startCol = exd.getOldCol();
        int endRow = startRow;
        int endCol = startCol;
     
        // special case for pearls, since we might pass the same pearl twice,
        // we only want to animate it the first time.
        boolean found = false;
        for (AnimationStep a : animations)
        {
          if (a.getCell().getState() == PEARL && 
              a.getStartRow() == startRow && 
              a.getStartCol() == startCol &&
              a.disapparating())
          {
            found = true;
            break;
          }
        }
        
        if (!found)
        {
          // subtracting 1 to disappear one step in advance of player/block
          int start = (j - mblockCount - 1) * animationMultiplier;         
          int end = (j - mblockCount) * animationMultiplier - animationMultiplier / 2;         

          Cell c = new Cell(PEARL);
          boolean doAppear = false;  // should be visible prior to animation
          boolean doDisappear = exd.isDisappeared();
          boolean isEdgeOut = false;
          boolean isEdgeIn = false;
          AnimationStep step = new AnimationStep(c, startRow, startCol, endRow, endCol, start, end, doDisappear, doAppear, isEdgeOut, isEdgeIn, dir, animationMultiplier);
          if (exd.isDisappeared()) 
          {
            step.setDisapparating();
          }
          animations.add(step);    
        }
      }
      else if (exd.getState() == OPEN_GATE)
      {
        // make an animation step that will just become non visible after end time
        int startRow = exd.getOldRow();
        int startCol = exd.getOldCol();
        int endRow = startRow;
        int endCol = startCol;

        int start = j * animationMultiplier;
        int end = j * animationMultiplier;
        Cell c = new Cell(OPEN_GATE);
        boolean doAppear = false;  // should be visible before animation starts
        boolean isEdgeOut = false;
        boolean isEdgeIn = false;
        AnimationStep step = new AnimationStep(c, startRow, startCol, endRow, endCol, start, end, true, doAppear, isEdgeOut, isEdgeIn, dir, animationMultiplier);
        // put these at the beginning, we want them drawn before player
        animations.add(0, step);  

        // only do this if the game really closed the gate
        if (game.getCell(exd.getOldRow(), exd.getOldCol()).getState() == CLOSED_GATE)
        {
          c = new Cell(CLOSED_GATE);
          doAppear = true;  // should be visible only after animation starts
          boolean doDisappear = false;
          step = new AnimationStep(c, startRow, startCol, endRow, endCol, start, end, doDisappear, doAppear, isEdgeOut, isEdgeIn, dir, animationMultiplier);
          animations.add(0, step); 
        }
      }           
      else if (State.isMovable(exd.getState()))
      {
        mblockCount += 1;
      }
    }

    // if player is dying on spikes, add a step for that too.
    ExtendedDescriptor exd = descriptors[descriptors.length - 1];
    State c = exd.getState();
    if (State.isSpikes(c) && descriptors[0].getNewIndex() == descriptors.length - 1)
    {
      int startRow = exd.getOldRow();
      int startCol = exd.getOldCol();
      int endRow = startRow;
      int endCol = startCol;

      // this one will display the spikes before player gets there
      int start = (descriptors.length - 1) * animationMultiplier;
      int end = start + animationMultiplier;
      boolean doAppear = false;  // should be visible before animation starts
      boolean doDisappear = true;
      boolean isEdgeOut = false;
      boolean isEdgeIn = false;
      Cell x = new Cell(c);
      AnimationStep step = new AnimationStep(x, startRow, startCol, endRow, endCol, start, end, doDisappear, doAppear, isEdgeOut, isEdgeIn, dir, animationMultiplier);
      animations.add(step);        

      // this one turns in to the spikes-with-player when player gets there
      doAppear = true;  // should not be visible before animation starts
      doDisappear = false;
      x = new Cell(c);
      x.setPlayerPresent(true);
      step = new AnimationStep(x, startRow, startCol, endRow, endCol, start, end, doDisappear, doAppear, isEdgeOut, isEdgeIn, dir, animationMultiplier);
      step.setApparating();
      animations.add(step);             
    }
  }
  
  /**
   * Handler for key events.
   */
  private class MyKeyListener implements KeyListener
  {
    @Override
    public void keyPressed(KeyEvent event)
    {
      int key = event.getKeyCode();
      Direction dir = null;
      String msg = "";
      switch( key ) { 
        case KeyEvent.VK_UP:
          msg = "INFO: Key UP";
          dir = Direction.UP;
          break;
        case KeyEvent.VK_DOWN:
          msg = "INFO: Key DOWN";
          dir = Direction.DOWN;
          break;
        case KeyEvent.VK_LEFT:
          msg = "INFO: Key LEFT";
          dir = Direction.LEFT;
          break;
        case KeyEvent.VK_RIGHT :
          msg = "INFO: Key RIGHT";
          dir = Direction.RIGHT;
          break;
        default:
          return;
      }

      // Ignore key repeats
      if (keyDown || animating || game.isOver()) 
      {
        return;
      }      
      keyDown = true;
      
      log(msg);
      
      // before the move, save some data
      int startingRow = game.getCurrentRow();
      int startingCol = game.getCurrentColumn();
      currentPearls = game.getScore();
      Cell[][] beforeGrid = new Cell[game.getRows()][game.getColumns()];
      for (int row = 0; row < beforeGrid.length; ++row)
      {
        for (int col = 0; col < beforeGrid[0].length; ++col)
        {
          beforeGrid[row][col] = new Cell(game.getCell(row, col));
        }
      }
      
      // do the move
      MoveRecord[] predescriptors = game.move(dir);

      if (predescriptors == null)
      {
        // nothing else to do
        log("INFO: No Descriptor array returned, no animation performed. ");
        repaint();
        return;
      }
      
      // basic checks for whether we can animate
      if (predescriptors.length <= 1)
      {
        // nothing else to do
        log("INFO: Descriptor array length 0 or 1, no animation performed. ");
        repaint();
        return;
      }      
      if (predescriptors[0].getMovedTo() == 0)
      {
        // nothing else to do
        log("INFO: First Descriptor shows no player motion , no animation performed. ");
        repaint();
        return;
      }      
      
      // More extensive sanity check,
      // this method also fills in the ExtendedDescriptors (incl. instance variable 'descriptors')
      boolean animationSetupOk = sanityCheck(predescriptors,beforeGrid, startingRow, startingCol, dir);

      if (!animationSetupOk)
      {
        log("INFO: Descriptor array failed sanity check, no animation performed. ");
        repaint();
        return;      
      }

      setUpAnimation(predescriptors, startingRow, startingCol, dir);
      animationCount = 0;
      animating = true;
      timer.start();      
    }

    @Override
    public void keyReleased(KeyEvent event)
    {
      if (!keyDown)
      {
        return;
      }
      keyDown = false;
    }

    @Override
    public void keyTyped(KeyEvent event)
    {
      // not used
    }
  }

  
  /**
   * Listener for timer events.  The actionPerformed method
   * is invoked each time the timer fires and the call to
   * repaint() at the bottom of the method causes the panel
   * to be redrawn.
   */
  private class TimerCallback implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent arg0)
    {
      animationCount += countPerFrame;
      int doneCount = 0;
      for (AnimationStep a : animations)
      {
        a.step(animationCount);
        if (a.done(animationCount))
        {
          doneCount += 1;
        }
        // did we just pass a pearl
        if (a.getCell().getState() == PEARL && a.done(animationCount) && !a.done(animationCount - countPerFrame))
        {
          currentPearls += 1;
        }
      }
      
      if (doneCount == animations.size())
      {
        timer.stop();
        animations = null;
        animating = false;
      }
      
      repaint();
    }
  }


}
