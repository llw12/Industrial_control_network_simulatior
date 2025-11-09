package com.industrial.sim.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for safe path operations
 */
public class PathUtils {
    
    /**
     * Sanitize a string to be used as a filename or directory name
     * Removes path traversal characters and limits to alphanumeric, dash, underscore
     */
    public static String sanitizePathComponent(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Path component cannot be null or empty");
        }
        
        // Remove any path traversal attempts
        String sanitized = input.replaceAll("[/\\\\]", "");
        
        // Remove any parent directory references
        sanitized = sanitized.replaceAll("\\.\\.", "");
        
        // Only allow alphanumeric, dash, underscore, and dot
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9_\\-.]", "_");
        
        // Ensure it's not empty after sanitization
        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("Invalid path component after sanitization");
        }
        
        return sanitized;
    }
    
    /**
     * Create a safe path under a base directory
     * Ensures the resulting path is within the base directory
     */
    public static Path createSafePath(Path baseDir, String... components) {
        Path result = baseDir;
        
        for (String component : components) {
            String sanitized = sanitizePathComponent(component);
            result = result.resolve(sanitized);
        }
        
        // Normalize the path and ensure it's still under base directory
        Path normalized = result.normalize();
        if (!normalized.startsWith(baseDir.normalize())) {
            throw new SecurityException("Path traversal attempt detected");
        }
        
        return normalized;
    }
}
