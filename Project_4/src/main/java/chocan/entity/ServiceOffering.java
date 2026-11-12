package chocan.entity;
import java.math.BigDecimal;
/** @author Dylan Filmer */
public class ServiceOffering implements java.io.Serializable {
    private static final long serialVersionUID=1L;
    private final String serviceCode,serviceName;
    private final BigDecimal fee;
    // Each directory entry pairs a code, readable name and fee.
    public ServiceOffering(String c,String n,double fee) {
        this(c,n,BigDecimal.valueOf(fee));
    }
    public ServiceOffering(String c,String n,BigDecimal fee) {
        if(c==null||!c.matches("[0-9]{6}"))throw new IllegalArgumentException("Service code requires six digits");
        serviceCode=c;
        serviceName=Person.text(n,20,"Service name");
        if(fee==null||fee.signum()<0||fee.compareTo(new BigDecimal("999.99"))>0||fee.stripTrailingZeros().scale()>2)throw new IllegalArgumentException("Fee must be 0.00 to 999.99 with at most two decimals");
        this.fee=fee.setScale(2);
    }
    public String getServiceCode() {
        return serviceCode;
    }
    public String getServiceName() {
        return serviceName;
    }
    public double getFee() {
        return fee.doubleValue();
    }
    // Decimal amounts preserve exact cents for accounting totals.
    public BigDecimal getFeeAmount() {
        return fee;
    }
}
