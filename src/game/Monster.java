package src.game;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.awt.Color;

public class Monster {
    private double x, y;
    private int health;
    private final double speed;
    private BufferedImage image;
    private final double scale = 1.5;
    private int width, height;
    private final boolean isBoss;
    private final boolean isMysterious;

    public Monster(double startX, double startY, int health, double speed, String imagePath, boolean isBoss, boolean isMysterious) {
        this.x = startX;
        this.y = startY;
        this.health = health;
        this.speed = speed;
        this.isBoss = isBoss;
        this.isMysterious = isMysterious;
        loadImage(imagePath);

        double currentScale = isBoss ? scale + 1.0 : scale;

        if (this.image != null) {
            this.width = (int) (image.getWidth() * currentScale);
            this.height = (int) (image.getHeight() * currentScale);
        } else {
            this.width = (int) (32 * currentScale);
            this.height = (int) (32 * currentScale);
        }
    }

    private void loadImage(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) throw new IOException("Resource not found: " + path);
            image = ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("Failed to load monster image: " + e.getMessage());
            image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            if (isBoss) g.setColor(Color.MAGENTA);
            else if (isMysterious) g.setColor(Color.CYAN); // Fallback color for Mysterious
            else g.setColor(new Color(128, 0, 128));
            g.fillRect(0, 0, 32, 32);
            g.dispose();
        }
    }

    public void update(Player player) {
        double playerCenterX = player.getX() + player.getWidth() / 2.0;
        double playerCenterY = player.getY() + player.getHeight() / 2.0;
        double dirX = playerCenterX - (this.x + this.width / 2.0);
        double dirY = playerCenterY - (this.y + this.height / 2.0);
        double distance = Math.sqrt(dirX * dirX + dirY * dirY);
        
        if (distance > 0) {
            this.x += (dirX / distance) * speed;
            this.y += (dirY / distance) * speed;
        }
    }

    public void draw(Graphics2D g2d) {
        if (image != null) {
            g2d.drawImage(image, (int)x, (int)y, width, height, null);
        }
    }

    public void takeDamage(int amount) {
        this.health -= amount;
    }

    public int getHealth() { return health; }
    public boolean isBoss() { return isBoss; }
    public boolean isMysterious() { return isMysterious; }
    public Rectangle getBounds() { return new Rectangle((int)x, (int)y, width, height); }
}

