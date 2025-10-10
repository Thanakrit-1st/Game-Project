package src.game;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.InputStream; // New import for loading resource streams

/**
 * Represents the main character in the game.
 * Manages position, speed, movement state, and rendering size.
 */
public class Player {

    // Position (x, y coordinates)
    private int x;
    private int y;

    // Movement speed in pixels per frame
    private final int speed = 5;
    
    // Scale factor to make the character bigger (e.g., 2 means twice the original size)
    private final int scale = 3; // <-- CHANGE THIS NUMBER TO YOUR DESIRED SCALE

    // Movement state flags
    public boolean movingUp = false;
    public boolean movingDown = false;
    public boolean movingLeft = false;
    public boolean movingRight = false;

    // Image for drawing the player
    public BufferedImage image;

    /**
     * Constructor to initialize the player's starting position and load the image.
     * @param startX Initial X coordinate.
     * @param startY Initial Y coordinate.
     */
    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        loadImage();
    }

    /**
     * Loads the player image from the project's resource path using ClassLoader.
     * This is the recommended way for files packaged with the application.
     */
    private void loadImage() {
        // Path starts with '/' to indicate the root of the classpath (which is usually the OOP-PROJECT directory).
        // The path structure mirrors the project structure: /res/images/Protagonist.png
        String resourcePath = "/res/images/Protagonist.png"; 

        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                // If the stream is null, the resource wasn't found at that path.
                throw new IOException("Resource not found in classpath: " + resourcePath);
            }
            
            image = ImageIO.read(is);
            if (image != null) {
                System.out.println("Image loaded successfully as resource stream: " + resourcePath);
            } else {
                 throw new IOException("ImageIO returned null. Could not read image stream.");
            }
        } catch (IOException e) {
            System.err.println("--- IMAGE LOADING ERROR ---");
            System.err.println("Failed to load image from resource path: " + resourcePath);
            System.err.println("Cause: " + e.getMessage());
            System.err.println("Fallback to white square.");
            
            // Create a fallback image (the white square)
            image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
            image.getGraphics().fillRect(0, 0, 32, 32);
        }
    }

    /**
     * Updates the player's position based on the current movement flags.
     */
    public void update() {
        if (movingUp) {
            y -= speed;
        }
        if (movingDown) {
            y += speed;
        }
        if (movingLeft) {
            x -= speed;
        }
        if (movingRight) {
            x += speed;
        }
    }

    // --- Getters and Setters ---
    public int getX() { return x; }
    public int getY() { return y; }
    
    // Getters now return scaled size
    public int getWidth() { 
        // Original width (e.g., 32) * scale (e.g., 2) = 64
        return (image != null ? image.getWidth() : 32) * scale; 
    }
    public int getHeight() { 
        // Original height (e.g., 32) * scale (e.g., 2) = 64
        return (image != null ? image.getHeight() : 32) * scale; 
    }
}
