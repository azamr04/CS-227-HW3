package ui;
import java.util.Scanner;

import api.MoveRecord;
import api.Direction;
import api.StringUtil;
import hw3.Pearls;
import hw3.PearlUtil;

/**
 * Dead simple text UI for experimenting with the Pearls game.
 * @author smkautz
 *
 */
public class ConsoleUI
{


  public static final String[] test1 = {
    "  #######  ",
    " #    @  # ",
    "# @ $   @##",
    " #    @# # ",
    "  #######  "       
  };
  
  // wrap around
  public static final String[] test2 = { 
    "##.###",
    "##...#",
    "#.#..#",
    "#.@@.#",
    "#...$#",
    "##.###"  
  };


  // the 'o' is a gate, that can be passed through just once, then it's closed
  public static final String[] test3 = {
    "#######",
    "##$ o #",
    "#     #",
    "#  @  #",
    "#     #",
    "#######"  
  };
  
  // spikes are deadly if you hit the pointy end (but ok on the side)
  public static final String[] test4 = {
    "#######",
    "## v  #",
    "#  @  #",
    "#     #",
    "#  $ ##",
    "#@    #",
    "#^    #",
    "#######"  
  };   
  
  // 'O' is a portal
  public static final String[] test5 = {
    "######",
    "#O  $#",
    "# @@ #",
    "#    #",
    "#@O ##",
    "#  ###",
    "######"  
  };
  
  // the '+' represents a "moveable block" that can be pushed
  public static final String[] test6 = {
    "######",
    "#   ##",
    "#$ o #",
    "# @  #",
    "# +@ #",
    "#    #",
    "#    #",
    "######"  
  };
  
  public static final String[] test6a = {
    "#######",
    "#    ##",
    "#$ o  #",
    "# @ # #",
    "# + @ #",
    "#     #",
    "#     #",
    "#######"  
  };
  
  // if a '+' and a '-' are pushed together, they disappear
  public static final String[] test7 = {
    "#######",
    "#$ +  #",
    "##@   #",
    "#@#@  #",
    "#     #",
    "#     #",
    "# -+  #",
    "#######"  
  };

  public static final String[] test7a = {
    "########",
    "#$ +   #",
    "##@    #",
    "#@#@   #",
    "#-     #",
    "#+   #+#",
    "#      #",
    "########"  
  };

  
  public static final String[] test8 = {
    " ##### ",
    "#....##",
    "#$.O.@#",
    "#....##",
    "#.O...#",
    "#..+..#",
    "#^.##@#",
    " ##### "  
  };
  
  // we can have multiple pairs of portals ('A', 'B')
  public static final String[] test9 = {
    "##### ####",
    "#  v     #",
    "#  @   A #",
    "   @##   $",
    "#A      ##",
    "#@ B   @ #",
    "#       ##",
    "#^   B @ #",
    "##### ####"  
  };  
  public static final String[] test10 = {
		    "######",
		    "#O -$#",
		    "# @@ #",
		    "#    #",
		    "#@O ##",
		    "#  ###",
		    "######" 
  };
  
  public static final String[] test11 = {
		    "#######",
		    "#$ +@-#",
		    "##    #",
		    "#@#   #",
		    "#     #",
		    "#  #@ #",
		    "# -+ v#",
		    "#######" 
  };
  
  public static final String[] test12 = {
		    "#######",
		    "#    ##",
		    "#$ o  #",
		    "# @ ^ #",
		    "# + @ #",
		    "#-    #",
		    "#P    #",
		    "#######"  
  };  

    public static void main(String[] args)
    {
      run(test12);
    }
    
    public static void run(String[] init) 
    {  
      Scanner in = new Scanner(System.in);
      Pearls g = new Pearls(init, new PearlUtil());
      
      System.out.println("Move the player ('$') to collect all the pearls ('@').");
      System.out.println("You can go off the edge and wrap around to the opposite side.");
      System.out.println("An 'o' is an open gate, but you can only pass through it once.");
      System.out.println("Watch out for the spikes ('<', '>', '^', 'v', '*')!");
      System.out.println("An 'O' is a portal, you come out the other portal in the same direction.");
      System.out.println("Blocks '+' and '-' can be pushed, and a '+' will combine with a '-' and vanish.");
      System.out.println();
      
      StringUtil.printGrid(g);
      
      while (!g.isOver())
      {
        System.out.println("Enter w, a, s, or d to move: ");
        String s = in.nextLine();
        MoveRecord[] desc = null;
        if (s.startsWith("a"))
        {
          desc = g.move(Direction.LEFT);
        }
        else if (s.startsWith("d"))
        {
          desc = g.move(Direction.RIGHT);
        }
        else if (s.startsWith("w"))
        {
          desc = g.move(Direction.UP);
        }
        else if (s.startsWith("s"))
        {
          desc = g.move(Direction.DOWN);
        }
        
        StringUtil.printGrid(g);
        
// Examine the descriptors if you have them...
//        if (desc != null)
//        {
//          for (Descriptor d : desc)
//          {
//            System.out.println(d);
//          }
//        }
        
      }
    }
    
  }
