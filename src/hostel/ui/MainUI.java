package hostel.ui;

import hostel.db.DBConnection;
import hostel.model.Expense;
import hostel.model.Payment;
import hostel.model.User;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * MainUI – single-file Swing application for the Hostel Expense Tracker.
 *
 * Layout:
 *   ┌──────────────────────────────────────────────────────┐
 *   │  Header (title + tagline)                            │
 *   ├────────┬─────────────────────────────────────────────┤
 *   │  Nav   │  Content panel (CardLayout)                 │
 *   │ panel  │  – Dashboard / Users / Expenses /           │
 *   │        │    Payments / Summary                       │
 *   └────────┴─────────────────────────────────────────────┘
 */
public class MainUI extends JFrame {

    // ── Colour palette (all in one place – easy to reskin) ───────────────────
    static final Color BG         = new Color(0xF7F8FA);
    static final Color NAV_BG     = new Color(0x1E1E2E);
    static final Color NAV_SEL    = new Color(0x4F46E5);   // indigo accent
    static final Color NAV_HOVER  = new Color(0x2D2D42);
    static final Color NAV_TEXT   = new Color(0xBDBDCC);
    static final Color WHITE      = Color.WHITE;
    static final Color CARD_BG    = Color.WHITE;
    static final Color ACCENT     = new Color(0x4F46E5);
    static final Color SUCCESS    = new Color(0x22C55E);
    static final Color DANGER     = new Color(0xEF4444);
    static final Color TEXT_PRI   = new Color(0x111827);
    static final Color TEXT_SEC   = new Color(0x6B7280);
    static final Color BORDER_COL = new Color(0xE5E7EB);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    static final Font F_HEADER = new Font("SansSerif", Font.BOLD, 20);
    static final Font F_TITLE  = new Font("SansSerif", Font.BOLD, 15);
    static final Font F_BODY   = new Font("SansSerif", Font.PLAIN, 13);
    static final Font F_SMALL  = new Font("SansSerif", Font.PLAIN, 11);
    static final Font F_NAV    = new Font("SansSerif", Font.PLAIN, 13);
    static final Font F_MONO   = new Font("Monospaced", Font.BOLD,  13);

    // ── CardLayout content panel ──────────────────────────────────────────────
    private final CardLayout  cards   = new CardLayout();
    private final JPanel      content = new JPanel(cards);

    // Keep references to panels that need refreshing
    private UsersPanel    usersPanel;
    private ExpensePanel  expensePanel;
    private PaymentPanel  paymentPanel;
    private SummaryPanel  summaryPanel;
    private DashboardPanel dashPanel;

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Verify DB connection before opening the window
            if (DBConnection.getConnection() == null) {
                JOptionPane.showMessageDialog(null,
                    "Cannot connect to MySQL.\n"
                    + "Please check DB credentials in DBConnection.java\n"
                    + "and ensure MySQL is running.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            new MainUI().setVisible(true);
        });
    }

    // ── Constructor ───────────────────────────────────────────────────────────
    public MainUI() {
        setTitle("Hostel Expense Tracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildNav(),    BorderLayout.WEST);
        add(buildContent(),BorderLayout.CENTER);

        // Shutdown hook – close DB cleanly
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { DBConnection.close(); }
        });
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Header
    // ────────────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(NAV_BG);
        p.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel title = new JLabel("🏠 Hostel Expense Tracker");
        title.setFont(F_HEADER);
        title.setForeground(WHITE);

        JLabel sub = new JLabel("Track · Split · Settle");
        sub.setFont(F_SMALL);
        sub.setForeground(new Color(0x818CF8));  // light indigo

        JPanel text = new JPanel(new GridLayout(2,1,0,2));
        text.setOpaque(false);
        text.add(title);
        text.add(sub);
        p.add(text, BorderLayout.WEST);

        JLabel dateLbl = new JLabel(new SimpleDateFormat("EEE, dd MMM yyyy").format(new Date()));
        dateLbl.setFont(F_SMALL);
        dateLbl.setForeground(NAV_TEXT);
        p.add(dateLbl, BorderLayout.EAST);

        return p;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Nav sidebar
    // ────────────────────────────────────────────────────────────────────────
    private JPanel buildNav() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(NAV_BG);
        nav.setPreferredSize(new Dimension(180, 0));
        nav.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));

        String[][] items = {
            {"📊", "Dashboard"},
            {"👥", "Users"},
            {"💸", "Expenses"},
            {"💳", "Payments"},
            {"📋", "Summary"},
        };

        ButtonGroup grp = new ButtonGroup();
        for (int i = 0; i < items.length; i++) {
            JToggleButton btn = navButton(items[i][0] + "  " + items[i][1], items[i][1]);
            if (i == 0) btn.setSelected(true);
            nav.add(btn);
            nav.add(Box.createRigidArea(new Dimension(0, 4)));
            grp.add(btn);
        }
        nav.add(Box.createVerticalGlue());
        return nav;
    }

    private JToggleButton navButton(String label, String card) {
        JToggleButton b = new JToggleButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setColor(NAV_SEL);
                    g2.fillRoundRect(8, 0, getWidth()-16, getHeight(), 8, 8);
                } else if (getModel().isRollover()) {
                    g2.setColor(NAV_HOVER);
                    g2.fillRoundRect(8, 0, getWidth()-16, getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(F_NAV);
        b.setForeground(NAV_TEXT);
        b.setBackground(NAV_BG);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(180, 40));
        b.setPreferredSize(new Dimension(180, 40));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        b.addActionListener(e -> {
            b.setForeground(WHITE);
            cards.show(content, card);
            refreshPanel(card);
        });

        // On deselect restore colour
        b.addChangeListener(e -> {
            if (!b.isSelected()) b.setForeground(NAV_TEXT);
            else                  b.setForeground(WHITE);
        });
        return b;
    }

    private void refreshPanel(String card) {
        switch (card) {
            case "Dashboard" -> dashPanel.refresh();
            case "Users"     -> usersPanel.refresh();
            case "Expenses"  -> expensePanel.refresh();
            case "Payments"  -> paymentPanel.refresh();
            case "Summary"   -> summaryPanel.refresh();
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Content panel
    // ────────────────────────────────────────────────────────────────────────
    private JPanel buildContent() {
        content.setBackground(BG);
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        dashPanel    = new DashboardPanel();
        usersPanel   = new UsersPanel();
        expensePanel = new ExpensePanel();
        paymentPanel = new PaymentPanel();
        summaryPanel = new SummaryPanel();

        content.add(dashPanel,    "Dashboard");
        content.add(usersPanel,   "Users");
        content.add(expensePanel, "Expenses");
        content.add(paymentPanel, "Payments");
        content.add(summaryPanel, "Summary");

        cards.show(content, "Dashboard");
        dashPanel.refresh();
        return content;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Shared helpers
    // ════════════════════════════════════════════════════════════════════════

    /** Styled card panel (white, rounded border, shadow-like). */
    static JPanel card(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COL, 1, true),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        if (title != null && !title.isEmpty()) {
            JLabel lbl = new JLabel(title);
            lbl.setFont(F_TITLE);
            lbl.setForeground(TEXT_PRI);
            lbl.setBorder(BorderFactory.createEmptyBorder(0,0,6,0));
            p.add(lbl, BorderLayout.NORTH);
        }
        return p;
    }

    /** Accent-coloured action button. */
    static JButton accentBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(F_BODY);
        b.setBackground(ACCENT);
        b.setForeground(WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        return b;
    }

    /** Red danger button. */
    static JButton dangerBtn(String text) {
        JButton b = accentBtn(text);
        b.setBackground(DANGER);
        return b;
    }

    /** Styled JTextField. */
    static JTextField field(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(F_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COL, 1, true),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return f;
    }

    /** Styled JComboBox. */
    static <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> c = new JComboBox<>(items);
        c.setFont(F_BODY);
        return c;
    }

    /** Label + field pair inside a grid panel. */
    static JPanel labelField(String labelText, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(F_SMALL);
        lbl.setForeground(TEXT_SEC);
        p.add(lbl,  BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    /** Right-align cell renderer for tables. */
    static DefaultTableCellRenderer rightRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.RIGHT);
        return r;
    }

    /** Styled JTable inside a scroll pane. */
    static JScrollPane tablePane(JTable tbl) {
        tbl.setFont(F_BODY);
        tbl.setRowHeight(28);
        tbl.getTableHeader().setFont(F_SMALL);
        tbl.getTableHeader().setBackground(new Color(0xF3F4F6));
        tbl.setGridColor(BORDER_COL);
        tbl.setSelectionBackground(new Color(0xEEF2FF));
        tbl.setSelectionForeground(TEXT_PRI);
        tbl.setShowGrid(true);
        tbl.setAutoCreateRowSorter(true);
        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(new LineBorder(BORDER_COL, 1));
        return sp;
    }

    /** Today's date as "YYYY-MM-DD". */
    static String today() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PANEL: Dashboard
    // ════════════════════════════════════════════════════════════════════════
    class DashboardPanel extends JPanel {
        private final JLabel totalExpLbl  = kpiLabel("—");
        private final JLabel totalPayLbl  = kpiLabel("—");
        private final JLabel userCountLbl = kpiLabel("—");
        private final JLabel bigOwedLbl   = kpiLabel("—");
        private final DefaultTableModel  recentModel;
        private final JTable             recentTable;

        DashboardPanel() {
            setLayout(new BorderLayout(0, 14));
            setOpaque(false);

            // ── KPI row ──────────────────────────────────────────────────────
            JPanel kpis = new JPanel(new GridLayout(1, 4, 12, 0));
            kpis.setOpaque(false);
            kpis.add(kpiCard("Total Expenses", totalExpLbl,  "💰", ACCENT));
            kpis.add(kpiCard("Payments Made",  totalPayLbl,  "💳", SUCCESS));
            kpis.add(kpiCard("Residents",      userCountLbl, "👥", new Color(0xF59E0B)));
            kpis.add(kpiCard("Highest Balance",bigOwedLbl,   "📈", DANGER));
            add(kpis, BorderLayout.NORTH);

            // ── Recent expenses table ─────────────────────────────────────────
            recentModel = new DefaultTableModel(
                new String[]{"Date","Title","Amount","Paid By","Type"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            recentTable = new JTable(recentModel);
            recentTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer());

            JPanel tableCard = card("Recent Expenses");
            tableCard.add(tablePane(recentTable), BorderLayout.CENTER);
            add(tableCard, BorderLayout.CENTER);
        }

        void refresh() {
            List<Expense> exps  = Expense.getAllExpenses();
            List<Payment> pays  = Payment.getAllPayments();
            List<User>    users = User.getAllUsers();

            double totalExp = exps.stream().mapToDouble(Expense::getAmount).sum();
            double totalPay = pays.stream().mapToDouble(Payment::getAmount).sum();

            totalExpLbl .setText(String.format("PKR %.2f", totalExp));
            totalPayLbl .setText(String.format("PKR %.2f", totalPay));
            userCountLbl.setText(String.valueOf(users.size()));

            List<Object[]> bals = Payment.getBalances();
            double max = bals.stream()
                .mapToDouble(r -> Math.abs((Double)r[3])).max().orElse(0);
            bigOwedLbl.setText(String.format("PKR %.2f", max));

            // Recent 10 expenses
            recentModel.setRowCount(0);
            exps.stream().limit(10).forEach(e ->
                recentModel.addRow(new Object[]{
                    e.getDate(), e.getTitle(),
                    String.format("%.2f", e.getAmount()),
                    e.getPaidByName(), e.getType()
                })
            );
        }

        private JLabel kpiLabel(String text) {
            JLabel l = new JLabel(text);
            l.setFont(F_MONO);
            l.setForeground(TEXT_PRI);
            return l;
        }

        private JPanel kpiCard(String label, JLabel valueLbl, String icon, Color accent) {
            JPanel p = new JPanel(new BorderLayout(0, 4));
            p.setBackground(CARD_BG);
            p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COL, 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
            ));
            JLabel iconLbl = new JLabel(icon + "  " + label);
            iconLbl.setFont(F_SMALL);
            iconLbl.setForeground(accent);
            p.add(iconLbl, BorderLayout.NORTH);
            p.add(valueLbl, BorderLayout.CENTER);
            return p;
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PANEL: Users
    // ════════════════════════════════════════════════════════════════════════
    class UsersPanel extends JPanel {
        private final DefaultTableModel model;
        private final JTable            table;
        private final JTextField        tfName  = field(15);
        private final JTextField        tfEmail = field(20);
        private final JTextField        tfRoom  = field(8);

        UsersPanel() {
            setLayout(new BorderLayout(0, 14));
            setOpaque(false);

            // ── Form ─────────────────────────────────────────────────────────
            JPanel formCard = card("Add / Edit User");
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            row.setOpaque(false);
            row.add(labelField("Name",    tfName));
            row.add(labelField("Email",   tfEmail));
            row.add(labelField("Room No", tfRoom));

            JButton addBtn = accentBtn("➕ Add User");
            JButton updBtn = accentBtn("✏ Update");
            JButton delBtn = dangerBtn ("🗑 Delete");
            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            btnRow.setOpaque(false);
            btnRow.add(addBtn); btnRow.add(updBtn); btnRow.add(delBtn);

            JPanel formInner = new JPanel(new GridLayout(2, 1, 0, 8));
            formInner.setOpaque(false);
            formInner.add(row); formInner.add(btnRow);
            formCard.add(formInner, BorderLayout.CENTER);

            // ── Table ─────────────────────────────────────────────────────────
            model = new DefaultTableModel(
                new String[]{"ID","Name","Email","Room"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            table = new JTable(model);
            JPanel tableCard = card("All Residents");
            tableCard.add(tablePane(table), BorderLayout.CENTER);

            add(formCard,  BorderLayout.NORTH);
            add(tableCard, BorderLayout.CENTER);

            // ── Listeners ─────────────────────────────────────────────────────
            addBtn.addActionListener(e -> {
                String nm = tfName.getText().trim();
                String em = tfEmail.getText().trim();
                String rm = tfRoom.getText().trim();
                if (nm.isEmpty() || em.isEmpty() || rm.isEmpty()) {
                    warn("All fields are required."); return;
                }
                if (User.addUser(nm, em, rm)) { clear(); refresh(); ok("User added!"); }
                else warn("Could not add user (duplicate email?).");
            });

            updBtn.addActionListener(e -> {
                int row2 = table.getSelectedRow();
                if (row2 < 0) { warn("Select a user to update."); return; }
                int id = (int) model.getValueAt(table.convertRowIndexToModel(row2), 0);
                String nm = tfName.getText().trim();
                String em = tfEmail.getText().trim();
                String rm = tfRoom.getText().trim();
                if (nm.isEmpty() || em.isEmpty() || rm.isEmpty()) {
                    warn("All fields are required."); return;
                }
                if (User.updateUser(id, nm, em, rm)) { clear(); refresh(); ok("Updated!"); }
                else warn("Update failed.");
            });

            delBtn.addActionListener(e -> {
                int row2 = table.getSelectedRow();
                if (row2 < 0) { warn("Select a user to delete."); return; }
                int id = (int) model.getValueAt(table.convertRowIndexToModel(row2), 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete this user and ALL their data?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (User.deleteUser(id)) { clear(); refresh(); ok("Deleted."); }
                    else warn("Delete failed.");
                }
            });

            // Click row → populate form
            table.getSelectionModel().addListSelectionListener(ev -> {
                if (!ev.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                    int mr = table.convertRowIndexToModel(table.getSelectedRow());
                    tfName .setText((String) model.getValueAt(mr, 1));
                    tfEmail.setText((String) model.getValueAt(mr, 2));
                    tfRoom .setText((String) model.getValueAt(mr, 3));
                }
            });
        }

        void refresh() {
            model.setRowCount(0);
            User.getAllUsers().forEach(u ->
                model.addRow(new Object[]{u.getUserId(), u.getName(), u.getEmail(), u.getRoomNo()})
            );
        }

        private void clear() { tfName.setText(""); tfEmail.setText(""); tfRoom.setText(""); }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PANEL: Expenses
    // ════════════════════════════════════════════════════════════════════════
    class ExpensePanel extends JPanel {
        private final DefaultTableModel     model;
        private final JTable                table;
        private final JTextField            tfTitle  = field(20);
        private final JTextField            tfAmount = field(10);
        private final JTextField            tfDate   = field(12);
        private       JComboBox<User>       cbPaidBy;
        private       JComboBox<String>     cbType;
        private       JList<User>           userList;
        private       DefaultListModel<User> listModel;
        private       JLabel               splitHint; // shows "Ctrl+click to multi-select"

        ExpensePanel() {
            setLayout(new BorderLayout(0, 14));
            setOpaque(false);
            tfDate.setText(today());

            cbPaidBy  = new JComboBox<>();
            cbType    = new JComboBox<>(new String[]{"shared", "personal"});
            listModel = new DefaultListModel<>();
            userList  = new JList<>(listModel);
            userList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            userList.setFont(F_BODY);
            userList.setBackground(WHITE);
            userList.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

            // ── Form card ─────────────────────────────────────────────────────
            JPanel formCard = card("Add Expense");

            /*
             *  Layout (GridBagLayout):
             *
             *  Row 0:  [Title label]  [Amount label] [Date label] [Paid By label] [Type label]
             *  Row 1:  [Title field]  [Amount field] [Date field] [PaidBy combo]  [Type combo]
             *  Row 2:  [Split label — spans all 5 cols]
             *  Row 3:  [Split scrollpane — spans all 5 cols, fixed height 110px]
             *  Row 4:  [hint label — spans 3] [Add btn] [Delete btn]
             */
            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            GridBagConstraints g = new GridBagConstraints();
            g.insets  = new Insets(3, 6, 3, 6);
            g.fill    = GridBagConstraints.HORIZONTAL;
            g.anchor  = GridBagConstraints.WEST;

            // — Row 0: labels ——————————————————————————————————————
            String[] lbls = {"Title", "Amount", "Date", "Paid By", "Type"};
            for (int i = 0; i < lbls.length; i++) {
                g.gridx = i; g.gridy = 0; g.weightx = (i == 0) ? 2.0 : 1.0;
                JLabel l = new JLabel(lbls[i]);
                l.setFont(F_SMALL); l.setForeground(TEXT_SEC);
                form.add(l, g);
            }

            // — Row 1: fields ——————————————————————————————————————
            g.gridy = 1;
            g.gridx = 0; g.weightx = 2.0; form.add(tfTitle,  g);
            g.gridx = 1; g.weightx = 1.0; form.add(tfAmount, g);
            g.gridx = 2;                   form.add(tfDate,   g);
            g.gridx = 3;                   form.add(cbPaidBy, g);
            g.gridx = 4;                   form.add(cbType,   g);

            // — Row 2: Split label ——————————————————————————————————
            g.gridy = 2; g.gridx = 0; g.gridwidth = 5; g.weightx = 0;
            JLabel splitLbl = new JLabel("Split Among  (hold Ctrl / Cmd to select multiple users)");
            splitLbl.setFont(F_SMALL); splitLbl.setForeground(TEXT_SEC);
            form.add(splitLbl, g);

            // — Row 3: Split list ———————————————————————————————————
            g.gridy = 3; g.gridx = 0; g.gridwidth = 5;
            g.fill  = GridBagConstraints.BOTH; g.weighty = 1.0;
            JScrollPane listScroll = new JScrollPane(userList);
            listScroll.setPreferredSize(new Dimension(0, 110));
            listScroll.setMinimumSize (new Dimension(0, 110));
            listScroll.setBorder(new LineBorder(BORDER_COL, 1, true));
            form.add(listScroll, g);

            // — Row 4: hint + buttons ——————————————————————————————
            g.gridy = 4; g.gridx = 0; g.gridwidth = 3;
            g.fill  = GridBagConstraints.NONE; g.weighty = 0;
            splitHint = new JLabel("Tip: for 'personal', only the payer is auto-selected.");
            splitHint.setFont(F_SMALL); splitHint.setForeground(TEXT_SEC);
            form.add(splitHint, g);

            JButton addBtn = accentBtn("➕ Add Expense");
            JButton delBtn = dangerBtn("🗑 Delete");
            g.gridx = 3; g.gridwidth = 1; form.add(addBtn, g);
            g.gridx = 4;                   form.add(delBtn, g);

            formCard.add(form, BorderLayout.CENTER);

            // ── Table ─────────────────────────────────────────────────────────
            model = new DefaultTableModel(
                new String[]{"ID","Title","Amount","Date","Paid By","Type"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            table = new JTable(model);
            table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer());
            JPanel tableCard = card("Expense History");
            tableCard.add(tablePane(table), BorderLayout.CENTER);

            add(formCard,  BorderLayout.NORTH);
            add(tableCard, BorderLayout.CENTER);

            // ── Listeners ─────────────────────────────────────────────────────
            addBtn.addActionListener(e -> {
                String title  = tfTitle.getText().trim();
                String amtStr = tfAmount.getText().trim();
                String date   = tfDate.getText().trim();
                User   payer  = (User) cbPaidBy.getSelectedItem();
                String type   = (String) cbType.getSelectedItem();
                List<User> selected = userList.getSelectedValuesList();

                if (title.isEmpty() || amtStr.isEmpty() || date.isEmpty() || payer == null) {
                    warn("Title, amount, date and payer are required."); return;
                }
                double amt;
                try { amt = Double.parseDouble(amtStr); } catch (NumberFormatException ex) {
                    warn("Amount must be a number."); return;
                }
                if (amt <= 0) { warn("Amount must be greater than zero."); return; }
                if (selected.isEmpty()) { warn("Select at least one user from the 'Split Among' list."); return; }

                List<Integer> ids = new ArrayList<>();
                selected.forEach(u -> ids.add(u.getUserId()));

                if (Expense.addExpense(title, amt, date, payer.getUserId(), type, ids)) {
                    tfTitle.setText(""); tfAmount.setText("");
                    userList.clearSelection();
                    refresh(); ok("Expense saved!");
                } else warn("Could not save expense.");
            });

            delBtn.addActionListener(e -> {
                int row2 = table.getSelectedRow();
                if (row2 < 0) { warn("Select an expense to delete."); return; }
                int id = (int) model.getValueAt(table.convertRowIndexToModel(row2), 0);
                if (JOptionPane.showConfirmDialog(this, "Delete this expense?",
                        "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (Expense.deleteExpense(id)) { refresh(); ok("Deleted."); }
                    else warn("Delete failed.");
                }
            });

            // When type changes to 'personal', auto-select only the payer
            cbType.addActionListener(e -> autoSelectForPersonal());
            cbPaidBy.addActionListener(e -> {
                if ("personal".equals(cbType.getSelectedItem())) autoSelectForPersonal();
            });
        }

        /** For personal expenses, auto-select only the payer in the split list. */
        private void autoSelectForPersonal() {
            boolean isPersonal = "personal".equals(cbType.getSelectedItem());
            if (isPersonal) {
                User payer = (User) cbPaidBy.getSelectedItem();
                userList.clearSelection();
                if (payer != null) {
                    for (int i = 0; i < listModel.size(); i++) {
                        if (listModel.get(i).getUserId() == payer.getUserId()) {
                            userList.setSelectedIndex(i);
                            break;
                        }
                    }
                }
                userList.setEnabled(false);
                splitHint.setText("Personal expense — only the payer's split is recorded.");
            } else {
                userList.setEnabled(true);
                userList.clearSelection();
                splitHint.setText("Tip: hold Ctrl / Cmd to select multiple users.");
            }
        }

        void refresh() {
            // Reload user dropdowns
            List<User> users = User.getAllUsers();
            cbPaidBy.removeAllItems();
            listModel.clear();
            users.forEach(u -> { cbPaidBy.addItem(u); listModel.addElement(u); });

            // Reload table
            model.setRowCount(0);
            Expense.getAllExpenses().forEach(exp ->
                model.addRow(new Object[]{
                    exp.getExpenseId(), exp.getTitle(),
                    String.format("%.2f", exp.getAmount()),
                    exp.getDate(), exp.getPaidByName(), exp.getType()
                })
            );
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PANEL: Payments
    // ════════════════════════════════════════════════════════════════════════
    class PaymentPanel extends JPanel {
        private final DefaultTableModel model;
        private final JTable            table;
        private final DefaultTableModel balModel;
        private final JTable            balTable;
        private final JTextField        tfAmount = field(10);
        private final JTextField        tfDate   = field(12);
        private       JComboBox<User>   cbFrom;
        private       JComboBox<User>   cbTo;

        PaymentPanel() {
            setLayout(new BorderLayout(0, 14));
            setOpaque(false);
            tfDate.setText(today());
            cbFrom = new JComboBox<>();
            cbTo   = new JComboBox<>();

            // ── Form ─────────────────────────────────────────────────────────
            JPanel formCard = card("Record Payment / Settlement");
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            row.setOpaque(false);
            row.add(labelField("From",   cbFrom));
            row.add(labelField("To",     cbTo));
            row.add(labelField("Amount", tfAmount));
            row.add(labelField("Date",   tfDate));

            JButton addBtn = accentBtn("➕ Record Payment");
            JButton delBtn = dangerBtn("🗑 Delete");
            JPanel btnRow  = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            btnRow.setOpaque(false);
            btnRow.add(addBtn); btnRow.add(delBtn);

            JPanel fi = new JPanel(new GridLayout(2, 1, 0, 8));
            fi.setOpaque(false);
            fi.add(row); fi.add(btnRow);
            formCard.add(fi, BorderLayout.CENTER);

            // ── Balance table ─────────────────────────────────────────────────
            balModel = new DefaultTableModel(
                new String[]{"Name","Room","Balance (PKR)","Status"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            balTable = new JTable(balModel) {
                @Override public Component prepareRenderer(
                        javax.swing.table.TableCellRenderer r, int row, int col) {
                    Component c = super.prepareRenderer(r, row, col);
                    String status = (String) getModel().getValueAt(
                            convertRowIndexToModel(row), 3);
                    if ("Owed".equals(status))       c.setForeground(SUCCESS);
                    else if ("Owes".equals(status))  c.setForeground(DANGER);
                    else                             c.setForeground(TEXT_SEC);
                    return c;
                }
            };
            balTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer());
            JPanel balCard = card("Current Balances");
            balCard.add(tablePane(balTable), BorderLayout.CENTER);

            // ── Payment history table ─────────────────────────────────────────
            model = new DefaultTableModel(
                new String[]{"ID","From","To","Amount","Date"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            table = new JTable(model);
            table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer());
            JPanel histCard = card("Payment History");
            histCard.add(tablePane(table), BorderLayout.CENTER);

            // ── Layout ────────────────────────────────────────────────────────
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, balCard, histCard);
            split.setDividerLocation(420);
            split.setBorder(null);
            split.setOpaque(false);

            add(formCard, BorderLayout.NORTH);
            add(split,    BorderLayout.CENTER);

            // ── Listeners ─────────────────────────────────────────────────────
            addBtn.addActionListener(e -> {
                User from = (User) cbFrom.getSelectedItem();
                User to   = (User) cbTo.getSelectedItem();
                String amtStr = tfAmount.getText().trim();
                String date   = tfDate.getText().trim();
                if (from == null || to == null || amtStr.isEmpty()) {
                    warn("All fields required."); return;
                }
                if (from.getUserId() == to.getUserId()) {
                    warn("From and To must be different users."); return;
                }
                double amt;
                try { amt = Double.parseDouble(amtStr); } catch (NumberFormatException ex) {
                    warn("Amount must be a number."); return;
                }
                if (amt <= 0) { warn("Amount must be > 0."); return; }

                if (Payment.addPayment(from.getUserId(), to.getUserId(), amt, date)) {
                    tfAmount.setText(""); refresh(); ok("Payment recorded!");
                } else warn("Could not record payment.");
            });

            delBtn.addActionListener(e -> {
                int row2 = table.getSelectedRow();
                if (row2 < 0) { warn("Select a payment to delete."); return; }
                int id = (int) model.getValueAt(table.convertRowIndexToModel(row2), 0);
                if (JOptionPane.showConfirmDialog(this, "Delete this payment?",
                        "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (Payment.deletePayment(id)) { refresh(); ok("Deleted."); }
                    else warn("Delete failed.");
                }
            });
        }

        void refresh() {
            List<User> users = User.getAllUsers();
            cbFrom.removeAllItems(); cbTo.removeAllItems();
            users.forEach(u -> { cbFrom.addItem(u); cbTo.addItem(u); });

            balModel.setRowCount(0);
            Payment.getBalances().forEach(row -> {
                double bal  = (Double) row[3];
                String stat = bal > 0.005 ? "Owed" : bal < -0.005 ? "Owes" : "Settled";
                balModel.addRow(new Object[]{
                    row[1], row[2], String.format("%.2f", bal), stat
                });
            });

            model.setRowCount(0);
            Payment.getAllPayments().forEach(p ->
                model.addRow(new Object[]{
                    p.getPaymentId(), p.getFromName(), p.getToName(),
                    String.format("%.2f", p.getAmount()), p.getDate()
                })
            );
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PANEL: Summary
    // ════════════════════════════════════════════════════════════════════════
    class SummaryPanel extends JPanel {
        private final JTextArea area = new JTextArea();

        SummaryPanel() {
            setLayout(new BorderLayout());
            setOpaque(false);

            area.setEditable(false);
            area.setFont(F_MONO);
            area.setBackground(CARD_BG);
            area.setForeground(TEXT_PRI);
            area.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

            JScrollPane sp = new JScrollPane(area);
            sp.setBorder(new LineBorder(BORDER_COL));

            JPanel wrap = card("📋 Summary Report");
            wrap.add(sp, BorderLayout.CENTER);

            JButton refBtn = accentBtn("🔄 Refresh");
            refBtn.addActionListener(e -> refresh());
            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
            top.setOpaque(false);
            top.add(refBtn);

            add(top,  BorderLayout.NORTH);
            add(wrap, BorderLayout.CENTER);
        }

        void refresh() {
            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════════════════\n");
            sb.append("         HOSTEL EXPENSE TRACKER — SUMMARY          \n");
            sb.append("═══════════════════════════════════════════════════\n\n");

            List<Expense> exps = Expense.getAllExpenses();
            double totalExp = exps.stream().mapToDouble(Expense::getAmount).sum();
            sb.append(String.format("  Total Expenses Recorded : PKR %,.2f%n", totalExp));
            sb.append(String.format("  Number of Expenses      : %d%n", exps.size()));

            List<Payment> pays   = Payment.getAllPayments();
            double totalPay = pays.stream().mapToDouble(Payment::getAmount).sum();
            sb.append(String.format("  Total Payments Made     : PKR %,.2f%n", totalPay));
            sb.append(String.format("  Number of Payments      : %d%n", pays.size()));

            sb.append("\n───────────────────────────────────────────────────\n");
            sb.append("  USER BALANCES\n");
            sb.append("───────────────────────────────────────────────────\n");
            sb.append(String.format("  %-20s %-6s %12s  %s%n",
                "Name", "Room", "Balance", "Status"));
            sb.append("  " + "─".repeat(50) + "\n");

            Payment.getBalances().forEach(row -> {
                double bal  = (Double) row[3];
                String stat = bal > 0.005  ? "← is owed"
                            : bal < -0.005 ? "→ owes"
                            : "✓ settled";
                sb.append(String.format("  %-20s %-6s %12.2f  %s%n",
                    row[1], row[2], bal, stat));
            });

            sb.append("\n───────────────────────────────────────────────────\n");
            sb.append("  TOP 5 RECENT EXPENSES\n");
            sb.append("───────────────────────────────────────────────────\n");
            exps.stream().limit(5).forEach(e ->
                sb.append(String.format("  [%s] %-25s PKR %9.2f  (%s) — %s%n",
                    e.getDate(), e.getTitle(), e.getAmount(),
                    e.getType(), e.getPaidByName()))
            );

            sb.append("\n═══════════════════════════════════════════════════\n");
            area.setText(sb.toString());
            area.setCaretPosition(0);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Dialog helpers
    // ════════════════════════════════════════════════════════════════════════
    static void warn(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    static void ok(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
