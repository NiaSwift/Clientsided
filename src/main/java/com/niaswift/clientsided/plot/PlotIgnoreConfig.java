package com.niaswift.clientsided.plot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.niaswift.clientsided.Clientsided;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PlotIgnoreConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static PlotIgnoreConfig instance;

    public List<String> plotIgnoreList = new ArrayList<>();

    private final Set<String> plotIgnoreSet = new LinkedHashSet<>();
    private Path configPath;

    /**
     * JSON on disk only contains this shape. Gson must never serialize {@link PlotIgnoreConfig}
     * directly — fields like {@link Path} cannot be serialized on modern Java (module access).
     */
    private static final class FileData {
        List<String> plotIgnoreList = new ArrayList<>();
    }

    public static void load() {
        instance = new PlotIgnoreConfig();
        instance.configPath = FabricLoader.getInstance().getConfigDir().resolve("clientsided.json");

        boolean shouldWriteFreshFile = !Files.exists(instance.configPath);

        if (Files.exists(instance.configPath)) {
            try (Reader reader = Files.newBufferedReader(instance.configPath)) {
                FileData loaded = GSON.fromJson(reader, FileData.class);
                if (loaded != null && loaded.plotIgnoreList != null) {
                    instance.plotIgnoreList = new ArrayList<>(loaded.plotIgnoreList);
                }
            } catch (IOException | JsonSyntaxException | JsonIOException exception) {
                Clientsided.LOGGER.error("Failed to load plot ignore config; backing up and resetting", exception);
                backupBrokenConfig(instance.configPath);
                instance.plotIgnoreList = new ArrayList<>();
                shouldWriteFreshFile = true;
            }
        }

        instance.rebuildSet();

        if (shouldWriteFreshFile) {
            instance.save();
        }
    }

    private static void backupBrokenConfig(Path path) {
        try {
            Path backup = path.resolveSibling("clientsided.json.broken-" + System.currentTimeMillis());
            Files.copy(path, backup, StandardCopyOption.REPLACE_EXISTING);
            Clientsided.LOGGER.warn("Backed up broken config to {}", backup);
        } catch (IOException exception) {
            Clientsided.LOGGER.error("Could not back up broken config file", exception);
        }
    }

    public static PlotIgnoreConfig get() {
        return instance;
    }

    private void rebuildSet() {
        plotIgnoreSet.clear();
        plotIgnoreSet.addAll(plotIgnoreList);
    }

    public boolean contains(String plotId) {
        return plotIgnoreSet.contains(plotId);
    }

    public boolean add(String plotId) {
        if (!plotIgnoreSet.add(plotId)) {
            return false;
        }
        plotIgnoreList.add(plotId);
        save();
        return true;
    }

    public void save() {
        try {
            Files.createDirectories(configPath.getParent());
            FileData data = new FileData();
            data.plotIgnoreList = new ArrayList<>(plotIgnoreList);
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException exception) {
            Clientsided.LOGGER.error("Failed to save plot ignore config", exception);
        }
    }
}
