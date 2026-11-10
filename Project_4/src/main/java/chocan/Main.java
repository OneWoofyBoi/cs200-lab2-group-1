package chocan;

import chocan.control.*;
import chocan.data.Database;
import chocan.data.DataPersistenceManager;
import chocan.ui.*;
import java.nio.file.*;

/** Executable composition root for the local in-memory reference application.
 * @author Landon Liles */
public final class Main {
    private Main() { }
    // One shared store connects the role menus to their controllers.
    public static void main(String[] args) {
        Path outputDirectory=Path.of("output"), stateFile=null;
        try {
            int index=0;
            if(args.length>0 && !args[0].equals("--state")) outputDirectory=Path.of(args[index++]);
            if(index<args.length) {
                if(!args[index++].equals("--state") || index>=args.length) throw new IllegalArgumentException("Missing --state file");
                stateFile=Path.of(args[index++]);
            }
            if(index!=args.length) throw new IllegalArgumentException("Unexpected arguments");
        } catch(IllegalArgumentException e) { System.err.println("Usage: chocan.Main [output-directory] [--state snapshot-file]"); return; }
        Database database;
        try { database=stateFile!=null && Files.exists(stateFile)?DataPersistenceManager.load(stateFile):Database.demo(); }
        catch(java.io.IOException e) { System.err.println("Cannot load snapshot; it has not been changed: "+e.getMessage()); return; }
        ProviderControl provider=new ProviderControl(database);
        RecordsControl records=new RecordsControl(database);
        AccountingControl accounting=new AccountingControl(database);
        accounting.setOutputDirectory(outputDirectory);
        provider.setOutputDirectory(outputDirectory);
        ConsoleInput in=new ConsoleInput(System.in,System.out);
        in.print("ChocAn Project 4 reference application\n"+(stateFile==null?"Demo data is reset each launch.":"Optional snapshot: "+stateFile)+" Reports and directory files are local outputs.");
        try {
            while(true) {
                in.print("1 Provider | 2 Operator | 3 Manager | 0 Exit");
                switch(in.read("Role:")) {
                    case "0": in.print("Goodbye."); return;
                    case "1": new ProviderTerminal(provider).run(in,database); break;
                    case "2": new OperatorConsole(records).run(in,database); break;
                    case "3": new ManagerTerminal(records,accounting).run(in); break;
                    default: in.print("Choose one of the listed options.");
                }
            }
        } catch(ConsoleInput.EndOfInput e) { in.print("\nInput ended. Goodbye."); }
        finally {
            if(stateFile!=null) {
                try { DataPersistenceManager.save(database,stateFile); in.print("Snapshot saved: "+stateFile); }
                catch(java.io.IOException e) { System.err.println("Snapshot could not be saved: "+e.getMessage()); }
            }
        }
    }
}
