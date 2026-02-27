package org.example;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ChartUtils;
import java.io.File;

public class PowerGuardGUI extends JFrame {
    private LinearRegressionModel predictor;
    private final Map<String, Map<String, Integer>> deviceLibrary = new HashMap<>();
    private final double UNIT_RATE = 8.0;

    private JTextField txtQuantity, txtHours, txtBudget, txtSearch;
    private JLabel lblResult;
    private ChartPanel chartPanel;
    private DefaultCategoryDataset dataset;
    private JPanel pnlStatus;
    private JComboBox<String> comboCompany, comboDevice;

    // History Table Components
    private DefaultTableModel tableModel;
    private JTable historyTable;

    public PowerGuardGUI(LinearRegressionModel predictor) {
        // 1. Initialize Predictor FIRST
        this.predictor = predictor;
        try {
            // Fix for "datasetHeader is null" crash
            this.predictor.initializeHeader();
        } catch (Exception e) {
            System.err.println("Predictor Header Init Failed: " + e.getMessage());
        }

        // 2. Apply Modern Look
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf");
        }

        initializeData();

        // 3. Set up the Window
        setTitle("PowerGuard Professional");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // 4. Initialize Sections
        setupChart();
        add(createSidebar(), BorderLayout.WEST);
        add(createMainDashboard(), BorderLayout.CENTER);
        add(createInputCard(), BorderLayout.EAST);
    }

    private void initializeData() {
        // EXISTING COMPANIES
        Map<String, Integer> samsung = new HashMap<>();
        samsung.put("Smart Refrigerator", 400);
        samsung.put("Inverter AC (1.5 Ton)", 1500);
        samsung.put("Microwave Solo", 800);

        Map<String, Integer> lg = new HashMap<>();
        lg.put("InstaView Fridge", 350);
        lg.put("OLED TV", 150);
        lg.put("Top Load Washer", 500);

        // NEW COMPANIES & DEVICES
        Map<String, Integer> whirlpool = new HashMap<>();
        whirlpool.put("Side-by-Side Fridge", 450);
        whirlpool.put("Convection Oven", 1200);
        whirlpool.put("Dishwasher", 1800);

        Map<String, Integer> dyson = new HashMap<>();
        dyson.put("Pure Cool Air Purifier", 40);
        dyson.put("Supersonic Hair Dryer", 1600);
        dyson.put("V15 Detect Vacuum", 660);

        Map<String, Integer> sony = new HashMap<>();
        sony.put("PlayStation 5", 200);
        sony.put("Bravia 4K TV", 180);
        sony.put("Soundbar System", 60);

        Map<String, Integer> mobility = new HashMap<>();
        mobility.put("Tesla Wall Connector", 11500);
        mobility.put("Ather 450X Charger", 850);

        deviceLibrary.put("Samsung", samsung);
        deviceLibrary.put("LG", lg);
        deviceLibrary.put("Whirlpool", whirlpool);
        deviceLibrary.put("Dyson", dyson);
        deviceLibrary.put("Sony", sony);
        deviceLibrary.put("Mobility (EV)", mobility);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        sidebar.setBackground(new Color(33, 37, 41));
        sidebar.setPreferredSize(new Dimension(160, 700));

        JLabel lblLogo = new JLabel("POWERGUARD");
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(new Font("SansSerif", Font.BOLD, 18));
        sidebar.add(lblLogo);

        JButton btnReset = new JButton("Reset All");
        btnReset.addActionListener(e -> {
            dataset.clear();
            tableModel.setRowCount(0); // Clears the new table too
            JOptionPane.showMessageDialog(this, "Analytics history cleared.");
        });
        sidebar.add(btnReset);

        JButton btnExport = new JButton("Export Chart");
        btnExport.addActionListener(e -> saveChartImage());
        sidebar.add(btnExport);

        return sidebar;
    }

    private JPanel createMainDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout(10, 10));
        dashboard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Chart Section
        dashboard.add(new JLabel("Real-time Consumption Trend"), BorderLayout.NORTH);
        dashboard.add(chartPanel, BorderLayout.CENTER);

        // Table Section - NEW
        String[] columns = {"Device", "Cost (₹)", "CO₂ (kg)", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setPreferredSize(new Dimension(600, 200));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Usage History Log"));
        dashboard.add(scrollPane, BorderLayout.SOUTH);

        return dashboard;
    }

    private JPanel createInputCard() {
        JPanel card = new JPanel(new GridLayout(18, 1, 5, 2));
        card.setBorder(BorderFactory.createTitledBorder("Calculations"));

        txtSearch = new JTextField("Search...");
        JButton btnSearch = new JButton("Search Appliance");
        btnSearch.setBackground(new Color(33, 150, 243));
        btnSearch.setForeground(Color.WHITE);

        comboCompany = new JComboBox<>(deviceLibrary.keySet().toArray(new String[0]));
        comboDevice = new JComboBox<>();
        txtQuantity = new JTextField("1");
        txtHours = new JTextField("5.5");
        txtBudget = new JTextField("500");
        pnlStatus = new JPanel();
        pnlStatus.setBackground(Color.GREEN);
        lblResult = new JLabel("<html>Cost: ₹0.00<br>CO₂: 0.00 kg</html>");

        card.add(new JLabel("Quick Search:"));
        card.add(txtSearch);
        card.add(btnSearch);
        card.add(new JLabel("Select Company:"));
        card.add(comboCompany);
        card.add(new JLabel("Select Device:"));
        card.add(comboDevice);
        card.add(new JLabel("Hours/Day:"));
        card.add(txtHours);
        card.add(new JLabel("Monthly Budget (₹):"));
        card.add(txtBudget);
        card.add(new JLabel("Budget Status:"));
        card.add(pnlStatus);

        JButton btnPredict = new JButton("Predict Bill");
        btnPredict.addActionListener(e -> calculate());
        card.add(btnPredict);
        card.add(lblResult);

        btnSearch.addActionListener(e -> filterDevices(txtSearch.getText().toLowerCase()));
        comboCompany.addActionListener(e -> updateDeviceList());
        updateDeviceList();

        // Add this inside createInputCard()
        btnSearch.addActionListener(e -> filterDevices(txtSearch.getText().trim()));

        txtSearch.addActionListener(e -> filterDevices(txtSearch.getText().trim()));

// Clear placeholder text when user clicks
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().equals("Search...")) {
                    txtSearch.setText("");
                }
            }
        });

        return card;
    }

    private void filterDevices(String query) {
        if (query.isEmpty() || query.equals("search...")) {
            updateDeviceList(); // Reset to current selected company's list
            return;
        }

        comboDevice.removeAllItems();
        boolean found = false;

        // We must search through ALL companies, not just the selected one
        for (String company : deviceLibrary.keySet()) {
            Map<String, Integer> devices = deviceLibrary.get(company);
            for (String deviceName : devices.keySet()) {
                if (deviceName.toLowerCase().contains(query.toLowerCase())) {
                    if (!found) {
                        // Auto-switch the company dropdown to the first match found
                        comboCompany.setSelectedItem(company);
                        found = true;
                    }
                    comboDevice.addItem(deviceName);
                }
            }
        }

        if (!found) {
            JOptionPane.showMessageDialog(this, "No appliance found for: " + query);
            updateDeviceList();
        }
    }

    private void updateDeviceList() {
        comboDevice.removeAllItems();
        String selected = (String) comboCompany.getSelectedItem();
        if (selected != null) {
            deviceLibrary.get(selected).keySet().forEach(comboDevice::addItem);
        }
    }

    private void calculate() {
        try {
            String company = (String) comboCompany.getSelectedItem();
            String device = (String) comboDevice.getSelectedItem();
            if (device == null) return;

            int rating = deviceLibrary.get(company).get(device);
            int qty = Integer.parseInt(txtQuantity.getText());
            double hours = Double.parseDouble(txtHours.getText());
            double budgetLimit = Double.parseDouble(txtBudget.getText());

            // 1. ML Prediction Logic
            double hourlyKW = (rating / 1000.0) * qty;
            double predictedHourlyUnits = predictor.predict(hourlyKW);
            double totalUnits = predictedHourlyUnits * hours;
            double cost = totalUnits * UNIT_RATE;
            double carbon = totalUnits * 0.85;

            // 2. Update Result Display
            lblResult.setText(String.format("<html>Cost: ₹%.2f<br>CO₂: %.2f kg</html>", cost, carbon));
            pnlStatus.setBackground(cost > budgetLimit ? Color.RED : Color.GREEN);

            // 3. Update Chart and Table
            String entryLabel = device + " (" + (dataset.getColumnCount() + 1) + ")";
            dataset.addValue(cost, "Cost", entryLabel);

            tableModel.addRow(new Object[]{
                    device,
                    String.format("₹%.2f", cost),
                    String.format("%.2f kg", carbon),
                    (cost > budgetLimit ? "Over Budget" : "Safe")
            });

            // 4. Set Bar Color Dynamically
            org.jfree.chart.plot.CategoryPlot plot = chartPanel.getChart().getCategoryPlot();
            org.jfree.chart.renderer.category.BarRenderer renderer =
                    (org.jfree.chart.renderer.category.BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, cost > budgetLimit ? new Color(255, 82, 82) : new Color(76, 175, 80));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Calculation Error: " + e.getMessage());
        }
    }

    private void setupChart() {
        dataset = new DefaultCategoryDataset();
        JFreeChart chart = ChartFactory.createBarChart("Consumption Trend", "Record", "Cost (₹)", dataset);
        chart.setBackgroundPaint(new Color(30, 30, 30));
        chart.getTitle().setPaint(Color.WHITE);

        org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(45, 45, 48));
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        chartPanel = new ChartPanel(chart);
    }

    private void saveChartImage() {
        try {
            JFileChooser fc = new JFileChooser();
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                ChartUtils.saveChartAsPNG(fc.getSelectedFile(), chartPanel.getChart(), 800, 600);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
