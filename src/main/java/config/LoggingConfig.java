package config;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.logging.*;


public class LoggingConfig {
    
    public static void initializeLogging() {
        
        try {
            Files.createDirectories(Paths.get("logs"));
            Logger rootLogger = Logger.getLogger("");
            
            Handler[] handlers = rootLogger.getHandlers();
            
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(new CustomFormatter());
            rootLogger.addHandler(consoleHandler);
            
            // Configurar handler para archivo
            FileHandler fileHandler = new FileHandler("logs/banking-app-%g.log", 
                    1024 * 1024, 5, true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new CustomFormatter());
            rootLogger.addHandler(fileHandler);
            
            // Configurar handler separado para errores
            FileHandler errorHandler = new FileHandler(
                    "logs/banking-app-errors-%g.log", 1024 * 1024, 3, true);
            errorHandler.setLevel(Level.WARNING);
            errorHandler.setFormatter(new CustomFormatter());
            rootLogger.addHandler(errorHandler);
            
            // Establecer nivel global
            rootLogger.setLevel(Level.ALL);

            Logger.getLogger(LoggingConfig.class.getName())
                    .info("Sistema de logging inicializado");
            
        } catch (IOException ex) {
            System.err.println("Error configurando logging: " + ex.getMessage());
        }
    }
    

    private static class CustomFormatter extends Formatter {
        private static final String FORMAT = "[%1$tF %1$tT.%1$tL] [%2$-7s] "
                + "[%3$s] %4$s %n";
        
        @Override
        public String format(LogRecord record) {
            return String.format(FORMAT,
                    new Date(record.getMillis()),
                    record.getLevel().getLocalizedName(),
                    record.getLoggerName(),
                    record.getMessage());
        }
        
        @Override
        public String formatMessage(LogRecord record) {
            String message = super.formatMessage(record);

            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                message += "\n" + sw.toString();
            }
            return message;
        }
    }
}