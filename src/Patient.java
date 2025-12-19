public class Patient extends Person {
    private String patientId;
    private String disease;

    public Patient(String patientId, String name, int age, String gender, String disease) {
        super(name, age, gender);
        this.patientId = patientId;
        this.disease = disease;
    }

    public String getPatientId() { return patientId; }

    @Override
    public String toString() {
        return patientId + "," + name + "," + age + "," + gender + "," + disease;
    }

    public static Patient fromString(String s) {
        try {
            String[] p = s.split(",");
            return new Patient(p[0], p[1], Integer.parseInt(p[2]), p[3], p[4]);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void display() {
        System.out.println("Patient ID: " + patientId);
        System.out.println("Name: " + name + " | Age: " + age + " | Gender: " + gender);
        System.out.println("Disease: " + disease);
    }
    public String getDisease() {
    return disease;
}

}
