package src.game;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.InputStream;

/**
 * Represents the main character in the game.
 * Manages position, speed, health, and rendering.
 */
public class Player {

    // Position (x, y coordinates)
    private int x;
    private int y;

    // --- NEW: Health Attributes ---
    private int health;
    private final int maxHealth = 100;

    // Movement speed in pixels per frame
    private final int speed = 5;

    // Scale factors
    private final int char_scale = 3;
    private final double gun_scale = 0.125;

    // Movement state flags
    public boolean movingUp = false;
    public boolean movingDown = false;
    public boolean movingLeft = false;
    public boolean movingRight = false;

    // Images for drawing
    public BufferedImage image;
    public BufferedImage gunImage;

    // Gun rotation angle
    public double gunAngle = 0.0;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.health = this.maxHealth; // Start with full health
        loadImages();
    }

    private void loadImages() {
        // --- Load Player Image ---
        try {
            String resourcePath = "/res/images/Protagonist.png";
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is == null) throw new IOException("Resource not found: " + resourcePath);
            image = ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("Failed to load player image: " + e.getMessage());
            image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        }

        // --- Load Gun Image ---
        try {
            String resourcePath = "/res/images/gun.png";
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is == null) throw new IOException("Resource not found: " + resourcePath);
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
        int playerCenterX = this.x + getWidth() / 2;
        int playerCenterY = this.y + getHeight() / 2;
        double dx = mouseX - playerCenterX;
        double dy = mouseY - playerCenterY;
        this.gunAngle = Math.atan2(dy, dx);
    }
    
    // --- NEW: Method to reduce player's health ---
    public void takeDamage(int amount) {
        this.health -= amount;
        if (this.health < 0) {
            this.health = 0; // Prevent health from going below zero
        }
    }

    // --- Getters ---
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return (image != null ? image.getWidth() : 0) * char_scale; }
    public int getHeight() { return (image != null ? image.getHeight() : 0) * char_scale; }
    public double getGunWidth() { return (gunImage != null ? gunImage.getWidth() : 0) * gun_scale; }
    public double getGunHeight() { return (gunImage != null ? gunImage.getHeight() : 0) * gun_scale; }
    public int getHealth() { return health; } // <-- NEW
    public int getMaxHealth() { return maxHealth; } // <-- NEW
}