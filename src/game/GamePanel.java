package src.game;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Iterator;

public class GamePanel extends JPanel implements Runnable, KeyListener, MouseMotionListener, MouseListener {

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    private final int FPS = 60;

    // --- Game State Management ---
    private enum GameState {
        START_MENU,
        PLAYING,
        GAME_OVER
    }
    private GameState gameState;

    private Thread gameThread;
    private final Player player;
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Monster> monsters = new ArrayList<>();
    private final Random rand = new Random();

    // --- Start Button, Timer, and Scaling Attributes ---
    private BufferedImage startButtonImage;
    private Rectangle startButtonBounds;
    private final double startButtonScale = 0.125; // <-- NEW: Scale factor for the button
    private long startTime; // Time when the game starts
    private long elapsedTime; // Time elapsed in seconds

    // --- Monster Spawning Attributes ---
    private long lastSpawnTime;
    private final long spawnCooldown = 2000;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();
        addKeyListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);

        // --- Initial Setup ---
        gameState = GameState.START_MENU; // Start at the menu
        player = new Player(WIDTH / 2, HEIGHT / 2);
        loadStartButtonImage();
        startGameLoop();
    }
    
    private void loadStartButtonImage() {
        try {
            String path = "/res/images/click to start.png";
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) throw new IOException("Resource not found: " + path);
            startButtonImage = ImageIO.read(is);

            // --- UPDATED: Center and scale the button ---
            int btnWidth = (int)(startButtonImage.getWidth() * startButtonScale);
            int btnHeight = (int)(startButtonImage.getHeight() * startButtonScale);
            int btnX = (WIDTH - btnWidth) / 2;
            int btnY = (HEIGHT - btnHeight) / 2;
            startButtonBounds = new Rectangle(btnX, btnY, btnWidth, btnHeight);

        } catch (IOException e) {
            System.err.println("Failed to load start button image: " + e.getMessage());
        }
    }

    private void startGameLoop() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        long targetTime = 1000 / FPS;
        while (gameThread != null) {
            long startTimeLoop = System.nanoTime();
            updateGame();
            repaint();
            long timeMillis = (System.nanoTime() - startTimeLoop) / 1_000_000;
            long waitTime = targetTime - timeMillis;
            if (waitTime > 0) {
                try { Thread.sleep(waitTime); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
    }

    private void updateGame() {
        // Only update game logic if we are in the PLAYING state
        if (gameState == GameState.PLAYING) {
            player.update();

            // Update timer
            elapsedTime = (System.currentTimeMillis() - startTime) / 1000;

            // Handle Monster Spawning
            if (System.currentTimeMillis() - lastSpawnTime > spawnCooldown) {
                spawnMonster();
                lastSpawnTime = System.currentTimeMillis();
            }
            
            // Update Bullets and Monsters
            for (Bullet b : bullets) b.update();
            for (Monster m : monsters) m.update(player);

            checkCollisions();
            
            bullets.removeIf(b -> b.getBounds().x > WIDTH || b.getBounds().x < 0 || b.getBounds().y > HEIGHT || b.getBounds().y < 0);
            monsters.removeIf(m -> m.getHealth() <= 0);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // --- Draw Background ---
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        if (gameState == GameState.START_MENU) {
            // --- Draw Start Screen ---
            if (startButtonImage != null) {
                // --- UPDATED: Draw the image using the scaled bounds ---
                g2d.drawImage(startButtonImage, startButtonBounds.x, startButtonBounds.y, startButtonBounds.width, startButtonBounds.height, null);
            }
        } else if (gameState == GameState.PLAYING) {
            // --- Draw Game Elements ---
            for (Monster m : monsters) m.draw(g2d);
            if (player.image != null) g2d.drawImage(player.image, player.getX(), player.getY(), player.getWidth(), player.getHeight(), null);
            if (player.gunImage != null) {
                AffineTransform old = g2d.getTransform();
                g2d.translate(player.getX() + player.getWidth() / 2.0, player.getY() + player.getHeight() / 2.0);
                g2d.rotate(player.gunAngle);
                g2d.drawImage(player.gunImage, 0, -(int)player.getGunHeight() / 2, (int)player.getGunWidth(), (int)player.getGunHeight(), null);
                g2d.setTransform(old);
            }
            for (Bullet b : bullets) b.draw(g2d);
            
            // --- Draw UI ---
            drawHealthBar(g2d);
            drawTimer(g2d);
        }
        
        g2d.dispose();
    }

    private void drawTimer(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Consolas", Font.BOLD, 24));
        String timeText = "Time: " + elapsedTime;
        int stringWidth = g2d.getFontMetrics().stringWidth(timeText);
        g2d.drawString(timeText, WIDTH - stringWidth - 15, 30); // 15px padding from top-right
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (gameState == GameState.START_MENU) {
            if (startButtonBounds != null && startButtonBounds.contains(e.getPoint())) {
                gameState = GameState.PLAYING;
                startTime = System.currentTimeMillis();
                lastSpawnTime = startTime;
            }
        } else if (gameState == GameState.PLAYING) {
            bullets.add(new Bullet(player.getX() + player.getWidth() / 2.0, player.getY() + player.getHeight() / 2.0, player.gunAngle));
        }
    }
    
    // --- All other methods (spawning, collisions, key input, etc.) remain the same ---
    private void spawnMonster() {
        int spawnSide = rand.nextInt(4); double x = 0, y = 0; int monsterSize = 64;
        switch (spawnSide) { case 0: x = rand.nextInt(WIDTH); y = -monsterSize; break; case 1: x = WIDTH; y = rand.nextInt(HEIGHT); break; case 2: x = rand.nextInt(WIDTH); y = HEIGHT; break; case 3: x = -monsterSize; y = rand.nextInt(HEIGHT); break; }
        if (rand.nextBoolean()) { monsters.add(new Monster(x, y, 20, 3.0, "/res/images/Monster1.png")); } else { monsters.add(new Monster(x, y, 10, 6.0, "/res/images/Monster2.png")); }
    }
    private void checkCollisions() {
        Iterator<Bullet> bulletIter = bullets.iterator(); while (bulletIter.hasNext()) { Bullet bullet = bulletIter.next(); Iterator<Monster> monsterIter = monsters.iterator(); while (monsterIter.hasNext()) { Monster monster = monsterIter.next(); if (bullet.getBounds().intersects(monster.getBounds())) { monster.takeDamage(5); bulletIter.remove(); break; } } }
        Iterator<Monster> monsterIter = monsters.iterator(); while (monsterIter.hasNext()) { Monster monster = monsterIter.next(); if (player.getBounds().intersects(monster.getBounds())) { player.takeDamage(10); monsterIter.remove(); } }
    }
    private void drawHealthBar(Graphics2D g2d) { int barWidth = 200, barHeight = 20, xPos = 15, yPos = HEIGHT - barHeight - 15; double healthPercent = (double) player.getHealth() / player.getMaxHealth(); int currentHealthWidth = (int) (barWidth * healthPercent); g2d.setColor(Color.DARK_GRAY); g2d.fillRect(xPos, yPos, barWidth, barHeight); g2d.setColor(Color.RED); g2d.fillRect(xPos, yPos, currentHealthWidth, barHeight); g2d.setColor(Color.WHITE); g2d.drawRect(xPos, yPos, barWidth, barHeight); }
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) { if (gameState != GameState.PLAYING) return; int c = e.getKeyCode(); if (c == KeyEvent.VK_W) player.movingUp = true; if (c == KeyEvent.VK_S) player.movingDown = true; if (c == KeyEvent.VK_A) player.movingLeft = true; if (c == KeyEvent.VK_D) player.movingRight = true; }
    @Override public void keyReleased(KeyEvent e) { if (gameState != GameState.PLAYING) return; int c = e.getKeyCode(); if (c == KeyEvent.VK_W) player.movingUp = false; if (c == KeyEvent.VK_S) player.movingDown = false; if (c == KeyEvent.VK_A) player.movingLeft = false; if (c == KeyEvent.VK_D) player.movingRight = false; }
    @Override public void mouseMoved(MouseEvent e) { if (gameState == GameState.PLAYING) player.updateGunAngle(e.getX(), e.getY()); }
    @Override public void mouseDragged(MouseEvent e) { if (gameState == GameState.PLAYING) player.updateGunAngle(e.getX(), e.getY()); }
    @Override public void mouseClicked(MouseEvent e) {} @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
    
    public static void main(String[] args) {
        JFrame window = new JFrame("Monster Survival Game");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.add(new GamePanel());
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}

