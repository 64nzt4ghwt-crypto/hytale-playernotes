package com.howlstudio.playernotes;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
public final class PlayerNotesPlugin extends JavaPlugin {
    private NotesManager mgr;
    public PlayerNotesPlugin(JavaPluginInit init){super(init);}
    @Override protected void setup(){
        System.out.println("[PlayerNotes] Loading...");
        mgr=new NotesManager(getDataDirectory());
        CommandManager.get().register(mgr.getNoteCommand());
        System.out.println("[PlayerNotes] Ready.");
    }
    @Override protected void shutdown(){if(mgr!=null)mgr.save();System.out.println("[PlayerNotes] Stopped.");}
}
