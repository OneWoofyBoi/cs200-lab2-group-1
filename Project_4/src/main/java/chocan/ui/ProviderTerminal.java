package chocan.ui;

import chocan.control.ProviderControl;
import chocan.entity.*;
import chocan.data.Database;
import java.time.ZoneId;
import java.util.Date;

/** Provider boundary delegates validation and recording to its controller.
 * @author Landon Liles */
public class ProviderTerminal {
    private final ProviderControl control;
    private String providerId;
    // Provider actions share one controller and its database.
    public ProviderTerminal(ProviderControl control) { this.control = control; }
    public boolean verify(String id) { providerId=null; if (!control.verify(id)) return false; providerId=id; return true; }
    public String validateCard(String id) { return control.validateCard(id); }
    public boolean bill(ServiceRecord record) { return control.bill(record); }
    public boolean addComments(String text) { return control.addComments(text); }
    // The boolean result reports whether directory export succeeded.
    public boolean requestDirectory() { return control.requestDirectory(); }
    public void display(String text) { System.out.println(text); }
    public void displayMainMenu() { display("1 Validate member | 2 Record service | 3 Provider directory | 0 Logout"); }
    // The terminal gathers input and delegates business validation.
    public void run(ConsoleInput in, Database db) {
        if (!verify(in.read("Provider number:"))) { in.print("Provider access denied."); return; }
        try { while (true) {
            displayMainMenu(); String choice=in.read("Choice:");
            try {
                switch(choice) {
                    case "0": return;
                    case "1": in.print(validateCard(in.read("Member number:"))); break;
                    case "2":
                        String member=in.read("Member number:");
                        String status=validateCard(member); in.print(status);
                        if (!status.equals("Validated")) break;
                        Date date=Date.from(in.serviceDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
                        String code=in.read("Six-digit service code:");
                        ServiceOffering offering=db.getDirectory().findOffering(code);
                        if(offering==null) { in.print("Unknown service code; no record saved."); break; }
                        in.print("Service: "+offering.getServiceName()+" | Fee: $"+String.format(java.util.Locale.US,"%.2f",offering.getFee()));
                        if (!in.yes("Confirm this service")) { in.print("Cancelled; no record saved."); break; }
                        String comments=in.read("Optional comments (maximum 100 characters):");
                        if (control.recordService(member,code,date,comments,true)) in.print("Service recorded successfully.");
                        else in.print("Record rejected: "+control.getLastError());
                        break;
                    case "3":
                        if(requestDirectory()) in.print(control.directoryText());
                        else in.print("Directory request failed: "+control.getLastError());
                        break;
                    default: in.print("Choose one of the listed options.");
                }
            } catch (IllegalArgumentException e) { in.print("Invalid entry: "+e.getMessage()); }
        } } finally { providerId=null; control.logout(); }
    }
}
