package chocan.control;
import chocan.data.Database;
import chocan.entity.Member;
import chocan.entity.Provider;
import chocan.entity.Person;
import chocan.entity.Operator;
import chocan.entity.Manager;
import java.util.List;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
/** @author Dawson Sconyers */
public class RecordsControl {
    private final Database database;
    private Person authenticatedStaff;
    public RecordsControl(Database d) {
        database=Objects.requireNonNull(d);
    }
    // A new login attempt clears any preceding staff session.
    public boolean verifyOperator(String id,String password) {
        logout();
        Operator op=database.findOperator(id);
        if(op!=null&&op.authenticate(password)) {
            authenticatedStaff=op;
            return true;
        }
        return false;
    }
    // Manager authorization uses the configured login ID.
    public boolean verifyManager(String id) {
        logout();
        Manager m=database.findManager(id);
        if(m!=null&&m.verifyAccess(id)) {
            authenticatedStaff=m;
            return true;
        }
        return false;
    }
    public void logout() {
        authenticatedStaff=null;
    }
    public Person getAuthenticatedStaff() {
        return authenticatedStaff;
    }
    // Only an authenticated operator may change member records.
    public boolean manageMembers(String action,Member m) {
        if(!(authenticatedStaff instanceof Operator)||action==null
                ||m==null)return false;
        return switch(action.toLowerCase(Locale.ROOT)) {
            case "add"->database.addMember(m);
            case "edit","update"->database.editMember(m);
            case "delete"->database.deleteMember(m.getNumber());
            default->false;
        };
    }
    // Storage methods return whether the requested change succeeded.
    public boolean manageProviders(String action,Provider p) {
        if(!(authenticatedStaff instanceof Operator)||action==null
                ||p==null)return false;
        return switch(action.toLowerCase(Locale.ROOT)) {
            case "add"->database.addProvider(p);
            case "edit","update"->database.editProvider(p);
            case "delete"->database.deleteProvider(p.getNumber());
            default->false;
        };
    }
    // Validate the entire simulated Acme batch before applying status
    // changes.
    // The batch is validated before any membership status is changed.
    public boolean syncAccounts(List<Member> updates) {
        if(updates==null||updates.stream().anyMatch(m->m==null
                ||database.findMember(m.getNumber())==null))return false;
        Set<String> ids=new HashSet<>();
        if(updates.stream().anyMatch(m->!ids.add(m.getNumber())))return false;
        for (Member m : updates) {
            database.findMember(m.getNumber()).setStatus(m.getStatus());
        }
        return true;
    }
}
