package com.howlstudio.playernotes;
import com.hypixel.hytale.component.Ref; import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.nio.file.*; import java.util.*;
public class NotesManager {
    private final Path dataDir;
    private final Map<String,List<String>> notes=new LinkedHashMap<>(); // lowercaseName → list of notes
    public NotesManager(Path d){this.dataDir=d;try{Files.createDirectories(d);}catch(Exception e){}load();}
    public void save(){try{StringBuilder sb=new StringBuilder();for(var e:notes.entrySet())for(String n:e.getValue())sb.append(e.getKey()+"\t"+n+"\n");Files.writeString(dataDir.resolve("notes.txt"),sb.toString());}catch(Exception e){}}
    private void load(){try{Path f=dataDir.resolve("notes.txt");if(!Files.exists(f))return;for(String l:Files.readAllLines(f)){String[]p=l.split("\t",2);if(p.length==2)notes.computeIfAbsent(p[0],k->new ArrayList<>()).add(p[1]);}}catch(Exception e){}}
    public AbstractPlayerCommand getNoteCommand(){
        return new AbstractPlayerCommand("note","[Staff] Player notes. /note <player> <text> | /note <player> | /note <player> del <n>"){
            @Override protected void execute(CommandContext ctx,Store<EntityStore> store,Ref<EntityStore> ref,PlayerRef playerRef,World world){
                String[]args=ctx.getInputString().trim().split("\\s+",3);
                if(args.length<1||args[0].isEmpty()){playerRef.sendMessage(Message.raw("Usage: /note <player> [text] | /note <player> del <n>"));return;}
                String target=args[0].toLowerCase();
                if(args.length==1){// view notes
                    List<String> ns=notes.get(target);
                    if(ns==null||ns.isEmpty()){playerRef.sendMessage(Message.raw("[Notes] No notes for "+target));return;}
                    playerRef.sendMessage(Message.raw("[Notes] §6"+target+"§r ("+ns.size()+"):"));
                    for(int i=0;i<ns.size();i++)playerRef.sendMessage(Message.raw("  §7"+(i+1)+"§r. "+ns.get(i)));
                    return;
                }
                if(args[1].equalsIgnoreCase("del")&&args.length==3){try{int idx=Integer.parseInt(args[2])-1;List<String> ns=notes.get(target);if(ns==null||idx<0||idx>=ns.size()){playerRef.sendMessage(Message.raw("[Notes] Invalid note number."));return;}String removed=ns.remove(idx);save();playerRef.sendMessage(Message.raw("[Notes] Deleted note: "+removed));}catch(Exception e){playerRef.sendMessage(Message.raw("[Notes] Usage: /note <player> del <n>"));}return;}
                String note=(args.length==2?args[1]:args[1]+" "+args[2])+" §7["+playerRef.getUsername()+"]";
                notes.computeIfAbsent(target,k->new ArrayList<>()).add(note);save();
                playerRef.sendMessage(Message.raw("[Notes] Added note for §6"+target+"§r: "+note));
            }
        };
    }
}
