package chocan.entity;
import java.nio.file.*;
import java.math.*;
import java.io.*;
/** @author Cayden Pomeroy */
public class EFTRecord implements java.io.Serializable 
{
    private static final long serialVersionUID=1L;
    private final String providerName,providerNumber;
    private final BigDecimal transferAmount;
    // This record holds local transfer data, not a banking connection.
    public EFTRecord(String name,String number,double amount) 
  {
        this(name,number,BigDecimal.valueOf(amount));
    }
    public EFTRecord(String name,String number,BigDecimal amount) 
  {
        providerName=Person.text(name,25,"Provider name");
        if(number==null||!number.matches("[0-9]{9}")||amount==null||amount.signum()<0||amount.stripTrailingZeros().scale()>2)throw new IllegalArgumentException("Invalid EFT data");
        providerNumber=number;
        transferAmount=amount.setScale(2);
    }
    public String getProviderName() 
  {
        return providerName;
    }
    public String getProviderNumber() 
  {
        return providerNumber;
    }
    public double getTransferAmount() 
  {
        return transferAmount.doubleValue();
    }
    // Each EFT line identifies a provider and the payable amount.
    public String line() 
  {
        return providerName+" | "+providerNumber+" | "+transferAmount+System.lineSeparator();
    }
    public boolean appendTo(String file) 
  {
        return appendTo(Path.of(file));
    }
    public boolean appendTo(Path file) 
  {
        try 
          {
            Files.createDirectories(file.toAbsolutePath().getParent());
            Files.writeString(file,line(),StandardOpenOption.CREATE,StandardOpenOption.APPEND);
            return true;
        }
        catch(IOException|SecurityException e) 
          {
            return false;
        }
    }
}
