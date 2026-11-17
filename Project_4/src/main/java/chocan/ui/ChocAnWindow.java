package chocan.ui;

import chocan.control.AccountingControl;
import chocan.control.ProviderControl;
import chocan.control.RecordsControl;
import chocan.data.Database;
import chocan.entity.Member;
import chocan.entity.Person;
import chocan.entity.Provider;
import chocan.entity.ServiceOffering;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Date;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

/** Swing interface for ChocAn's provider, operator, and manager workflows.
 * @author Dawson */
public final class ChocAnWindow extends JPanel {
    private static final ColorUIResource TEXT = new ColorUIResource(230, 237, 246);
    private static final ColorUIResource MUTED = new ColorUIResource(176, 192, 209);
    private static final ColorUIResource BACKGROUND = new ColorUIResource(16, 23, 34);
    private static final ColorUIResource SURFACE = new ColorUIResource(26, 38, 53);
    private static final ColorUIResource CONTROL = new ColorUIResource(43, 62, 83);
    private static final ColorUIResource BORDER = new ColorUIResource(76, 99, 122);
    private static final ColorUIResource SELECTED = new ColorUIResource(40, 84, 106);
    private static final ColorUIResource ACCENT = new ColorUIResource(113, 224, 199);
    private static final Color ERROR = new Color(255, 163, 173);
    private static final DateTimeFormatter SERVICE_DATE = DateTimeFormatter
            .ofPattern("MM-dd-uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final Database database;
    private final ProviderControl provider;
    private final RecordsControl records;
    private final AccountingControl accounting;
    private final ManagerTerminal manager;
    private final JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 6));
    private final JPanel workspace = new JPanel(new BorderLayout(0, 8));
    private final JLabel section = new JLabel();
    private final JLabel status = new JLabel();
    private final JLabel loginFeedback = new JLabel(" ");
    private final JLabel accountLabel = new JLabel();
    private final CardLayout pages = new CardLayout();
    private final JPanel content = new JPanel(pages);
    private final JTextArea results = new JTextArea();
    private final JComboBox<String> role = new JComboBox<>(new String[]{"Provider", "Operator", "Manager"});
    private final JTextField login = field("login-id", "");
    private final JPasswordField password = new JPasswordField(16);
    private final JLabel passwordLabel = new JLabel("Password");

    @FunctionalInterface
    public interface SaveOnClose {
        void save() throws IOException;
    }

    public static void open(Database database, ProviderControl provider, RecordsControl records,
                            AccountingControl accounting, SaveOnClose save) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("GUI mode requires a desktop display. Run without --gui for terminal mode.");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            installTheme();
            ChocAnWindow view = new ChocAnWindow(database, provider, records, accounting);
            JFrame frame = new JFrame("ChocAn");
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.setContentPane(view);
            frame.setMinimumSize(new Dimension(860, 660));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.addWindowListener(new WindowAdapter() {
                @Override public void windowOpened(WindowEvent event) {
                    view.login.requestFocusInWindow();
                }

                @Override public void windowClosing(WindowEvent event) {
                    view.close(save, frame::dispose);
                }
            });
            frame.setVisible(true);
        });
    }

    static void installTheme() {
        MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme() {
            @Override protected ColorUIResource getPrimary1() { return BORDER; }
            @Override protected ColorUIResource getPrimary2() { return SELECTED; }
            @Override protected ColorUIResource getPrimary3() { return CONTROL; }
            @Override protected ColorUIResource getSecondary1() { return BORDER; }
            @Override protected ColorUIResource getSecondary2() { return CONTROL; }
            @Override protected ColorUIResource getSecondary3() { return SURFACE; }
            @Override protected ColorUIResource getBlack() { return TEXT; }
            @Override protected ColorUIResource getWhite() { return BACKGROUND; }
            @Override public ColorUIResource getFocusColor() { return ACCENT; }
            @Override public ColorUIResource getInactiveControlTextColor() { return MUTED; }
        });
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            throw new IllegalStateException("Swing theme could not be initialized.", e);
        }
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        defaults.put("Button.background", CONTROL);
        defaults.put("Button.select", SELECTED);
        defaults.put("Button.margin", new Insets(7, 11, 7, 11));
        defaults.put("ComboBox.background", CONTROL);
        defaults.put("ComboBox.selectionBackground", SELECTED);
        defaults.put("ComboBox.selectionForeground", TEXT);
        for (String key : new String[]{"TextField", "PasswordField", "TextArea"}) {
            defaults.put(key + ".background", BACKGROUND);
            defaults.put(key + ".foreground", TEXT);
            defaults.put(key + ".caretForeground", ACCENT);
            defaults.put(key + ".selectionBackground", SELECTED);
            defaults.put(key + ".selectionForeground", TEXT);
        }
    }

    ChocAnWindow(Database database, ProviderControl provider, RecordsControl records, AccountingControl accounting) {
        super(new BorderLayout(0, 12));
        this.database = database;
        this.provider = provider;
        this.records = records;
        this.accounting = accounting;
        manager = new ManagerTerminal(records, accounting);
        setPreferredSize(new Dimension(900, 720));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(SURFACE);
        banner.setBorder(new EmptyBorder(15, 16, 15, 16));
        JLabel title = new JLabel("ChocAn");
        title.setForeground(TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 25f));
        section.setForeground(ACCENT);
        section.setFont(section.getFont().deriveFont(Font.BOLD, 14f));
        banner.add(title, BorderLayout.WEST);
        banner.add(section, BorderLayout.EAST);
        add(banner, BorderLayout.NORTH);

        JPanel middle = new JPanel(new BorderLayout(0, 10));
        middle.setOpaque(false);
        JPanel tools = new JPanel(new BorderLayout(0, 8));
        tools.setOpaque(false);
        actions.setOpaque(false);
        workspace.setOpaque(false);
        tools.add(actions, BorderLayout.NORTH);
        tools.add(workspace, BorderLayout.CENTER);
        middle.add(tools, BorderLayout.NORTH);

        results.setName("results");
        results.setEditable(false);
        results.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        results.setForeground(TEXT);
        results.setMargin(new Insets(12, 12, 12, 12));
        JScrollPane scroll = new JScrollPane(results);
        scroll.setBackground(BACKGROUND);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER), "Results",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION, null, MUTED));
        scroll.setPreferredSize(new Dimension(500, 200));
        middle.add(scroll, BorderLayout.CENTER);
        content.setOpaque(false);
        content.add(middle, "workspace");
        content.add(loginPage(), "login");
        add(content, BorderLayout.CENTER);

        status.setName("status");
        status.setBorder(new EmptyBorder(2, 3, 2, 3));
        add(status, BorderLayout.SOUTH);
        role.setName("role");
        password.setName("password");
        role.addActionListener(event -> updatePasswordVisibility());
        login.addActionListener(event -> signIn());
        password.addActionListener(event -> signIn());
        showLogin();
    }

    void close(SaveOnClose save, Runnable dispose) {
        try {
            save.save();
            dispose.run();
        } catch (IOException | SecurityException e) {
            if (status.isVisible()) {
                message("Could not save the session: " + e.getMessage(), true);
            } else {
                loginFeedback.setText("Could not save the session. Check the snapshot path.");
            }
        }
    }

    private JPanel loginPage() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setOpaque(false);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(26, 30, 24, 30)));
        GridBagConstraints row = new GridBagConstraints();
        row.gridx = 0;
        row.weightx = 1;
        row.fill = GridBagConstraints.HORIZONTAL;
        row.anchor = GridBagConstraints.WEST;

        JLabel title = new JLabel("Sign in to ChocAn");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        row.gridy = 0;
        row.insets = new Insets(0, 0, 6, 0);
        card.add(title, row);

        JLabel description = new JLabel("Select a role and enter your account details.");
        description.setForeground(MUTED);
        row.gridy++;
        row.insets = new Insets(0, 0, 22, 0);
        card.add(description, row);

        JLabel roleLabel = new JLabel("Role");
        roleLabel.setLabelFor(role);
        accountLabel.setLabelFor(login);
        passwordLabel.setLabelFor(password);
        Dimension fieldSize = new Dimension(320, 32);
        for (JComponent control : new JComponent[]{role, login, password}) {
            control.setPreferredSize(fieldSize);
        }
        for (JComponent control : new JComponent[]{roleLabel, role, accountLabel, login, passwordLabel, password}) {
            row.gridy++;
            row.insets = new Insets(0, 0, control instanceof JLabel ? 5 : 13, 0);
            card.add(control, row);
        }

        JButton submit = button("Sign in", this::signIn);
        submit.setBackground(SELECTED);
        row.gridy++;
        row.insets = new Insets(5, 0, 8, 0);
        card.add(submit, row);

        loginFeedback.setName("login-feedback");
        loginFeedback.setForeground(ERROR);
        row.gridy++;
        row.insets = new Insets(0, 0, 0, 0);
        card.add(loginFeedback, row);

        page.add(card);
        return page;
    }

    private void showLogin() {
        provider.logout();
        records.logout();
        section.setText("Operations console");
        actions.removeAll();
        showForm(null);
        write("");
        password.setText("");
        login.setText("");
        updatePasswordVisibility();
        pages.show(content, "login");
        status.setVisible(false);
        login.requestFocusInWindow();
    }

    private void updatePasswordVisibility() {
        String selected = (String) role.getSelectedItem();
        boolean operator = "Operator".equals(selected);
        accountLabel.setText(selected + " ID");
        passwordLabel.setVisible(operator);
        password.setVisible(operator);
        if (!operator) password.setText("");
        loginFeedback.setText(" ");
        content.revalidate();
        content.repaint();
    }

    private void signIn() {
        String selected = (String) role.getSelectedItem();
        String id = value(login);
        if (id.isEmpty()) {
            loginFeedback.setText("Enter your " + selected.toLowerCase(java.util.Locale.ROOT) + " ID.");
            login.requestFocusInWindow();
            password.setText("");
            return;
        }
        char[] secret = password.getPassword();
        boolean accepted;
        try {
            accepted = switch (selected) {
                case "Provider" -> provider.verify(id);
                case "Operator" -> records.verifyOperator(id, new String(secret));
                case "Manager" -> manager.login(id);
                default -> false;
            };
        } finally {
            java.util.Arrays.fill(secret, '\0');
            password.setText("");
        }
        if (!accepted) {
            loginFeedback.setText("Access denied. Check your account details.");
            if ("Operator".equals(selected)) password.requestFocusInWindow();
            else login.requestFocusInWindow();
            return;
        }
        actions.removeAll();
        section.setText(selected + " workspace");
        switch (selected) {
            case "Provider" -> {
                action("Validate member", this::validateMember);
                action("Record service", this::serviceForm);
                action("Provider directory", this::directory);
            }
            case "Operator" -> {
                action("Members", () -> recordList(true));
                action("Providers", () -> recordList(false));
                action("Simulate Acme update", this::acmeUpdate);
            }
            case "Manager" -> {
                action("Member report", () -> reportForm(true));
                action("Provider report", () -> reportForm(false));
                action("Summary report", () -> { showForm(null); report(manager.summaryReport()); });
                action("Weekly accounting", () -> { showForm(null); report(new TimerDaemon(accounting).weeklyAccounting()); });
                action("Report period", this::periodForm);
            }
        }
        action("Sign out", this::showLogin);
        pages.show(content, "workspace");
        status.setVisible(true);
        if (actions.getComponentCount() > 0) actions.getComponent(0).requestFocusInWindow();
        showForm(null);
        write(selected.equals("Manager") ? "Report period: " + accounting.getWeekStart()
                + " to " + accounting.getWeekEnd() : "Choose an action above.");
        message("Signed in as " + selected.toLowerCase(java.util.Locale.ROOT) + ".", false);
    }

    private void validateMember() {
        Form form = new Form("Validate a member");
        JTextField id = form.text("Member ID", "");
        form.footer("Validate", () -> {
            String outcome = provider.validateCard(value(id));
            write(outcome);
            message(outcome, !outcome.equals("Validated"));
        });
        showForm(form);
    }

    private void serviceForm() {
        Form form = new Form("Record a service");
        JTextField member = form.text("Member ID", "");
        JTextField date = form.text("Service date (MM-DD-YYYY)", LocalDate.now().format(SERVICE_DATE));
        JTextField code = form.text("Service code", "");
        JTextField comments = form.text("Comments (optional)", "");
        form.footer("Review service", () -> {
            String validation = provider.validateCard(value(member));
            if (!validation.equals("Validated")) {
                message(validation, true);
                return;
            }
            LocalDate day = LocalDate.parse(value(date), SERVICE_DATE);
            ServiceOffering offering = provider.findOffering(value(code));
            if (offering == null) {
                message("Unknown service code.", true);
                return;
            }
            Form review = new Form("Confirm service");
            review.row("Member", new JLabel(value(member)));
            review.row("Service", new JLabel(offering.getServiceName() + " (" + offering.getServiceCode() + ")"));
            review.row("Fee", new JLabel("$" + offering.getFeeAmount().toPlainString()));
            review.row("Service date", new JLabel(day.format(SERVICE_DATE)));
            String id = value(member), note = value(comments);
            Date billedOn = Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant());
            review.footer("Confirm and record", () -> {
                if (provider.recordService(id, offering.getServiceCode(), billedOn, note, true)) {
                    showForm(null);
                    write("Service recorded for member " + id + "\n" + offering.getServiceName()
                            + " | $" + offering.getFeeAmount().toPlainString());
                    message("Service recorded.", false);
                } else {
                    showForm(form);
                    message(provider.getLastError(), true);
                }
            });
            review.footer("Cancel", () -> {
                showForm(form);
                message("Service cancelled.", false);
            });
            showForm(review);
        });
        showForm(form);
    }

    private void directory() {
        showForm(null);
        boolean exported = provider.requestDirectory();
        write(provider.directoryText());
        message(exported ? "Directory displayed and exported." : provider.getLastError(), !exported);
    }

    private void recordList(boolean members) {
        String kind = members ? "Member" : "Provider";
        var people = members ? database.getMembers() : database.getProviders();
        Form form = new Form(kind + " records");
        JComboBox<String> selection = new JComboBox<>();
        selection.setName("record-selection");
        StringBuilder listing = new StringBuilder();
        for (Person person : people) {
            String line = person.getNumber() + " | " + person.getName();
            if (person instanceof Member member) line += " | " + member.getStatus();
            selection.addItem(line);
            listing.append(line).append('\n');
        }
        form.row("Select " + kind.toLowerCase(java.util.Locale.ROOT), selection);
        form.footer("Add " + kind.toLowerCase(java.util.Locale.ROOT), () -> recordForm(members, null));
        if (!people.isEmpty()) {
            form.footer("Edit selected", () -> recordForm(members, people.get(selection.getSelectedIndex())));
            form.footer("Delete selected", () -> deleteForm(members, people.get(selection.getSelectedIndex())));
        }
        showForm(form);
        write(people.isEmpty() ? "No records." : listing.toString());
    }

    private void recordForm(boolean members, Person original) {
        boolean editing = original != null;
        Form form = new Form((editing ? "Edit " : "Add ") + (members ? "member" : "provider"));
        JTextField id = form.text("Record ID", editing ? original.getNumber() : "");
        id.setEditable(!editing);
        JTextField name = form.text("Name", editing ? original.getName() : "");
        JTextField address = form.text("Street address", editing ? original.getStreetAddress() : "");
        JTextField city = form.text("City", editing ? original.getCity() : "");
        JTextField state = form.text("State", editing ? original.getState() : "");
        JTextField zip = form.text("ZIP", editing ? original.getZipCode() : "");
        JComboBox<String> statusChoice = new JComboBox<>(new String[]{"Active", "Suspended"});
        statusChoice.setName("member-status");
        if (members) {
            if (editing) statusChoice.setSelectedItem(((Member) original).getStatus());
            form.row("Status", statusChoice);
        }
        form.footer("Save record", () -> {
            String action = editing ? "edit" : "add";
            boolean ok = members
                    ? records.manageMembers(action, new Member(value(name), value(id), value(address),
                            value(city), value(state), value(zip), (String) statusChoice.getSelectedItem()))
                    : records.manageProviders(action, new Provider(value(name), value(id), value(address),
                            value(city), value(state), value(zip)));
            if (ok) recordList(members);
            message(ok ? "Record saved." : "Record could not be saved. Check for a duplicate or missing ID.", !ok);
        });
        form.footer("Cancel", () -> recordList(members));
        showForm(form);
    }

    private void deleteForm(boolean members, Person original) {
        Form form = new Form("Confirm deletion");
        form.row("Record", new JLabel(original.getName() + " (" + original.getNumber() + ")"));
        form.footer("Confirm deletion", () -> {
            boolean ok = members ? records.manageMembers("delete", (Member) original)
                    : records.manageProviders("delete", (Provider) original);
            recordList(members);
            message(ok ? "Record deleted." : "Record could not be deleted.", !ok);
        });
        form.footer("Cancel", () -> recordList(members));
        showForm(form);
    }

    private void acmeUpdate() {
        Form form = new Form("Simulate an Acme member status update");
        JTextField id = form.text("Member ID", "");
        JComboBox<String> choice = new JComboBox<>(new String[]{"Active", "Suspended"});
        choice.setName("acme-status");
        form.row("New status", choice);
        form.footer("Apply update", () -> {
            Member m = database.findMember(value(id));
            if (m == null) {
                message("Member not found.", true);
                return;
            }
            Member update = new Member(m.getName(), m.getNumber(), m.getStreetAddress(), m.getCity(),
                    m.getState(), m.getZipCode(), (String) choice.getSelectedItem());
            boolean ok = new AcmeInterface(records).syncAccounts(java.util.List.of(update));
            message(ok ? "Member status updated." : "Status update rejected.", !ok);
        });
        showForm(form);
    }

    private void reportForm(boolean member) {
        String kind = member ? "Member" : "Provider";
        Form form = new Form(kind + " report");
        JTextField id = form.text(kind + " ID", "");
        form.footer("Generate report", () -> report(member ? manager.memberReport(value(id))
                : manager.providerReport(value(id))));
        showForm(form);
    }

    private void periodForm() {
        Form form = new Form("Set report period (YYYY-MM-DD)");
        JTextField start = form.text("Start date", accounting.getWeekStart().toString());
        JTextField end = form.text("End date (inclusive)", accounting.getWeekEnd().toString());
        form.footer("Set period", () -> {
            accounting.setPeriod(LocalDate.parse(value(start)), LocalDate.parse(value(end)));
            showForm(null);
            write("Report period: " + accounting.getWeekStart() + " to " + accounting.getWeekEnd());
            message("Report period updated.", false);
        });
        showForm(form);
    }

    private void report(boolean ok) {
        if (ok) write(accounting.getLastReport());
        else write("");
        message(ok ? "Report operation succeeded." : accounting.getLastError(), !ok);
    }

    private void action(String label, Runnable handler) {
        actions.add(button(label, handler));
    }

    private JButton button(String label, Runnable handler) {
        JButton button = new JButton(label);
        button.addActionListener(event -> {
            try {
                handler.run();
            } catch (java.time.DateTimeException e) {
                message("Enter a valid date in the displayed format.", true);
            } catch (IllegalArgumentException e) {
                message(e.getMessage(), true);
            }
        });
        return button;
    }

    private void showForm(Form form) {
        workspace.removeAll();
        if (form != null) workspace.add(form);
        revalidate();
        repaint();
        message(" ", false);
    }

    private void write(String text) {
        results.setText(text);
        results.setCaretPosition(0);
    }

    private void message(String text, boolean error) {
        status.setForeground(error ? ERROR : ACCENT);
        status.setText(text);
    }

    private static JTextField field(String label, String text) {
        JTextField field = new JTextField(text, 21);
        field.setName(label);
        return field;
    }

    private static String value(JTextField field) {
        return field.getText().trim();
    }

    private final class Form extends JPanel {
        private final JPanel fields = new JPanel(new GridBagLayout());
        private final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        private int row;

        Form(String title) {
            super(new BorderLayout(0, 8));
            setBackground(SURFACE);
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),
                    new EmptyBorder(10, 12, 10, 12)));
            fields.setOpaque(false);
            buttons.setOpaque(false);
            JLabel heading = new JLabel(title);
            heading.setFont(heading.getFont().deriveFont(Font.BOLD, 15f));
            heading.setForeground(TEXT);
            add(heading, BorderLayout.NORTH);
            add(fields, BorderLayout.WEST);
            add(buttons, BorderLayout.SOUTH);
        }

        JTextField text(String label, String initial) {
            JTextField input = field(label, initial);
            row(label, input);
            return input;
        }

        void row(String label, JComponent input) {
            row(new JLabel(label), input);
        }

        void row(JLabel label, JComponent input) {
            label.setLabelFor(input);
            GridBagConstraints position = new GridBagConstraints();
            position.gridy = row++;
            position.anchor = GridBagConstraints.WEST;
            position.insets = new Insets(2, 2, 2, 14);
            fields.add(label, position);
            position.gridx = 1;
            fields.add(input, position);
        }

        void footer(String label, Runnable handler) {
            buttons.add(button(label, handler));
        }
    }
}
