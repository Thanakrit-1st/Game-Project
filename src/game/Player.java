package src.game;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.InputStream;

/**
 * Represents the main character in the game.
 * Manages position, speed, movement state, rendering size, and equipped gun.
 */
public class Player {

    // Position (x, y coordinates)
    private int x;
    private int y;

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
        loadImages();
    }

    private void loadImages() {
        // --- Load Player Image ---
        try {
            String resourcePath = "/res/images/Protagonist.png";
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is == null) throw new IOException("Resource not found in classpath: " + resourcePath);
            image = ImageIO.read(is);
            System.out.println("Player image loaded successfully.");
        } catch (IOException e) {
            System.err.println("Failed to load player image: " + e.getMessage());
            image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
            image.getGraphics().fillRect(0, 0, 32, 32);
        }

        // --- Load Gun Image ---
        try {
            String resourcePath = "/res/images/gun.png";
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is == null) throw new IOException("Resource not found in classpath: " + resourcePath);
            gunImage = ImageIO.read(is);
            System.out.println("Gun image loaded successfully.");
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

    // --- Getters and Setters ---
    public int getX() { return x; }
    public int getY() { return y; }

    public int getWidth() {
        return (image != null ? image.getWidth() : 32) * char_scale;
    }
    public int getHeight() {
        return (image != null ? image.getHeight() : 32) * char_scale;
    }

    // NEW: Getters for scaled size of the gun (return double)
    public double getGunWidth() {
        return (gunImage != null ? gunImage.getWidth() : 0) * gun_scale;
    }
    public double getGunHeight() {
        return (gunImage != null ? gunImage.getHeight() : 0) * gun_scale;
    }
}