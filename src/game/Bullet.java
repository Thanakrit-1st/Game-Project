package src.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Represents a single bullet fired by the player.
 * Manages its own position, velocity, and rendering.
 */
public class Bullet {

    private double x, y; // Use double for precise position
    private final int size = 8; // The width and height of the bullet
    private final double speed = 10.0; // The speed at which the bullet travels

    // The velocity components (how much x and y change each frame)
    private final double velX;
    private final double velY;

    /**
     * Constructor to create a new bullet.
     * @param startX The starting X coordinate (usually the player's center).
     * @param startY The starting Y coordinate (usually the player's center).
     * @param angle  The angle (in radians) at which the bullet should travel.
     */
    public Bullet(double startX, double startY, double angle) {
        this.x = startX;
        this.y = startY;

        // Calculate velocity based on the angle of the gun
        // This uses trigonometry to determine the direction of travel
        this.velX = Math.cos(angle) * speed;
        this.velY = Math.sin(angle) * speed;
    }

    /**
     * Updates the bullet's position based on its velocity.
     */
    public void update() {
        x += velX;
        y += velY;
    }

    /**
     * Draws the bullet on the screen.
     * @param g2d The Graphics2D context to draw with.
     */
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW); // A bright color for the bullet
        g2d.fillOval((int)x, (int)y, size, size); // Draw a small circle
    }

    /**
     * Creates a rectangle representing the bullet's bounds.
     * Useful for collision detection or checking if it's off-screen.
     */
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, size, size);
    }
}