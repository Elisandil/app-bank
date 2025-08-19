package services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ThreadPoolManager {

    private static final Logger LOGGER = Logger
            .getLogger(ThreadPoolManager.class.getName());
    private static ThreadPoolManager instance;
    private final ExecutorService executorService;

    private ThreadPoolManager() {
        // Usar un pool de hilos optimizado para operaciones I/O
        this.executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() * 2,
                r -> {
                    Thread t = new Thread(r, "BankingApp-Worker");
                    t.setDaemon(true); // Permitir que la JVM termine aunque estos hilos estén ejecutándose
                    return t;
                }
        );
        // Registrar shutdown hook para limpieza
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        LOGGER.info("ThreadPoolManager inicializado con pool de hilos");
    }

    public static synchronized ThreadPoolManager getInstance() {
        
        if (instance == null) {
            instance = new ThreadPoolManager();
        }
        return instance;
    }

    
    public Future<?> executeAsync(Runnable task) {
        return executorService.submit(() -> {
            
            try {
                task.run();
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error ejecutando tarea asíncrona", ex);
                // Re-lanzar como RuntimeException para que Future.get() la capture
                throw new RuntimeException("Error en tarea asíncrona", ex);
            }
        });
    }

    
    public Future<?> executeWithRetry(Runnable task, int maxRetries, 
            long delayMs) {
        return executorService.submit(() -> {
            int attempts = 0;
            Exception lastException = null;

            while (attempts <= maxRetries) {
                
                try {
                    task.run();
                    return; 
                } catch (Exception ex) {
                    lastException = ex;
                    attempts++;

                    LOGGER.log(Level.WARNING,
                            String.format("Intento %d/%d falló", attempts, 
                                    maxRetries + 1), 
                            ex);

                    if (attempts <= maxRetries) {
                        
                        try {
                            Thread.sleep(delayMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Tarea interrumpida", e);
                        }
                    }
                }
            }

            LOGGER.log(Level.SEVERE, "Todos los reintentos agotados", 
                    lastException);
            throw new RuntimeException("Operación falló después de " + 
                    (maxRetries + 1) + " intentos", lastException);
        });
    }


    public void shutdown() {
        LOGGER.info("Iniciando shutdown del ThreadPoolManager");
        executorService.shutdown();

        try {
            
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                LOGGER.warning("Pool no terminó en 10 segundos, forzando shutdown");
                executorService.shutdownNow();

                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    LOGGER.severe("Pool no pudo ser terminado completamente");
                }
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Shutdown interrumpido", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LOGGER.info("ThreadPoolManager shutdown completado");
    }
}
