package net.qilla.destructible.util;

import net.qilla.destructible.Destructible;
import org.bukkit.Bukkit;
import java.util.concurrent.*;
import java.util.logging.Level;

public class DExecutor {

    private static final int DEFAULT_SHUTDOWN_TIMEOUT = 10;
    private final ExecutorService executor;
    private final Destructible plugin;

    public DExecutor(Destructible plugin, int threadPoolSize) {
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
        this.plugin = plugin;
    }

    public void submitAsync(Runnable task) {
        executor.submit(wrapWithLogging(task));
    }

    public <T> CompletableFuture<T> submitAsync(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch(Exception e) {
                plugin.getLogger().log(Level.SEVERE, "An error occured during task execution: ", e);
                throw new CompletionException(e);
            }
        }, executor);
    }

    public void submitSync(Runnable task) {
        Bukkit.getScheduler().runTask(plugin, wrapWithLogging(task));
    }

    public void shutdown() {
        plugin.getLogger().info("Shutting down DExecutor.");
        executor.shutdown();

        try {
            if(!executor.awaitTermination(DEFAULT_SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                plugin.getLogger().warning("Executor did not shutdown in time. Forcefully shutting down.");
                executor.shutdownNow();

                if(!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    plugin.getLogger().warning("Executor did not shutdown properly, even after forced shutdown.");
                }
            }
        } catch(InterruptedException e) {
            plugin.getLogger().log(Level.SEVERE, "Shutdown interrupted. Forcing shutdown now.", e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        plugin.getLogger().info("DExecutor shutdown complete.");
    }

    private Runnable wrapWithLogging(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch(Exception e) {
                plugin.getLogger().log(Level.SEVERE, "An error occured during task execution: ", e);
            }
        };
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}