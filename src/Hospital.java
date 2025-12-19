import java.io.*;
import java.util.*;

public class Hospital {

    private ArrayList<Doctor> doctors = new ArrayList<>();
    private ArrayList<Patient> patients = new ArrayList<>();
    private ArrayList<Appointment> appointments = new ArrayList<>();
    private ArrayList<Bill> bills = new ArrayList<>();

    // FILE NAMES
    private final String DF = "doctors.txt";
    private final String PF = "patients.txt";
    private final String AF = "appointments.txt";
    private final String BF = "bills.txt";

    // ADD METHODS
    public void addDoctor(Doctor d) { doctors.add(d); saveDoctors(); }
    public void addPatient(Patient p) { patients.add(p); savePatients(); }
    public void addAppointment(Appointment a) { appointments.add(a); saveAppointments(); }
    public void addBill(Bill b) { bills.add(b); saveBills(); }

    // GET METHODS
    public ArrayList<Doctor> getDoctors() { return doctors; }
    public ArrayList<Patient> getPatients() { return patients; }
    public ArrayList<Appointment> getAppointments() { return appointments; }
    public ArrayList<Bill> getBills() { return bills; }

    // SEARCH
    public Doctor findDoctor(String id) {
        return doctors.stream().filter(d -> d.getDoctorId().equals(id)).findFirst().orElse(null);
    }

    public Patient findPatient(String id) {
        return patients.stream().filter(p -> p.getPatientId().equals(id)).findFirst().orElse(null);
    }

    // DELETE
    public boolean removeDoctor(String id) {
        boolean removed = doctors.removeIf(d -> d.getDoctorId().equals(id));
        saveDoctors();
        return removed;
    }

    public boolean removePatient(String id) {
        boolean removed = patients.removeIf(p -> p.getPatientId().equals(id));
        savePatients();
        return removed;
    }

    // BILL CALCULATION
    public double getDoctorFee(String docId) {
        return 500; // fixed fee
    }

    // SAVE ALL
    public void saveAll() {
        saveDoctors();
        savePatients();
        saveAppointments();
        saveBills();
    }

    // LOAD ALL
    public void loadAll() {
        loadDoctors();
        loadPatients();
        loadAppointments();
        loadBills();
    }

    // SAVE METHODS
    private void saveDoctors() { saveList(DF, doctors); }
    private void savePatients() { saveList(PF, patients); }
    private void saveAppointments() { saveList(AF, appointments); }
    private void saveBills() { saveList(BF, bills); }

    // LOAD METHODS
    private void loadDoctors() { doctors = loadList(DF, Doctor::fromString); }
    private void loadPatients() { patients = loadList(PF, Patient::fromString); }
    private void loadAppointments() { appointments = loadList(AF, Appointment::fromString); }
    private void loadBills() { bills = loadList(BF, Bill::fromString); }

    // GENERIC SAVE
    private <T> void saveList(String filename, ArrayList<T> list) {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            for (T obj : list) out.println(obj);
        } catch (Exception ignored) {}
    }

    // GENERIC LOAD
    private <T> ArrayList<T> loadList(String filename, Converter<T> converter) {
        ArrayList<T> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                T obj = converter.convert(line);
                if (obj != null) list.add(obj);
            }
        } catch (Exception ignored) {}
        return list;
    }

    interface Converter<T> {
        T convert(String line);
    }
}
