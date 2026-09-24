package net.neganote.monilabs.packmode;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public enum PackMode {

    NORMAL,
    HARD,
    EXPERT;

    private static final Logger LOGGER = LogManager.getLogger("MoniLabs-PackMode");

    public boolean hasFreeXpCosts() {
        return this == HARD || this == EXPERT;
    }

    public static PackMode read(Path file) {
        if (Files.notExists(file)) {
            return NORMAL;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) {
                throw new JsonParseException("Expected a pack mode object!");
            }
            JsonElement mode = root.getAsJsonObject().get("mode");
            if (mode == null || !mode.isJsonPrimitive() || !mode.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("Expected a mode string!");
            }
            return valueOf(mode.getAsString().strip().toUpperCase(Locale.ROOT));
        } catch (IOException | JsonParseException | IllegalArgumentException exception) {
            LOGGER.warn("Cannot read pack mode from {} assuming normal XP costs: {}", file, exception.getMessage());
            return NORMAL;
        }
    }
}
