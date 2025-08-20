package config;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;


public class ConfigurationManager {
    private static final Logger LOGGER = Logger.getLogger(ConfigurationManager
            .class.getName());
    private static ConfigurationManager instance;
    private Properties properties;
    
    private static final String DEFAULT_CONFIG_FILE = "application.properties";
    private static final int DEFAULT_CONNECTION_TIMEOUT = 30000; // 30 segundos
    private static final int DEFAULT_READ_TIMEOUT = 15000; // 15 segundos
    private static final int DEFAULT_MAX_RETRIES = 3;
    
    private ConfigurationManager() {
        loadConfiguration();
    }
    
    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }
    
    
    private void loadConfiguration() {
        properties = new Properties();
        loadDefaultProperties();

        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream(DEFAULT_CONFIG_FILE)) {
            
            if (is != null) {
                properties.load(is);
                LOGGER.info("Configuración cargada desde: " + DEFAULT_CONFIG_FILE);
            } else {
                LOGGER.warning("Archivo de configuración no encontrado, "
                        + "usando valores por defecto");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error cargando configuración", e);
        }
        overrideWithSystemProperties();
        
        LOGGER.info("ConfigurationManager inicializado correctamente");
    }

    private void loadDefaultProperties() {
        properties.setProperty("app.name", "Banking App");
        properties.setProperty("app.version", "1.0.0");
        properties.setProperty("app.environment", "development");
        
        // Configuración de red
        properties.setProperty("network.connection.timeout", 
                String.valueOf(DEFAULT_CONNECTION_TIMEOUT));
        properties.setProperty("network.read.timeout", 
                String.valueOf(DEFAULT_READ_TIMEOUT));
        properties.setProperty("network.max.retries", 
                String.valueOf(DEFAULT_MAX_RETRIES));
        
        // Configuración de UI
        properties.setProperty("ui.theme", "system");
        properties.setProperty("ui.window.width", "800");
        properties.setProperty("ui.window.height", "600");
        
        // Configuración de logging
        properties.setProperty("logging.level", "INFO");
        properties.setProperty("logging.file.enabled", "true");
        properties.setProperty("logging.console.enabled", "true");
        
        // Configuración de seguridad
        properties.setProperty("security.session.timeout", "1800000"); // 30 minutos
        properties.setProperty("security.max.login.attempts", "3");
        properties.setProperty("security.lockout.duration", "300000"); // 5 minutos
    }
    

    private void overrideWithSystemProperties() {
        String[] overridableProperties = {
            "app.environment",
            "network.connection.timeout",
            "logging.level",
            "security.session.timeout"
        };
        
        for (String prop : overridableProperties) {
            String systemValue = System.getProperty(prop);
            
            if (systemValue != null && !systemValue.trim().isEmpty()) {
                properties.setProperty(prop, systemValue);
                LOGGER.log(Level.INFO, 
                        "Propiedad sobrescrita por sistema: {0} = {1}", 
                        new Object[]{prop, systemValue});
            }
        }
    }
    
    
    public String getString(String key) {
        return properties.getProperty(key);
    }
    
 
    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    
    public int getInt(String key) {
        String value = properties.getProperty(key);
        
        if (value == null) {
            throw new IllegalArgumentException("Propiedad no encontrada: " + 
                    key);
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Valor inválido para propiedad " + 
                    key + ": " + value, ex);
        }
    }
    

    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Valor inválido para propiedad " + 
                    key + ": " + value + 
                      ", usando valor por defecto: " + defaultValue, e);
            return defaultValue;
        }
    }

    
    public boolean getBoolean(String key) {
        String value = properties.getProperty(key);
        
        if (value == null) {
            throw new IllegalArgumentException("Propiedad no encontrada: " + key);
        }
        return Boolean.parseBoolean(value.trim());
    }
    

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }
    

    public long getLong(String key, long defaultValue) {
        String value = properties.getProperty(key);
        
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Valor inválido para propiedad " + key + 
                    ": " + value + 
                      ", usando valor por defecto: " + defaultValue, e);
            return defaultValue;
        }
    }

    
    public boolean isDevelopmentMode() {
        return "development".equalsIgnoreCase(getString("app.environment", 
                "production"));
    }
    

    public int getConnectionTimeout() {
        return getInt("network.connection.timeout", DEFAULT_CONNECTION_TIMEOUT);
    }
    

    public int getReadTimeout() {
        return getInt("network.read.timeout", DEFAULT_READ_TIMEOUT);
    }
    

    public int getMaxRetries() {
        return getInt("network.max.retries", DEFAULT_MAX_RETRIES);
    }
    

    public long getSessionTimeout() {
        return getLong("security.session.timeout", 1800000L);
    }
    

    public Dimension getDefaultWindowSize() {
        int width = getInt("ui.window.width", 800);
        int height = getInt("ui.window.height", 600);
        return new java.awt.Dimension(width, height);
    }
    

    public synchronized void reload() {
        LOGGER.info("Recargando configuración...");
        loadConfiguration();
    }
}