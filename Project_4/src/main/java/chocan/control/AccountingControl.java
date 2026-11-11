package chocan.control;
import chocan.data.Database;
import chocan.entity.*;
import java.util.*;
import java.time.*;
import java.nio.file.*;
import java.math.*;
/** @author Cayden Pomeroy */
public class AccountingControl 
{
    private final Database database;
    private LocalDate weekStart,weekEnd;
    private Path outputDirectory=Path.of("output");
    private String lastError="",lastReport="";
    public AccountingControl(Database d) {
        database=Objects.requireNonNull(d);
        LocalDate today=LocalDate.now();
        weekEnd=today.with(java.time.temporal.TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        weekStart=weekEnd.minusDays(6);
    }
    public void setPeriod(LocalDate start,LocalDate end) 
  {
        if(start==null||end==null||end.isBefore(start))throw new IllegalArgumentException("Invalid inclusive reporting period");
        weekStart=start;
        weekEnd=end;
    }
    public LocalDate getWeekStart() 
  {
        return weekStart;
    }
    public LocalDate getWeekEnd() 
  {
        return weekEnd;
    }
    public void setOutputDirectory(Path p) 
  {
        outputDirectory=Objects.requireNonNull(p);
    }
    public String getLastError() 
  {
        return lastError;
    }
    public String getLastReport() 
  {
        return lastReport;
    }
    // Reporting uses receipt dates, so delayed service billing belongs to its acceptance week.
    private List<ServiceRecord> records() 
  {
        return database.recordsForPeriod(weekStart,weekEnd);
    }
    private String header(String type) 
  {
        return type+"\nReceipt period: "+weekStart+" through "+weekEnd+" (inclusive)\nGenerated: "+LocalDate.now()+"\n";
    }
    private String person(Person p) 
  {
        return p.getName()+" | "+p.getNumber()+"\n"+p.getStreetAddress()+"\n"+p.getCity()+", "+p.getState()+" "+p.getZipCode()+"\n";
    }
    private String safe(String name) 
  {
        return name.replaceAll("[^A-Za-z0-9 ._-]","_").replaceAll("[. ]+$", "_");
    }
    private boolean save(String kind,String name,String id,String text) 
  {
        lastReport=text;
        String file=safe(name)+"_"+LocalDate.now()+"_"+id+"_"+weekStart+"_"+weekEnd+"_"+kind+".txt";
        boolean ok=new Report(kind,file,text).save(outputDirectory.resolve(file));
        lastError=ok?"":"Cannot write report: "+file;
        return ok;
    }
    private boolean fail(String e) 
  {
        lastReport="";
        lastError=e;
        return false;
    }
    private List<ServiceRecord> providerRecords(String id) 
  {
        return records().stream().filter(r->r.getProviderNumber().equals(id)).sorted(Comparator.comparing(ServiceRecord::getRecordedDateTime)).toList();
    }
    private BigDecimal total(List<ServiceRecord> rows) 
  {
        return rows.stream().map(ServiceRecord::getFeeAmount).reduce(new BigDecimal("0.00"),BigDecimal::add);
    }
    private boolean limits(List<ServiceRecord> rows) 
  {
        return rows.size()<=999&&total(rows).compareTo(new BigDecimal("99999.99"))<=0;
    }
    // Member rows are ordered by the date the service was provided.
    public boolean memberReport(String id) 
  {
        List<ServiceRecord> rows=records().stream().filter(r->r.getMemberNumber().equals(id)).sorted(Comparator.comparing(ServiceRecord::getServiceDate).thenComparing(ServiceRecord::getRecordedDateTime)).toList();
        Member m=database.findMember(id);
        if(!rows.isEmpty())m=database.memberForRecord(rows.get(0));
        if(m==null)return fail("Unknown member");
        StringBuilder b=new StringBuilder(header("Member report")).append(person(m)).append("Service date | Provider | Service\n");
        for(var r:rows) 
        {
            ServiceOffering s=database.getDirectory().findOffering(r.getServiceCode());
            b.append(r.getFormattedServiceDate()).append(" | ").append(database.providerForRecord(r).getName()).append(" | ").append(s==null?r.getServiceCode():s.getServiceName()).append('\n');
        }
        if(rows.isEmpty())b.append("No services in this period.\n");
        return save("member",m.getName(),id,b.toString());
    }
    // Provider rows follow the order records reached the system.
    public boolean providerReport(String id) 
  {
        List<ServiceRecord> rows=providerRecords(id);
        Provider p=database.findProvider(id);
        if(!rows.isEmpty())p=database.providerForRecord(rows.get(0));
        if(p==null)return fail("Unknown provider");
        if(!limits(rows))return fail("Provider totals exceed supported report limits");
        StringBuilder b=new StringBuilder(header("Provider report")).append(person(p)).append("Service date | Received | Member | Member number | Code | Fee\n");
        for(var r:rows)b.append(r.getFormattedServiceDate()).append(" | ").append(r.getFormattedRecordedTime()).append(" | ").append(database.memberForRecord(r).getName()).append(" | ").append(r.getMemberNumber()).append(" | ").append(r.getServiceCode()).append(" | $").append(r.getFeeAmount()).append('\n');
        b.append("Consultations: ").append(rows.size()).append("\nTotal fee: $").append(total(rows)).append('\n');
        if(database.findProvider(id)!=null)database.findProvider(id).setAccountingTotals(rows.size(),total(rows));
        return save("provider",p.getName(),id,b.toString());
    }
    private SortedSet<String> paidProviders() 
  {
        SortedSet<String> ids=new TreeSet<>();
        for(var r:records())ids.add(r.getProviderNumber());
        return ids;
    }
    // The summary includes only providers paid in the selected period.
    public boolean summaryReport() 
  {
        StringBuilder b=new StringBuilder(header("Accounts payable summary")).append("Provider | Number | Consultations | Total fee\n");
        BigDecimal grand=new BigDecimal("0.00");
        int count=0;
        for(String id:paidProviders()) 
        {
            var rows=providerRecords(id);
            if(!limits(rows))return fail("Provider totals exceed supported report limits");
            Provider p=database.providerForRecord(rows.get(0));
            BigDecimal t=total(rows);
            b.append(p.getName()).append(" | ").append(id).append(" | ").append(rows.size()).append(" | $").append(t).append('\n');
            grand=grand.add(t);
            count+=rows.size();
        }
        b.append("Total providers: ").append(paidProviders().size()).append("\nTotal consultations: ").append(count).append("\nOverall fee total: $").append(grand).append('\n');
        return save("summary","Summary","all",b.toString());
    }
    // Replacing the period file makes repeated accounting runs idempotent,
    // never a second transfer.
    public boolean eftData() 
  {
        StringBuilder b=new StringBuilder("Provider name | Provider number | Amount\n");
        for(String id:paidProviders()) 
        {
            var rows=providerRecords(id);
            if(!limits(rows))return fail("Provider totals exceed supported EFT limits");
            b.append(new EFTRecord(database.providerForRecord(rows.get(0)).getName(),id,total(rows)).line());
        }
        try 
          {
            Files.createDirectories(outputDirectory);
            Files.writeString(outputDirectory.resolve("eft_"+weekStart+"_"+weekEnd+".txt"),b);
            lastReport=b.toString();
            lastError="";
            return true;
        }
        catch(java.io.IOException|SecurityException e) 
          {
            return fail("Cannot write EFT file: "+e.getMessage());
        }
    }
    // A failed report prevents the batch from reporting success.
    public boolean weeklyAccounting() 
  {
        for(String id:paidProviders())if(!limits(providerRecords(id)))return fail("Provider totals exceed supported report limits");
        SortedSet<String> members=new TreeSet<>();
        for(var r:records())members.add(r.getMemberNumber());
        for(String id:members)if(!memberReport(id))return false;
        for(String id:paidProviders())if(!providerReport(id))return false;
        if(!eftData())return false;
        return summaryReport();
    }
}
