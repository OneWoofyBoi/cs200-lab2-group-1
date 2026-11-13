package chocan.entity;
import java.util.*;
import java.nio.file.*;
import java.io.*;
/** @author Dylan Filmer */
public class ProviderDirectory implements java.io.Serializable {
    private static final long serialVersionUID=1L;
    private final List<ServiceOffering> offerings=new ArrayList<>();
    private String fileName="provider_directory.txt";
    public ProviderDirectory() {
    }
    public ProviderDirectory(String fileName) {
        this.fileName=fileName;
    }
    public void addOffering(ServiceOffering s) {
        Objects.requireNonNull(s);
        if(findOffering(s.getServiceCode())!=null)throw new IllegalArgumentException("Duplicate service code");
        offerings.add(s);
    }
    // Service codes identify entries independently of display order.
    public ServiceOffering findOffering(String code) {
        return offerings.stream().filter(x->x.getServiceCode().equals(code)).findFirst().orElse(null);
    }
    public List<ServiceOffering> getOfferings() {
        return offerings.stream().sorted(Comparator.comparing(ServiceOffering::getServiceName,String.CASE_INSENSITIVE_ORDER).thenComparing(ServiceOffering::getServiceCode)).toList();
    }
    public String getFileName() {
        return fileName;
    }
    public String displayText() {
        StringBuilder b=new StringBuilder("Provider Directory\nService name | Code | Fee\n");
        for(var s:getOfferings())b.append(s.getServiceName()).append(" | ").append(s.getServiceCode()).append(" | $").append(s.getFeeAmount()).append('\n');
        return b.toString();
    }
    // Directory delivery uses a local alphabetically ordered file.
    public boolean export() {
        return export(Path.of(fileName));
    }
    public boolean export(Path file) {
        try {
            if(file.toAbsolutePath().getParent()!=null)Files.createDirectories(file.toAbsolutePath().getParent());
            Files.writeString(file,displayText());
            return true;
        }
        catch(IOException|SecurityException e) {
            return false;
        }
    }
}
