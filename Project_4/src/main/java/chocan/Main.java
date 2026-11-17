package chocan;

import chocan.control.AccountingControl;
import chocan.control.ProviderControl;
import chocan.control.RecordsControl;
import chocan.data.Database;
import chocan.data.DataPersistenceManager;
import chocan.ui.ChocAnWindow;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/** Starts the desktop application and manages its saved session.
 * @author Landon Liles & Dawson Sconyers */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("ChocAn requires a desktop display.");
            return;
        }
        Path output = Path.of("output").toAbsolutePath();
        Path snapshot = output.resolve("chocan-state.bin");
        try {
            Files.createDirectories(output);
            FileChannel channel = FileChannel.open(output.resolve("chocan-state.lock"),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            try {
                FileLock lock = channel.tryLock();
                if (lock == null) {
                    throw new IOException("ChocAn is already open in this project folder.");
                }
                Database database = Files.notExists(snapshot) ? Database.demo() : DataPersistenceManager.load(snapshot);
                ProviderControl provider = new ProviderControl(database);
                RecordsControl records = new RecordsControl(database);
                AccountingControl accounting = new AccountingControl(database);
                provider.setOutputDirectory(output);
                accounting.setOutputDirectory(output);
                ChocAnWindow.open(database, provider, records, accounting, () -> {
                    DataPersistenceManager.save(database, snapshot);
                    channel.close(); // Also releases the session lock.
                });
            } catch (IOException | RuntimeException e) {
                channel.close();
                throw e;
            }
        } catch (IOException | SecurityException | java.nio.channels.OverlappingFileLockException e) {
            String reason = e instanceof java.nio.channels.OverlappingFileLockException
                    ? "ChocAn is already open in this project folder." : e.getMessage();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, reason,
                    "ChocAn could not start", JOptionPane.ERROR_MESSAGE));
        }
    }
}
