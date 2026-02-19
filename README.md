<img width="500" height="600" alt="Screenshot 2026-02-19 082936" src="https://github.com/user-attachments/assets/6c5621da-72f8-4a31-b3ff-c304fba93765" />
<img width="500" height="600" alt="Screenshot 2026-02-19 083119" src="https://github.com/user-attachments/assets/d589b1b4-f2f8-42cc-9159-9804bb532fc2" />
<img width="1919" height="881" alt="image" src="https://github.com/user-attachments/assets/f62913ea-a089-4332-b072-922cf7a126e8" />
<img width="500" height="600" alt="image" src="https://github.com/user-attachments/assets/ac4f60ff-542b-467e-a515-995fceb67548" />
<img width="500" height="600" alt="Screenshot 2026-02-19 083145" src="https://github.com/user-attachments/assets/0e5b4afc-a3e7-49b6-957f-03b1e189567d" />
<img width="1917" height="904" alt="Screenshot 2026-02-19 083208" src="https://github.com/user-attachments/assets/6244f030-4988-4306-920c-61b1dabf4d9c" />
<img width="500" height="600" alt="image" src="https://github.com/user-attachments/assets/f4a6237d-3e22-4c99-8b1a-cb1c4ec9c1eb" />
<img width="500" height="600" alt="image" src="https://github.com/user-attachments/assets/daa4bd7e-c8c8-4f14-acfc-77a159ab1df5" />

🩺 Regional Health Trend Dashboard (AI-Driven Outbreak Early Warning System)

An AI-assisted public health intelligence platform that detects abnormal symptom trends from clinics in real-time and alerts administrators before a disease outbreak spreads.

🌍 The Problem

In many regions, disease outbreaks are identified too late.

Current healthcare reporting systems are:

Paper-based

Delayed

Non-centralized

Reactive (respond after outbreak)

Because of this:

Dengue spreads before action

Flu clusters go unnoticed

Local epidemics are reported only after hospitals overflow

Public health officers lack real-time visibility into symptom trends across clinics.

💡 Our Solution

We built a Regional Health Trend Dashboard that continuously collects symptom data from clinics and uses anomaly detection to identify unusual increases in illness patterns.

Instead of waiting for confirmed diagnoses, the system monitors early symptoms like:

Fever

Cough

Vomiting

Diarrhea

Fatigue

When symptom frequency crosses a statistical baseline, the platform automatically:

✔ Detects anomaly
✔ Generates alert
✔ Highlights region on heatmap
✔ Notifies administrators

This enables early intervention before an outbreak occurs.

🎯 Impact (SDG 3 – Good Health & Well-Being)

This system supports:

Epidemic preparedness

Faster response time

Preventive healthcare

Reduced mortality

Government surveillance support

The platform can be deployed at:

District level

State level

National health networks

🧠 Key Features
1. Real-Time Clinic Reporting

Clinics submit daily patient symptom counts via secure login.

2. AI-Based Anomaly Detection

The system compares current data with historical baseline and identifies abnormal spikes.

3. Heatmap Visualization

A geographic heatmap shows where symptoms are increasing.

4. Automatic Alerts

Health administrators receive alerts when risk levels increase.

5. Role-Based Access

Admin (health authority)

Clinic staff (data submission)

6. Public Health Dashboard

Shows:

Trends

Risk severity

Symptom distribution

Active alerts

7. Audit & Transparency

Every data entry is logged for accountability.

🏗 System Architecture
Clinics → Data Submission → Backend Processing → Anomaly Detection → Alert Engine → Dashboard Visualization

⚙️ Tech Stack
Frontend

React.js

Vite

Context API

REST API Integration

Backend

Spring Boot

Spring Security

JWT Authentication

RESTful APIs

Database

MySQL

Core Services

Anomaly Detection Engine

Alert Service

Visualization Service

Notification Service

Data Ingestion Pipeline

🧩 Major Modules
Module	Purpose
Authentication	Secure login using JWT
Clinic Submission	Daily symptom reporting
Dashboard	Real-time health analytics
Heatmap	Geographic disease visualization
Alerts	Early outbreak warning
Admin Panel	System monitoring & control
Settings	Alert threshold configuration
Audit Logs	Data transparency
🔐 Security

JWT-based authentication

Role-based authorization

Protected APIs

Audit logging

🧪 Anomaly Detection Logic

The system calculates a baseline symptom average using historical records.

If:

Current Cases > Expected Baseline + Threshold


→ The system flags a potential outbreak.

This allows detection before lab confirmations.

📊 Example Use Case

12 clinics submit daily data.

Fever cases suddenly rise in one locality.

System detects abnormal deviation.

Alert generated.

Region turns red on heatmap.

Health officer intervenes (testing, sanitation, awareness).

Outbreak prevented.

🗄 Database

The project includes SQL schema:

/database/schema.sql


Import into MySQL before running backend.

🚀 How to Run the Project
1️⃣ Database Setup

Create database:

CREATE DATABASE health_dashboard;


Import schema:

database/schema.sql


Update credentials in:

backend/src/main/resources/application.properties

2️⃣ Backend (Spring Boot)
cd backend
mvn spring-boot:run


Server runs at:

http://localhost:8080

3️⃣ Frontend (React)
cd frontend
npm install
npm run dev


App runs at:

http://localhost:5173

👤 Default Accounts (Seeded)

Admin:

email: admin@health.com
password: admin123


Clinic:

email: clinic@health.com
password: clinic123

🧭 Future Improvements

SMS alerts to authorities

Government API integration

ML disease prediction model

Mobile application

National-scale deployment

📌 Why This Project Matters

Healthcare systems today react to outbreaks.

This system enables predictive public health surveillance.

Instead of counting patients after infection spreads, authorities can act when the first warning signs appear.

👥 Team

Hackathon Project — AI for SDG 3
Focus: Preventive Healthcare & Epidemic Preparedness

📜 License

This project is for academic and research demonstration purposes.





