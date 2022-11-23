package api;
import static api.State.*;

import java.util.ArrayList;

import hw3.Pearls;

/**
 * Utilities for Strings to represent grid and cell information 
 * in the Pearls game.
 * @author smkautz
 */
public class StringUtil
{
  /**
   * Basic character representations of cell states, corresponding to State values.
   */
  public static final char[] TEXT = {
    '.',  //  EMPTY, 
    '#', //  WALL, 
    '@', //  PEARL, 
    'o', //  OPEN_GATE, 
    'x', //  CLOSED_GATE, 
    '+', //  MOVABLE_POS, 
    '-', //  MOVABLE_NEG, 
    '<', //  SPIKES_LEFT, 
    '>', //  SPIKES_RIGHT, 
    'v', //  SPIKES_DOWN, 
    '^', //  SPIKES_UP, 
    '*', //  SPIKES_ALL, 
    'O'//  PORTAL;
  };

  /**
   * Character for representing the player position in the grid.
   */
  public static final char PLAYER_CHAR = '$';
  
  /**
   * Character for representing the player position in the grid,
   * when the player lands on a portal
   */
  public static final char PLAYER_IN_PORTAL_CHAR = '0';
  
  /**
   * Character for representing the player position in the grid,
   * when the player lands on spikes.
   */
  public static final char PLAYER_IN_SPIKES_CHAR = '!';
  
  /**
   * Character for representing the player position in the grid,
   * when the player lands on a gate.
   */
  public static final char PLAYER_IN_GATE_CHAR = '%';
  
  /**
   * Character to represent a null state in string conversions.
   */
  public static final char NULL_CHAR = 'n';
  

  /**
   * Returns the basic character for a state.  Returns NULL_CHAR if the
   * given state is null.
   * @param s
   *   a State
   * @return
   *   character for the given state
   */
  public static char getChar(State s)
  {
    if (s == null)
    {
      return NULL_CHAR;
    }
    else
    {
      return TEXT[s.ordinal()];
    }
  }
  
  
  /**
   * Returns the State corresponding to the given character,
   * if possible.  Any capital letter in the range 'A' - 'Z'
   * is treated as a portal.
   * @param c
   *   given character
   * @return
   *   State corresponding to the given character, of null if
   *   the character does not correspond to a State
   */
  public static State getValue(char c)
  {
    int i;
    for (i = 0; i < TEXT.length; ++i)
    {
      if (TEXT[i] == c)
      {
        break;
      }
    }
    if (i < TEXT.length)
    {
      return State.values()[i];
    }
    else if (c >= 'A' && c <= 'Z')
    {
      return State.PORTAL;  
    }
    else return null;
  }
  
  /**
   * Converts a state array to a string representation, using the character
   * representations defined in this class, with an extra space 
   * inserted between characters. A null state will be represented by the 
   * character NULL_CHAR.
   * @param arr
   *   array of State
   * @return
   *   string representation of the State array
   */
  public static String toString(State[] arr)
  {
    return toString(arr, true);
  }
  
  /**
   * Converts a state array to a string representation, using the character
   * representations defined in this class, where an extra space is optionally
   * inserted between characters.  A null state will be represented by the 
   * character NULL_CHAR.
   * @param arr
   *   array of State
   * @param addSpaces
   *   if true, method inserts a space between characters
   * @return
   *   string representation of the State array
   */
  public static String toString(State[] arr, boolean addSpaces)
  {
    String text = "";
    for (int col = 0; col < arr.length; ++col)
    {
      State s = arr[col];
      char ch = getChar(s);
      text += ch;
      if (addSpaces && col < arr.length - 1)
      {
        text += " ";
      }
    }
    return text;
  }
  
  /**
   * Initializes a cell grid from an array of strings. All strings must be
   * the same length and may include the characters from TEXT, the 
   * PLAYER_CHAR, and capital letters 'A' - 'Z'.  Spaces are also treated
   * as empty cells.  Each string represents one row of the grid.
   * Capital letters represent portals, and therefore 
   * any capital letters must occur exactly twice in the
   * string array.  This method will identify pairs of portals and calculate
   * the correct row and column offsets.
   * @param strings
   *   array of strings representing an initial game grid
   * @return
   *   2D array of cell object constructed from the string array
   */
  public static Cell[][] createFromStringArray(String[] strings)
  {
    int width = strings[0].length();
    int height = strings.length;
    Cell[][] grid = new Cell[height][width];
    ArrayList<PortalInfo> portals = new ArrayList<>();
    for (int row = 0; row < height; row += 1)
    {
      for (int col = 0; col < width; col += 1)
      {
        char ch = strings[row].charAt(col);
        State state;
        if (ch >= 'A' && ch <= 'Z')
        {
          // it's a portal
          portals.add(new PortalInfo(ch, row, col));
          grid[row][col] = new Cell(PORTAL);     
        }
        else if (ch == '$')
        {
          // player initial position
          grid[row][col] = new Cell(EMPTY);  
          grid[row][col].setPlayerPresent(true);          
        }
        else
        {
          state = getValue(ch);
          if (state != null)
          {
            grid[row][col] = new Cell(state);     
          }
          else
          {
            grid[row][col] = new Cell(EMPTY);     
          }
        }
      }
    }
    
    // fill in the offsets for the portals
    for (int i = 0; i < portals.size(); ++i)
    {
      String ids = "";
      PortalInfo p1 = portals.get(i);
      char id = p1.getId();
      if (ids.indexOf(id) < 0)
      {
        ids += id;
        for (int j = i + 1; j < portals.size(); ++j)
        {
          PortalInfo p2 = portals.get(j);
          if (p2.getId() == id)
          {
            int r1 = p1.getRow();
            int c1 = p1.getCol();
            int r2 = p2.getRow();
            int c2 = p2.getCol();
            grid[r1][c1].setOffsets(r2 - r1, c2 - c1);
            grid[r2][c2].setOffsets(r1 - r2, c1 - c2);
          }       
        }
      }
      
    }
    
    return grid;
  }
  

  /**
   * Converts a grid to a string array representation using the characters 
   * defined in this class.  All portals will be represented as the 'O'
   * character.  The returned array has one string per row of the grid.
   * @param grid
   *   given 2D array of cells
   * @return
   *   string representation of the given grid
   */
  public static String[] toStringArray(Cell[][] grid)
  {
    int rows = grid.length;
    String[] ret = new String[rows];
    for (int row = 0; row < rows; ++row)
    {
      ret[row] = toString(grid[row]);
    }
    
    return ret;
  }
  
  /**
   * Converts the given game's grid to a string array representation using the characters 
   * defined in this class.  All portals will be represented as the 'O'
   * character.  The returned array has one string per row of the grid.
   * @param game
   *   game instance
   * @return
   *   string representation of the game's grid
   */

  public static String[] toStringArray(Pearls game)
  {
    Cell[][] grid = new Cell[game.getRows()][game.getColumns()];
    for (int row = 0; row < game.getRows(); ++row)
    {
      for (int col = 0; col < game.getColumns(); ++col)
      {
        grid[row][col] = game.getCell(row, col);
      }
    }
    return toStringArray(grid);
  }
  
  /**
   * Creates a cell array from a string description, using the character
   * representations defined in this class.  This method does not
   * calculate portal offsets.
   * @param text
   *   given string
   * @return
   *   Cell array constructed from the string 
   */
  public static Cell[] createCellsFromString(String text)
  {
    Cell[] ret = new Cell[text.length()];

    for (int col = 0; col < text.length(); col += 1)
    {
      char ch = text.charAt(col);
      State state;
      if (ch >= 'A' && ch <= 'Z')
      {
        // NOTE: this will not set up the offsets to companion portal!
        ret[col] = new Cell(PORTAL);
      }
      else if (ch == PLAYER_CHAR)
      {
        // player initial position
        ret[col] = new Cell(EMPTY);  
        ret[col].setPlayerPresent(true);          
      }
      else
      {
        state = getValue(ch);
        if (state != null)
        {
          ret[col] = new Cell(state);     
        }
      }
    }
    return ret;
  }

  /**
   * Creates a cell state array from a string description, using the character
   * representations defined in this class.  The player position is 
   * ignored. PLAYER_IN_GATE_CHAR is rendered as an open gate, and
   * PLAYER_IN_SPIKES_CHAR is rendered as SPIKES_ALL character.
   * @param text
   *   given string
   * @return
   *   Cell array constructed from the string 
   */
  public static State[] createFromString(String text)
  {
    text = text.replace(" ", "");
    State[] ret = new State[text.length()];

    for (int col = 0; col < text.length(); col += 1)
    {
      char ch = text.charAt(col);
      State state;
      if ((ch >= 'A' && ch <= 'Z') || ch == PLAYER_IN_PORTAL_CHAR)
      {
        // NOTE: this will not set up the offsets to companion portal!
        ret[col] = PORTAL;
      }
      else if (ch == PLAYER_CHAR)
      {
        // player initial position
        ret[col] = EMPTY;  
      }
      else if (ch == PLAYER_IN_GATE_CHAR)
      {
        ret[col] = OPEN_GATE;
      }
      else if (ch == PLAYER_IN_SPIKES_CHAR)
      {
        ret[col] = SPIKES_ALL;
      }
      
      else
      {
        state = getValue(ch);
        if (state != null)
        {
          ret[col] = state;     
        }
      }
    }
    return ret;
  }
  
  
  /**
   * Converts a cell state array to a string representation, using the character
   * representations defined in this class, including the player position.
   * @param arr
   *   array of State
   * @return
   *   string representation of the State array
   * @param playerIndex
   *   index within the array where a symbol for the player should appear
   */
  public static String toString(State[] arr, int playerIndex)
  {
    String text = "";
    for (int col = 0; col < arr.length; ++col)
    {
      State s = arr[col];
      char ch = getChar(s);
      if (col == playerIndex)
      {
        if (s == PORTAL)
        {
          ch = PLAYER_IN_PORTAL_CHAR;
        }
        else if (s.toString().startsWith("SPIKES"))
        {
          ch = PLAYER_IN_SPIKES_CHAR;
        }
        else if (s.toString().endsWith("GATE"))
        {
          ch = PLAYER_IN_GATE_CHAR;
        }
        else
        {
          ch = PLAYER_CHAR;
        }
      }
      text += ch;
    }

    return text;
  }
  
  
  /**
   * Converts a cell array to a string representation, using the character
   * representations defined in this class.
   * @param arr
   *   array of Cells
   * @return
   *   string representation of the Cell array
   */
  public static String toString(Cell[] arr)
  {
    String text = "";
    for (int col = 0; col < arr.length; ++col)
    {
      State s = arr[col].getState();
      char ch = getChar(s);
      if (arr[col].isPlayerPresent())
      {
        if (s == PORTAL)
        {
          ch = PLAYER_IN_PORTAL_CHAR;
        }
        else if (s.toString().startsWith("SPIKES"))
        {
          ch = PLAYER_IN_SPIKES_CHAR;
        }
        else if (s.toString().endsWith("GATE"))
        {
          ch = PLAYER_IN_GATE_CHAR;
        }
        else
        {
          ch = PLAYER_CHAR;
        }
      }
      text += ch;
    }

    return text;
  }
  
  /**
   * Prints an array of Cells using the character representations
   * defined in this class.
   * @param cells
   *   array of cells
   */
  public static void printCellArray(Cell[] cells)
  {
    String s = StringUtil.toString(cells);
    printString(s);
  }
  
  /**
   * Prints an array of States using the character representations
   * defined in this class.
   * @param states
   *   array of States
   * @param playerIndex
   *   index within the array where a character for the player should appear
   */
  public static void printStateArray(State[] states, int playerIndex)
  {
    String s = StringUtil.toString(states, playerIndex);
    printString(s);
  }
  
  /**
   * Prints a string with an extra space between characters.
   * @param s
   *   any string
   */
  public static void printString(String s)
  {
    String toPrint = "";
    for (int i = 0; i < s.length() - 1; ++i)
    {
      toPrint += s.charAt(i) + " ";
    }
    toPrint += s.charAt(s.length() - 1);
    System.out.println(toPrint);    
  }
  
  /**
   * Prints out a game's grid.
   * @param g
   *   game instance
   */
  public static void printGrid(Pearls g)
  {
    String[] strings = StringUtil.toStringArray(g);
    printGrid(strings);
  }
  
  /**
   * Prints a string array as a 2D grid.
   * @param strings
   *   array of strings, one per row of the grid
   */
  public static void printGrid(String[] strings)
  {
    for (String s : strings)
    {
      printString(s);
    }
  }

  
}
