package chocan.entity;
/** @author Dawson Sconyers */
public class Provider extends Person {
    private static final long serialVersionUID=1L;
    private double totalFeeOwed;
    private int consultationCount;
    // Identity fields use the shared Person validation rules.
    public Provider(String n,String id,String a,String c,String s,String z) {
        super(n,id,a,c,s,z);
    }
    public double getTotalFeeOwed() {
        return totalFeeOwed;
    }
    public int getConsultationCount() {
        return consultationCount;
    }
    // Stores totals for the selected reporting period.
    // Period totals replace prior values rather than accumulate lifetime
    // fees.
    public void setAccountingTotals(int count,java.math.BigDecimal total) {
        if (count < 0 || count > 999 || total == null
                || total.signum() < 0
                || total.compareTo(new java.math.BigDecimal("99999.99")) > 0
                || total.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(
                    "Invalid period consultation count or fee total");
        }
        consultationCount=count;
        totalFeeOwed=total.doubleValue();
    }
}
