package src.game;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.awt.Rectangle;

public class Player {
    private int x, y;
    private int health;
    private int maxHealth = 100;
    private final int speed = 5;
    private final int char_scale = 3;
    private final double gun_scale = 2;
    public boolean movingUp, movingDown, movingLeft, movingRight;
    public BufferedImage image, gunImage;
    public double gunAngle = 0.0;
    
    // --- Flag to track gun orientation ---
    public boolean isGunFlipped = false;

    private int maxAmmo = 15;
    private int currentAmmo;
    private boolean isReloading = false;
    private long reloadStartTime;
    private double reloadTime = 1500;
    private int bulletDamage = 5;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.health = this.maxHealth;
        this.currentAmmo = this.maxAmmo;
        loadImages();
    }

    private void loadImages() {
        try {
            InputStream is = getClass().getResourceAsStream("/res/images/Protagonist.png");
            if (is == null) throw new IOException("Resource not found");
            image = ImageIO.read(is);
        } catch (IOException e) { System.err.println("Failed to load player image: " + e.getMessage()); }
        try {
            InputStream is = getClass().getResourceAsStream("/res/images/gun.png");
            if (is == null) throw new IOException("Resource not found");
            gunImage = ImageIO.read(is);
        } catch (IOException e) { System.err.println("Failed to load gun image: " + e.getMessage()); }
    }

    public void update() {
        if (movingUp) y -= speed;
        if (movingDown) y += speed;
        if (movingLeft) x -= speed;
        if (movingRight) x += speed;

        if (isReloading) {
            if (System.currentTimeMillis() - reloadStartTime >= reloadTime) {
                isReloading = false;
                currentAmmo = maxAmmo;
            }
        }
    }

    public void resetMovementFlags() {
        movingUp = false;
        movingDown = false;
        movingLeft = false;
        movingRight = false;
    }

    public boolean shoot() {
        if (!isReloading && currentAmmo > 0) {
            currentAmmo--;
            if (currentAmmo == 0) {
                startReload();
            }
            return true;
        }
        return false;
    }

    public void startReload() {
        if (!isReloading && currentAmmo < maxAmmo) {
            isReloading = true;
            reloadStartTime = System.currentTimeMillis();
        }
    }
    
    public void increaseMaxHealth(int amount) {
        this.maxHealth += amount;
    }

    public void increaseBulletDamage(int amount) {
        this.bulletDamage += amount;
    }
    
    public void improveGunStats(double reloadReduction, int ammoIncrease) {
        this.reloadTime -= reloadReduction;
        if (this.reloadTime < 100) {
            this.reloadTime = 100;
        }
        this.maxAmmo += ammoIncrease;
    }
    
    public void heal(int amount) {
        this.health += amount;
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
    }

    /**
     * UPDATED: This method now calculates the angle AND determines if the gun should be flipped.
     */
    public void updateGunAngle(int mouseX, int mouseY) {
        double dx = mouseX - (this.x + getWidth() / 2.0);
        double dy = mouseY - (this.y + getHeight() / 2.0);
        this.gunAngle = Math.atan2(dy, dx);

        // This is the crucial logic: check if the angle is pointing to the left
        if (this.gunAngle > Math.PI / 2 || this.gunAngle < -Math.PI / 2) {
            isGunFlipped = true;
        } else {
            isGunFlipped = false;
        }
    }
    
    public void takeDamage(int amount) {
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

    public Rectangle getBounds() {
        int fullWidth = getWidth();
        int fullHeight = getHeight();
        int hitboxWidth = (int)(fullWidth * 0.5);
        int hitboxHeight = (int)(fullHeight * 0.8);
        int hitboxX = x + (fullWidth - hitboxWidth) / 2;
        int hitboxY = y + (fullHeight - hitboxHeight) / 2;
        return new Rectangle(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return (image != null ? image.getWidth() : 0) * char_scale; }
    public int getHeight() { return (image != null ? image.getHeight() : 0) * char_scale; }
   aublic double getGunWidth() { return (gunImage != null ? gunImage.getWidth() : 0) * gun_scale; }
    public double getGunHeight() { return (gunImage != null ? gunImage.getHeight() : 0) * gun_scale; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getCurrentAmmo() { return currentAmmo; }
    public int getMaxAmmo() { return maxAmmo; }
    public boolean isReloading() { return isReloading; }
    public int getBulletDamage() { return bulletDamage; }
}

