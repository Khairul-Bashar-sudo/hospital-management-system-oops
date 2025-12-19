# 🏥 Hospital Management System (CLI Based)

A **console-based Hospital Management System** developed using **Java** and **Object-Oriented Programming (OOP)** concepts.  
This project supports **role-based login**, **file handling**, and **structured data management** using a simple command-line interface.

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

✔ Role-based login system  
✔ Add, view, search & delete doctors and patients  
✔ Schedule appointments  
✔ Generate and view bills  
✔ Persistent data storage using files  
✔ Tabular view for better readability  
✔ Colorful CLI output  

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
- **CLI Interface**

---

## 🗂 Project Structure

Hospital-Management-System-CLI/
│
├── src/
│ ├── Main.java
│ ├── Hospital.java
│ ├── Auth.java
│ ├── ConsoleColors.java
│ ├── Person.java
│ ├── Doctor.java
│ ├── Patient.java
│ ├── Appointment.java
│ ├── Bill.java
│
├── data/
│ ├── doctors.txt
│ ├── patients.txt
│ ├── appointments.txt
│ ├── bills.txt
│
├── README.md
└── .gitignore

---

## ▶ How to Run the Project
### Step 1: Compile the Project
- javac src/*.java

Step 2: Run the Application
- java src.Main

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

## ▶ Future Enhancements

Database integration (MySQL)

GUI using JavaFX or Swing

Password encryption

Multiple doctor accounts

Appointment reminders

---

## 👨‍💻 Author

- Khairul Bashar
- B.Tech Computer Science Student
- Hospital Management System – OOP Project

- ⭐ This project was developed as part of an academic requirement and demonstrates strong understanding of Java fundamentals and object-oriented design principles.