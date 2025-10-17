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

    public static final int WIDTH = 950;
    public static final int HEIGHT = 750;
    private final int FPS = 60;
    private enum GameState { START_MENU, PLAYING, GAME_OVER, WAVE_COMPLETED, CHEST_OPEN }
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
    
    private BufferedImage hpCardImg, pistolDamageCardImg, pistolMasterCardImg,
                          rifleDamageCardImg, rifleMasterCardImg,
                          shotgunDamageCardImg, shotgunMasterCardImg,
                          backgroundImage;
                          
    private Rectangle hpCardBounds, damageCardBounds, masterCardBounds, confirmButtonBounds;
    private int selectedSkillCard = -1;

    private Chest droppedChest = null;
    private boolean chestDroppedThisWave = false;
    private BufferedImage pistolCardImg, rifleCardImg, shotgunCardImg;
    private Rectangle pistolCardBounds, rifleCardBounds, shotgunCardBounds;

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
            backgroundImage = ImageIO.read(getClass().getResourceAsStream("/res/images/background.png"));
            
            int btnWidth = 200, btnHeight = 60;
            startButtonBounds = new Rectangle((WIDTH - btnWidth) / 2, (HEIGHT - btnHeight) / 2, btnWidth, btnHeight);
            
            hpCardImg = ImageIO.read(getClass().getResourceAsStream("/res/images/need more hp.png"));
            pistolDamageCardImg = ImageIO.read(getClass().getResourceAsStream("/res/images/DeadlyBullet.png"));
            pistolMasterCardImg = ImageIO.read(getClass().getResourceAsStream("/res/images/GunMaster.png"));
            rifleDamageCardImg = ImageIO.read(getClass().getResourceAsStream("/res/images/deadly bullet(rifle).png"));
            rifleMasterCardImg = ImageIO.read(getClass().getResourceAsStream("/res/images/sniper master.png"));
            shotgunDamageCardImg = ImageIO.read(getClass().getResourceAsStream("/res/images/big bullet.png"));
            shotgunMasterCardImg = ImageIO.read(getClass().getResourceAsStream("/res/images/shotgun master.png"));

            int cardWidth = hpCardImg.getWidth(), cardHeight = hpCardImg.getHeight();
            int spacing = 30, totalWidth = (cardWidth * 3) + (spacing * 2);
            int startX = (WIDTH - totalWidth) / 2, y = (HEIGHT - cardHeight) / 2;
            
            hpCardBounds = new Rectangle(startX, y, cardWidth, cardHeight);
            damageCardBounds = new Rectangle(startX + cardWidth + spacing, y, cardWidth, cardHeight);
            masterCardBounds = new Rectangle(startX + (cardWidth + spacing) * 2, y, cardWidth, cardHeight);

            int confirmWidth = 180, confirmHeight = 50;
            confirmButtonBounds = new Rectangle((WIDTH - confirmWidth) / 2, y + cardHeight + 20, confirmWidth, confirmHeight);
            
            pistolCardImg = ImageIO.read(getClass().getResourceAsStream("/res/images/pistol card.png"));
            rifleCardImg = ImageIO.read(getClass().getResourceAsStream("/res/images/rifle card.png"));
            shotgunCardImg = ImageIO.read(getClass().getResourceAsStream("/res/images/shotgun card.png"));
            
            pistolCardBounds = new Rectangle(hpCardBounds);
            rifleCardBounds = new Rectangle(damageCardBounds);
            shotgunCardBounds = new Rectangle(masterCardBounds);

        } catch (Exception e) { e.printStackTrace(); }
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
            if (waitTime > 0) { try { Thread.sleep(waitTime); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }
        }
    }

    private void updateGame() {
        if (gameState == GameState.PLAYING) {
            player.update();
            if (player.getHealth() <= 0) { gameState = GameState.GAME_OVER; player.resetMovementFlags(); return; }
            
            if (droppedChest != null && player.getBounds().intersects(droppedChest.getBounds())) {
                gameState = GameState.CHEST_OPEN;
                player.resetMovementFlags();
                droppedChest = null;
                return;
            }

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
        if (wave >= 3 && rand.nextInt(100) < 10) { spawnMysterious(); return; }
        double healthMultiplier = Math.pow(1.15, wave - 1);
        int m1Health = (int)(20 * healthMultiplier), m2Health = (int)(10 * healthMultiplier);
        int spawnSide = rand.nextInt(4); double x = 0, y = 0;
        switch (spawnSide) { case 0: x = rand.nextInt(WIDTH); y = -64; break; case 1: x = WIDTH; y = rand.nextInt(HEIGHT); break; case 2: x = rand.nextInt(WIDTH); y = HEIGHT; break; case 3: x = -64; y = rand.nextInt(HEIGHT); break; }
        if (rand.nextBoolean()) { monsters.add(new Monster(x, y, m1Health, 3.0, "/res/images/Monster1.png", false, false)); } 
        else { monsters.add(new Monster(x, y, m2Health, 6.0, "/res/images/Monster2.png", false, false)); }
    }

    private void spawnMysterious() {
        int health = 1, spawnSide = rand.nextInt(4); double speed = 9, x = 0, y = 0;
        switch (spawnSide) { case 0: x = rand.nextInt(WIDTH); y = -64; break; case 1: x = WIDTH; y = rand.nextInt(HEIGHT); break; case 2: x = rand.nextInt(WIDTH); y = HEIGHT; break; case 3: x = -64; y = rand.nextInt(HEIGHT); break; }
        monsters.add(new Monster(x, y, health, speed, "/res/images/Mysterious.png", false, true));
    }

    private void spawnBoss() {
        int bossHealth = (int)(100 * Math.pow(1.15, wave - 1));
        monsters.add(new Monster(WIDTH / 2.0, -100, bossHealth, 3.0, "/res/images/boss.png", true, false));
        bossSpawnedThisWave = true;
    }

    private void checkCollisions() {
        boolean bossIsDead = false;
        Iterator<Bullet> bulletIter = bullets.iterator();
        while (bulletIter.hasNext()) {
            Bullet bullet = bulletIter.next();
            for (Monster monster : monsters) {
                if (monster.getHealth() > 0 && bullet.getBounds().intersects(monster.getBounds())) {
                    monster.takeDamage(bullet.getDamage());
                    bulletIter.remove();
                    break;
                }
            }
        }
        Iterator<Monster> monsterIter = monsters.iterator();
        while (monsterIter.hasNext()) {
            Monster monster = monsterIter.next();
            if (player.getBounds().intersects(monster.getBounds())) {
                if (monster.isBoss()) { player.takeDamage(player.getMaxHealth() * 2); } 
                else if (monster.isMysterious()) { player.takeDamage((int)(player.getMaxHealth() * 0.25)); } 
                else { player.takeDamage(10); }
                monsterIter.remove();
                continue;
            }
            if (monster.getHealth() <= 0) {
                if (monster.isBoss()) bossIsDead = true;
                if (!chestDroppedThisWave) {
                    if (rand.nextInt(100) < 100) {
                        droppedChest = new Chest(monster.getX(), monster.getY());
                        chestDroppedThisWave = true;
                    }
                }
                monsterIter.remove();
            }
        }
        if (bossIsDead) { gameState = GameState.WAVE_COMPLETED; player.resetMovementFlags(); }
    }

    private void startNextWave() {
        wave++;
        waveStartTime = System.currentTimeMillis();
        bossSpawnedThisWave = false;
        bullets.clear();
        monsters.clear();
        selectedSkillCard = -1;
        chestDroppedThisWave = false;
        gameState = GameState.PLAYING;
    }

    private void resetGame() {
        player = new Player(WIDTH / 2, HEIGHT / 2);
        monsters.clear();
        bullets.clear();
        wave = 1;
        bossSpawnedThisWave = false;
        selectedSkillCard = -1;
        chestDroppedThisWave = false;
        gameState = GameState.PLAYING;
        waveStartTime = System.currentTimeMillis();
        lastSpawnTime = waveStartTime;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (backgroundImage != null) {
            for (int y = 0; y < HEIGHT; y += 64) {
                for (int x = 0; x < WIDTH; x += 64) {
                    g2d.drawImage(backgroundImage, x, y, 64, 64, null);
                }
            }
        } else {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, WIDTH, HEIGHT);
        }

        if (gameState == GameState.START_MENU || gameState == GameState.GAME_OVER) {
            if (gameState == GameState.START_MENU) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Consolas", Font.BOLD, 72));
                String title = "Escape the Death";
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, HEIGHT / 2 - 100);
            }
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
            if (droppedChest != null) droppedChest.draw(g2d);
            for (Monster m : monsters) m.draw(g2d);
            if (player.characterImage != null) g2d.drawImage(player.characterImage, player.getX(), player.getY(), player.getWidth(), player.getHeight(), null);
            if (player.equippedWeaponImage != null) {
                AffineTransform old = g2d.getTransform();
                g2d.translate(player.getX() + player.getWidth() / 2.0, player.getY() + player.getHeight() / 2.0);
                g2d.rotate(player.gunAngle);
                int gunW = (int)player.getGunWidth(), gunH = (int)player.getGunHeight();
                if (player.isGunFlipped) { g2d.drawImage(player.equippedWeaponImage, 0, gunH / 2, gunW, -gunH, null); }
                else { g2d.drawImage(player.equippedWeaponImage, 0, -gunH / 2, gunW, gunH, null); }
                g2d.setTransform(old);
            }
            for (Bullet b : bullets) b.draw(g2d);
            drawHealthUI(g2d); drawWaveUI(g2d); drawAmmoUI(g2d);
        } else if (gameState == GameState.WAVE_COMPLETED) {
            drawSkillCardScreen(g2d);
        } else if (gameState == GameState.CHEST_OPEN) {
            drawWeaponChoiceScreen(g2d);
        }
        g2d.dispose();
    }
    
    private void drawSkillCardScreen(Graphics2D g2d) {
        g2d.setColor(Color.WHITE); g2d.setFont(new Font("Consolas", Font.BOLD, 48));
        String title = "Choose Your Upgrade!"; int w = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, (WIDTH - w) / 2, 100);
        if (hpCardImg != null) g2d.drawImage(hpCardImg, hpCardBounds.x, hpCardBounds.y, null);
        switch(player.getCurrentWeapon()) {
            case PISTOL:
                if (pistolDamageCardImg != null) g2d.drawImage(pistolDamageCardImg, damageCardBounds.x, damageCardBounds.y, null);
                if (pistolMasterCardImg != null) g2d.drawImage(pistolMasterCardImg, masterCardBounds.x, masterCardBounds.y, null);
                break;
            case RIFLE:
                if (rifleDamageCardImg != null) g2d.drawImage(rifleDamageCardImg, damageCardBounds.x, damageCardBounds.y, null);
                if (rifleMasterCardImg != null) g2d.drawImage(rifleMasterCardImg, masterCardBounds.x, masterCardBounds.y, null);
                break;
            case SHOTGUN:
                if (shotgunDamageCardImg != null) g2d.drawImage(shotgunDamageCardImg, damageCardBounds.x, damageCardBounds.y, null);
                if (shotgunMasterCardImg != null) g2d.drawImage(shotgunMasterCardImg, masterCardBounds.x, masterCardBounds.y, null);
                break;
        }
        if (selectedSkillCard != -1) {
            g2d.setColor(Color.YELLOW); g2d.setStroke(new BasicStroke(4));
            if (selectedSkillCard == 0) g2d.drawRect(hpCardBounds.x, hpCardBounds.y, hpCardBounds.width, hpCardBounds.height);
            if (selectedSkillCard == 1) g2d.drawRect(damageCardBounds.x, damageCardBounds.y, damageCardBounds.width, damageCardBounds.height);
            if (selectedSkillCard == 2) g2d.drawRect(masterCardBounds.x, masterCardBounds.y, masterCardBounds.width, masterCardBounds.height);
            drawButton(g2d, confirmButtonBounds, "Confirm");
        }
    }
    
    private void drawWeaponChoiceScreen(Graphics2D g2d) {
        g2d.setColor(Color.WHITE); g2d.setFont(new Font("Consolas", Font.BOLD, 48));
        String title = "Choose Your Weapon!"; int w = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, (WIDTH - w) / 2, 100);
        if (pistolCardImg != null) g2d.drawImage(pistolCardImg, pistolCardBounds.x, pistolCardBounds.y, null);
        if (rifleCardImg != null) g2d.drawImage(rifleCardImg, rifleCardBounds.x, rifleCardBounds.y, null);
        if (shotgunCardImg != null) g2d.drawImage(shotgunCardImg, shotgunCardBounds.x, shotgunCardBounds.y, null);
    }
    
    private void drawWaveUI(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Consolas", Font.BOLD, 24));
        String waveText = "Wave: " + wave; g2d.drawString(waveText, 15, 30);
        long timeInWave = System.currentTimeMillis() - waveStartTime;
        long timeToBoss = (bossSpawnInterval - timeInWave) / 1000;
        String bossText;
        if (bossSpawnedThisWave) { g2d.setColor(Color.RED); bossText = "BOSS SPAWNED!"; } 
        else { g2d.setColor(Color.ORANGE); bossText = "Boss in: " + (timeToBoss > 0 ? timeToBoss : 0); }
        int w = g2d.getFontMetrics().stringWidth(bossText);
        g2d.drawString(bossText, WIDTH - w - 15, 30);
    }

    private void drawHealthUI(Graphics2D g2d) {
        g2d.setFont(new Font("Consolas", Font.BOLD, 24)); g2d.setColor(Color.RED);
        String healthText = "HP: " + player.getHealth() + " / " + player.getMaxHealth();
        g2d.drawString(healthText, 15, HEIGHT - 20);
    }

    private void drawAmmoUI(Graphics2D g2d) {
        g2d.setFont(new Font("Consolas", Font.BOLD, 24));
        if (player.isReloading()) { g2d.setColor(Color.RED); g2d.drawString("RELOADING...", 15, HEIGHT - 50); } 
        else { g2d.setColor(Color.WHITE); String ammoText = "Ammo: " + player.getCurrentAmmo() + " / " + player.getMaxAmmo(); g2d.drawString(ammoText, 15, HEIGHT - 50); }
    }

    private void drawButton(Graphics2D g2d, Rectangle bounds, String text) {
        g2d.setColor(new Color(60, 60, 60)); g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g2d.setColor(Color.WHITE); g2d.setStroke(new BasicStroke(2)); g2d.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
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
            if (player.attack()) {
                double baseX = player.getX() + player.getWidth() / 2.0;
                double baseY = player.getY() + player.getHeight() / 2.0;
                double baseAngle = player.gunAngle;
                int damage = player.getBulletDamage();

                if (player.getCurrentWeapon() == Player.WeaponType.SHOTGUN) {
                    for (int i = 0; i < 8; i++) {
                        double spread = Math.toRadians((rand.nextDouble() - 0.5) * 20);
                        bullets.add(new Bullet(baseX, baseY, baseAngle + spread, damage));
                    }
                } else {
                    bullets.add(new Bullet(baseX, baseY, baseAngle, damage));
                }
            }
        } else if (gameState == GameState.WAVE_COMPLETED) {
            if (hpCardBounds.contains(e.getPoint())) { selectedSkillCard = 0; }
            else if (damageCardBounds.contains(e.getPoint())) { selectedSkillCard = 1; }
            else if (masterCardBounds.contains(e.getPoint())) { selectedSkillCard = 2; }
            if (selectedSkillCard != -1 && confirmButtonBounds.contains(e.getPoint())) {
                int healAmount = (int)(player.getMaxHealth() * 0.40);
                switch (selectedSkillCard) {
                    case 0: 
                        player.increaseMaxHealth(10); 
                        break;
                    case 1: 
                        player.upgradeDamage(); 
                        break;
                    case 2: 
                        player.upgradeMastery(); 
                        break;
                }
                player.heal(healAmount);
                player.switchWeapon(player.getCurrentWeapon()); 
                startNextWave();
            }
        } else if (gameState == GameState.CHEST_OPEN) {
            if (pistolCardBounds.contains(e.getPoint())) { player.switchWeapon(Player.WeaponType.PISTOL); gameState = GameState.PLAYING; } 
            else if (rifleCardBounds.contains(e.getPoint())) { player.switchWeapon(Player.WeaponType.RIFLE); gameState = GameState.PLAYING; }
            else if (shotgunCardBounds.contains(e.getPoint())) { player.switchWeapon(Player.WeaponType.SHOTGUN); gameState = GameState.PLAYING; }
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
        JFrame window = new JFrame("Escape the Death");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.add(new GamePanel());
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}

