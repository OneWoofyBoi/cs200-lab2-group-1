package chocan.ui;
import chocan.control.AccountingControl;
/** @author Michael Ifeanyichukwu */
public class TimerDaemon {
    private final AccountingControl control;
    // The simulated timer delegates to the accounting controller.
    public TimerDaemon(AccountingControl c) {
        control=c;
    }
    // The batch can be triggered manually for a repeatable demonstration.
    public boolean weeklyAccounting() {
        return control.weeklyAccounting();
    }
    public boolean eftData() {
        return control.eftData();
    }
}
