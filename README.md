# 🏥 Hospital Management System (Web + Payment Enabled)

A modernized **Hospital Management System** developed in **Java** with **Object-Oriented Programming (OOP)** principles.  
The project now includes a **web-based booking interface** and a **payment step** for appointments, making it more relevant for real-world hospital workflows.

---

## 📌 Project Overview

The Hospital Management System allows hospitals to manage:

- Doctors
- Patients
- Appointments
- Bills

It uses **role-based access control** to ensure that different users have appropriate permissions.

---

## 🔐 User Roles & Access

| Role | Username | Password | Permissions |
|----|----|----|----|
| Admin | admin | 1234 | Full access (Add, View, Delete, Search, Bills) |
| Doctor | doctor1 | 1111 | View own appointments & patients |
| Receptionist | reception | 2222 | Add patients, schedule appointments, generate bills |

---

## 🚀 Features

✔ Web-based appointment booking interface  
✔ Online payment simulation for appointment booking  
✔ Role-based login system  
✔ Add, view, search & delete doctors and patients  
✔ Schedule appointments  
✔ Generate and view bills  
✔ Persistent data storage using files  
✔ OOP-based domain model for doctors, patients, appointments, and bills  

---

## 🛠 Technologies Used

- **Java (Core Java)**
- **Object-Oriented Programming**
  - Inheritance
  - Polymorphism
  - Encapsulation
  - Abstraction
- **Data Structures**
  - `ArrayList` for storing records
- **File Handling**
  - `File`
  - `Scanner`
  - `PrintWriter`
- **Web Interface**
  - `HttpServer`
  - HTML forms for booking

---

## 🗂 Project Structure

Hospital-Management-System-CLI/
│
├── src/
│   ├── Main.java
│   ├── Hospital.java
│   ├── Auth.java
│   ├── ConsoleColors.java
│   ├── Person.java
│   ├── Doctor.java
│   ├── Patient.java
│   ├── Appointment.java
│   └── Bill.java
│
├── data/
│   ├── doctors.txt
│   ├── patients.txt
│   ├── appointments.txt
│   └── bills.txt
│
├── README.md
└── .gitignore


---

## ▶ How to Run the Project
### Step 1: Compile the Project
- javac src/*.java

Step 2: Run the Application
- java src.Main

Step 3: Open the Browser
- Visit http://localhost:8080
- Use the booking form to schedule an appointment and complete the payment step

--- 

## 💾 Data Storage

### All data is saved and retrieved using text files:

- doctors.txt

- patients.txt

- appointments.txt

- bills.txt

### This ensures data persistence even after program termination.

--- 

## 🧠 OOP Design

- Person → Abstract base class

- Doctor & Patient → Extend Person

- Hospital → Manages all data collections

- Appointment → Links doctor & patient

- Bill → Handles billing details

- Method overriding used for display and data handling

---

## 💡 Suggested New Features

- Real payment gateway integration (Stripe / Razorpay / PayPal)
- Patient login and appointment history
- Email or SMS reminders
- Doctor availability calendar
- Online prescription and medical reports
- Admin dashboard with analytics
- Database integration (MySQL)

---

## 👨‍💻 Author

- Khairul Bashar
- B.Tech Computer Science Student
- Hospital Management System – OOP Project

- ⭐ This project was developed as part of an academic requirement and demonstrates strong understanding of Java fundamentals and object-oriented design principles.