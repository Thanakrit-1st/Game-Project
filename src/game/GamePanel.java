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
    private enum GameState { START_MENU, PLAYING, GAME_OVER, WAVE_COMPLETED }
    private GameState gameState;
    private Thread gameThread;
    private Player player;
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Monster> monsters = new ArrayList<>();
    private final Random rand = new Random();
    private Rectangle startButtonBounds;
    private int wave = 1;
    private long waveStartTime;
    private final long bossSpawnInterval = 45000;
    private boolean bossSpawnedThisWave = false;
    private long lastSpawnTime;
    private final long spawnCooldown = 2000;
    
    // --- Skill Card and Confirmation Attributes ---
    private BufferedImage hpCardImg, damageCardImg, gunMasterCardImg;
    private Rectangle hpCardBounds, damageCardBounds, gunMasterCardBounds, confirmButtonBounds;
    private int selectedCard = -1; // -1: none, 0: HP, 1: Damage, 2: Gun Master

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true); requestFocus();
        addKeyListener(this); addMouseMotionListener(this); addMouseListener(this);
        gameState = GameState.START_MENU;
        player = new Player(WIDTH / 2, HEIGHT / 2);
        loadUIImages();
        startGameLoop();
    }
    
    private void loadUIImages() {
        try {
            int btnWidth = 200;
            int btnHeight = 60;
            startButtonBounds = new Rectangle((WIDTH - btnWidth) / 2, (HEIGHT - btnHeight) / 2, btnWidth, btnHeight);
            
            hpCardImg = ImageIO.read(getClass().getResourceAsStream("/res/images/need more hp.png"));
            damageCardImg = ImageIO.read(getClass().getResourceAsStream("/res/images/DeadlyBullet.png"));
            gunMasterCardImg = ImageIO.read(getClass().getResourceAsStream("/res/images/GunMaster.png"));
            
            int cardWidth = hpCardImg.getWidth();
            int cardHeight = hpCardImg.getHeight();
            int spacing = 30;
            int totalWidth = (cardWidth * 3) + (spacing * 2);
            int startX = (WIDTH - totalWidth) / 2;
            int y = (HEIGHT - cardHeight) / 2;
            
            hpCardBounds = new Rectangle(startX, y, cardWidth, cardHeight);
            damageCardBounds = new Rectangle(startX + cardWidth + spacing, y, cardWidth, cardHeight);
            gunMasterCardBounds = new Rectangle(startX + (cardWidth + spacing) * 2, y, cardWidth, cardHeight);

            int confirmWidth = 180;
            int confirmHeight = 50;
            confirmButtonBounds = new Rectangle((WIDTH - confirmWidth) / 2, y + cardHeight + 20, confirmWidth, confirmHeight);

        } catch (Exception e) {
            System.err.println("Failed to load one or more UI images.");
            e.printStackTrace();
        }
    }

    private void startGameLoop() { gameThread = new Thread(this); gameThread.start(); }

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
        if (gameState == GameState.PLAYING) {
            player.update();
            if (player.getHealth() <= 0) { gameState = GameState.GAME_OVER; player.resetMovementFlags(); return; }
            long timeInWave = System.currentTimeMillis() - waveStartTime;
            if (timeInWave > bossSpawnInterval && !bossSpawnedThisWave) { spawnBoss(); }
            if (System.currentTimeMillis() - lastSpawnTime > spawnCooldown) { spawnMonster(); lastSpawnTime = System.currentTimeMillis(); }
            for (Bullet b : bullets) b.update();
            for (Monster m : monsters) m.update(player);
            checkCollisions();
            bullets.removeIf(b -> !getBounds().contains(b.getBounds()));
        }
    }

    private void spawnMonster() {
        double healthMultiplier = Math.pow(1.15, wave - 1);
        int m1Health = (int)(20 * healthMultiplier);
        int m2Health = (int)(10 * healthMultiplier);
        int spawnSide = rand.nextInt(4); double x = 0, y = 0;
        switch (spawnSide) { case 0: x = rand.nextInt(WIDTH); y = -64; break; case 1: x = WIDTH; y = rand.nextInt(HEIGHT); break; case 2: x = rand.nextInt(WIDTH); y = HEIGHT; break; case 3: x = -64; y = rand.nextInt(HEIGHT); break; }
        if (rand.nextBoolean()) { monsters.add(new Monster(x, y, m1Health, 3.0, "/res/images/Monster1.png", false)); } else { monsters.add(new Monster(x, y, m2Health, 6.0, "/res/images/Monster2.png", false)); }
    }

    private void spawnBoss() {
        int bossHealth = (int)(100 * Math.pow(1.15, wave - 1));
        monsters.add(new Monster(WIDTH / 2.0, -100, bossHealth, 3.0, "/res/images/boss.png", true));
        bossSpawnedThisWave = true;
    }

    // --- UPDATED: This method is now safer against crashes ---
    private void checkCollisions() {
        boolean bossIsDead = false;

        // Use iterators for all list modifications to prevent crashes
        Iterator<Monster> monsterIter = monsters.iterator();
        while (monsterIter.hasNext()) {
            Monster monster = monsterIter.next();

            // Player vs Monster Collision
            if (player.getBounds().intersects(monster.getBounds())) {
                if (monster.isBoss()) {
                    player.takeDamage(player.getMaxHealth() * 2);
                } else {
                    player.takeDamage(10);
                }
                monsterIter.remove(); // Safely remove the monster
                continue; // Monster is gone, continue to the next one
            }

            // Bullet vs Monster Collision
            Iterator<Bullet> bulletIter = bullets.iterator();
            while (bulletIter.hasNext()) {
                Bullet bullet = bulletIter.next();
                if (bullet.getBounds().intersects(monster.getBounds())) {
                    monster.takeDamage(bullet.getDamage());
                    bulletIter.remove(); // Safely remove the bullet
                    break; // One bullet hits one monster, then stop checking this bullet
                }
            }
            
            // Check if monster died from bullet damage
            if (monster.getHealth() <= 0) {
                if (monster.isBoss()) {
                    bossIsDead = true;
                }
                monsterIter.remove(); // Safely remove dead monster
            }
        }

        // After all collision checks, if the boss was defeated, change game state
        if (bossIsDead) {
            player.healToMax();
            gameState = GameState.WAVE_COMPLETED;
            player.resetMovementFlags();
        }
    }

    private void startNextWave() {
        wave++;
        waveStartTime = System.currentTimeMillis();
        bossSpawnedThisWave = false;
        bullets.clear();
        monsters.clear();
        selectedCard = -1; // Reset selection
        gameState = GameState.PLAYING;
    }

    private void resetGame() {
        player = new Player(WIDTH / 2, HEIGHT / 2);
        monsters.clear();
        bullets.clear();
        wave = 1;
        bossSpawnedThisWave = false;
        selectedCard = -1;
        gameState = GameState.PLAYING;
        waveStartTime = System.currentTimeMillis();
        lastSpawnTime = waveStartTime;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        if (gameState == GameState.START_MENU || gameState == GameState.GAME_OVER) {
            drawButton(g2d, startButtonBounds, gameState == GameState.START_MENU ? "START" : "RESTART");
            if (gameState == GameState.GAME_OVER) {
                g2d.setColor(Color.RED); g2d.setFont(new Font("Consolas", Font.BOLD, 72));
                String msg = "GAME OVER"; int w = g2d.getFontMetrics().stringWidth(msg);
                g2d.drawString(msg, (WIDTH - w) / 2, HEIGHT / 2 - 100);
                g2d.setColor(Color.WHITE); g2d.setFont(new Font("Consolas", Font.BOLD, 36));
                String waveMsg = "You reached wave " + wave; w = g2d.getFontMetrics().stringWidth(waveMsg);
                g2d.drawString(waveMsg, (WIDTH - w) / 2, HEIGHT / 2 - 50);
            }
        } else if (gameState == GameState.PLAYING) {
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
            drawHealthUI(g2d);
            drawWaveUI(g2d);
            drawAmmoUI(g2d);
        } else if (gameState == GameState.WAVE_COMPLETED) {
            drawSkillCardScreen(g2d);
        }
        g2d.dispose();
    }
    
    private void drawSkillCardScreen(Graphics2D g2d) {
        g2d.setColor(Color.WHITE); g2d.setFont(new Font("Consolas", Font.BOLD, 48));
        String title = "Choose Your Upgrade!";
        int w = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, (WIDTH - w) / 2, 100);

        if (hpCardImg != null) g2d.drawImage(hpCardImg, hpCardBounds.x, hpCardBounds.y, null);
        if (damageCardImg != null) g2d.drawImage(damageCardImg, damageCardBounds.x, damageCardBounds.y, null);
        if (gunMasterCardImg != null) g2d.drawImage(gunMasterCardImg, gunMasterCardBounds.x, gunMasterCardBounds.y, null);

        if (selectedCard != -1) {
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(4));
            if (selectedCard == 0) g2d.drawRect(hpCardBounds.x, hpCardBounds.y, hpCardBounds.width, hpCardBounds.height);
            if (selectedCard == 1) g2d.drawRect(damageCardBounds.x, damageCardBounds.y, damageCardBounds.width, damageCardBounds.height);
            if (selectedCard == 2) g2d.drawRect(gunMasterCardBounds.x, gunMasterCardBounds.y, gunMasterCardBounds.width, gunMasterCardBounds.height);
            
            drawButton(g2d, confirmButtonBounds, "Confirm");
        }
    }

    private void drawWaveUI(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Consolas", Font.BOLD, 24));
        String waveText = "Wave: " + wave;
        g2d.drawString(waveText, 15, 30);
        long timeInWave = System.currentTimeMillis() - waveStartTime;
        long timeToBoss = (bossSpawnInterval - timeInWave) / 1000;
        String bossText;
        if (bossSpawnedThisWave) {
            g2d.setColor(Color.RED);
            bossText = "BOSS SPAWNED!";
        } else {
            g2d.setColor(Color.ORANGE);
            bossText = "Boss in: " + (timeToBoss > 0 ? timeToBoss : 0);
        }
        int w = g2d.getFontMetrics().stringWidth(bossText);
        g2d.drawString(bossText, WIDTH - w - 15, 30);
    }

    private void drawHealthUI(Graphics2D g2d) {
        g2d.setFont(new Font("Consolas", Font.BOLD, 24));
        g2d.setColor(Color.RED);
        String healthText = "HP: " + player.getHealth() + " / " + player.getMaxHealth();
        g2d.drawString(healthText, 15, HEIGHT - 20);
    }

    private void drawAmmoUI(Graphics2D g2d) {
        g2d.setFont(new Font("Consolas", Font.BOLD, 24));
        if (player.isReloading()) {
            g2d.setColor(Color.RED);
            g2d.drawString("RELOADING...", 15, HEIGHT - 50);
        } else {
            g2d.setColor(Color.WHITE);
            String ammoText = "Ammo: " + player.getCurrentAmmo() + " / " + player.getMaxAmmo();
            g2d.drawString(ammoText, 15, HEIGHT - 50);
        }
    }

    private void drawButton(Graphics2D g2d, Rectangle bounds, String text) {
        g2d.setColor(new Color(60, 60, 60));
        g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g2d.setFont(new Font("Consolas", Font.BOLD, 30));
        FontMetrics fm = g2d.getFontMetrics();
        int stringX = bounds.x + (bounds.width - fm.stringWidth(text)) / 2;
        int stringY = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, stringX, stringY);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (gameState == GameState.START_MENU || gameState == GameState.GAME_OVER) {
            if (startButtonBounds != null && startButtonBounds.contains(e.getPoint())) { resetGame(); }
        } else if (gameState == GameState.PLAYING) {
            if (player.shoot()) { bullets.add(new Bullet(player.getX() + player.getWidth() / 2.0, player.getY() + player.getHeight() / 2.0, player.gunAngle, player.getBulletDamage())); }
        } else if (gameState == GameState.WAVE_COMPLETED) {
            if (hpCardBounds.contains(e.getPoint())) { selectedCard = 0; }
            else if (damageCardBounds.contains(e.getPoint())) { selectedCard = 1; }
            else if (gunMasterCardBounds.contains(e.getPoint())) { selectedCard = 2; }
            
            if (selectedCard != -1 && confirmButtonBounds.contains(e.getPoint())) {
                switch (selectedCard) {
                    case 0: player.increaseMaxHealth(10); break;
                    case 1: player.increaseBulletDamage(1); break;
                    case 2: player.improveGunStats(200, 5); break;
                }
                startNextWave();
            }
        }
    }
    
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) { if (gameState != GameState.PLAYING) return; int c = e.getKeyCode(); if (c == KeyEvent.VK_W) player.movingUp = true; if (c == KeyEvent.VK_S) player.movingDown = true; if (c == KeyEvent.VK_A) player.movingLeft = true; if (c == KeyEvent.VK_D) player.movingRight = true; if (c == KeyEvent.VK_R) player.startReload(); }
    @Override public void keyReleased(KeyEvent e) { if (gameState != GameState.PLAYING) return; int c = e.getKeyCode(); if (c == KeyEvent.VK_W) player.movingUp = false; if (c == KeyEvent.VK_S) player.movingDown = false; if (c == KeyEvent.VK_A) player.movingLeft = false; if (c == KeyEvent.VK_D) player.movingRight = false; }
    @Override public void mouseMoved(MouseEvent e) { if (gameState == GameState.PLAYING) player.updateGunAngle(e.getX(), e.getY()); }
    @Override public void mouseDragged(MouseEvent e) { if (gameState == GameState.PLAYING) player.updateGunAngle(e.getX(), e.getY()); }
    @Override public void mouseClicked(MouseEvent e) {} @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
    
    public static void main(String[] args) {
        JFrame window = new JFrame("Wave Survival Game");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.add(new GamePanel());
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}

