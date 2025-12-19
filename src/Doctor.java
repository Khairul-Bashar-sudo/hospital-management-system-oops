public class Doctor extends Person {
    private String doctorId;
    private String specialization;

    public Doctor(String doctorId, String name, int age, String gender, String specialization) {
        super(name, age, gender);
        this.doctorId = doctorId;
        this.specialization = specialization;
    }

    public String getDoctorId() { return doctorId; }

    @Override
    public String toString() {
        return doctorId + "," + name + "," + age + "," + gender + "," + specialization;
    }

    @Override
    public void display() {
        System.out.println("Doctor ID: " + doctorId);
        System.out.println("Name: " + name + " | Age: " + age + " | Gender: " + gender);
        System.out.println("Specialization: " + specialization);
    }

    public static Doctor fromString(String s) {
        try {
            String[] p = s.split(",");
            return new Doctor(p[0], p[1], Integer.parseInt(p[2]), p[3], p[4]);
        } catch (Exception e) {
            return null;
        }
    }
    public String getSpecialization() {
    return specialization;
}

}
