package chocan.entity;
/** @author Dawson Sconyers */
public class Manager extends Person {
    private static final long serialVersionUID=1L;
    private final String loginId;
    // The configured login ID identifies the authorized manager.
    public Manager(String n,String id,String a,String c,String s,String z,
            String login) {
        super(n,id,a,c,s,z);
        loginId=Person.text(login,50,"Login");
    }
    public Manager(String id) {
        this("Manager",id,"1 Main St","Tuscaloosa","AL","35401",id);
    }
    public String getLoginId() {
        return loginId;
    }
    // Only the configured manager login ID passes this check.
    public boolean verifyAccess(String id) {
        return loginId.equals(id);
    }
}
