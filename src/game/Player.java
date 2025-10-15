package src.game;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.awt.Rectangle;

public class Player {

    private int x, y;
    private int health;
    private int maxHealth = 100;
    private final int speed = 5;
    private final int char_scale = 3;
    public boolean movingUp, movingDown, movingLeft, movingRight;
    public BufferedImage characterImage;
    public double gunAngle = 0.0;
    public boolean isGunFlipped = false;

    public enum WeaponType { PISTOL, SHOTGUN, RIFLE }
    private WeaponType currentWeapon;
    private BufferedImage pistolImage, shotgunImage, rifleImage;
    public BufferedImage equippedWeaponImage;
    
    private int currentAmmo;
    private int maxAmmo;
    private int bulletDamage;
    private double reloadTime;
    private long attackCooldown;
    private long lastAttackTime = 0;

    private int damageUpgradeLevel = 0;
    private int masteryUpgradeLevel = 0;

    private boolean isReloading = false;
    private long reloadStartTime;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.health = this.maxHealth;
        loadImages();
        switchWeapon(WeaponType.PISTOL);
    }

    private void loadImages() {
        try {
            characterImage = ImageIO.read(getClass().getResourceAsStream("/res/images/Protagonist.png"));
            pistolImage = ImageIO.read(getClass().getResourceAsStream("/res/images/gun.png"));
            shotgunImage = ImageIO.read(getClass().getResourceAsStream("/res/images/shotgun.png")); 
            rifleImage = ImageIO.read(getClass().getResourceAsStream("/res/images/rifle.png"));
        } catch (Exception e) { System.err.println("Failed to load one or more player/weapon images."); }
    }

    public void update() {
        if (movingUp) y -= speed;
        if (movingDown) y += speed;
        if (movingLeft) x -= speed;
        if (movingRight) x += speed;

        if (isReloading) {
            if (System.currentTimeMillis() - reloadStartTime >= reloadTime) {
                isReloading = false;
                currentAmmo = maxAmmo;
            }
        }
    }
    
    public void switchWeapon(WeaponType newWeapon) {
        this.currentWeapon = newWeapon;
        isReloading = false; 
        
        int baseDamage = 0, baseMaxAmmo = 0;
        double baseReloadTime = 0;

        switch (newWeapon) {
            case PISTOL:
                equippedWeaponImage = pistolImage;
                baseDamage = 7; baseMaxAmmo = 15; baseReloadTime = 1500; attackCooldown = 200;
                break;
            case SHOTGUN:
                equippedWeaponImage = shotgunImage;
                baseDamage = 5; baseMaxAmmo = 5; baseReloadTime = 2500; attackCooldown = 1000;
                break;
            case RIFLE:
                equippedWeaponImage = rifleImage;
                baseDamage = 30; baseMaxAmmo = 2; baseReloadTime = 2500; attackCooldown = 800;
                break;
        }

        int damageBonusPerLevel = (newWeapon == WeaponType.RIFLE) ? 5 : 1;
        this.bulletDamage = baseDamage + (damageUpgradeLevel * damageBonusPerLevel);

        int ammoBonusPerLevel = (newWeapon == WeaponType.PISTOL) ? 5 : 1;
        double reloadReductionPerLevel = (newWeapon == WeaponType.SHOTGUN) ? 100 : 200;
        this.maxAmmo = baseMaxAmmo + (masteryUpgradeLevel * ammoBonusPerLevel);
        this.reloadTime = baseReloadTime - (masteryUpgradeLevel * reloadReductionPerLevel);
        if (this.reloadTime < 100) this.reloadTime = 100;

        currentAmmo = maxAmmo;
    }

    public boolean attack() {
        if (isReloading || (System.currentTimeMillis() - lastAttackTime < attackCooldown) || currentAmmo <= 0) {
            return false;
        }
        lastAttackTime = System.currentTimeMillis();
        currentAmmo--;
        if (currentAmmo <= 0) {
            startReload();
        }
        return true;
    }

    public void startReload() {
        if (!isReloading && currentAmmo < maxAmmo) {
            isReloading = true;
            reloadStartTime = System.currentTimeMillis();
        }
    }
    
    public void updateGunAngle(int mouseX, int mouseY) {
        double dx = mouseX - (this.x + getWidth() / 2.0);
        double dy = mouseY - (this.y + getHeight() / 2.0);
        this.gunAngle = Math.atan2(dy, dx);
        isGunFlipped = this.gunAngle > Math.PI / 2 || this.gunAngle < -Math.PI / 2;
    }

    public void resetMovementFlags() { movingUp = false; movingDown = false; movingLeft = false; movingRight = false; }
    
    public void increaseMaxHealth(int amount) { this.maxHealth += amount; }
    public void upgradeDamage() { this.damageUpgradeLevel++; }
    public void upgradeMastery() { this.masteryUpgradeLevel++; }

    public void heal(int amount) { this.health += amount; if (this.health > this.maxHealth) this.health = this.maxHealth; }
    public void takeDamage(int amount) { this.health -= amount; if (this.health < 0) this.health = 0; }
    
    public Rectangle getBounds() {
        int w = getWidth(), h = getHeight();
        int hw = (int)(w * 0.5), hh = (int)(h * 0.8);
        return new Rectangle(x + (w - hw) / 2, y + (h - hh) / 2, hw, hh);
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return (characterImage != null ? characterImage.getWidth() : 0) * char_scale; }
    public int getHeight() { return (characterImage != null ? characterImage.getHeight() : 0) * char_scale; }
    public double getGunWidth() { return (equippedWeaponImage != null ? equippedWeaponImage.getWidth() : 0) * 2; }
    public double getGunHeight() { return (equippedWeaponImage != null ? equippedWeaponImage.getHeight() : 0) * 2; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getCurrentAmmo() { return currentAmmo; }
    public int getMaxAmmo() { return maxAmmo; }
    public boolean isReloading() { return isReloading; }
    public int getBulletDamage() { return bulletDamage; }
    public Player.WeaponType getCurrentWeapon() { return currentWeapon; }
}

