package chocan.ui;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
/** Line-oriented input shared by all menus; EOF exits instead of spinning.
 * @author Landon Liles */
public final class ConsoleInput {
    private final BufferedReader reader;
    private final PrintStream output;
    public ConsoleInput(InputStream input, PrintStream output) {
        reader = new BufferedReader(new InputStreamReader(input, java.nio.charset.StandardCharsets.UTF_8));
        this.output = output;
    }
    public void print(String value) {
        output.println(value);
    }
    // End-of-input exits menus instead of repeatedly requesting input.
    public String read(String prompt) {
        output.print(prompt + " ");
        output.flush();
        try {
            String value = reader.readLine();
            if (value == null) throw new EndOfInput();
            return value.trim();
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    // Report-period input uses strict ISO calendar dates.
    public LocalDate date(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(read(prompt + " (YYYY-MM-DD):"));
            }
            catch (DateTimeParseException e) {
                print("Enter a real date in YYYY-MM-DD format.");
            }
        }
    }
    // Service-date input uses the essay format MM-DD-YYYY.
    public LocalDate serviceDate() {
        java.time.format.DateTimeFormatter format = java.time.format.DateTimeFormatter.ofPattern("MM-dd-uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT);
        while (true) {
            try {
                String value = read("Date service provided (MM-DD-YYYY):");
                if (!value.matches("[0-9]{2}-[0-9]{2}-[0-9]{4}")) throw new DateTimeParseException("Wrong format", value, 0);
                return LocalDate.parse(value, format);
            } catch (DateTimeParseException e) {
                print("Enter a real date in MM-DD-YYYY format.");
            }
        }
    }
    public boolean yes(String prompt) {
        return read(prompt + " (yes/no):").equalsIgnoreCase("yes");
    }
    /** @author Landon Liles */
    public static final class EndOfInput extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
