public class Bill {
    private String billId;
    private String patientId;
    private String doctorId;
    private double amount;

    public Bill(String billId, String patientId, String doctorId, double amount) {
        this.billId = billId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.amount = amount;
    }

    // ----------- GETTERS (Required by your updated view functions) --------------
    public String getBillId() { return billId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public double getAmount() { return amount; }

    @Override
    public String toString() {
        return billId + "," + patientId + "," + doctorId + "," + amount;
    }

    public static Bill fromString(String s) {
        String[] p = s.split(",");
        return new Bill(p[0], p[1], p[2], Double.parseDouble(p[3]));
    }
}
