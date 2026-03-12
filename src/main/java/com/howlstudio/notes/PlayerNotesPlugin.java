package com.howlstudio.notes;
import com.hypixel.hytale.component.Ref; import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.io.*; import java.nio.file.*; import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
/**
 * PlayerNotes — Staff can attach notes to players. View note history for moderation.
 * /note add <player> <note> — add a note
 * /note view <player> — view all notes
 * /note clear <player> — clear notes
 */
public final class PlayerNotesPlugin extends JavaPlugin {
    private final Map<String, List<String>> notes = new LinkedHashMap<>();
    private Path dataDir;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MM/dd HH:mm");
    public PlayerNotesPlugin(JavaPluginInit init) { super(init); }
    @Override protected void setup() {
        System.out.println("[PlayerNotes] Loading...");
        dataDir = getDataDirectory();
        try { Files.createDirectories(dataDir); } catch (Exception e) {}
        load();
        CommandManager.get().register(new AbstractPlayerCommand("note", "[Staff] Add/view notes on players. /note add|view|clear <player>") {
            @Override protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef pl, World world) {
                String[] args = ctx.getInputString().trim().split("\\s+", 3);
                String sub = args.length > 0 ? args[0].toLowerCase() : "help";
                switch (sub) {
                    case "add" -> {
                        if (args.length < 3) { pl.sendMessage(Message.raw("Usage: /note add <player> <note>")); return; }
                        String key = args[1].toLowerCase();
                        String entry = "[" + LocalDateTime.now().format(FMT) + "] " + pl.getUsername() + ": " + args[2];
                        notes.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
                        save();
                        pl.sendMessage(Message.raw("[Notes] Added note for " + args[1] + "."));
                    }
                    case "view" -> {
                        if (args.length < 2) return;
                        String key = args[1].toLowerCase();
                        List<String> playerNotes = notes.getOrDefault(key, List.of());
                        if (playerNotes.isEmpty()) { pl.sendMessage(Message.raw("[Notes] No notes for " + args[1])); return; }
                        pl.sendMessage(Message.raw("=== Notes for " + args[1] + " (" + playerNotes.size() + ") ==="));
                        for (String n : playerNotes) pl.sendMessage(Message.raw("  " + n));
                    }
                    case "clear" -> {
                        if (args.length < 2) return;
                        notes.remove(args[1].toLowerCase()); save();
                        pl.sendMessage(Message.raw("[Notes] Cleared notes for " + args[1]));
                    }
                    default -> pl.sendMessage(Message.raw("Usage: /note add <player> <note> | /note view <player> | /note clear <player>"));
                }
            }
        });
        System.out.println("[PlayerNotes] Ready.");
    }
    private void save() {
        try {
            StringBuilder sb = new StringBuilder();
            for (var e : notes.entrySet()) for (String n : e.getValue()) sb.append(e.getKey()).append("\t").append(n).append("\n");
            Files.writeString(dataDir.resolve("notes.tsv"), sb.toString());
        } catch (Exception e) {}
    }
    private void load() {
        try {
            Path f = dataDir.resolve("notes.tsv"); if (!Files.exists(f)) return;
            for (String line : Files.readAllLines(f)) {
                int tab = line.indexOf('\t'); if (tab < 0) continue;
                notes.computeIfAbsent(line.substring(0, tab), k -> new ArrayList<>()).add(line.substring(tab + 1));
            }
        } catch (Exception e) {}
    }
    @Override protected void shutdown() { save(); System.out.println("[PlayerNotes] Stopped."); }
}
