package com.craftteams;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Guarda em config/craftteams.json quais mods cada time pode craftar. */
public final class CraftConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("craftteams.json");

    private static final Map<String, Set<String>> TEAMS = new TreeMap<>();
    /** Mods que qualquer jogador pode craftar (por padrao so o vanilla). */
    private static final Set<String> ALWAYS = new TreeSet<>();

    private static class Data {
        Map<String, List<String>> teams = new TreeMap<>();
        List<String> alwaysAllowed = new ArrayList<>();
    }

    private CraftConfig() {}

    public static void load() {
        TEAMS.clear();
        ALWAYS.clear();
        boolean existed = Files.exists(FILE);
        if (existed) {
            try (Reader r = Files.newBufferedReader(FILE)) {
                Data d = GSON.fromJson(r, Data.class);
                if (d != null) {
                    if (d.teams != null) d.teams.forEach((k, v) -> TEAMS.put(k, new TreeSet<>(v)));
                    if (d.alwaysAllowed != null) ALWAYS.addAll(d.alwaysAllowed);
                }
            } catch (Exception e) {
                CraftTeams.LOGGER.error("Falha ao ler craftteams.json", e);
            }
        } else {
            ALWAYS.add("minecraft");
        }
        save();
    }

    public static void save() {
        Data d = new Data();
        TEAMS.forEach((k, v) -> d.teams.put(k, new ArrayList<>(v)));
        d.alwaysAllowed = new ArrayList<>(ALWAYS);
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(d, w);
        } catch (Exception e) {
            CraftTeams.LOGGER.error("Falha ao salvar craftteams.json", e);
        }
    }

    public static boolean isAlwaysAllowed(String modId) {
        return ALWAYS.contains(modId);
    }

    public static boolean teamAllows(String team, String modId) {
        Set<String> s = TEAMS.get(team);
        return s != null && s.contains(modId);
    }

    public static Set<String> modsOf(String team) {
        Set<String> s = TEAMS.get(team);
        return s == null ? Collections.emptySet() : s;
    }

    public static void toggle(String team, String modId) {
        Set<String> s = TEAMS.computeIfAbsent(team, k -> new TreeSet<>());
        if (!s.remove(modId)) s.add(modId);
        save();
    }

    public static void removeTeam(String team) {
        TEAMS.remove(team);
        save();
    }
}
