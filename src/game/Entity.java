package src.game;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.awt.Color;

public abstract class Entity {
    protected double x, y; 
    protected int health;
    protected int maxHealth;
    protected double speed;
    protected BufferedImage image;
    protected int width, height;
    protected double scale = 1.0; 

    public Entity(double startX, double startY, int health, int maxHealth, double speed) {
        this.x = startX;
        this.y = startY;
        this.health = health;
        this.maxHealth = maxHealth;
        this.speed = speed;
    }

    public abstract void update();
    public abstract Rectangle getBounds();

    public void takeDamage(int amount) {
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

    public void draw(Graphics2D g2d) {
        if (image != null) {
            g2d.drawImage(image, (int)x, (int)y, width, height, null);
        }
    }

    protected void loadImage(String path, Color fallbackColor) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) throw new IOException("Resource not found: " + path);
            this.image = ImageIO.read(is);
            if (this.image != null) {
                this.width = (int) (this.image.getWidth() * this.scale);
                this.height = (int) (this.image.getHeight() * this.scale);
            } else {
                 throw new IOException("ImageIO.read returned null for path: " + path);
            }
        } catch (Exception e) {
            System.err.println("Failed to load image: " + path + " | Cause: " + e.getMessage());
            this.width = (int) (32 * this.scale); 
            this.height = (int) (32 * this.scale);
            this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = this.image.createGraphics();
            g.setColor(fallbackColor != null ? fallbackColor : Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.dispose();
        }
    }

    public int getX() { return (int)x; }
    public int getY() { return (int)y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
}

