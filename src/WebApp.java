import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class WebApp {

    private final Hospital hospital;
    private final PaymentService paymentService;

    public WebApp(Hospital hospital) {
        this.hospital = hospital;
        this.paymentService = new PaymentService();
    }

    public void start(int port) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handleRoot);
        server.createContext("/book", this::handleBookAppointment);
        server.createContext("/doctors", this::handleDoctors);
        server.createContext("/appointments", this::handleAppointments);
        server.setExecutor(null);
        server.start();
        System.out.println("Web server started on http://localhost:" + port);
    }

    private void handleRoot(HttpExchange exchange) throws IOException {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset=\"UTF-8\">
                <title>City Hospital Portal</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; background: #f5f7fb; color: #1f2937; }
                    .card { background: white; padding: 24px; border-radius: 12px; box-shadow: 0 6px 18px rgba(0,0,0,0.08); margin-bottom: 20px; }
                    input, select, button { padding: 10px; margin-top: 8px; width: 100%; box-sizing: border-box; }
                    button { background: #2563eb; color: white; border: none; border-radius: 8px; cursor: pointer; }
                    .small { font-size: 0.9em; color: #6b7280; }
                    h1, h2 { color: #1d4ed8; }
                </style>
            </head>
            <body>
                <div class=\"card\">
                    <h1>City Hospital Online Booking</h1>
                    <p class=\"small\">Book appointments, pay securely, and track your booking status.</p>
                </div>
                <div class=\"card\">
                    <h2>Book an Appointment</h2>
                    <form method=\"POST\" action=\"/book\">
                        <input name=\"patientId\" placeholder=\"Patient ID\" required>
                        <input name=\"patientName\" placeholder=\"Patient Name\" required>
                        <input name=\"doctorId\" placeholder=\"Doctor ID\" required>
                        <input name=\"date\" type=\"date\" required>
                        <select name=\"paymentMethod\">
                            <option value=\"card\">Card</option>
                            <option value=\"upi\">UPI</option>
                            <option value=\"netbanking\">Net Banking</option>
                            <option value=\"wallet\">Wallet</option>
                            <option value=\"cash\">Cash at Reception</option>
                        </select>
                        <button type=\"submit\">Book and Pay</button>
                    </form>
                </div>
                <div class=\"card\">
                    <h2>Available Doctors</h2>
                    <p class=\"small\">Visit /doctors to see the doctor list.</p>
                </div>
            </body>
            </html>
            """;
        sendHtml(exchange, html);
    }

    private void handleDoctors(HttpExchange exchange) throws IOException {
        StringBuilder body = new StringBuilder();
        body.append("<h1>Doctors</h1>");
        for (Doctor doctor : hospital.getDoctors()) {
            body.append("<p>").append(doctor.getDoctorId()).append(" - ")
                .append(doctor.getSpecialization()).append(" | ")
                .append(doctor.getName()).append("</p>");
        }
        sendHtml(exchange, body.toString());
    }

    private void handleAppointments(HttpExchange exchange) throws IOException {
        StringBuilder body = new StringBuilder();
        body.append("<h1>Appointments</h1>");
        for (Appointment appointment : hospital.getAppointments()) {
            body.append("<p>").append(appointment.getAppointmentId()).append(" | Doctor: ")
                .append(appointment.getDoctorId()).append(" | Patient: ")
                .append(appointment.getPatientId()).append(" | Date: ")
                .append(appointment.getDate()).append("</p>");
        }
        sendHtml(exchange, body.toString());
    }

    private void handleBookAppointment(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendHtml(exchange, "<h2>Use POST to book.</h2>");
            return;
        }

        Map<String, String> form = parseForm(exchange);
        String patientId = form.getOrDefault("patientId", "");
        String patientName = form.getOrDefault("patientName", "");
        String doctorId = form.getOrDefault("doctorId", "");
        String date = form.getOrDefault("date", "");
        String paymentMethod = form.getOrDefault("paymentMethod", "card");

        Patient patient = hospital.findPatient(patientId);
        if (patient == null) {
            patient = new Patient(patientId, patientName, 0, "Unknown", "Consultation");
            hospital.addPatient(patient);
        }

        Doctor doctor = hospital.findDoctor(doctorId);
        if (doctor == null) {
            sendHtml(exchange, "<h2>Doctor not found.</h2>");
            return;
        }

        PaymentService.PaymentResult paymentResult = paymentService.processPayment(paymentMethod, hospital.getDoctorFee(doctorId));
        String appointmentId = "APT-" + (hospital.getAppointments().size() + 1);
        Appointment appointment = new Appointment(appointmentId, doctorId, patientId, date);
        hospital.addAppointment(appointment);

        Bill bill = new Bill("BILL-" + (hospital.getBills().size() + 1), patientId, doctorId, hospital.getDoctorFee(doctorId));
        hospital.addBill(bill);

        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset=\"UTF-8\">
                <title>Booking Confirmation</title>
                <style>body{font-family:Arial,sans-serif;margin:40px;} .card{background:#f8fafc;padding:20px;border-radius:10px;}</style>
            </head>
            <body>
                <div class=\"card\">
                    <h2>Appointment Booked Successfully</h2>
                    <p><strong>Appointment ID:</strong> %s</p>
                    <p><strong>Patient:</strong> %s</p>
                    <p><strong>Doctor:</strong> %s</p>
                    <p><strong>Date:</strong> %s</p>
                    <p><strong>Payment Status:</strong> %s</p>
                    <p><strong>Transaction ID:</strong> %s</p>
                    <p><strong>Message:</strong> %s</p>
                    <p><a href=\"/\">Back to home</a></p>
                </div>
            </body>
            </html>
            """.formatted(appointmentId, patientName, doctor.getName(), date, paymentResult.getStatus(), paymentResult.getTransactionId(), paymentResult.getMessage());
        sendHtml(exchange, html);
    }

    private Map<String, String> parseForm(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String body = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
            .lines().collect(Collectors.joining("\n"));
        Map<String, String> values = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                values.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8), URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
            }
        }
        return values;
    }

    private void sendHtml(HttpExchange exchange, String html) throws IOException {
        byte[] response = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}
