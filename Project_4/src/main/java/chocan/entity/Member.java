package chocan.entity;
/** @author Dawson Sconyers */
public class Member extends Person {
    private static final long serialVersionUID=1L;
    private String status;
    public Member(String n,String id,String a,String c,String s,String z,
            String status) {
        super(n,id,a,c,s,z);
        setStatus(status);
    }
    public String getStatus() {
        return status;
    }
    // Only the two supported membership states are accepted.
    public void setStatus(String x) {
        if(!"Active".equalsIgnoreCase(x)&&!"Suspended".equalsIgnoreCase(x))
            throw new IllegalArgumentException(
                    "Status must be Active or Suspended");
        status="Active".equalsIgnoreCase(x)?"Active":"Suspended";
    }
    // Billing is permitted only while membership is Active.
    public boolean isValid() {
        return status.equals("Active");
    }
}
