package src.game;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.imageio.ImageIO;

public class Chest {
    private int x, y;
    private BufferedImage image;
    private int width, height;

    public Chest(int x, int y) {
        this.x = x;
        this.y = y;
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/res/images/chest.png"));
            width = image.getWidth();
            height = image.getHeight();
        } catch (Exception e) {
            System.err.println("Failed to load chest image.");
            width = 32;
            height = 32;
        }
    }

    public void draw(Graphics2D g2d) {
        if (image != null) {
            g2d.drawImage(image, x, y, null);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
