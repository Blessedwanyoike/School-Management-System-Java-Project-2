import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

class SchoolManagement {
    private static final Color BLUE = new Color(93, 177, 220);
    private static final Color LIGHT_BLUE = new Color(226, 244, 255);
    private static final Color RED = new Color(207, 55, 75);
    private static final Color WHITE = Color.WHITE;
    private static final Color INK = new Color(36, 57, 78);
    private static final Color BACKGROUND = new Color(246, 251, 255);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final List<Book> books = new ArrayList<Book>();
    private final List<Student> students = new ArrayList<Student>();
    private final List<Teacher> teachers = new ArrayList<Teacher>();
    private final List<Bus> buses = new ArrayList<Bus>();
    private final List<DormItem> dormItems = new ArrayList<DormItem>();

    private JTable bookTable;
    private JTable studentTable;
    private JTable teacherTable;
    private JTable busTable;
    private JTable dormTable;
    private JTable overviewBusTable;
    private JLabel[] summaryValues;
    private JComboBox<String> parentStudentSelector;
    private JLabel parentStudentName;
    private JLabel parentGrade;
    private JLabel parentStatus;
    private JLabel parentReason;

    private SchoolManagement() {
        addSampleData();
        showWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new SchoolManagement();
            }
        });
    }

    private void addSampleData() {
        books.add(new Book("BK-001", "Things Fall Apart", "Chinua Achebe", "Literature", 12));
        books.add(new Book("BK-002", "Introduction to Biology", "M. Otieno", "Science", 8));
        books.add(new Book("BK-003", "Algebra Basics", "R. Kamau", "Mathematics", 0));

        for (int i = 1; i <= 1500; i++) {
            String id = String.format("ST-%04d", i);
            String grade = "Grade " + (((i - 1) % 12) + 1);
            students.add(new Student(id, String.format("Student %04d", i), grade, "Not provided"));
        }

        String[] subjects = { "Mathematics", "English", "Science", "History", "Geography" };
        for (int i = 1; i <= 50; i++) {
            teachers.add(new Teacher(String.format("TE-%03d", i), String.format("Teacher %02d", i),
                    subjects[(i - 1) % subjects.length], "Not provided"));
        }

        String[] routes = { "Town", "West", "East", "North", "South" };
        for (int i = 1; i <= 10; i++) {
            buses.add(new Bus(String.format("BU-%03d", i), String.format("CA-BUS-%02d", i),
                    routes[(i - 1) % routes.length] + " route", 30, String.format("Driver %02d", i), i <= 8));
        }

        dormItems.add(new DormItem("Girls' Dormitory", "Bed", 80, 78));
        dormItems.add(new DormItem("Boys' Dormitory", "Mattress", 100, 100));
        dormItems.add(new DormItem("Girls' Dormitory", "Study desk", 40, 37));
    }

    private void showWindow() {
        JFrame frame = new JFrame("Carol Academy | School Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1080, 680));
        frame.setSize(1380, 820);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(BACKGROUND);
        frame.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(BLUE);
        header.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));
        header.add(new AcademyLogo(), BorderLayout.WEST);
        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 3));
        heading.setOpaque(false);
        JLabel title = new JLabel("Carol Academy");
        title.setForeground(WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        JLabel subtitle = new JLabel("School management system");
        subtitle.setForeground(INK);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        heading.add(title);
        heading.add(subtitle);
        header.add(heading, BorderLayout.CENTER);
        frame.add(header, BorderLayout.NORTH);

        JTabbedPane portals = new JTabbedPane();
        portals.setFont(new Font("SansSerif", Font.BOLD, 14));
        portals.addTab("Administration", createAdminPortal());
        portals.addTab("Parents", createParentPortal());
        frame.add(portals, BorderLayout.CENTER);

        refreshAll();
        frame.setVisible(true);
    }

    private JPanel createAdminPortal() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.addTab("Overview", createDashboard());
        tabs.addTab("Books", createBookPanel());
        tabs.addTab("Students", createStudentPanel());
        tabs.addTab("Teachers & attendance", createTeacherPanel());
        tabs.addTab("Buses & drivers", createBusPanel());
        tabs.addTab("Dormitory inventory", createDormPanel());
        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDashboard() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel welcome = new JLabel("Administration overview");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 22));
        welcome.setForeground(INK);
        panel.add(welcome, BorderLayout.NORTH);

        String[] labels = { "Students", "Teachers", "Buses", "Active buses" };
        JPanel cards = new JPanel(new GridLayout(1, labels.length, 14, 0));
        cards.setOpaque(false);
        summaryValues = new JLabel[labels.length];
        for (int i = 0; i < labels.length; i++) {
            JPanel card = new JPanel(new GridLayout(2, 1, 0, 8));
            card.setBackground(WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(LIGHT_BLUE),
                    BorderFactory.createEmptyBorder(15, 17, 15, 17)));
            JLabel label = new JLabel(labels[i]);
            label.setForeground(INK);
            label.setFont(new Font("SansSerif", Font.PLAIN, 14));
            summaryValues[i] = new JLabel("0");
            summaryValues[i].setForeground(RED);
            summaryValues[i].setFont(new Font("SansSerif", Font.BOLD, 28));
            card.add(label);
            card.add(summaryValues[i]);
            cards.add(card);
        }

        JPanel busSummary = new JPanel(new BorderLayout(0, 12));
        busSummary.setOpaque(false);
        JLabel busHeading = new JLabel("Bus fleet and driver status");
        busHeading.setFont(new Font("SansSerif", Font.BOLD, 17));
        busHeading.setForeground(INK);
        busSummary.add(busHeading, BorderLayout.NORTH);
        overviewBusTable = makeTable(new String[] {
                "Bus ID", "Registration", "Route", "Driver", "Driver status", "Bus status"
        });
        busSummary.add(new JScrollPane(overviewBusTable), BorderLayout.CENTER);
        JPanel center = new JPanel(new BorderLayout(0, 22));
        center.setOpaque(false);
        center.add(cards, BorderLayout.NORTH);
        center.add(busSummary, BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBookPanel() {
        bookTable = makeTable(new String[] {
                "Book ID", "Title", "Author", "Subject", "Copies", "Availability"
        });
        JPanel panel = createTablePanel(bookTable, "Add book", "Remove book");
        addAction(panel, "Update copies", e -> changeCopies());
        return panel;
    }

    private JPanel createStudentPanel() {
        studentTable = makeTable(new String[] {
                "Student ID", "Name", "Grade", "Phone", "Status", "Reason"
        });
        JPanel panel = createTablePanel(studentTable, "Add student", "Remove student");
        addAction(panel, "Update student status", e -> changeStudentStatus());
        return panel;
    }

    private JPanel createTeacherPanel() {
        teacherTable = makeTable(new String[] {
                "Teacher ID", "Name", "Subject", "Phone", "Attendance",
                "Check-in", "Check-out", "Expected back"
        });
        JPanel panel = createTablePanel(teacherTable, "Add teacher", "Remove teacher");
        addAction(panel, "Check in", e -> teacherCheckIn());
        addAction(panel, "Check out", e -> teacherCheckOut());
        addAction(panel, "Set expected back", e -> setTeacherReturnTime());
        return panel;
    }

    private JPanel createBusPanel() {
        busTable = makeTable(new String[] {
                "Bus ID", "Registration", "Route", "Capacity", "Driver", "Driver status",
                "Check-in", "Check-out", "Expected back", "Bus status"
        });
        JPanel panel = createTablePanel(busTable, "Add bus", "Remove bus");
        addAction(panel, "Driver present / check in", e -> driverCheckIn());
        addAction(panel, "Driver absent", e -> markDriverAbsent());
        addAction(panel, "Driver check out", e -> driverCheckOut());
        addAction(panel, "Set expected back", e -> setDriverReturnTime());
        addAction(panel, "Toggle bus active", e -> toggleBus());
        return panel;
    }

    private JPanel createDormPanel() {
        dormTable = makeTable(new String[] {
                "Dormitory", "Item", "Expected", "Present", "Missing"
        });
        JPanel panel = createTablePanel(dormTable, "Add inventory item", "Remove item");
        addAction(panel, "Update counts", e -> updateDormCounts());
        return panel;
    }

    private JPanel createParentPortal() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 28, 28));

        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 5));
        heading.setOpaque(false);
        JLabel title = new JLabel("Parent portal");
        title.setForeground(INK);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel notice = new JLabel("Choose a student to preview the parent report.");
        notice.setForeground(RED);
        notice.setFont(new Font("SansSerif", Font.PLAIN, 13));
        heading.add(title);
        heading.add(notice);
        panel.add(heading, BorderLayout.NORTH);

        JPanel details = new JPanel(new GridLayout(5, 2, 12, 14));
        details.setBackground(WHITE);
        details.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_BLUE),
                BorderFactory.createEmptyBorder(22, 24, 22, 24)));

        parentStudentSelector = new JComboBox<String>();
        parentStudentSelector.setFont(new Font("SansSerif", Font.PLAIN, 14));
        parentStudentName = parentValue();
        parentGrade = parentValue();
        parentStatus = parentValue();
        parentReason = parentValue();
        addDetail(details, "Select your student", parentStudentSelector);
        addDetail(details, "Student", parentStudentName);
        addDetail(details, "Grade", parentGrade);
        addDetail(details, "School status", parentStatus);
        addDetail(details, "Status reason", parentReason);
        parentStudentSelector.addActionListener(e -> refreshParentDetails());
        panel.add(details, BorderLayout.CENTER);
        return panel;
    }

    private JLabel parentValue() {
        JLabel label = new JLabel("—");
        label.setForeground(INK);
        label.setFont(new Font("SansSerif", Font.BOLD, 15));
        return label;
    }

    private void addDetail(JPanel panel, String label, java.awt.Component value) {
        JLabel key = new JLabel(label);
        key.setForeground(INK);
        key.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(key);
        panel.add(value);
    }

    private JPanel createTablePanel(final JTable table, String addLabel, String removeLabel) {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 22, 22, 22));
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        JButton add = button(addLabel);
        JButton remove = button(removeLabel);
        add.addActionListener(e -> addRecord(table));
        remove.addActionListener(e -> removeRecord(table));
        actions.add(add);
        actions.add(Box.createHorizontalStrut(8));
        actions.add(remove);
        actions.add(Box.createHorizontalStrut(8));
        actions.add(Box.createHorizontalGlue());
        panel.add(actions, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void addAction(JPanel panel, String label, java.awt.event.ActionListener listener) {
        JPanel actions = (JPanel) ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.NORTH);
        actions.add(Box.createHorizontalStrut(8));
        JButton action = button(label);
        action.addActionListener(listener);
        actions.add(action);
        actions.revalidate();
    }

    private JButton button(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(RED);
        button.setForeground(WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return button;
    }

    private JTable makeTable(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(29);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(LIGHT_BLUE);
        table.setSelectionBackground(new Color(195, 228, 247));
        return table;
    }

    private void addRecord(JTable table) {
        if (table == bookTable) {
            String[] values = prompt(new String[] { "Book ID", "Title", "Author", "Subject", "Copies" });
            if (values == null) return;
            try {
                ensureUniqueBookId(values[0]);
                books.add(new Book(values[0], values[1], values[2], values[3],
                        parseNonNegative(values[4], "Copies")));
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
                return;
            }
        } else if (table == studentTable) {
            String[] values = prompt(new String[] { "Student ID", "Name", "Grade", "Phone" });
            if (values == null) return;
            try {
                ensureUniqueStudentId(values[0]);
                students.add(new Student(values[0], values[1], values[2], values[3]));
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
                return;
            }
        } else if (table == teacherTable) {
            String[] values = prompt(new String[] { "Teacher ID", "Name", "Subject", "Phone" });
            if (values == null) return;
            try {
                ensureUniqueTeacherId(values[0]);
                teachers.add(new Teacher(values[0], values[1], values[2], values[3]));
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
                return;
            }
        } else if (table == busTable) {
            String[] values = prompt(new String[] { "Bus ID", "Registration", "Route",
                    "Capacity", "Driver name" });
            if (values == null) return;
            try {
                ensureUniqueBusId(values[0]);
                buses.add(new Bus(values[0], values[1], values[2],
                        parseNonNegative(values[3], "Capacity"), values[4], false));
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
                return;
            }
        } else if (table == dormTable) {
            String[] values = prompt(new String[] { "Dormitory", "Item", "Expected quantity", "Present quantity" });
            if (values == null) return;
            try {
                int expected = parseNonNegative(values[2], "Expected quantity");
                int present = parseNonNegative(values[3], "Present quantity");
                dormItems.add(new DormItem(values[0], values[1], expected, present));
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
                return;
            }
        }
        refreshAll();
    }

    private String[] prompt(String[] labels) {
        JPanel form = new JPanel(new GridLayout(labels.length, 2, 8, 8));
        JTextField[] inputs = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            form.add(new JLabel(labels[i] + ":"));
            inputs[i] = new JTextField(18);
            form.add(inputs[i]);
        }
        int result = JOptionPane.showConfirmDialog(null, form, "Enter details",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return null;

        String[] values = new String[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            values[i] = inputs[i].getText().trim();
            if (values[i].isEmpty()) {
                showError("Please fill in every field.");
                return null;
            }
        }
        return values;
    }

    private void removeRecord(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            showMessage("Select a record to remove.");
            return;
        }
        if (table == bookTable) books.remove(row);
        else if (table == studentTable) students.remove(row);
        else if (table == teacherTable) teachers.remove(row);
        else if (table == busTable) buses.remove(row);
        else if (table == dormTable) dormItems.remove(row);
        refreshAll();
    }

    private void changeCopies() {
        int row = selectedRow(bookTable);
        if (row < 0) return;
        String value = JOptionPane.showInputDialog(null, "New number of copies:", "Update book stock",
                JOptionPane.QUESTION_MESSAGE);
        if (value == null) return;
        try {
            books.get(row).copies = parseNonNegative(value.trim(), "Copies");
            refreshAll();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void changeStudentStatus() {
        int row = selectedRow(studentTable);
        if (row < 0) return;
        Student student = students.get(row);
        String[] statuses = { "Enrolled", "Home", "Suspended", "Expelled" };
        String status = (String) JOptionPane.showInputDialog(null, "Select status:",
                "Update student status", JOptionPane.QUESTION_MESSAGE, null, statuses, student.status);
        if (status == null) return;
        String reason = "";
        if (!"Enrolled".equals(status)) {
            reason = JOptionPane.showInputDialog(null, "Enter the reason for " + status.toLowerCase() + ":",
                    "Status reason", JOptionPane.QUESTION_MESSAGE);
            if (reason == null) return;
            reason = reason.trim();
            if (reason.isEmpty()) {
                showError("A reason is required for this status.");
                return;
            }
        }
        student.status = status;
        student.reason = reason;
        refreshAll();
    }

    private void teacherCheckIn() {
        int row = selectedRow(teacherTable);
        if (row < 0) return;
        Teacher teacher = teachers.get(row);
        teacher.checkIn = now();
        teacher.checkOut = "";
        teacher.attendance = "Present";
        refreshAll();
    }

    private void teacherCheckOut() {
        int row = selectedRow(teacherTable);
        if (row < 0) return;
        Teacher teacher = teachers.get(row);
        if (teacher.checkIn.isEmpty() || !"Present".equals(teacher.attendance)) {
            showError("Check in the teacher before checking out.");
            return;
        }
        teacher.checkOut = now();
        teacher.attendance = "Checked out";
        refreshAll();
    }

    private void setTeacherReturnTime() {
        int row = selectedRow(teacherTable);
        if (row < 0) return;
        Teacher teacher = teachers.get(row);
        String time = askTime("Enter expected return time (HH:mm):");
        if (time == null) return;
        teacher.expectedBack = time;
        refreshAll();
    }

    private void driverCheckIn() {
        int row = selectedRow(busTable);
        if (row < 0) return;
        Bus bus = buses.get(row);
        bus.driverStatus = "Present";
        bus.checkIn = now();
        bus.checkOut = "";
        refreshAll();
    }

    private void markDriverAbsent() {
        int row = selectedRow(busTable);
        if (row < 0) return;
        Bus bus = buses.get(row);
        bus.driverStatus = "Absent";
        bus.checkIn = "";
        bus.checkOut = "";
        refreshAll();
    }

    private void driverCheckOut() {
        int row = selectedRow(busTable);
        if (row < 0) return;
        Bus bus = buses.get(row);
        if (!"Present".equals(bus.driverStatus) || bus.checkIn.isEmpty()) {
            showError("Mark the driver present and check in before checking out.");
            return;
        }
        bus.checkOut = now();
        bus.driverStatus = "Checked out";
        refreshAll();
    }

    private void setDriverReturnTime() {
        int row = selectedRow(busTable);
        if (row < 0) return;
        Bus bus = buses.get(row);
        String time = askTime("Enter expected return time (HH:mm):");
        if (time == null) return;
        bus.expectedBack = time;
        refreshAll();
    }

    private void toggleBus() {
        int row = selectedRow(busTable);
        if (row < 0) return;
        buses.get(row).active = !buses.get(row).active;
        refreshAll();
    }

    private void updateDormCounts() {
        int row = selectedRow(dormTable);
        if (row < 0) return;
        DormItem item = dormItems.get(row);
        String[] values = prompt(new String[] { "Expected quantity", "Present quantity" });
        if (values == null) return;
        try {
            item.expected = parseNonNegative(values[0], "Expected quantity");
            item.present = parseNonNegative(values[1], "Present quantity");
            refreshAll();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private int selectedRow(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) showMessage("Select a record first.");
        return row;
    }

    private String askTime(String message) {
        String value = JOptionPane.showInputDialog(null, message, "Expected return time",
                JOptionPane.QUESTION_MESSAGE);
        if (value == null) return null;
        try {
            return LocalTime.parse(value.trim(), TIME_FORMAT).format(TIME_FORMAT);
        } catch (DateTimeParseException ex) {
            showError("Enter a valid time using 24-hour HH:mm format, for example 15:30.");
            return null;
        }
    }

    private String now() {
        return LocalTime.now().format(TIME_FORMAT);
    }

    private int parseNonNegative(String value, String field) {
        try {
            int number = Integer.parseInt(value);
            if (number < 0) throw new NumberFormatException();
            return number;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(field + " must be a whole number greater than or equal to 0.");
        }
    }

    private void ensureUniqueBookId(String id) {
        for (Book item : books) if (item.id.equalsIgnoreCase(id)) throw new IllegalArgumentException("That book ID already exists.");
    }

    private void ensureUniqueStudentId(String id) {
        for (Student item : students) if (item.id.equalsIgnoreCase(id)) throw new IllegalArgumentException("That student ID already exists.");
    }

    private void ensureUniqueTeacherId(String id) {
        for (Teacher item : teachers) if (item.id.equalsIgnoreCase(id)) throw new IllegalArgumentException("That teacher ID already exists.");
    }

    private void ensureUniqueBusId(String id) {
        for (Bus item : buses) if (item.id.equalsIgnoreCase(id)) throw new IllegalArgumentException("That bus ID already exists.");
    }

    private void refreshAll() {
        if (bookTable != null) {
            DefaultTableModel model = model(bookTable);
            model.setRowCount(0);
            for (Book book : books) {
                model.addRow(new Object[] { book.id, book.title, book.author, book.subject,
                        book.copies, book.copies > 0 ? "Available" : "Out of stock" });
            }
        }
        if (studentTable != null) {
            DefaultTableModel model = model(studentTable);
            model.setRowCount(0);
            for (Student student : students) {
                model.addRow(new Object[] { student.id, student.name, student.grade, student.phone,
                        student.status, student.reason });
            }
        }
        if (teacherTable != null) {
            DefaultTableModel model = model(teacherTable);
            model.setRowCount(0);
            for (Teacher teacher : teachers) {
                model.addRow(new Object[] { teacher.id, teacher.name, teacher.subject, teacher.phone,
                        teacher.attendance, teacher.checkIn, teacher.checkOut, teacher.expectedBack });
            }
        }
        if (busTable != null) {
            DefaultTableModel model = model(busTable);
            model.setRowCount(0);
            for (Bus bus : buses) {
                model.addRow(new Object[] { bus.id, bus.registration, bus.route, bus.capacity, bus.driver,
                        bus.driverStatus, bus.checkIn, bus.checkOut, bus.expectedBack,
                        bus.active ? "Active" : "Inactive" });
            }
        }
        if (overviewBusTable != null) {
            DefaultTableModel model = model(overviewBusTable);
            model.setRowCount(0);
            for (Bus bus : buses) {
                model.addRow(new Object[] { bus.id, bus.registration, bus.route, bus.driver,
                        bus.driverStatus, bus.active ? "Active" : "Inactive" });
            }
        }
        if (dormTable != null) {
            DefaultTableModel model = model(dormTable);
            model.setRowCount(0);
            for (DormItem item : dormItems) {
                model.addRow(new Object[] { item.dormitory, item.name, item.expected, item.present,
                        Math.max(0, item.expected - item.present) });
            }
        }
        if (summaryValues != null) {
            int activeBuses = 0;
            for (Bus bus : buses) if (bus.active) activeBuses++;
            summaryValues[0].setText(String.valueOf(students.size()));
            summaryValues[1].setText(String.valueOf(teachers.size()));
            summaryValues[2].setText(String.valueOf(buses.size()));
            summaryValues[3].setText(activeBuses + " / " + buses.size());
        }
        refreshParentSelector();
    }

    private DefaultTableModel model(JTable table) {
        return (DefaultTableModel) table.getModel();
    }

    private void refreshParentSelector() {
        if (parentStudentSelector == null) return;
        Object previous = parentStudentSelector.getSelectedItem();
        String selectedId = previous == null ? null : previous.toString().split(" - ", 2)[0];
        parentStudentSelector.removeAllItems();
        for (Student student : students) {
            parentStudentSelector.addItem(student.id + " - " + student.name);
        }
        if (selectedId != null) {
            for (int i = 0; i < parentStudentSelector.getItemCount(); i++) {
                if (parentStudentSelector.getItemAt(i).startsWith(selectedId + " - ")) {
                    parentStudentSelector.setSelectedIndex(i);
                    break;
                }
            }
        }
        refreshParentDetails();
    }

    private void refreshParentDetails() {
        if (parentStudentSelector == null) return;
        Object selected = parentStudentSelector.getSelectedItem();
        if (selected == null) return;
        String id = selected.toString().split(" - ", 2)[0];
        for (Student student : students) {
            if (student.id.equals(id)) {
                showParentDetails(student);
                return;
            }
        }
    }

    private void showParentDetails(Student student) {
        parentStudentName.setText(student.name + " (" + student.id + ")");
        parentGrade.setText(student.grade);
        parentStatus.setText(student.status);
        parentReason.setText(student.reason.isEmpty() ? "—" : student.reason);
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Carol Academy", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Please check the information", JOptionPane.ERROR_MESSAGE);
    }

    private static class AcademyLogo extends JPanel {
        private AcademyLogo() {
            setOpaque(false);
            setPreferredSize(new Dimension(62, 62));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(WHITE);
            g.fillOval(2, 2, 58, 58);
            g.setColor(RED);
            g.setFont(new Font("SansSerif", Font.BOLD, 20));
            g.drawString("CA", 12, 38);
            g.dispose();
        }
    }

    private static class Book {
        private final String id;
        private final String title;
        private final String author;
        private final String subject;
        private int copies;

        private Book(String id, String title, String author, String subject, int copies) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.subject = subject;
            this.copies = copies;
        }
    }

    private static class Student {
        private final String id;
        private final String name;
        private final String grade;
        private final String phone;
        private String status = "Enrolled";
        private String reason = "";

        private Student(String id, String name, String grade, String phone) {
            this.id = id;
            this.name = name;
            this.grade = grade;
            this.phone = phone;
        }
    }

    private static class Teacher {
        private final String id;
        private final String name;
        private final String subject;
        private final String phone;
        private String attendance = "Absent";
        private String checkIn = "";
        private String checkOut = "";
        private String expectedBack = "";

        private Teacher(String id, String name, String subject, String phone) {
            this.id = id;
            this.name = name;
            this.subject = subject;
            this.phone = phone;
        }
    }

    private static class Bus {
        private final String id;
        private final String registration;
        private final String route;
        private final int capacity;
        private final String driver;
        private boolean active;
        private String driverStatus = "Absent";
        private String checkIn = "";
        private String checkOut = "";
        private String expectedBack = "";

        private Bus(String id, String registration, String route, int capacity, String driver, boolean active) {
            this.id = id;
            this.registration = registration;
            this.route = route;
            this.capacity = capacity;
            this.driver = driver;
            this.active = active;
        }
    }

    private static class DormItem {
        private final String dormitory;
        private final String name;
        private int expected;
        private int present;

        private DormItem(String dormitory, String name, int expected, int present) {
            this.dormitory = dormitory;
            this.name = name;
            this.expected = expected;
            this.present = present;
        }
    }
}
