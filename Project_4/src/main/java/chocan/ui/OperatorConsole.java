package chocan.ui;

import chocan.control.RecordsControl;
import chocan.entity.*;
import chocan.data.Database;

/** Administrative boundary for authenticated operators.
 * @author Landon Liles */
public class OperatorConsole {
    private final RecordsControl control;
    // All administrative actions use the authenticated records controller.
    public OperatorConsole(RecordsControl control) { this.control=control; }
    public boolean login(String id,String password) { return control.verifyOperator(id,password); }
    public boolean manageMembers(String action,Member member) { return control.manageMembers(action,member); }
    public boolean manageProviders(String action,Provider provider) { return control.manageProviders(action,provider); }
    public void display(String text) { System.out.println(text); }
    public void displayMainMenu() { display("1 List members | 2 Add member | 3 Edit member | 4 Delete member\n5 List providers | 6 Add provider | 7 Edit provider | 8 Delete provider\n9 Simulate Acme member status update | 0 Logout"); }
    // The menu gathers values before requesting a record change.
    public void run(ConsoleInput in,Database db) {
        if(!login(in.read("Operator login:"),in.read("Password (visible in this local CLI):"))) { in.print("Operator access denied."); return; }
        try {
            while(true) {
                displayMainMenu(); String choice=in.read("Choice:");
                try {
                    switch(choice) {
                        case "0": return;
                        case "1": for(Member m:db.getMembers()) in.print(m.getNumber()+" | "+m.getName()+" | "+m.getStatus()); break;
                        case "5": for(Provider p:db.getProviders()) in.print(p.getNumber()+" | "+p.getName()); break;
                        case "2": case "3": {
                            String id=in.read("Member number:");
                            if(choice.equals("3") && db.findMember(id)==null) { in.print("Member not found."); break; }
                            Member m=readMember(in,id);
                            in.print(manageMembers(choice.equals("2")?"add":"edit",m)?"Member saved.":"Member not saved (duplicate, missing, or unauthorized)."); break;
                        }
                        case "4": {
                            Member m=db.findMember(in.read("Member number to delete:"));
                            if(m==null) { in.print("Member not found."); break; }
                            in.print(in.yes("Delete "+m.getName()) && manageMembers("delete",m)?"Member deleted.":"Member not deleted."); break;
                        }
                        case "6": case "7": {
                            String id=in.read("Provider number:");
                            if(choice.equals("7") && db.findProvider(id)==null) { in.print("Provider not found."); break; }
                            Provider p=new Provider(in.read("Name:"),id,in.read("Street address:"),in.read("City:"),in.read("State (2 letters):"),in.read("ZIP (5 digits):"));
                            in.print(manageProviders(choice.equals("6")?"add":"edit",p)?"Provider saved.":"Provider not saved (duplicate, missing, or unauthorized)."); break;
                        }
                        case "8": {
                            Provider p=db.findProvider(in.read("Provider number to delete:"));
                            if(p==null) { in.print("Provider not found."); break; }
                            in.print(in.yes("Delete "+p.getName()) && manageProviders("delete",p)?"Provider deleted.":"Provider not deleted."); break;
                        }
                        case "9": {
                            Member m=db.findMember(in.read("Member number:"));
                            if(m==null) { in.print("Member not found."); break; }
                            Member update=new Member(m.getName(),m.getNumber(),m.getStreetAddress(),m.getCity(),m.getState(),m.getZipCode(),in.read("Status (Active/Suspended):"));
                            in.print(new AcmeInterface(control).syncAccounts(java.util.List.of(update))?"Simulated Acme update accepted.":"Update rejected."); break;
                        }
                        default: in.print("Choose one of the listed options.");
                    }
                } catch(IllegalArgumentException e) { in.print("Invalid entry: "+e.getMessage()); }
            }
        } finally { control.logout(); }
    }
    private Member readMember(ConsoleInput in,String id) {
        return new Member(in.read("Name:"),id,in.read("Street address:"),in.read("City:"),in.read("State (2 letters):"),in.read("ZIP (5 digits):"),in.read("Status (Active/Suspended):"));
    }
}
