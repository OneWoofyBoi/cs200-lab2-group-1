package chocan.ui;

import chocan.control.*;
import java.time.LocalDate;

/** Manager boundary offers individual reports independently of batch accounting.
 * @author Michael Ifeanyichukwu */
public class ManagerTerminal {
    private final RecordsControl recordsControl;
    private final AccountingControl accountingControl;
    // Staff verification and reporting use their existing controllers.
    public ManagerTerminal(RecordsControl recordsControl,AccountingControl accountingControl) {
        this.recordsControl=recordsControl; this.accountingControl=accountingControl;
    }
    public boolean login(String id) { return recordsControl.verifyManager(id); }
    public boolean memberReport(String id) { return recordsControl.getAuthenticatedStaff() instanceof chocan.entity.Manager && accountingControl.memberReport(id); }
    public boolean providerReport(String id) { return recordsControl.getAuthenticatedStaff() instanceof chocan.entity.Manager && accountingControl.providerReport(id); }
    public boolean summaryReport() { return recordsControl.getAuthenticatedStaff() instanceof chocan.entity.Manager && accountingControl.summaryReport(); }
    public void display(String text) { System.out.println(text); }
    public void displayMainMenu() { display("1 Member report | 2 Provider report | 3 Summary report\n4 Run weekly accounting (simulated timer) | 5 Set report period | 0 Logout"); }
    // Individual reports remain available independently of the batch.
    public void run(ConsoleInput in) {
        if(!login(in.read("Manager number:"))) { in.print("Manager access denied."); return; }
        try {
            while(true) {
                in.print("Report period: "+accountingControl.getWeekStart()+" through "+accountingControl.getWeekEnd());
                displayMainMenu(); String choice=in.read("Choice:");
                try {
                    boolean success;
                    switch(choice) {
                        case "0": return;
                        case "1": success=memberReport(in.read("Member number:")); break;
                        case "2": success=providerReport(in.read("Provider number:")); break;
                        case "3": success=summaryReport(); break;
                        case "4": success=new TimerDaemon(accountingControl).weeklyAccounting(); break;
                        case "5":
                            LocalDate start=in.date("Period start"),end=in.date("Period end (inclusive)");
                            accountingControl.setPeriod(start,end); in.print("Period updated."); continue;
                        default: in.print("Choose one of the listed options."); continue;
                    }
                    if(success) in.print("Report operation succeeded.\n"+accountingControl.getLastReport());
                    else in.print("Report operation failed: "+accountingControl.getLastError());
                } catch(IllegalArgumentException e) { in.print("Invalid entry: "+e.getMessage()); }
            }
        } finally { recordsControl.logout(); }
    }
}
