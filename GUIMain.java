package ui;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import hw3.*;


/**
 * Entry point for creating and starting graphical interface for
 * Pearls game.
 * @author smkautz
 */
public class GUIMain
{
  
  /**
   * Entry point.  Edit here to change the initialization of the game.
   * @param args
   */
  public static void main(String[] args)
  {
    Pearls g = new Pearls(ConsoleUI.test9, new PearlUtil());
    start(g);
  }

  /**
   * Start up the game on the UI event thread.
   * @param game
   */
  private static void start(final Pearls game)
  {
    Runnable r = new Runnable()
    {
      public void run()
      {
        createAndShow(game);
      }
    };
    SwingUtilities.invokeLater(r);
  }
  
  /**
   * Initialize GUI components.  This should only be executed on the GUI
   * event thread.
   * @param game
   * @param sleepTime
   */
  private static void createAndShow(final Pearls game)
  {
    // create score panel and grid panel
    JPanel scorePanel = new JPanel();
    GamePanel panel = new GamePanel(game, scorePanel);
    
    // arrange the two panels vertically
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.add(scorePanel);
    mainPanel.add(panel);

    // create the frame
    JFrame frame = new JFrame("Pearls");
    frame.getContentPane().add(mainPanel);

    // give it a nonzero size
    panel.setPreferredSize(new Dimension(game.getColumns() * GamePanel.CELL_SIZE, game.getRows() * GamePanel.CELL_SIZE));    
    frame.pack();
    
    // we want to shut down the application if the 
    // "close" button is pressed on the frame
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    // make sure key events get to the main panel
    panel.grabFocus();
    
    // make the frame visible and start the UI machinery
    frame.setVisible(true);   

  }
}
