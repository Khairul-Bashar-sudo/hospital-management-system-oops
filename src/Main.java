import java.util.*;

public class Main {

    private static Hospital hospital = new Hospital();
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        hospital.loadAll();
        showWelcome();

        if (!loginFlow()) {
            ConsoleColors.printlnError("Login failed. Exiting.");
            return;
        }

        mainMenu();
    }

    private static void showWelcome() {
        ConsoleColors.printlnTitle("=====================================");
        ConsoleColors.printlnTitle(" WELCOME TO CITY HOSPITAL HMS - CLI");
        ConsoleColors.printlnTitle("=====================================");
    }


private static String userRole = "";

private static boolean loginFlow() {
    System.out.print("Username: ");
    String u = sc.nextLine();

    System.out.print("Password: ");
    String p = sc.nextLine();

    String role = Auth.login(u, p);

    if (role != null) {
        userRole = role;
        ConsoleColors.printlnSuccess("Login successful! Role: " + role + "\n");
        return true;
    }

    ConsoleColors.printlnError("Invalid credentials.\n");
    return false;
}


   private static void mainMenu() {

    switch (userRole) {
        case "ADMIN" -> adminMenu();
        case "DOCTOR" -> doctorMenu();
        case "RECEPTION" -> receptionistMenu();
    }
}
private static void adminMenu() {
    int ch;
    do {
        ConsoleColors.printlnBold("\n===== ADMIN MENU =====");
        System.out.println("1. Add Doctor");
        System.out.println("2. Add Patient");
        System.out.println("3. Schedule Appointment");
        System.out.println("4. View Doctors");
        System.out.println("5. View Patients");
        System.out.println("6. View Appointments");
        System.out.println("7. Search Doctor/Patient");
        System.out.println("8. Delete Doctor/Patient");
        System.out.println("9. Generate Bill");
        System.out.println("10. View Bills");
        System.out.println("0. Save & Logout");
        System.out.print("Choice: ");

        ch = Integer.parseInt(sc.nextLine());

        switch (ch) {
            case 1 -> addDoctor();
            case 2 -> addPatient();
            case 3 -> scheduleAppointment();
            case 4 -> viewDoctors();
            case 5 -> viewPatients();
            case 6 -> viewAppointments();
            case 7 -> searchMenu();
            case 8 -> deleteMenu();
            case 9 -> generateBill();
            case 10 -> viewBills();
            case 0 -> {
                hospital.saveAll();
                ConsoleColors.printlnSuccess("Logged out!");
            }
            default -> ConsoleColors.printlnError("Invalid choice!");
        }

    } while (ch != 0);
}
private static void doctorMenu() {
    int ch;
    do {
        ConsoleColors.printlnBold("\n===== DOCTOR MENU =====");
        System.out.println("1. View My Appointments");
        System.out.println("2. View My Patients");
        System.out.println("0. Logout");
        System.out.print("Choice: ");

        ch = Integer.parseInt(sc.nextLine());

        switch (ch) {
            case 1 -> viewAppointmentsForDoctor();
            case 2 -> viewPatientsForDoctor();
            case 0 -> ConsoleColors.printlnSuccess("Logged out!");
            default -> ConsoleColors.printlnError("Invalid choice!");
        }

    } while (ch != 0);
}
private static void viewAppointmentsForDoctor() {
    System.out.print("Enter your Doctor ID: ");
    String did = sc.nextLine();

    ConsoleColors.printlnBold("\n--- Your Appointments ---");

    printLine(4);
    printRow("Appointment ID", "Doctor ID", "Patient ID", "Date");
    printLine(4);

    for (Appointment a : hospital.getAppointments()) {
        if (a.getDoctorId().equals(did)) {
            printRow(a.getAppointmentId(), a.getDoctorId(), a.getPatientId(), a.getDate());
        }
    }

    printLine(4);
}


private static void viewPatientsForDoctor() {
    System.out.print("Enter your Doctor ID: ");
    String did = sc.nextLine();

    ConsoleColors.printlnBold("\n--- Your Patients ---");

    printLine(5);
    printRow("Patient ID", "Name", "Age", "Gender", "Disease");
    printLine(5);

    for (Appointment a : hospital.getAppointments()) {
        if (a.getDoctorId().equals(did)) {
            Patient p = hospital.findPatient(a.getPatientId());
            if (p != null) {
                printRow(
                    p.getPatientId(),
                    p.getName(),
                    p.getAge() + "",
                    p.getGender(),
                    p.getDisease()
                );
            }
        }
    }

    printLine(5);
}

private static void receptionistMenu() {
    int ch;
    do {
        ConsoleColors.printlnBold("\n===== RECEPTIONIST MENU =====");
        System.out.println("1. Add Patient");
        System.out.println("2. Schedule Appointment");
        System.out.println("3. View Doctors");
        System.out.println("4. View Patients");
        System.out.println("5. View Appointments");
        System.out.println("6. Generate Bill");
        System.out.println("0. Logout");
        System.out.print("Choice: ");

        ch = Integer.parseInt(sc.nextLine());

        switch (ch) {
            case 1 -> addPatient();
            case 2 -> scheduleAppointment();
            case 3 -> viewDoctors();
            case 4 -> viewPatients();
            case 5 -> viewAppointments();
            case 6 -> generateBill();
            case 0 -> ConsoleColors.printlnSuccess("Logged out!");
            default -> ConsoleColors.printlnError("Invalid choice!");
        }

    } while (ch != 0);
}


    // ==================================================================
    //      FULL IMPLEMENTATION OF ALL FEATURES
    // ==================================================================

    private static void addDoctor() {
        System.out.println("\n--- Add Doctor ---");

        System.out.print("Enter Doctor ID: ");
        String id = sc.nextLine();

        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Age: ");
        int age = Integer.parseInt(sc.nextLine());

        System.out.print("Enter Gender: ");
        String gender = sc.nextLine();

        System.out.print("Enter Specialization: ");
        String spec = sc.nextLine();

        Doctor d = new Doctor(id, name, age, gender, spec);
        hospital.addDoctor(d);

        ConsoleColors.printlnSuccess("Doctor added successfully!");
    }

    private static void addPatient() {
        System.out.println("\n--- Add Patient ---");

        System.out.print("Enter Patient ID: ");
        String id = sc.nextLine();

        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Age: ");
        int age = Integer.parseInt(sc.nextLine());

        System.out.print("Enter Gender: ");
        String gender = sc.nextLine();

        System.out.print("Enter Disease: ");
        String dis = sc.nextLine();

        Patient p = new Patient(id, name, age, gender, dis);
        hospital.addPatient(p);

        ConsoleColors.printlnSuccess("Patient added successfully!");
    }

    private static void scheduleAppointment() {
        System.out.println("\n--- Schedule Appointment ---");

        System.out.print("Enter Appointment ID: ");
        String id = sc.nextLine();

        System.out.print("Enter Doctor ID: ");
        String did = sc.nextLine();

        System.out.print("Enter Patient ID: ");
        String pid = sc.nextLine();

        System.out.print("Enter Date (YYYY-MM-DD): ");
        String date = sc.nextLine();

        Appointment ap = new Appointment(id, did, pid, date);
        hospital.addAppointment(ap);

        ConsoleColors.printlnSuccess("Appointment scheduled successfully!");
    }

    private static void viewDoctors() {
    ConsoleColors.printlnBold("\n--- Doctors List ---");

    printLine(5);
    printRow("Doctor ID", "Name", "Age", "Gender", "Specialization");
    printLine(5);

    for (Doctor d : hospital.getDoctors()) {
        printRow(d.getDoctorId(), d.getName(), d.getAge() + "", d.getGender(), d.getSpecialization());
    }

    printLine(5);
}


    private static void viewPatients() {
    ConsoleColors.printlnBold("\n--- Patients List ---");

    printLine(5);
    printRow("Patient ID", "Name", "Age", "Gender", "Disease");
    printLine(5);

    for (Patient p : hospital.getPatients()) {
        printRow(p.getPatientId(), p.getName(), p.getAge() + "", p.getGender(), p.getDisease());
    }

    printLine(5);
}


    private static void viewAppointments() {
    ConsoleColors.printlnBold("\n--- Appointments ---");

    printLine(4);
    printRow("Appointment ID", "Doctor ID", "Patient ID", "Date");
    printLine(4);

    for (Appointment a : hospital.getAppointments()) {
        printRow(a.getAppointmentId(), a.getDoctorId(), a.getPatientId(), a.getDate());
    }

    printLine(4);
}


    private static void searchMenu() {
    System.out.println("\n1. Search Doctor\n2. Search Patient");
    System.out.print("Choice: ");

    int c = Integer.parseInt(sc.nextLine());

    System.out.print("Enter ID: ");
    String id = sc.nextLine();

    if (c == 1) {
        Doctor d = hospital.findDoctor(id);
        if (d == null) {
            ConsoleColors.printlnError("Doctor not found.");
            return;
        }

        printLine(5);
        printRow("Doctor ID", "Name", "Age", "Gender", "Specialization");
        printLine(5);

        printRow(d.getDoctorId(), d.getName(), d.getAge() + "",
                d.getGender(), d.getSpecialization());

        printLine(5);

    } else {
        Patient p = hospital.findPatient(id);
        if (p == null) {
            ConsoleColors.printlnError("Patient not found.");
            return;
        }

        printLine(5);
        printRow("Patient ID", "Name", "Age", "Gender", "Disease");
        printLine(5);

        printRow(p.getPatientId(), p.getName(), p.getAge() + "",
                p.getGender(), p.getDisease());

        printLine(5);
    }
}


    private static void deleteMenu() {
        System.out.println("\n1. Delete Doctor\n2. Delete Patient");
        System.out.print("Choice: ");

        int c = Integer.parseInt(sc.nextLine());

        System.out.print("Enter ID: ");
        String id = sc.nextLine();

        boolean ok = (c == 1)
                ? hospital.removeDoctor(id)
                : hospital.removePatient(id);

        ConsoleColors.printlnSuccess(ok ? "Deleted successfully." : "ID not found!");
    }

    private static void generateBill() {
    System.out.println("\n--- Generate Bill ---");

    System.out.print("Enter Bill ID: ");
    String bid = sc.nextLine();

    System.out.print("Enter Patient ID: ");
    String pid = sc.nextLine();

    Patient p = hospital.findPatient(pid);
    if (p == null) {
        ConsoleColors.printlnError("Patient not found!");
        return;
    }

    System.out.print("Enter Doctor ID: ");
    String did = sc.nextLine();

    Doctor d = hospital.findDoctor(did);
    if (d == null) {
        ConsoleColors.printlnError("Doctor not found!");
        return;
    }

    double fee = hospital.getDoctorFee(did);  // e.g., 500  
    double serviceCharge = 100;
    double total = fee + serviceCharge;

    Bill b = new Bill(bid, pid, did, total);
    hospital.addBill(b);

    ConsoleColors.printlnSuccess("\nBill Generated Successfully!");
    printLine(4);
    printRow("Bill ID", "Patient", "Doctor", "Amount");
    printLine(4);
    printRow(bid, p.getName(), d.getName(), "Rs. " + total);
    printLine(4);
}


    private static void viewBills() {
    ConsoleColors.printlnBold("\n--- Bills List ---");

    printLine(4);
    printRow("Bill ID", "Patient ID", "Doctor ID", "Amount");
    printLine(4);

    for (Bill b : hospital.getBills()) {
        printRow(b.getBillId(), b.getPatientId(), b.getDoctorId(), "Rs. " + b.getAmount());
    }

    printLine(4);
}

    private static final int COL_WIDTH = 25;

private static void printRow(String... cols) {
    for (String col : cols) {
        System.out.printf("%-" + COL_WIDTH + "s", col);
    }
    System.out.println();
}

private static void printLine(int columns) {
    for (int i = 0; i < columns * COL_WIDTH; i++) {
        System.out.print("-");
    }
    System.out.println();
}

}

