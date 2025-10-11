package src.game;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.awt.Rectangle; // <-- NEW: Import for collision detection

/**
 * Represents the main character in the game.
 * Manages position, speed, health, and rendering.
 */
public class Player {

    private int x, y;
    private int health;
    private final int maxHealth = 100;
    private final int speed = 5;
    private final int char_scale = 3;
    private final double gun_scale = 0.125;

    public boolean movingUp, movingDown, movingLeft, movingRight;
    public BufferedImage image, gunImage;
    public double gunAngle = 0.0;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.health = this.maxHealth;
        loadImages();
    }

    private void loadImages() {
        try {
            InputStream is = getClass().getResourceAsStream("/res/images/Protagonist.png");
            if (is == null) throw new IOException("Resource not found: /res/images/Protagonist.png");
            image = ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("Failed to load player image: " + e.getMessage());
        }
        try {
            InputStream is = getClass().getResourceAsStream("/res/images/gun.png");
            if (is == null) throw new IOException("Resource not found: /res/images/gun.png");
            gunImage = ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("Failed to load gun image: " + e.getMessage());
        }
    }

    public void update() {
        if (movingUp) y -= speed;
        if (movingDown) y += speed;
        if (movingLeft) x -= speed;
        if (movingRight) x += speed;
    }

    public void updateGunAngle(int mouseX, int mouseY) {
        double dx = mouseX - (this.x + getWidth() / 2.0);
        double dy = mouseY - (this.y + getHeight() / 2.0);
        this.gunAngle = Math.atan2(dy, dx);
    }
    
    public void takeDamage(int amount) {
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

    // --- NEW: Method for collision detection ---
    public Rectangle getBounds() {
        return new Rectangle(x, y, getWidth(), getHeight());
    }

    // --- Getters ---
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return (image != null ? image.getWidth() : 0) * char_scale; }
    public int getHeight() { return (image != null ? image.getHeight() : 0) * char_scale; }
    public double getGunWidth() { return (gunImage != null ? gunImage.getWidth() : 0) * gun_scale; }
    public double getGunHeight() { return (gunImage != null ? gunImage.getHeight() : 0) * gun_scale; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
}
