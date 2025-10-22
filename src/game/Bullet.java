package src.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

public class Bullet {
    private double x, y;
    private final int bulletWidth = 14;
    private final int bulletHeight = 6;
    private final double speed = 20.0;
    private final double angle;
    private final int damage;
    private final double velX;
    private final double velY;

    public Bullet(double startX, double startY, double angle, int damage) {
        this.angle = angle;
        this.damage = damage;
        double gunTipOffset = 30;
        this.x = startX + Math.cos(angle) * gunTipOffset - bulletWidth / 2.0;
        this.y = startY + Math.sin(angle) * gunTipOffset - bulletHeight / 2.0;
        this.velX = Math.cos(angle) * speed;
        this.velY = Math.sin(angle) * speed;
    }

    public void update() {
        x += velX;
        y += velY;
    }

    public void draw(Graphics2D g2d) {
        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(x + bulletWidth / 2.0, y + bulletHeight / 2.0);
        g2d.rotate(angle);
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(-bulletWidth / 2, -bulletHeight / 2, bulletWidth, bulletHeight);
        g2d.setTransform(oldTransform);
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, bulletWidth, bulletHeight);
    }
    
    public int getDamage() {
        return damage;
    }
}

