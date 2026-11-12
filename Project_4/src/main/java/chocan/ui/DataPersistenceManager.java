package chocan.data;
import java.nio.file.*;
import java.io.*;
/** Local optional restart snapshots. Never load untrusted snapshot files.
 * @author Michael Ifeanyichukwu */
public final class DataPersistenceManager {
    private DataPersistenceManager() {
    }
    // A temporary snapshot is replaced only after serialization succeeds.
    public static void save(Database database,Path file)throws IOException {
        if(database==null)throw new IllegalArgumentException("Database required");
        Path absolute=file.toAbsolutePath();
        Files.createDirectories(absolute.getParent());
        Path temp=Files.createTempFile(absolute.getParent(),"chocan-", ".tmp");
        try {
            try(ObjectOutputStream out=new ObjectOutputStream(Files.newOutputStream(temp))) {
                out.writeObject(database);
            }
            try {
                Files.move(temp,absolute,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
            }
            catch(AtomicMoveNotSupportedException e) {
                Files.move(temp,absolute,StandardCopyOption.REPLACE_EXISTING);
            }
        }
        finally {
            Files.deleteIfExists(temp);
        }
    }
    // Snapshot loading rejects unsupported data rather than resetting it.
    public static Database load(Path file)throws IOException {
        try(ObjectInputStream in=new ObjectInputStream(Files.newInputStream(file))) {
            in.setObjectInputFilter(info-> {
                if(info.depth()>50||info.references()>100000||info.streamBytes()>20000000||info.arrayLength()>100000)return ObjectInputFilter.Status.REJECTED;Class<?> c=info.serialClass();if(c==null)return ObjectInputFilter.Status.UNDECIDED;while(c.isArray())c=c.getComponentType();String n=c.getName();return c.isPrimitive()||n.startsWith("chocan.entity.")||n.equals("chocan.data.Database")||n.startsWith("java.util.")||n.startsWith("java.lang.")||n.startsWith("java.math.")?ObjectInputFilter.Status.ALLOWED:ObjectInputFilter.Status.REJECTED;
            }
            );
            Object value=in.readObject();
            if(!(value instanceof Database))throw new IOException("Not a ChocAn database snapshot");
            return (Database)value;
        }
        catch(ClassNotFoundException|RuntimeException e) {
            throw new IOException("Invalid database snapshot",e);
        }
    }
}
