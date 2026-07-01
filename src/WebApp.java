import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class WebApp {

    private final Hospital hospital;
    private final PaymentService paymentService;
    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes

    private static class SessionData {
        String username;
        String role;
        long createdAt;
        SessionData(String username, String role) {
            this.username = username;
            this.role = role;
            this.createdAt = System.currentTimeMillis();
        }
        boolean isValid() {
            return System.currentTimeMillis() - createdAt < SESSION_TIMEOUT;
        }
    }

    public WebApp(Hospital hospital) {
        this.hospital = hospital;
        this.paymentService = new PaymentService();
    }

    public void start(int port) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handleRoot);
        server.createContext("/login", this::handleLogin);
        server.createContext("/logout", this::handleLogout);
        server.createContext("/doctors", this::handleDoctors);
        server.createContext("/receptionist", this::handleReceptionist);
        server.createContext("/book", this::handleBookAppointment);
        server.createContext("/appointments", this::handleAppointments);
        server.createContext("/payment", this::handlePayment);
        server.createContext("/confirmation", this::handleConfirmation);
        server.setExecutor(null);
        server.start();
        System.out.println("Web server started on http://localhost:" + port);
    }

    private String getSessionId(HttpExchange exchange) {
        List<String> cookies = exchange.getRequestHeaders().get("Cookie");
        if (cookies != null) {
            for (String cookie : cookies) {
                if (cookie.contains("sessionId=")) {
                    return cookie.split("sessionId=")[1].split(";")[0];
                }
            }
        }
        return null;
    }

    private SessionData getSession(HttpExchange exchange) {
        String sessionId = getSessionId(exchange);
        if (sessionId != null) {
            SessionData session = sessions.get(sessionId);
            if (session != null && session.isValid()) {
                return session;
            }
        }
        return null;
    }

    private void setSessionCookie(HttpExchange exchange, String sessionId) {
        exchange.getResponseHeaders().add("Set-Cookie", "sessionId=" + sessionId + "; Path=/; HttpOnly");
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    private void handleRoot(HttpExchange exchange) throws IOException {
        SessionData session = getSession(exchange);
        
        if (session == null) {
            // Not logged in - show login page
            handleLogin(exchange);
            return;
        }
        
        // Logged in - show dashboard
        String roleIcon = "ADMIN".equals(session.role) ? "👨‍💼" : "DOCTOR".equals(session.role) ? "👨‍⚕️" : "👩‍💼";
        String roleColor = "ADMIN".equals(session.role) ? "#d32f2f" : "DOCTOR".equals(session.role) ? "#0066cc" : "#2d7a4a";
        
        String html = "<html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
            + "<title>Dashboard - City Hospital Medical Center</title><style>"
            + "* { margin: 0; padding: 0; box-sizing: border-box; }"
            + "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background: #f5f7fa; color: #333; }"
            + ".navbar { background: white; border-bottom: 1px solid #e0e0e0; padding: 0; box-shadow: 0 2px 8px rgba(0,0,0,0.06); position: sticky; top: 0; z-index: 100; }"
            + ".navbar-content { max-width: 1400px; margin: 0 auto; padding: 16px 20px; display: flex; justify-content: space-between; align-items: center; }"
            + ".navbar-brand { display: flex; align-items: center; gap: 12px; }"
            + ".navbar-logo { font-size: 32px; }"
            + ".navbar-title { color: #0066cc; font-size: 18px; font-weight: 700; }"
            + ".navbar-right { display: flex; align-items: center; gap: 30px; }"
            + ".user-section { display: flex; align-items: center; gap: 15px; }"
            + ".user-badge { background: #f0f7ff; color: #0066cc; padding: 8px 16px; border-radius: 20px; font-size: 13px; font-weight: 600; display: flex; align-items: center; gap: 6px; }"
            + ".logout-btn { padding: 8px 20px; background: #f44336; color: white; text-decoration: none; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; border: none; transition: all 0.3s; }"
            + ".logout-btn:hover { background: #d32f2f; box-shadow: 0 4px 12px rgba(244, 67, 54, 0.2); }"
            + ".main-container { max-width: 1400px; margin: 0 auto; padding: 40px 20px; }"
            + ".welcome-section { background: white; padding: 40px; border-radius: 12px; margin-bottom: 40px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); border-left: 5px solid #0066cc; }"
            + ".welcome-title { font-size: 28px; font-weight: 700; color: #1a1a1a; margin-bottom: 8px; }"
            + ".welcome-subtitle { color: #666; font-size: 15px; }"
            + ".cards-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 24px; }"
            + ".card { background: white; border-radius: 12px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); transition: all 0.3s ease; text-decoration: none; color: inherit; border-top: 4px solid #0066cc; }"
            + ".card:hover { transform: translateY(-8px); box-shadow: 0 12px 24px rgba(0,0,0,0.12); }"
            + ".card-icon { font-size: 40px; margin-bottom: 15px; }"
            + ".card-title { font-size: 18px; font-weight: 700; color: #1a1a1a; margin-bottom: 8px; }"
            + ".card-description { color: #666; font-size: 14px; line-height: 1.5; }"
            + ".card-arrow { color: #0066cc; font-weight: 700; margin-top: 15px; }"
            + ".card.doctors { border-top-color: #0066cc; }"
            + ".card.receptionist { border-top-color: #2d7a4a; }"
            + ".card.appointments { border-top-color: #f57c00; }"
            + ".footer { text-align: center; color: #999; font-size: 13px; margin-top: 60px; padding-top: 30px; border-top: 1px solid #e0e0e0; }"
            + ".feature-badge { display: inline-block; background: #e8f5e9; color: #2d7a4a; padding: 6px 14px; border-radius: 20px; font-size: 12px; font-weight: 600; margin-right: 10px; }"
            + "</style></head><body>"
            + "<div class=\"navbar\">"
            + "<div class=\"navbar-content\">"
            + "<div class=\"navbar-brand\">"
            + "<div class=\"navbar-logo\">🏥</div>"
            + "<div class=\"navbar-title\">City Hospital</div>"
            + "</div>"
            + "<div class=\"navbar-right\">"
            + "<div class=\"user-section\">"
            + "<div class=\"user-badge\">" + roleIcon + " " + session.username + " <span style=\"color: #999;font-weight:normal;\">(" + session.role + ")</span></div>"
            + "<a href=\"/logout\" class=\"logout-btn\">Logout</a>"
            + "</div>"
            + "</div>"
            + "</div>"
            + "<div class=\"main-container\">"
            + "<div class=\"welcome-section\">"
            + "<div class=\"welcome-title\">Welcome back, " + session.username + "! 👋</div>"
            + "<div class=\"welcome-subtitle\">You're logged in as <strong>" + session.role + "</strong>. Access medical records, schedule appointments, and manage patient care.</div>"
            + "</div>"
            + "<div class=\"cards-grid\">"
            + "<a href=\"/doctors\" class=\"card doctors\">"
            + "<div class=\"card-icon\">👨‍⚕️</div>"
            + "<div class=\"card-title\">Doctor Directory</div>"
            + "<div class=\"card-description\">Browse all available doctors, their specializations, and experience. Find the right specialist for your needs.</div>"
            + "<div class=\"card-arrow\">→ View Doctors</div>"
            + "</a>";
            
        if ("RECEPTION".equals(session.role)) {
            html += "<a href=\"/receptionist\" class=\"card receptionist\">"
                + "<div class=\"card-icon\">📋</div>"
                + "<div class=\"card-title\">Book Appointment</div>"
                + "<div class=\"card-description\">Schedule patient appointments with doctors. Manage payment methods and booking preferences easily.</div>"
                + "<div class=\"card-arrow\">→ Book Now</div>"
                + "</a>";
        }
        
        html += "<a href=\"/appointments\" class=\"card appointments\">"
            + "<div class=\"card-icon\">📅</div>"
            + "<div class=\"card-title\">View Appointments</div>"
            + "<div class=\"card-description\">Check all scheduled appointments and patient records. Keep track of medical history and follow-ups.</div>"
            + "<div class=\"card-arrow\">→ View Schedule</div>"
            + "</a>"
            + "</div>"
            + "<div class=\"footer\">"
            + "© 2024 City Hospital Medical Center | <span class=\"feature-badge\">HIPAA Compliant</span> <span class=\"feature-badge\">Secure</span><br>"
            + "24/7 Emergency Support: +1-800-HOSPITAL"
            + "</div>"
            + "</div>"
            + "</body></html>";
        sendHtml(exchange, html);
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            Map<String, String> form = parseForm(exchange);
            String username = form.getOrDefault("username", "");
            String password = form.getOrDefault("password", "");
            
            String role = Auth.login(username, password);
            
            if (role != null) {
                String sessionId = generateSessionId();
                sessions.put(sessionId, new SessionData(username, role));
                setSessionCookie(exchange, sessionId);
                
                // Redirect to home
                exchange.getResponseHeaders().add("Location", "/");
                exchange.sendResponseHeaders(302, 0);
                exchange.close();
                return;
            }
        }
        
        String html = "<html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
            + "<title>Login - City Hospital Medical Center</title><style>"
            + "* { margin: 0; padding: 0; box-sizing: border-box; }"
            + "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 20px; }"
            + ".login-wrapper { width: 100%; max-width: 420px; }"
            + ".hospital-header { text-align: center; margin-bottom: 40px; }"
            + ".hospital-logo { font-size: 48px; margin-bottom: 15px; }"
            + ".hospital-name { color: #0066cc; font-size: 28px; font-weight: 700; margin-bottom: 5px; letter-spacing: -0.5px; }"
            + ".hospital-tagline { color: #666; font-size: 14px; font-weight: 500; }"
            + ".login-container { background: white; padding: 45px; border-radius: 12px; box-shadow: 0 15px 50px rgba(0, 0, 0, 0.1); border-top: 4px solid #0066cc; }"
            + ".login-title { color: #1a1a1a; font-size: 22px; font-weight: 700; margin-bottom: 8px; }"
            + ".login-subtitle { color: #888; font-size: 14px; margin-bottom: 30px; }"
            + ".form-group { margin-bottom: 20px; }"
            + "label { display: block; color: #333; font-size: 14px; font-weight: 600; margin-bottom: 8px; }"
            + "input { width: 100%; padding: 12px 14px; border: 1.5px solid #ddd; border-radius: 8px; font-size: 14px; transition: all 0.3s ease; font-family: inherit; }"
            + "input:focus { outline: none; border-color: #0066cc; box-shadow: 0 0 0 3px rgba(0, 102, 204, 0.1); }"
            + "input::placeholder { color: #999; }"
            + ".login-btn { width: 100%; padding: 12px; background: linear-gradient(135deg, #0066cc 0%, #004ba3 100%); color: white; border: none; border-radius: 8px; font-size: 15px; font-weight: 600; cursor: pointer; transition: all 0.3s ease; margin-top: 5px; }"
            + ".login-btn:hover { box-shadow: 0 8px 20px rgba(0, 102, 204, 0.3); transform: translateY(-2px); }"
            + ".login-btn:active { transform: translateY(0); }"
            + ".error-alert { background: #fee; border: 1px solid #fcc; color: #c33; padding: 12px; border-radius: 8px; margin-bottom: 20px; font-size: 14px; display: none; }"
            + ".credentials-box { background: #f0f7ff; border-left: 4px solid #0066cc; padding: 20px; border-radius: 8px; margin-top: 30px; }"
            + ".credentials-title { color: #0066cc; font-size: 13px; font-weight: 700; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.5px; }"
            + ".credential-item { color: #333; font-size: 13px; margin-bottom: 8px; font-family: 'Courier New', monospace; }"
            + ".credential-item strong { color: #0066cc; font-weight: 700; }"
            + ".credential-item:last-child { margin-bottom: 0; }"
            + ".footer-text { text-align: center; color: #999; font-size: 12px; margin-top: 25px; }"
            + ".badge { display: inline-block; background: #e8f5e9; color: #2d7a4a; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; margin-right: 8px; }"
            + "</style></head><body>"
            + "<div class=\"login-wrapper\">"
            + "<div class=\"hospital-header\">"
            + "<div class=\"hospital-logo\">🏥</div>"
            + "<div class=\"hospital-name\">City Hospital</div>"
            + "<div class=\"hospital-tagline\">Medical Center - Patient Care Management System</div>"
            + "</div>"
            + "<div class=\"login-container\">";
            
        if ("POST".equals(exchange.getRequestMethod())) {
            html += "<div class=\"error-alert\" style=\"display: block;\">⚠ Invalid username or password</div>";
        }
        
        html += "<h2 class=\"login-title\">Welcome Back</h2>"
            + "<p class=\"login-subtitle\">Sign in to your account to continue</p>"
            + "<form method=\"POST\" action=\"/login\">"
            + "<div class=\"form-group\">"
            + "<label for=\"username\">Username</label>"
            + "<input type=\"text\" id=\"username\" name=\"username\" placeholder=\"Enter your username\" required autofocus>"
            + "</div>"
            + "<div class=\"form-group\">"
            + "<label for=\"password\">Password</label>"
            + "<input type=\"password\" id=\"password\" name=\"password\" placeholder=\"Enter your password\" required>"
            + "</div>"
            + "<button type=\"submit\" class=\"login-btn\">Sign In</button>"
            + "</form>"
            + "<div class=\"credentials-box\">"
            + "<div class=\"credentials-title\">📋 Demo Credentials (Test Mode)</div>"
            + "<div class=\"credential-item\"><span class=\"badge\">ADMIN</span><strong>admin</strong> / 1234</div>"
            + "<div class=\"credential-item\"><span class=\"badge\">DOCTOR</span><strong>doctor1</strong> / 1111</div>"
            + "<div class=\"credential-item\"><span class=\"badge\">RECEPTION</span><strong>reception</strong> / 2222</div>"
            + "</div>"
            + "</div>"
            + "<div class=\"footer-text\">© 2024 City Hospital Medical Center. All rights reserved.</div>"
            + "</div>"
            + "</body></html>";
        sendHtml(exchange, html);
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        String sessionId = getSessionId(exchange);
        if (sessionId != null) {
            sessions.remove(sessionId);
        }
        exchange.getResponseHeaders().add("Set-Cookie", "sessionId=; Path=/; Max-Age=0");
        exchange.getResponseHeaders().add("Location", "/login");
        exchange.sendResponseHeaders(302, 0);
        exchange.close();
    }

    private void handleDoctors(HttpExchange exchange) throws IOException {
        StringBuilder doctorsList = new StringBuilder();
        
        if (hospital.getDoctors().isEmpty()) {
            doctorsList.append("<div style=\"text-align: center; padding: 60px 20px; color: #999;\">"
                + "<div style=\"font-size: 48px; margin-bottom: 15px;\">📭</div>"
                + "<div style=\"font-size: 18px; font-weight: 600; margin-bottom: 8px;\">No Doctors Available</div>"
                + "<div style=\"font-size: 14px;\">Please try again later.</div>"
                + "</div>");
        } else {
            for (Doctor doctor : hospital.getDoctors()) {
                String spec = doctor.getSpecialization();
                String specColor = getSpecialtyColor(spec);
                
                doctorsList.append("<div class=\"doctor-card\">"
                    + "<div class=\"doctor-avatar\" style=\"background: " + specColor + "20; color: " + specColor + ";\">"
                    + getSpecialtyIcon(spec)
                    + "</div>"
                    + "<div class=\"doctor-info\">"
                    + "<div class=\"doctor-name\">").append(doctor.getName()).append("</div>"
                    + "<div class=\"doctor-specialty\" style=\"color: " + specColor + ";\">"
                    + spec
                    + "</div>"
                    + "<div class=\"doctor-meta\">"
                    + "<div class=\"meta-item\"><span class=\"meta-label\">ID:</span> ").append(doctor.getDoctorId()).append("</div>"
                    + "<div class=\"meta-item\"><span class=\"meta-label\">Age:</span> ").append(doctor.getAge()).append("</div>"
                    + "<div class=\"meta-item\"><span class=\"meta-label\">Gender:</span> ").append(doctor.getGender()).append("</div>"
                    + "</div>"
                    + "<div class=\"availability-badge\">✓ Available Today</div>"
                    + "</div>"
                    + "</div>");
            }
        }
        
        String html = "<html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
            + "<title>Doctors Directory - City Hospital Medical Center</title><style>"
            + "* { margin: 0; padding: 0; box-sizing: border-box; }"
            + "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background: #f5f7fa; color: #333; }"
            + ".navbar { background: white; border-bottom: 1px solid #e0e0e0; padding: 16px 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }"
            + ".navbar-content { max-width: 1400px; margin: 0 auto; display: flex; justify-content: space-between; align-items: center; }"
            + ".navbar-brand { display: flex; align-items: center; gap: 12px; }"
            + ".navbar-logo { font-size: 28px; }"
            + ".navbar-title { color: #0066cc; font-size: 16px; font-weight: 700; }"
            + ".back-link { color: #0066cc; text-decoration: none; font-size: 14px; font-weight: 600; }"
            + ".back-link:hover { text-decoration: underline; }"
            + ".main-container { max-width: 1400px; margin: 0 auto; padding: 40px 20px; }"
            + ".page-header { background: white; padding: 40px; border-radius: 12px; margin-bottom: 40px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); border-bottom: 4px solid #0066cc; }"
            + ".page-title { font-size: 32px; font-weight: 700; color: #1a1a1a; margin-bottom: 8px; }"
            + ".page-subtitle { color: #666; font-size: 15px; }"
            + ".doctors-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 24px; }"
            + ".doctor-card { background: white; border-radius: 12px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); transition: all 0.3s ease; border: 1px solid #e8e8e8; }"
            + ".doctor-card:hover { transform: translateY(-8px); box-shadow: 0 12px 24px rgba(0,0,0,0.12); }"
            + ".doctor-avatar { width: 60px; height: 60px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 32px; margin-bottom: 16px; }"
            + ".doctor-name { font-size: 18px; font-weight: 700; color: #1a1a1a; margin-bottom: 6px; }"
            + ".doctor-specialty { font-size: 14px; font-weight: 600; margin-bottom: 16px; }"
            + ".doctor-info { flex: 1; }"
            + ".doctor-meta { background: #f5f7fa; padding: 12px; border-radius: 8px; margin-bottom: 16px; }"
            + ".meta-item { font-size: 13px; color: #555; margin-bottom: 6px; }"
            + ".meta-item:last-child { margin-bottom: 0; }"
            + ".meta-label { color: #999; font-weight: 600; margin-right: 6px; }"
            + ".availability-badge { display: inline-block; background: #e8f5e9; color: #2d7a4a; padding: 8px 16px; border-radius: 20px; font-size: 12px; font-weight: 600; }"
            + ".footer { text-align: center; color: #999; font-size: 13px; margin-top: 60px; padding-top: 30px; border-top: 1px solid #e0e0e0; }"
            + "</style></head><body>"
            + "<div class=\"navbar\">"
            + "<div class=\"navbar-content\">"
            + "<div class=\"navbar-brand\">"
            + "<div class=\"navbar-logo\">🏥</div>"
            + "<div class=\"navbar-title\">City Hospital</div>"
            + "</div>"
            + "<a href=\"/\" class=\"back-link\">← Back to Dashboard</a>"
            + "</div>"
            + "</div>"
            + "<div class=\"main-container\">"
            + "<div class=\"page-header\">"
            + "<div class=\"page-title\">Our Medical Professionals</div>"
            + "<div class=\"page-subtitle\">" + hospital.getDoctors().size() + " experienced doctors ready to serve you with specialized medical care</div>"
            + "</div>"
            + "<div class=\"doctors-grid\">"
            + doctorsList.toString()
            + "</div>"
            + "<div class=\"footer\">"
            + "© 2024 City Hospital Medical Center | All doctors are board certified and highly experienced"
            + "</div>"
            + "</div>"
            + "</body></html>";
        sendHtml(exchange, html);
    }

    private String getSpecialtyColor(String specialty) {
        switch(specialty.toLowerCase()) {
            case "cardiology": return "#d32f2f";
            case "neurology": return "#7b1fa2";
            case "orthopedics": return "#f57c00";
            case "pediatrics": return "#0288d1";
            case "dermatology": return "#c2185b";
            case "ophthalmology": return "#388e3c";
            default: return "#0066cc";
        }
    }

    private String getSpecialtyIcon(String specialty) {
        switch(specialty.toLowerCase()) {
            case "cardiology": return "❤️";
            case "neurology": return "🧠";
            case "orthopedics": return "🦴";
            case "pediatrics": return "👶";
            case "dermatology": return "🩺";
            case "ophthalmology": return "👁️";
            default: return "👨‍⚕️";
        }
    }

    private void handleReceptionist(HttpExchange exchange) throws IOException {
        SessionData session = getSession(exchange);
        if (session == null) {
            sendHtml(exchange, "<html><body><h1>❌ Unauthorized</h1><p>Please <a href=\"/login\">login</a> first.</p></body></html>");
            return;
        }
        
        if (!"RECEPTION".equals(session.role)) {
            sendHtml(exchange, "<html><body><h1>❌ Access Denied</h1><p>Only receptionists can access this page. <a href=\"/\">Back to home</a></p></body></html>");
            return;
        }
        
        StringBuilder doctorOptions = new StringBuilder();
        
        if (hospital.getDoctors().isEmpty()) {
            doctorOptions.append("<option disabled selected>No doctors available</option>");
        } else {
            doctorOptions.append("<option value=\"\" disabled selected>-- Select a Doctor --</option>");
            for (Doctor doctor : hospital.getDoctors()) {
                doctorOptions.append("<option value=\"").append(doctor.getDoctorId()).append("\">")
                    .append(doctor.getDoctorId()).append(" - ")
                    .append(doctor.getName()).append(" (")
                    .append(doctor.getSpecialization()).append(")")
                    .append("</option>");
            }
        }
        
        String html = "<html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
            + "<title>Book Appointment - City Hospital Medical Center</title><style>"
            + "* { margin: 0; padding: 0; box-sizing: border-box; }"
            + "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background: #f5f7fa; color: #333; }"
            + ".navbar { background: white; border-bottom: 1px solid #e0e0e0; padding: 16px 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }"
            + ".navbar-content { max-width: 900px; margin: 0 auto; display: flex; justify-content: space-between; align-items: center; }"
            + ".navbar-brand { display: flex; align-items: center; gap: 12px; }"
            + ".navbar-logo { font-size: 28px; }"
            + ".navbar-title { color: #0066cc; font-size: 16px; font-weight: 700; }"
            + ".back-link { color: #0066cc; text-decoration: none; font-size: 14px; font-weight: 600; }"
            + ".back-link:hover { text-decoration: underline; }"
            + ".main-container { max-width: 900px; margin: 0 auto; padding: 40px 20px; }"
            + ".page-header { background: white; padding: 40px; border-radius: 12px; margin-bottom: 40px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); border-bottom: 4px solid #2d7a4a; }"
            + ".page-title { font-size: 28px; font-weight: 700; color: #1a1a1a; margin-bottom: 8px; }"
            + ".page-subtitle { color: #666; font-size: 15px; }"
            + ".form-card { background: white; border-radius: 12px; padding: 40px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }"
            + ".form-section { margin-bottom: 30px; padding-bottom: 30px; border-bottom: 1px solid #e0e0e0; }"
            + ".form-section:last-child { margin-bottom: 0; padding-bottom: 0; border-bottom: none; }"
            + ".section-title { font-size: 14px; font-weight: 700; color: #0066cc; text-transform: uppercase; margin-bottom: 20px; letter-spacing: 0.5px; }"
            + ".form-group { margin-bottom: 20px; }"
            + "label { display: block; color: #333; font-size: 14px; font-weight: 600; margin-bottom: 8px; }"
            + "input, select { width: 100%; padding: 12px 14px; border: 1.5px solid #ddd; border-radius: 8px; font-size: 14px; transition: all 0.3s ease; font-family: inherit; }"
            + "input:focus, select:focus { outline: none; border-color: #2d7a4a; box-shadow: 0 0 0 3px rgba(45, 122, 74, 0.1); }"
            + "input::placeholder { color: #999; }"
            + ".info-banner { background: #f0f7ff; border-left: 4px solid #0066cc; padding: 16px; border-radius: 8px; margin-bottom: 30px; font-size: 14px; color: #0c4a6e; }"
            + ".submit-btn { width: 100%; padding: 14px; background: linear-gradient(135deg, #2d7a4a 0%, #1e5233 100%); color: white; border: none; border-radius: 8px; font-size: 15px; font-weight: 600; cursor: pointer; transition: all 0.3s ease; margin-top: 10px; }"
            + ".submit-btn:hover { box-shadow: 0 8px 20px rgba(45, 122, 74, 0.3); transform: translateY(-2px); }"
            + ".submit-btn:active { transform: translateY(0); }"
            + ".footer { text-align: center; color: #999; font-size: 13px; margin-top: 60px; padding-top: 30px; border-top: 1px solid #e0e0e0; }"
            + ".required-indicator { color: #d32f2f; }"
            + "</style></head><body>"
            + "<div class=\"navbar\">"
            + "<div class=\"navbar-content\">"
            + "<div class=\"navbar-brand\">"
            + "<div class=\"navbar-logo\">🏥</div>"
            + "<div class=\"navbar-title\">City Hospital</div>"
            + "</div>"
            + "<a href=\"/\" class=\"back-link\">← Back to Dashboard</a>"
            + "</div>"
            + "</div>"
            + "<div class=\"main-container\">"
            + "<div class=\"page-header\">"
            + "<div class=\"page-title\">Book Patient Appointment</div>"
            + "<div class=\"page-subtitle\">Schedule a medical consultation with our specialized doctors</div>"
            + "</div>"
            + "<div class=\"form-card\">"
            + "<div class=\"info-banner\">"
            + "📋 <strong>Note:</strong> All fields are required. Doctor selection is mandatory to ensure proper appointment booking."
            + "</div>"
            + "<form method=\"POST\" action=\"/book\">"
            + "<div class=\"form-section\">"
            + "<div class=\"section-title\">👤 Patient Information</div>"
            + "<div class=\"form-group\">"
            + "<label for=\"patientId\">Patient ID <span class=\"required-indicator\">*</span></label>"
            + "<input type=\"text\" id=\"patientId\" name=\"patientId\" placeholder=\"e.g., P-001\" required>"
            + "</div>"
            + "<div class=\"form-group\">"
            + "<label for=\"patientName\">Patient Full Name <span class=\"required-indicator\">*</span></label>"
            + "<input type=\"text\" id=\"patientName\" name=\"patientName\" placeholder=\"John Doe\" required>"
            + "</div>"
            + "</div>"
            + "<div class=\"form-section\">"
            + "<div class=\"section-title\">⏰ Appointment Details</div>"
            + "<div class=\"form-group\">"
            + "<label for=\"doctorId\">Select Doctor <span class=\"required-indicator\">*</span></label>"
            + "<select id=\"doctorId\" name=\"doctorId\" required>"
            + doctorOptions.toString()
            + "</select>"
            + "</div>"
            + "<div class=\"form-group\">"
            + "<label for=\"date\">Appointment Date <span class=\"required-indicator\">*</span></label>"
            + "<input type=\"date\" id=\"date\" name=\"date\" required>"
            + "</div>"
            + "</div>"
            + "<div class=\"form-section\">"
            + "<div class=\"section-title\">💳 Payment Method</div>"
            + "<div class=\"form-group\">"
            + "<label for=\"paymentMethod\">Select Payment Method <span class=\"required-indicator\">*</span></label>"
            + "<select id=\"paymentMethod\" name=\"paymentMethod\" required>"
            + "<optgroup label=\"Online Payment Gateways\">"
            + "<option value=\"razorpay\">💳 Razorpay - Secure Online Payment</option>"
            + "<option value=\"stripe\">💳 Stripe - International Payment</option>"
            + "</optgroup>"
            + "<optgroup label=\"Other Methods\">"
            + "<option value=\"card\">Card - Manual Entry</option>"
            + "<option value=\"upi\">📱 UPI Payment</option>"
            + "<option value=\"cash\">💰 Cash at Reception</option>"
            + "</optgroup>"
            + "</select>"
            + "</div>"
            + "</div>"
            + "<button type=\"submit\" class=\"submit-btn\">✓ Complete Booking</button>"
            + "</form>"
            + "</div>"
            + "<div class=\"footer\">"
            + "© 2024 City Hospital Medical Center | Confidentiality & Patient Privacy Assured"
            + "</div>"
            + "</div>"
            + "</body></html>";
        sendHtml(exchange, html);
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

        // Check if online payment is required
        if ("razorpay".equalsIgnoreCase(paymentMethod) || "stripe".equalsIgnoreCase(paymentMethod)) {
            // Redirect to payment gateway
            String appointmentId = "APT-" + (hospital.getAppointments().size() + 1);
            Appointment appointment = new Appointment(appointmentId, doctorId, patientId, date);
            hospital.addAppointment(appointment);

            Bill bill = new Bill("BILL-" + (hospital.getBills().size() + 1), patientId, doctorId, hospital.getDoctorFee(doctorId));
            hospital.addBill(bill);

            // Store temp data for payment page
            String paymentId = "PAY-" + System.currentTimeMillis();
            
            exchange.getResponseHeaders().add("Location", "/payment?method=" + paymentMethod + "&amount=" + hospital.getDoctorFee(doctorId) 
                + "&appointmentId=" + appointmentId + "&patientName=" + patientName + "&doctorName=" + doctor.getName());
            exchange.sendResponseHeaders(302, 0);
            exchange.close();
            return;
        }

        // Process non-gateway payment
        PaymentService.PaymentResult paymentResult = paymentService.processPayment(paymentMethod, hospital.getDoctorFee(doctorId));
        String appointmentId = "APT-" + (hospital.getAppointments().size() + 1);
        Appointment appointment = new Appointment(appointmentId, doctorId, patientId, date);
        hospital.addAppointment(appointment);

        Bill bill = new Bill("BILL-" + (hospital.getBills().size() + 1), patientId, doctorId, hospital.getDoctorFee(doctorId));
        hospital.addBill(bill);

        String html = "<html><head><meta charset=\"UTF-8\"><title>Booking Confirmation</title>"
            + "<style>body{font-family:Arial,sans-serif;margin:40px;}.card{background:#f8fafc;padding:20px;border-radius:10px;}.success{color:#059669;}.pending{color:#dc2626;}</style>"
            + "</head><body>"
            + "<div class=\"card\">"
            + "<h2 class=\"" + ("PAID".equals(paymentResult.getStatus()) ? "success" : "pending") + "\">Appointment Booked Successfully ✓</h2>"
            + "<p><strong>Appointment ID:</strong> " + appointmentId + "</p>"
            + "<p><strong>Patient:</strong> " + patientName + "</p>"
            + "<p><strong>Doctor:</strong> " + doctor.getName() + "</p>"
            + "<p><strong>Date:</strong> " + date + "</p>"
            + "<p><strong>Consultation Fee:</strong> ₹" + hospital.getDoctorFee(doctorId) + "</p>"
            + "<p><strong>Payment Status:</strong> " + paymentResult.getStatus() + "</p>";
            
        if (!paymentResult.getTransactionId().isEmpty()) {
            html += "<p><strong>Transaction ID:</strong> " + paymentResult.getTransactionId() + "</p>";
        }
        html += "<p><strong>Message:</strong> " + paymentResult.getMessage() + "</p>"
            + "<p><a href=\"/\">Back to home</a></p>"
            + "</div>"
            + "</body></html>";
        sendHtml(exchange, html);
    }

    private void handleConfirmation(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseQueryParams(exchange);
        String appointmentId = params.getOrDefault("appointmentId", "");
        String transactionId = params.getOrDefault("transactionId", "");

        // Find the appointment
        Appointment appointment = null;
        for (Appointment apt : hospital.getAppointments()) {
            if (apt.getAppointmentId().equals(appointmentId)) {
                appointment = apt;
                break;
            }
        }

        if (appointment == null) {
            sendHtml(exchange, "<html><head><meta charset=\"UTF-8\"><title>Error</title></head><body>"
                + "<h2>❌ Appointment Not Found</h2><p><a href=\"/\">Back to home</a></p></body></html>");
            return;
        }

        // Get patient and doctor details
        Patient patient = hospital.findPatient(appointment.getPatientId());
        Doctor doctor = hospital.findDoctor(appointment.getDoctorId());
        String patientName = patient != null ? patient.getName() : appointment.getPatientId();
        String doctorName = doctor != null ? doctor.getName() : appointment.getDoctorId();

        String html = "<html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
            + "<title>Booking Confirmation - City Hospital Medical Center</title><style>"
            + "* { margin: 0; padding: 0; box-sizing: border-box; }"
            + "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background: #f5f7fa; color: #333; }"
            + ".navbar { background: white; border-bottom: 1px solid #e0e0e0; padding: 16px 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }"
            + ".navbar-content { max-width: 900px; margin: 0 auto; display: flex; justify-content: space-between; align-items: center; }"
            + ".navbar-brand { display: flex; align-items: center; gap: 12px; }"
            + ".navbar-logo { font-size: 28px; }"
            + ".navbar-title { color: #0066cc; font-size: 16px; font-weight: 700; }"
            + ".main-container { max-width: 700px; margin: 0 auto; padding: 40px 20px; }"
            + ".confirmation-card { background: white; border-radius: 12px; padding: 40px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); border-top: 4px solid #2d7a4a; }"
            + ".success-header { text-align: center; margin-bottom: 40px; }"
            + ".success-icon { font-size: 60px; margin-bottom: 20px; }"
            + ".success-title { font-size: 28px; font-weight: 700; color: #2d7a4a; margin-bottom: 8px; }"
            + ".success-subtitle { font-size: 15px; color: #666; }"
            + ".confirmation-details { background: #f0f7ff; border-left: 4px solid #0066cc; padding: 20px; border-radius: 8px; margin-bottom: 30px; }"
            + ".detail-row { display: flex; justify-content: space-between; margin-bottom: 12px; }"
            + ".detail-label { font-weight: 600; color: #333; font-size: 14px; }"
            + ".detail-value { color: #0066cc; font-weight: 700; font-size: 14px; }"
            + ".payment-info { background: #f0fdf4; border: 1px solid #86efac; padding: 16px; border-radius: 8px; margin-bottom: 30px; }"
            + ".payment-status { color: #166534; font-size: 14px; }"
            + ".payment-status strong { font-weight: 700; }"
            + ".action-buttons { display: flex; gap: 12px; margin-bottom: 20px; }"
            + ".btn { flex: 1; padding: 14px; border-radius: 8px; font-size: 15px; font-weight: 600; cursor: pointer; text-decoration: none; text-align: center; transition: all 0.3s ease; border: none; }"
            + ".btn-primary { background: linear-gradient(135deg, #2d7a4a 0%, #1e5233 100%); color: white; }"
            + ".btn-primary:hover { box-shadow: 0 8px 20px rgba(45, 122, 74, 0.3); transform: translateY(-2px); }"
            + ".btn-secondary { background: #f0f0f0; color: #0066cc; }"
            + ".btn-secondary:hover { background: #e0e0e0; }"
            + ".footer { text-align: center; color: #999; font-size: 13px; margin-top: 40px; padding-top: 20px; border-top: 1px solid #e0e0e0; }"
            + "</style></head><body>"
            + "<div class=\"navbar\">"
            + "<div class=\"navbar-content\">"
            + "<div class=\"navbar-brand\">"
            + "<div class=\"navbar-logo\">🏥</div>"
            + "<div class=\"navbar-title\">City Hospital</div>"
            + "</div>"
            + "</div>"
            + "</div>"
            + "<div class=\"main-container\">"
            + "<div class=\"confirmation-card\">"
            + "<div class=\"success-header\">"
            + "<div class=\"success-icon\">✅</div>"
            + "<div class=\"success-title\">Appointment Confirmed!</div>"
            + "<div class=\"success-subtitle\">Your appointment has been successfully booked</div>"
            + "</div>"
            + "<div class=\"confirmation-details\">"
            + "<div class=\"detail-row\">"
            + "<span class=\"detail-label\">📋 Appointment ID:</span>"
            + "<span class=\"detail-value\">" + appointmentId + "</span>"
            + "</div>"
            + "<div class=\"detail-row\">"
            + "<span class=\"detail-label\">👤 Patient Name:</span>"
            + "<span class=\"detail-value\">" + patientName + "</span>"
            + "</div>"
            + "<div class=\"detail-row\">"
            + "<span class=\"detail-label\">👨‍⚕️ Doctor Name:</span>"
            + "<span class=\"detail-value\">" + doctorName + "</span>"
            + "</div>"
            + "<div class=\"detail-row\">"
            + "<span class=\"detail-label\">📅 Appointment Date:</span>"
            + "<span class=\"detail-value\">" + appointment.getAppointmentDate() + "</span>"
            + "</div>"
            + "<div class=\"detail-row\">"
            + "<span class=\"detail-label\">💰 Consultation Fee:</span>"
            + "<span class=\"detail-value\">₹" + hospital.getDoctorFee(appointment.getDoctorId()) + "</span>"
            + "</div>";
            
        if (!transactionId.isEmpty()) {
            html += "<div class=\"detail-row\">"
                + "<span class=\"detail-label\">🔐 Transaction ID:</span>"
                + "<span class=\"detail-value\">" + transactionId + "</span>"
                + "</div>";
        }
        
        html += "</div>"
            + "<div class=\"payment-info\">"
            + "<span class=\"payment-status\">✓ <strong>Payment Successful</strong> - Appointment is confirmed</span>"
            + "</div>"
            + "<div class=\"action-buttons\">"
            + "<a href=\"/appointments\" class=\"btn btn-primary\">📅 View My Appointments</a>"
            + "<a href=\"/\" class=\"btn btn-secondary\">🏠 Back to Home</a>"
            + "</div>"
            + "</div>"
            + "<div class=\"footer\">"
            + "© 2024 City Hospital Medical Center | A confirmation email will be sent shortly"
            + "</div>"
            + "</div>"
            + "</body></html>";
        
        sendHtml(exchange, html);
    }

    private void handlePayment(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseQueryParams(exchange);
        String method = params.getOrDefault("method", "razorpay");
        String amount = params.getOrDefault("amount", "500");
        String appointmentId = params.getOrDefault("appointmentId", "");
        String patientName = params.getOrDefault("patientName", "");
        String doctorName = params.getOrDefault("doctorName", "");
        
        if ("razorpay".equalsIgnoreCase(method)) {
            // Razorpay integration with test credentials
            String razorpayKeyId = "rzp_test_1DP5mmOlF5G5ag"; // Test key
            String html = "<html><head><meta charset=\"UTF-8\"><title>Razorpay Payment - City Hospital</title>"
                + "<script src=\"https://checkout.razorpay.com/v1/checkout.js\"></script>"
                + "<style>"
                + "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; display: flex; align-items: center; justify-content: center; }"
                + ".payment-container { background: white; padding: 40px; border-radius: 15px; box-shadow: 0 20px 60px rgba(0,0,0,0.3); max-width: 500px; width: 100%; }"
                + ".payment-container h1 { color: #667eea; margin: 0 0 30px 0; text-align: center; }"
                + ".payment-details { background: #f0f9ff; padding: 20px; border-radius: 8px; margin-bottom: 30px; border-left: 4px solid #667eea; }"
                + ".payment-details p { margin: 8px 0; color: #333; }"
                + ".payment-details strong { color: #667eea; }"
                + ".btn { width: 100%; padding: 15px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border: none; border-radius: 8px; font-size: 1.05em; font-weight: 600; cursor: pointer; }"
                + ".btn:hover { box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3); }"
                + ".back-link { text-align: center; margin-top: 20px; }"
                + ".back-link a { color: #667eea; text-decoration: none; }"
                + ".info-box { background: #f0fdf4; border: 1px solid #86efac; padding: 15px; border-radius: 8px; margin-bottom: 20px; font-size: 0.9em; color: #166534; }"
                + "</style>"
                + "</head><body>"
                + "<div class=\"payment-container\">"
                + "<h1>💳 Complete Your Payment</h1>"
                + "<div class=\"info-box\">"
                + "📌 <strong>TEST MODE:</strong> Use card: 4111111111111111 | Expiry: Any Future Date | CVV: Any 3 digits"
                + "</div>"
                + "<div class=\"payment-details\">"
                + "<p><strong>Appointment ID:</strong> " + appointmentId + "</p>"
                + "<p><strong>Patient:</strong> " + patientName + "</p>"
                + "<p><strong>Doctor:</strong> " + doctorName + "</p>"
                + "<p><strong>Amount:</strong> ₹" + amount + "</p>"
                + "</div>"
                + "<button class=\"btn\" onclick=\"payWithRazorpay()\">Pay ₹" + amount + " with Razorpay</button>"
                + "<div class=\"back-link\"><a href=\"/receptionist\">← Back to Booking</a></div>"
                + "</div>"
                + "<script>"
                + "function payWithRazorpay() {"
                + "  var options = {"
                + "    key: '" + razorpayKeyId + "',"
                + "    amount: " + (Integer.parseInt(amount) * 100) + ","
                + "    currency: 'INR',"
                + "    name: 'City Hospital',"
                + "    description: 'Doctor Appointment',"
                + "    image: 'data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 100 100%22><text x=%2250%25%22 y=%2250%25%22 font-size=%2240%22>🏥</text></svg>',"
                + "    handler: function(response){"
                + "      window.location.href = '/confirmation?appointmentId=" + appointmentId + "&transactionId=' + response.razorpay_payment_id;\"
                + "    },"
                + "    prefill: {"
                + "      name: '" + patientName + "',"
                + "      email: 'patient@hospital.com',"
                + "      contact: '9876543210'"
                + "    },"
                + "    theme: {"
                + "      color: '#667eea'"
                + "    }"
                + "  };"
                + "  var rzp1 = new Razorpay(options);"
                + "  rzp1.open();"
                + "}"
                + "</script>"
                + "</body></html>";
            sendHtml(exchange, html);
        } else if ("stripe".equalsIgnoreCase(method)) {
            // Stripe integration with test credentials
            String stripeKey = "pk_test_51PqNZD00C0N8L5xQ"; // Test public key
            String html = "<html><head><meta charset=\"UTF-8\"><title>Stripe Payment - City Hospital</title>"
                + "<script src=\"https://js.stripe.com/v3/\"></script>"
                + "<style>"
                + "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; display: flex; align-items: center; justify-content: center; }"
                + ".payment-container { background: white; padding: 40px; border-radius: 15px; box-shadow: 0 20px 60px rgba(0,0,0,0.3); max-width: 500px; width: 100%; }"
                + ".payment-container h1 { color: #667eea; margin: 0 0 30px 0; text-align: center; }"
                + ".payment-details { background: #f0f9ff; padding: 20px; border-radius: 8px; margin-bottom: 30px; border-left: 4px solid #667eea; }"
                + ".payment-details p { margin: 8px 0; color: #333; }"
                + ".btn { width: 100%; padding: 15px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border: none; border-radius: 8px; font-size: 1.05em; font-weight: 600; cursor: pointer; }"
                + ".info-box { background: #f0fdf4; border: 1px solid #86efac; padding: 15px; border-radius: 8px; margin-bottom: 20px; font-size: 0.9em; color: #166534; }"
                + "#card-element { padding: 12px; border: 2px solid #e0e0e0; border-radius: 8px; margin-bottom: 20px; }"
                + ".back-link { text-align: center; margin-top: 20px; }"
                + ".back-link a { color: #667eea; text-decoration: none; }"
                + "</style>"
                + "</head><body>"
                + "<div class=\"payment-container\">"
                + "<h1>💳 Complete Your Payment</h1>"
                + "<div class=\"info-box\">"
                + "📌 <strong>TEST MODE:</strong> Use card: 4242424242424242 | Expiry: 12/25 | CVC: Any 3 digits"
                + "</div>"
                + "<div class=\"payment-details\">"
                + "<p><strong>Appointment ID:</strong> " + appointmentId + "</p>"
                + "<p><strong>Patient:</strong> " + patientName + "</p>"
                + "<p><strong>Doctor:</strong> " + doctorName + "</p>"
                + "<p><strong>Amount:</strong> ₹" + amount + "</p>"
                + "</div>"
                + "<div id=\"card-element\"></div>"
                + "<button class=\"btn\" onclick=\"submitPayment()\">Pay ₹" + amount + " with Stripe</button>"
                + "<div class=\"back-link\"><a href=\"/receptionist\">← Back to Booking</a></div>"
                + "</div>"
                + "<script>"
                + "var stripe = Stripe('" + stripeKey + "');"
                + "var elements = stripe.elements();"
                + "var cardElement = elements.create('card');"
                + "cardElement.mount('#card-element');"
                + "function submitPayment() {"
                + "  window.location.href = '/confirmation?appointmentId=" + appointmentId + "&transactionId=TEST_' + Date.now();\
                + "}"
                + "</script>"
                + "</body></html>";
            sendHtml(exchange, html);
        }
    }

    private Map<String, String> parseQueryParams(HttpExchange exchange) {
        Map<String, String> params = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            for (String pair : query.split("&")) {
                String[] parts = pair.split("=", 2);
                if (parts.length == 2) {
                    try {
                        params.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8), 
                                  URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
                    } catch (Exception e) {}
                }
            }
        }
        return params;
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
