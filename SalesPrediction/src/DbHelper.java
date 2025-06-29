
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbHelper {
    private static final String DB_URL = "jdbc:ucanaccess://assets/Bc220201582.accdb";
    private Connection conn;

    public DbHelper() {
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addDataPoint(DataPoint dp) throws SQLException {
        String sql = "INSERT INTO SalesData (Temperature, Sales) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, dp.getTemperature());
            pstmt.setDouble(2, dp.getSales());
            pstmt.executeUpdate();
        }
    }

    public boolean dataPointExists(double temperature, double sales) throws SQLException {
        String sql = "SELECT COUNT(*) FROM SalesData WHERE Temperature = ? AND Sales = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, temperature);
            pstmt.setDouble(2, sales);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public List<DataPoint> getAllDataPoints() throws SQLException {
        List<DataPoint> dataPoints = new ArrayList<>();
        String sql = "SELECT * FROM SalesData ORDER BY Temperature ASC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                DataPoint dp = new DataPoint(
                    rs.getDouble("Temperature"),
                    rs.getDouble("Sales")
                );
                dataPoints.add(dp);
            }
        }
        return dataPoints;
    }

    public void deleteAllDataPoints() throws SQLException {
        String sql = "DELETE FROM SalesData";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    public void close() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}