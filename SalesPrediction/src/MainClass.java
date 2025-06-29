import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class MainClass {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtTemp, txtSales;
    private double slope, intercept;
    private boolean isTrained = false;
    private DbHelper dbHelper = new DbHelper();

    public MainClass() {
        frame = new JFrame("Sales Prediction");
        frame.setSize(750, 450);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLayout(null);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(null);
        leftPanel.setBounds(10, 10, 170, 380);
        leftPanel.setBorder(new TitledBorder("Main Menu"));
        frame.add(leftPanel);

        ImageIcon logoIcon = new ImageIcon("assets/logo.png");
        Image logoImg = logoIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(logoImg));
        logoLabel.setBounds(30, 20, 80, 80);
        leftPanel.add(logoLabel);

        JButton btnLoad = new JButton("Load Data");
        btnLoad.setBounds(10, 110, 150, 25);
        leftPanel.add(btnLoad);

        JButton btnDelete = new JButton("Delete All");
        btnDelete.setBounds(10, 145, 150, 25);
        leftPanel.add(btnDelete);

        JButton btnTrain = new JButton("Train Model");
        btnTrain.setBounds(10, 180, 150, 25);
        leftPanel.add(btnTrain);

        JLabel titleLabel = new JLabel("Welcome to Sales Prediction!", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBounds(190, 10, 540, 30);
        frame.add(titleLabel);

        JLabel lblTemp = new JLabel("Temperature:");
        lblTemp.setBounds(200, 50, 100, 25);
        frame.add(lblTemp);

        txtTemp = new JTextField();
        txtTemp.setBounds(280, 50, 150, 25);
        frame.add(txtTemp);

        JLabel lblSales = new JLabel("Sales:");
        lblSales.setBounds(200, 80, 100, 25);
        frame.add(lblSales);

        txtSales = new JTextField();
        txtSales.setBounds(280, 80, 150, 25);
        frame.add(txtSales);

        JButton btnAdd = new JButton("Add Data Point");
        btnAdd.setBounds(450, 50, 150, 25);
        frame.add(btnAdd);

        JButton btnPredict = new JButton("Predict Sale");
        btnPredict.setBounds(450, 80, 150, 25);
        frame.add(btnPredict);

        JLabel dataLabel = new JLabel("All Data Points!");
        dataLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dataLabel.setBounds(200, 110, 200, 25);
        frame.add(dataLabel);

        model = new DefaultTableModel(new Object[]{"No", "Temperature", "Sales"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(190, 140, 530, 220);
        frame.add(scrollPane);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                JOptionPane.showMessageDialog(frame,
                        "Thanks for using Sales Prediction.\nDeveloper: Ayesha\nID: Bc220201582");
                frame.dispose();
            }
        });

        btnAdd.addActionListener(e -> addDataPoint());
        btnLoad.addActionListener(e -> loadData());
        btnDelete.addActionListener(e -> deleteAll());
        btnTrain.addActionListener(e -> trainModel());
        btnPredict.addActionListener(e -> predictSale());

        frame.setVisible(true);
    }

    private void addDataPoint() {
        try {
            double temp = Double.parseDouble(txtTemp.getText());
            double sales = Double.parseDouble(txtSales.getText());
            if (temp < 1 || temp > 50 || sales < 1 || sales > 500) throw new Exception();

            if (!dbHelper.dataPointExists(temp, sales)) {
                dbHelper.addDataPoint(new DataPoint(temp, sales));
                JOptionPane.showMessageDialog(frame, "Data point added successfully.");
                loadData();
            } else {
                JOptionPane.showMessageDialog(frame, "Duplicate entry.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadData() {
        try {
            List<DataPoint> dataList = dbHelper.getAllDataPoints();
            model.setRowCount(0);
            int count = 1;
            for (DataPoint dp : dataList) {
                model.addRow(new Object[]{
                    count++,
                    String.format("%.0f", dp.getTemperature()), // show without decimal
                    dp.getSales() // keep sales with decimal
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAll() {
        int confirm = JOptionPane.showConfirmDialog(frame,
                "Do you want to remove all Data Points from database?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dbHelper.deleteAllDataPoints();
                model.setRowCount(0);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Error deleting data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void trainModel() {
        int n = model.getRowCount();
        if (n < 2) {
            JOptionPane.showMessageDialog(frame, "At least 2 data points are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            double x = Double.parseDouble(model.getValueAt(i, 1).toString());
            double y = Double.parseDouble(model.getValueAt(i, 2).toString());
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        intercept = (sumY - slope * sumX) / n;
        isTrained = true;

        JOptionPane.showMessageDialog(frame, "Model Trained Successfully.");
    }

    private void predictSale() {
        try {
            double temp = Double.parseDouble(txtTemp.getText());
            if (temp < 1 || temp > 50 || !isTrained) throw new Exception();

            double predictedSales = slope * temp + intercept;

            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Predicted sales for " + String.format("%.0f", temp) + "°C is: " + (int) predictedSales +
                            "\nModel Parameters:\nSlope: " + slope + "\nIntercept: " + intercept +
                            "\n\nWould you like to save the predicted value to the database?",
                    "Prediction", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                dbHelper.addDataPoint(new DataPoint(temp, predictedSales));
                loadData();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Invalid input or model not trained.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new MainClass();
    }
}
