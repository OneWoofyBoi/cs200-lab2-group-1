package chocan.entity;
import java.nio.file.*;
import java.io.*;
import java.util.*;
/** @author Cayden Pomeroy */
public class Report implements java.io.Serializable 
{
    private static final long serialVersionUID=1L;
    private final String kind,fileName,content;
    private final Date generationDate;
    // Report metadata and generated text travel together.
    public Report(String kind,Date date,String fileName,String content) 
  {
        this.kind=kind;
        this.generationDate=new Date(date.getTime());
        this.fileName=fileName;
        this.content=content;
    }
    public Report(String kind,String fileName,String content) 
  {
        this(kind,new Date(),fileName,content);
    }
    public String getKind() 
  {
        return kind;
    }
    public Date getGenerationDate() 
  {
        return new Date(generationDate.getTime());
    }
    public String getFileName() 
  {
        return fileName;
    }
    public String getContent() 
  {
        return content;
    }
    // File-writing failures are returned to the calling controller.
    public boolean save() 
  {
        return save(Path.of(fileName));
    }
    public boolean save(Path file) 
  {
        try 
          {
            Files.createDirectories(file.toAbsolutePath().getParent());
            Files.writeString(file,content);
            return true;
        }
        catch(IOException|SecurityException e) 
          {
            return false;
        }
    }
}
