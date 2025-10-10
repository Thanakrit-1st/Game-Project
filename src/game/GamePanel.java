package src.game;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Main game panel that handles the game loop, rendering, and input.
 */
public class GamePanel extends JPanel implements Runnable, KeyListener {

    private final Player player;
    private Thread gameThread;
    private final int FPS = 60; // Frames per second
    private final long targetTime = 1000 / FPS; // Time in ms per frame

    // Window dimensions
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    public GamePanel() {
        // Initialize JPanel settings
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true); // Required for KeyListener to work
        requestFocus();
        addKeyListener(this);

        // Initialize the player in the center of the screen
        player = new Player(WIDTH / 2, HEIGHT / 2);

        // Start the game loop thread
        startGameLoop();
    }

    private void startGameLoop() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    // --- Game Loop Implementation (Runnable) ---

    @Override
    public void run() {
        long startTime;
        long timeMillis;
        long waitTime;

        // The core game loop
        while (gameThread != null) {
            startTime = System.nanoTime();

            // 1. Update Game State
            updateGame();

            // 2. Redraw Screen
            repaint(); // Calls paintComponent

            // Calculate delay to maintain target FPS
            timeMillis = (System.nanoTime() - startTime) / 1_000_000;
            waitTime = targetTime - timeMillis;

            if (waitTime > 0) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void updateGame() {
        player.update();
        // You would add other game logic here (e.g., enemy movement, collision checks)
    }

    // --- Rendering (Drawing) ---

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw a black background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw the player
        if (player.image != null) {
            // FIX: Use the scaled width and height calculated in the Player class.
            g.drawImage(player.image, player.getX(), player.getY(), player.getWidth(), player.getHeight(), null);
        } else {
            // Draw a placeholder if image failed to load
            g.setColor(Color.WHITE);
            // Use player.getWidth() and player.getHeight() for the fallback as well, so it's scaled.
            g.fillRect(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        }

        g.dispose();
    }

    // --- Input Handling (KeyListener) ---

    @Override
    public void keyTyped(KeyEvent e) {
        // Not typically used for continuous movement
    }

    /**
     * Handles key press: Sets the corresponding movement flag to true.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        char key = Character.toUpperCase(e.getKeyChar());

        if (key == 'W' || code == KeyEvent.VK_UP) {
            player.movingUp = true;
        }
        if (key == 'S' || code == KeyEvent.VK_DOWN) {
            player.movingDown = true;
        }
        if (key == 'A' || code == KeyEvent.VK_LEFT) {
            player.movingLeft = true;
        }
        if (key == 'D' || code == KeyEvent.VK_RIGHT) {
            player.movingRight = true;
        }
    }

    /**
     * Handles key release: Sets the corresponding movement flag back to false.
     * This is crucial for stopping movement when the key is released.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        char key = Character.toUpperCase(e.getKeyChar());

        if (key == 'W' || code == KeyEvent.VK_UP) {
            player.movingUp = false;
        }
        if (key == 'S' || code == KeyEvent.VK_DOWN) {
            player.movingDown = false;
        }
        if (key == 'A' || code == KeyEvent.VK_LEFT) {
            player.movingLeft = false;
        }
        if (key == 'D' || code == KeyEvent.VK_RIGHT) {
            player.movingRight = false;
        }
    }


    // --- Main Method to Run the Game ---

    public static void main(String[] args) {
        // 1. Create the main window
        JFrame window = new JFrame("Simple Java Game - WASD Movement");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        // 2. Create the game panel and add it to the window
        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        // 3. Size the window to fit the preferred size of the GamePanel
        window.pack();

        // 4. Center the window and make it visible
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}
