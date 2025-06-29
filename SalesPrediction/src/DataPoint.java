public class DataPoint {
    private double temperature;
    private double sales;

    public DataPoint(double temperature, double sales) {
        this.temperature = temperature;
        this.sales = sales;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getSales() {
        return sales;
    }
}