package src.game;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel implements Runnable, KeyListener, MouseMotionListener, MouseListener {

    private final Player player;
    private Thread gameThread;
    private final int FPS = 60;
    private final long targetTime = 1000 / FPS;

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    private final List<Bullet> bullets = new ArrayList<>();

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();
        addKeyListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);

        player = new Player(WIDTH / 2, HEIGHT / 2);
        startGameLoop();
    }

    private void startGameLoop() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        while (gameThread != null) {
            long startTime = System.nanoTime();
            updateGame();
            repaint();
            long timeMillis = (System.nanoTime() - startTime) / 1_000_000;
            long waitTime = targetTime - timeMillis;

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
        var iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.update();
            if (bullet.getBounds().x > WIDTH || bullet.getBounds().x < 0 ||
                bullet.getBounds().y > HEIGHT || bullet.getBounds().y < 0) {
                iterator.remove();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // --- Draw Game Elements ---
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        if (player.image != null) {
            g2d.drawImage(player.image, player.getX(), player.getY(), player.getWidth(), player.getHeight(), null);
        }
        if (player.gunImage != null) {
            AffineTransform oldTransform = g2d.getTransform();
            int playerCenterX = player.getX() + player.getWidth() / 2;
            int playerCenterY = player.getY() + player.getHeight() / 2;
            g2d.translate(playerCenterX, playerCenterY);
            g2d.rotate(player.gunAngle);
            g2d.drawImage(player.gunImage, 0, -(int)player.getGunHeight() / 2, (int)player.getGunWidth(), (int)player.getGunHeight(), null);
            g2d.setTransform(oldTransform);
        }
        for (Bullet bullet : bullets) {
            bullet.draw(g2d);
        }

        // --- NEW: Draw the UI on top of everything else ---
        drawHealthBar(g2d);

        g2d.dispose();
    }

    /**
     * NEW: Draws the player's health bar in the bottom-left corner.
     */
    private void drawHealthBar(Graphics2D g2d) {
        // --- Health Bar Dimensions and Position ---
        int barWidth = 200;
        int barHeight = 20;
        int xPos = 15;
        int yPos = HEIGHT - barHeight - 15; // 15 pixels from the bottom

        // --- Calculate the width of the current health ---
        double healthPercent = (double) player.getHealth() / player.getMaxHealth();
        int currentHealthWidth = (int) (barWidth * healthPercent);

        // --- Draw the background of the health bar (the empty part) ---
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(xPos, yPos, barWidth, barHeight);
        
        // --- Draw the foreground of the health bar (the current health) ---
        g2d.setColor(Color.RED);
        g2d.fillRect(xPos, yPos, currentHealthWidth, barHeight);

        // --- Draw a border for a cleaner look ---
        g2d.setColor(Color.WHITE);
        g2d.drawRect(xPos, yPos, barWidth, barHeight);
    }
    
    // --- Input Handling ---
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W) player.movingUp = true;
        if (code == KeyEvent.VK_S) player.movingDown = true;
        if (code == KeyEvent.VK_A) player.movingLeft = true;
        if (code == KeyEvent.VK_D) player.movingRight = true;
    }
    @Override public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W) player.movingUp = false;
        if (code == KeyEvent.VK_S) player.movingDown = false;
        if (code == KeyEvent.VK_A) player.movingLeft = false;
        if (code == KeyEvent.VK_D) player.movingRight = false;
    }
    @Override public void mouseMoved(MouseEvent e) { player.updateGunAngle(e.getX(), e.getY()); }
    @Override public void mouseDragged(MouseEvent e) { player.updateGunAngle(e.getX(), e.getY()); }
    @Override public void mousePressed(MouseEvent e) {
        int playerCenterX = player.getX() + player.getWidth() / 2;
        int playerCenterY = player.getY() + player.getHeight() / 2;
        bullets.add(new Bullet(playerCenterX, playerCenterY, player.gunAngle));
    }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    
    // --- Main Method ---
    public static void main(String[] args) {
        JFrame window = new JFrame("Game with Health Bar");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}