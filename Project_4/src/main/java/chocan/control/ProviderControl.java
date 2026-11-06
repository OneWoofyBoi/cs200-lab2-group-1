package chocan.control;
import chocan.data.Database;
import chocan.entity.*;
import java.nio.file.*;
import java.util.*;
import java.time.*;
/** @author Dylan Filmer */
public class ProviderControl {
    private final Database database;
    private String providerId,pendingComments="",lastError="";
    private Path outputDirectory=Path.of("output");
    public ProviderControl(Database d) {
        database=Objects.requireNonNull(d);
    }
    // Each provider login starts with a fresh session.
    public boolean verify(String id) {
        providerId=null;
        pendingComments="";
        if(database.findProvider(id)==null) {
            lastError="Invalid provider number";
            return false;
        }
        providerId=id;
        lastError="";
        return true;
    }
    public void logout() {
        providerId=null;
        pendingComments="";
    }
    public String getProviderId() {
        return providerId;
    }
    public String getLastError() {
        return lastError;
    }
    public void setOutputDirectory(Path p) {
        outputDirectory=Objects.requireNonNull(p);
    }
    public String validateCard(String id) {
        if(providerId==null)return "Provider not authenticated";
        Member m=database.findMember(id);
        return m==null?"Invalid number":m.isValid()?"Validated":"Member suspended";
    }
    public boolean addComments(String text) {
        if(providerId==null||text==null||text.length()>100||text.chars().anyMatch(Character::isISOControl)) {
            lastError="Comments must contain at most 100 printable characters";
            return false;
        }
        pendingComments=text;
        return true;
    }
    public ServiceOffering findOffering(String code) {
        return database.getDirectory().findOffering(code);
    }
    /** Trusted post-confirmation persistence operation; interactive callers use recordService. */
    // Member status and the directory fee are checked again at acceptance.
    public boolean bill(ServiceRecord record) {
        lastError="";
        if(providerId==null||database.findProvider(providerId)==null||record==null||!providerId.equals(record.getProviderNumber()))return fail("Provider not authenticated or mismatched");
        if(!"Validated".equals(validateCard(record.getMemberNumber())))return fail(validateCard(record.getMemberNumber()));
        ServiceOffering s=findOffering(record.getServiceCode());
        if(s==null)return fail("Unknown service code");
        if(s.getFeeAmount().compareTo(record.getFeeAmount())!=0)return fail("Fee must match provider directory");
        if(record.getServiceDate().after(new Date()))return fail("Service date cannot be in the future");
        if(database.getServiceRecords().contains(record))return fail("Record already stored");
        try {
            Files.createDirectories(outputDirectory);
        }
        catch(java.io.IOException|SecurityException e) {
            return fail("Cannot create output directory: "+e.getMessage());
        }
        if(!record.save(outputDirectory.resolve("service_records.txt")))return fail("Cannot write service record");
        boolean ok=database.addServiceRecord(record);
        pendingComments="";
        return ok;
    }
    // Confirmation precedes receipt-time capture and directory-fee lookup.
    public boolean recordService(String member,String code,java.util.Date serviceDate,String comments,boolean confirmed) {
        if(!confirmed)return fail("Service was not confirmed");
        ServiceOffering s=findOffering(code);
        if(s==null)return fail("Unknown service code");
        try {
            return bill(new ServiceRecord(serviceDate,providerId,member,code,s.getFee(),comments==null?pendingComments:comments));
        }
        catch(IllegalArgumentException e) {
            return fail(e.getMessage());
        }
    }
    private boolean fail(String message) {
        lastError=message;
        return false;
    }
    public String directoryText() {
        if(providerId==null)return "Provider not authenticated";
        return database.getDirectory().displayText();
    }
    // The requested directory is delivered as a local file.
    public boolean requestDirectory() {
        if(providerId==null)return fail("Provider not authenticated");
        if(!database.getDirectory().export(outputDirectory.resolve("provider_directory.txt")))return fail("Cannot write directory");
        lastError="";
        return true;
    }
}
