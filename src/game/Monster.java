package src.game;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.awt.Color;

/**
 * Represents an enemy monster that chases the player.
 * Manages its own position, health, speed, and rendering.
 */
public class Monster {
    private double x, y;
    private int health;
    private final double speed;
    private BufferedImage image;
    private final int scale = 3;
    private int width, height;

    /**
     * Constructor for creating a new monster.
     * @param startX The initial X coordinate.
     * @param startY The initial Y coordinate.
     * @param health The monster's starting health.
     * @param speed The monster's movement speed.
     * @param imagePath The resource path to the monster's image.
     */
    public Monster(double startX, double startY, int health, double speed, String imagePath) {
        this.x = startX;
        this.y = startY;
        this.health = health;
        this.speed = speed;
        loadImage(imagePath);

        if (this.image != null) {
            this.width = image.getWidth() * scale;
            this.height = image.getHeight() * scale;
        } else {
            this.width = 32 * scale; // Fallback size
            this.height = 32 * scale;
        }
    }

    private void loadImage(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) throw new IOException("Resource not found: " + path);
            image = ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("Failed to load monster image: " + e.getMessage());
            // Create a fallback purple square if image fails to load
            image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(new Color(128, 0, 128)); // Purple
            g.fillRect(0, 0, 32, 32);
            g.dispose();
        }
    }

    /**
     * Updates the monster's position to move towards the player.
     * @param player The player object to chase.
     */
    public void update(Player player) {
        // Calculate the direction vector from the monster to the player's center
        double playerCenterX = player.getX() + player.getWidth() / 2.0;
        double playerCenterY = player.getY() + player.getHeight() / 2.0;
        
        double dirX = playerCenterX - (this.x + this.width / 2.0);
        double dirY = playerCenterY - (this.y + this.height / 2.0);
        
        // Normalize the vector to get a consistent speed in all directions
        double distance = Math.sqrt(dirX * dirX + dirY * dirY);
        
        if (distance > 0) { // Avoid division by zero
            double moveX = (dirX / distance) * speed;
            double moveY = (dirY / distance) * speed;
            
            // Update position
            this.x += moveX;
            this.y += moveY;
        }
    }

    /**
     * Draws the monster on the screen.
     * @param g2d The graphics context to draw with.
     */
    public void draw(Graphics2D g2d) {
        if (image != null) {
            g2d.drawImage(image, (int)x, (int)y, width, height, null);
        }
    }

    /**
     * Reduces the monster's health by a given amount.
     * @param amount The amount of damage to take.
     */
    public void takeDamage(int amount) {
        this.health -= amount;
    }

    public int getHealth() {
        return health;
    }

    /**
     * Returns the bounding box of the monster for collision detection.
     */
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, width, height);
    }
}
