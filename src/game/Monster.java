package src.game;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Color;

public class Monster extends Entity {
    private final boolean isBoss;
    private final boolean isMysterious;
    private Player targetPlayer; 

    public Monster(double startX, double startY, int health, double speed, String imagePath, boolean isBoss, boolean isMysterious, Player playerToChase) {
        super(startX, startY, health, health, speed); 
        this.isBoss = isBoss;
        this.isMysterious = isMysterious;
        this.targetPlayer = playerToChase; 
        this.scale = isBoss ? 2.5 : 1.5; 
        Color fallback = isBoss ? Color.MAGENTA : (isMysterious ? Color.CYAN : new Color(128, 0, 128));
        super.loadImage(imagePath, fallback);
    }

    @Override
    public void update() {
        if (targetPlayer == null) return; 
        double playerCenterX = targetPlayer.getX() + targetPlayer.getWidth() / 2.0;
        double playerCenterY = targetPlayer.getY() + targetPlayer.getHeight() / 2.0;
        double dirX = playerCenterX - (this.x + this.width / 2.0);
        double dirY = playerCenterY - (this.y + this.height / 2.0);
        double distance = Math.sqrt(dirX * dirX + dirY * dirY);
        if (distance > 0) {
            this.x += (dirX / distance) * speed;
            this.y += (dirY / distance) * speed;
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, width, height);
    }

    public boolean isBoss() { return isBoss; }
    public boolean isMysterious() { return isMysterious; }
}

