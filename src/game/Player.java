package src.game;

import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.awt.Color;
import java.io.IOException; // Added for IOException
import java.io.InputStream; // Added for InputStream
import javax.imageio.ImageIO; // <-- FIX: Added missing import

public class Player extends Entity {

    // --- Player-Specific Fields ---
    public boolean movingUp, movingDown, movingLeft, movingRight;
    public double gunAngle = 0.0;
    public boolean isGunFlipped = false;

    // Weapon System
    public enum WeaponType { PISTOL, SHOTGUN, RIFLE }
    private WeaponType currentWeapon;
    private BufferedImage pistolImage, shotgunImage, rifleImage;
    public BufferedImage equippedWeaponImage;
    
    // Weapon-Specific Stats (combine base + bonus)
    private int currentAmmo;
    private int maxAmmo;
    private int bulletDamage;
    private double reloadTime;
    private long attackCooldown;
    private long lastAttackTime = 0;

    // Stored Player Upgrade Levels
    private int damageUpgradeLevel = 0;
    private int masteryUpgradeLevel = 0;

    // Reloading State
    private boolean isReloading = false;
    private long reloadStartTime;

    // Player scale (overrides Entity's default)
    private final int char_scale = 3;

    public Player(int startX, int startY) {
        super(startX, startY, 100, 100, 5.0); // x, y, health, maxHealth, speed
        this.scale = char_scale; 
        loadImages(); // Load character and weapon images
        switchWeapon(WeaponType.PISTOL);
    }

    private void loadImages() {
        // Load character image using Entity's method
        super.loadImage("/res/images/Protagonist.png", Color.BLUE); 

        // Load weapon images separately (not handled by Entity)
        try {
            pistolImage = ImageIO.read(getClass().getResourceAsStream("/res/images/gun.png"));
            shotgunImage = ImageIO.read(getClass().getResourceAsStream("/res/images/shotgun.png")); 
            rifleImage = ImageIO.read(getClass().getResourceAsStream("/res/images/rifle.png"));
        } catch (Exception e) { System.err.println("Failed to load one or more weapon images."); }
    }

    @Override
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
    
    @Override
    public Rectangle getBounds() {
        int w = getWidth(), h = getHeight(); 
        int hw = (int)(w * 0.5), hh = (int)(h * 0.8);
        return new Rectangle((int)x + (w - hw) / 2, (int)y + (h - hh) / 2, hw, hh);
    }

    public void switchWeapon(WeaponType newWeapon) {
        this.currentWeapon = newWeapon;
        isReloading = false; 
        int baseDamage = 0, baseMaxAmmo = 0;
        double baseReloadTime = 0;
        switch (newWeapon) {
            case PISTOL: equippedWeaponImage = pistolImage; baseDamage = 5; baseMaxAmmo = 15; baseReloadTime = 1500; attackCooldown = 200; break;
            case SHOTGUN: equippedWeaponImage = shotgunImage; baseDamage = 5; baseMaxAmmo = 2; baseReloadTime = 3000; attackCooldown = 1000; break;
            case RIFLE: equippedWeaponImage = rifleImage; baseDamage = 30; baseMaxAmmo = 3; baseReloadTime = 2500; attackCooldown = 800; break;
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
        if (isReloading || (System.currentTimeMillis() - lastAttackTime < attackCooldown) || currentAmmo <= 0) return false;
        lastAttackTime = System.currentTimeMillis();
        currentAmmo--;
        if (currentAmmo <= 0) startReload();
        return true;
    }

    public void startReload() {
        if (!isReloading && currentAmmo < maxAmmo) { isReloading = true; reloadStartTime = System.currentTimeMillis(); }
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
    
    // --- Getters ---
    public double getGunWidth() { return (equippedWeaponImage != null ? equippedWeaponImage.getWidth() : 0) * 2; }
    public double getGunHeight() { return (equippedWeaponImage != null ? equippedWeaponImage.getHeight() : 0) * 2; }
    public int getCurrentAmmo() { return currentAmmo; }
    public int getMaxAmmo() { return maxAmmo; }
    public boolean isReloading() { return isReloading; }
    public int getBulletDamage() { return bulletDamage; }
    public Player.WeaponType getCurrentWeapon() { return currentWeapon; }
}

