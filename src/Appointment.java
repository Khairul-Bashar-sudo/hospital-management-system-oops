public class Appointment {
    private String appointmentId;
    private String doctorId;
    private String patientId;
    private String date;

    public Appointment(String appointmentId, String doctorId, String patientId, String date) {
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.date = date;
    }

    @Override
    public String toString() {
        return appointmentId + "," + doctorId + "," + patientId + "," + date;
    }

    public static Appointment fromString(String s) {
        String[] p = s.split(",");
        return new Appointment(p[0], p[1], p[2], p[3]);
    }
    public String getDoctorId() {
    return doctorId;
}

public String getPatientId() {
    return patientId;
}
public String getAppointmentId() { return appointmentId; }

public String getDate() { return date; }


}
