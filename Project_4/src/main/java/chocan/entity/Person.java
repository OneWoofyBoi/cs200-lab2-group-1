package chocan.entity;
/** @author Dawson Sconyers */
public class Person implements java.io.Serializable {
    private static final long serialVersionUID=1L;
    protected String name;
    protected String number;
    protected String streetAddress;
    protected String city;
    protected String state;
    protected String zipCode;
    // Validated setters apply the same rules during construction and edits.
    public Person(String name,String number,String street,String city,
            String state,String zip) {
        setName(name);
        if(number==null||!number.matches("[0-9]{9}"))
            throw new IllegalArgumentException(
                    "Number must contain nine digits");
        this.number=number;
        setStreetAddress(street);
        setCity(city);
        setState(state);
        setZipCode(zip);
    }
    // Shared text checks reject blank, overlong or control-character input.
    public static String text(String v,int max,String label) {
        if(v==null||v.isBlank()||v.length()>max
                ||v.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException(
                    label + " must contain 1 to " + max
                    + " printable characters");
        return v.trim();
    }
    public String getName() {
        return name;
    }
    public void setName(String x) {
        name=text(x,25,"Name");
    }
    public String getNumber() {
        return number;
    }
    public String getStreetAddress() {
        return streetAddress;
    }
    public void setStreetAddress(String x) {
        streetAddress=text(x,25,"Street");
    }
    public String getCity() {
        return city;
    }
    public void setCity(String x) {
        city=text(x,14,"City");
    }
    public String getState() {
        return state;
    }
    // State abbreviations are stored in a consistent uppercase form.
    public void setState(String x) {
        if(x==null||!x.matches("[A-Za-z]{2}"))
            throw new IllegalArgumentException("State requires two letters");
        state=x.toUpperCase(java.util.Locale.ROOT);
    }
    public String getZipCode() {
        return zipCode;
    }
    // ZIP codes stay as text so leading zeroes are retained.
    public void setZipCode(String x) {
        if(x==null||!x.matches("[0-9]{5}"))
            throw new IllegalArgumentException("ZIP requires five digits");
        zipCode=x;
    }
}
