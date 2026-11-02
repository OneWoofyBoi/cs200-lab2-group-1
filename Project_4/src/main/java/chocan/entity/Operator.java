package chocan.entity;
/** @author Dawson Sconyers */
public class Operator extends Person {
    private static final long serialVersionUID=1L;
    private final String loginId;
    private final String password;
    // Identity and configured login credentials belong to one staff record.
    public Operator(String n,String id,String a,String c,String s,String z,
            String login,String password) {
        super(n,id,a,c,s,z);
        this.loginId=Person.text(login,50,"Login");
        if (password == null || password.isBlank() || password.length() > 100
                || password.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(
                    "Password must contain 1 to 100 printable characters");
        }
        this.password = password;
    }
    public Operator(String id,String password) {
        this("Operator",id,"1 Main St","Tuscaloosa","AL","35401",id,password);
    }
    public String getLoginId() {
        return loginId;
    }
    // This local simulation compares the configured classroom password.
    public boolean authenticate(String password) {
        return this.password.equals(password);
    }
}
