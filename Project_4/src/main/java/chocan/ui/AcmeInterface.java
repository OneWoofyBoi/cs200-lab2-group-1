package chocan.ui;
import chocan.control.RecordsControl;
import chocan.data.Database;
import chocan.entity.Member;
import java.util.List;
/** @author Michael Ifeanyichukwu */
public class AcmeInterface {
    private final RecordsControl control;
    // The boundary shares the existing records controller.
    public AcmeInterface(Database d) {
        this(new RecordsControl(d));
    }
    public AcmeInterface(RecordsControl c) {
        control=c;
    }
    // Only the local membership-status update contract crosses this boundary.
    public boolean syncAccounts(List<Member> updates) {
        return control.syncAccounts(updates);
    }
}
