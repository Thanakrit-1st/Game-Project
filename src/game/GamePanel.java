package src.game;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Iterator;

public class GamePanel extends JPanel implements Runnable, KeyListener, MouseMotionListener, MouseListener {

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    private final int FPS = 60;
    
    private Thread gameThread;
    private final Player player;
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Monster> monsters = new ArrayList<>();
    private final Random rand = new Random();
    
    // --- Monster Spawning Attributes ---
    private long lastSpawnTime;
    private final long spawnCooldown = 2000; // Spawn a new monster every 2 seconds

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();
        addKeyListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
        player = new Player(WIDTH / 2, HEIGHT / 2);
        lastSpawnTime = System.currentTimeMillis();
        startGameLoop();
    }

    private void startGameLoop() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        long targetTime = 1000 / FPS;
        while (gameThread != null) {
            long startTime = System.nanoTime();
            updateGame();
            repaint();
            long timeMillis = (System.nanoTime() - startTime) / 1_000_000;
            long waitTime = targetTime - timeMillis;
            if (waitTime > 0) {
                try { Thread.sleep(waitTime); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
    }

    private void updateGame() {
        player.update();

        // --- Handle Monster Spawning ---
        if (System.currentTimeMillis() - lastSpawnTime > spawnCooldown) {
            spawnMonster();
            lastSpawnTime = System.currentTimeMillis();
        }
        
        // --- Update Bullets and Monsters ---
        for (Bullet b : bullets) b.update();
        for (Monster m : monsters) m.update(player);

        // --- Handle Collisions ---
        checkCollisions();

        // --- Remove Off-screen Bullets and Dead Monsters ---
        bullets.removeIf(b -> b.getBounds().x > WIDTH || b.getBounds().x < 0 || b.getBounds().y > HEIGHT || b.getBounds().y < 0);
        monsters.removeIf(m -> m.getHealth() <= 0);
    }

    private void spawnMonster() {
        int spawnSide = rand.nextInt(4); // 0=top, 1=right, 2=bottom, 3=left
        double x = 0, y = 0;
        int monsterSize = 64; // Approx size for spawning outside screen

        switch (spawnSide) {
            case 0: x = rand.nextInt(WIDTH); y = -monsterSize; break;
            case 1: x = WIDTH; y = rand.nextInt(HEIGHT); break;
            case 2: x = rand.nextInt(WIDTH); y = HEIGHT; break;
            case 3: x = -monsterSize; y = rand.nextInt(HEIGHT); break;
        }

        // Randomly choose which monster type to spawn
        if (rand.nextBoolean()) {
            monsters.add(new Monster(x, y, 20, 3.0, "/res/images/Monster1.png")); // Slower monster
        } else {
            monsters.add(new Monster(x, y, 10, 6.0, "/res/images/Monster2.png")); // Faster monster
        }
    }

    private void checkCollisions() {
        // --- Bullet vs Monster Collision ---
        Iterator<Bullet> bulletIter = bullets.iterator();
        while (bulletIter.hasNext()) {
            Bullet bullet = bulletIter.next();
            Iterator<Monster> monsterIter = monsters.iterator();
            while (monsterIter.hasNext()) {
                Monster monster = monsterIter.next();
                if (bullet.getBounds().intersects(monster.getBounds())) {
                    monster.takeDamage(5); // Each bullet does 5 damage
                    bulletIter.remove(); // Remove the bullet
                    break; // A bullet can only hit one monster
                }
            }
        }

        // --- Player vs Monster Collision ---
        Iterator<Monster> monsterIter = monsters.iterator();
        while (monsterIter.hasNext()) {
            Monster monster = monsterIter.next();
            if (player.getBounds().intersects(monster.getBounds())) {
                player.takeDamage(10);
                monsterIter.remove(); // Remove the monster that hit the player
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Draw Monsters (drawn first so player is on top)
        for (Monster m : monsters) m.draw(g2d);
        
        // Draw Player and Gun
        if (player.image != null) g2d.drawImage(player.image, player.getX(), player.getY(), player.getWidth(), player.getHeight(), null);
        if (player.gunImage != null) {
            AffineTransform old = g2d.getTransform();
            g2d.translate(player.getX() + player.getWidth() / 2.0, player.getY() + player.getHeight() / 2.0);
            g2d.rotate(player.gunAngle);
            g2d.drawImage(player.gunImage, 0, -(int)player.getGunHeight() / 2, (int)player.getGunWidth(), (int)player.getGunHeight(), null);
            g2d.setTransform(old);
        }
        
        // Draw Bullets
        for (Bullet b : bullets) b.draw(g2d);

        // Draw UI
        drawHealthBar(g2d);
        g2d.dispose();
    }

    private void drawHealthBar(Graphics2D g2d) {
        int barWidth = 200, barHeight = 20, xPos = 15, yPos = HEIGHT - barHeight - 15;
        double healthPercent = (double) player.getHealth() / player.getMaxHealth();
        int currentHealthWidth = (int) (barWidth * healthPercent);
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(xPos, yPos, barWidth, barHeight);
        g2d.setColor(Color.RED);
        g2d.fillRect(xPos, yPos, currentHealthWidth, barHeight);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(xPos, yPos, barWidth, barHeight);
    }
    
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {
        int c = e.getKeyCode();
        if (c == KeyEvent.VK_W) player.movingUp = true; if (c == KeyEvent.VK_S) player.movingDown = true;
        if (c == KeyEvent.VK_A) player.movingLeft = true; if (c == KeyEvent.VK_D) player.movingRight = true;
    }
    @Override public void keyReleased(KeyEvent e) {
        int c = e.getKeyCode();
        if (c == KeyEvent.VK_W) player.movingUp = false; if (c == KeyEvent.VK_S) player.movingDown = false;
        if (c == KeyEvent.VK_A) player.movingLeft = false; if (c == KeyEvent.VK_D) player.movingRight = false;
    }
    @Override public void mouseMoved(MouseEvent e) { player.updateGunAngle(e.getX(), e.getY()); }
    @Override public void mouseDragged(MouseEvent e) { player.updateGunAngle(e.getX(), e.getY()); }
    @Override public void mousePressed(MouseEvent e) {
        bullets.add(new Bullet(player.getX() + player.getWidth() / 2.0, player.getY() + player.getHeight() / 2.0, player.gunAngle));
    }
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