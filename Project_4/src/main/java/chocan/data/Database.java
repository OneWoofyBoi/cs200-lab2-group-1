package chocan.data;
import chocan.entity.*;
import java.util.*;
import java.time.*;
/** @author Michael Ifeanyichukwu */
public class Database implements java.io.Serializable {
    private static final long serialVersionUID=1L;
    private final Map<String,Member> members=new LinkedHashMap<>();
    private final Map<String,Provider> providers=new LinkedHashMap<>();
    private final Map<String,Operator> operators=new LinkedHashMap<>();
    private final Map<String,Manager> managers=new LinkedHashMap<>();
    private final Map<ServiceRecord,Member> recordMembers=new IdentityHashMap<>();
    private final Map<ServiceRecord,Provider> recordProviders=new IdentityHashMap<>();
    private final List<ServiceRecord> records=new ArrayList<>();
    private final ProviderDirectory directory=new ProviderDirectory();
    public List<Member> getMembers() {
        return List.copyOf(members.values());
    }
    public List<Provider> getProviders() {
        return List.copyOf(providers.values());
    }
    public List<Operator> getOperators() {
        return List.copyOf(operators.values());
    }
    public List<Manager> getManagers() {
        return List.copyOf(managers.values());
    }
    public List<ServiceRecord> getServiceRecords() {
        return List.copyOf(records);
    }
    public ProviderDirectory getDirectory() {
        return directory;
    }
    // Member lookups use the stored nine-digit identifier.
    public Member findMember(String id) {
        return members.get(id);
    }
    public Provider findProvider(String id) {
        return providers.get(id);
    }
    public Operator findOperator(String id) {
        return operators.values().stream().filter(x->x.getNumber().equals(id)||x.getLoginId().equals(id)).findFirst().orElse(null);
    }
    public Manager findManager(String id) {
        return managers.values().stream().filter(x->x.getNumber().equals(id)||x.getLoginId().equals(id)).findFirst().orElse(null);
    }
    public boolean addMember(Member x) {
        return x!=null&&members.putIfAbsent(x.getNumber(),x)==null;
    }
    public boolean editMember(Member x) {
        return x!=null&&members.replace(x.getNumber(),x)!=null;
    }
    public boolean deleteMember(String id) {
        return members.remove(id)!=null;
    }
    public boolean addProvider(Provider x) {
        return x!=null&&providers.putIfAbsent(x.getNumber(),x)==null;
    }
    public boolean editProvider(Provider x) {
        return x!=null&&providers.replace(x.getNumber(),x)!=null;
    }
    public boolean deleteProvider(String id) {
        return providers.remove(id)!=null;
    }
    public boolean addOperator(Operator x) {
        return x!=null&&operators.putIfAbsent(x.getNumber(),x)==null;
    }
    public boolean addManager(Manager x) {
        return x!=null&&managers.putIfAbsent(x.getNumber(),x)==null;
    }
    public boolean addServiceRecord(ServiceRecord r) {
        if(r==null||records.contains(r))return false;
        Member m=findMember(r.getMemberNumber());
        Provider p=findProvider(r.getProviderNumber());
        if(m==null||p==null)return false;
        recordMembers.put(r,new Member(m.getName(),m.getNumber(),m.getStreetAddress(),m.getCity(),m.getState(),m.getZipCode(),m.getStatus()));
        recordProviders.put(r,new Provider(p.getName(),p.getNumber(),p.getStreetAddress(),p.getCity(),p.getState(),p.getZipCode()));
        records.add(r);
        return true;
    }
    public Member memberForRecord(ServiceRecord r) {
        return recordMembers.get(r);
    }
    public Provider providerForRecord(ServiceRecord r) {
        return recordProviders.get(r);
    }
    public List<ServiceRecord> recordsForPeriod(Date start,Date end) {
        if(start==null||end==null||!end.after(start))throw new IllegalArgumentException("Period requires start before exclusive end");
        return records.stream().filter(x->!x.getRecordedDateTime().before(start)&&x.getRecordedDateTime().before(end)).toList();
    }
    public List<ServiceRecord> recordsForPeriod(LocalDate start,LocalDate end) {
        if(start==null||end==null||end.isBefore(start))throw new IllegalArgumentException("Invalid inclusive date period");
        return recordsForPeriod(Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant()),Date.from(end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }
    public static Database demo() {
        Database d=new Database();
        d.addMember(new Member("Alice Member","000000001","1 Main St","Tuscaloosa","AL","35401","Active"));
        d.addMember(new Member("Sam Suspended","000000002","2 Main St","Tuscaloosa","AL","35401","Suspended"));
        d.addProvider(new Provider("Pat Provider","000000100","3 Main St","Tuscaloosa","AL","35401"));
        d.addOperator(new Operator("000000200","operator123"));
        d.addManager(new Manager("000000300"));
        d.directory.addOffering(new ServiceOffering("598470","Dietitian session",50.00));
        d.directory.addOffering(new ServiceOffering("883948","Aerobics session",35.50));
        return d;
    }
}
