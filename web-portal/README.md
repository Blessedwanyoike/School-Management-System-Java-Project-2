# Carol Academy web portal

This is a separate Spring Boot web application. It does not replace or share a
database with the existing Swing application.

## System capabilities

- Student Admissions & Registration (NEMIS / UPI tracking)
- Student Profiles & Parent/Guardian Contacts
- CBC Assessment & Competency Rubrics
- Learner Progress Reports & Termly Report Cards
- Fee Billing, Receipts & Balance Tracking
- M-Pesa & Bank Payment Integration
- Accounting & Expense Management
- Class & Lesson Timetabling
- Daily Student & Teacher Attendance
- Parent-Teacher Communication & Bulk SMS Alerts
- Parent, Teacher & Admin Portals
- School Transport & Bus Route Tracking
- Boarding & Dormitory Management (if applicable)
- Library & Textbook Issuing
- Health, Allergy & Sickbay Records
- Staff Management & Payroll
- Kitchen, Uniform & Store Inventory
- Reports & Analytics

## Run locally

Requirements: JDK 25 or later and Maven.

Set a local seed password before starting the portal. In PowerShell:

```powershell
$env:APP_SEED_DATA_PASSWORD = "choose-a-local-password"
```

From this directory:

```powershell
mvn spring-boot:run
```

You can also double-click `Run Carol Academy Web Portal.bat` in the project
root.

Open <http://localhost:8080>. The portal database is stored under
`web-portal/data/` and remains between local runs.

## Local seed accounts

Seed accounts use the value of `APP_SEED_DATA_PASSWORD`:

| Role | Username |
|---|---|
| Head teacher | `headteacher` |
| Deputy head teacher | `deputy` |
| Secretary | `secretary` |
| Bursar | `bursar` |
| Teacher | `teacher` |
| Bus driver | `driver` |
| Parent (use the student's admission number) | `B/001/26` through `B/1500/26` |

There are 60 teacher accounts (`teacher`, `teacher02` through `teacher60`) and
10 driver accounts (`driver`, `driver02` through `driver10`), with sample names.
There are parent accounts for all 1,500 admissions, from `B/001/26` through
`B/1500/26`. The seed password is hashed in the database, but these shared
credentials are not suitable for real student records or deployment. On
restart, the configured seed password is applied to the seeded accounts.

Staff who need a new teacher or bus-driver account can use the sign-up link on
the login page. New accounts remain disabled until a head teacher, deputy head
teacher, or secretary approves them from the administration portal. Public
sign-up cannot create administrator or parent accounts.

The portal seeds 1,500 sample students with admission numbers from `B/001/26`
through `B/1500/26`. Each admission number is also the username for its linked
parent account. The head teacher can search students by admission number or
name and open their academic and fee reports. Other roles cannot access that
search.

## Important limitations

- Administrators, the secretary, and bursars can open **Logs** to review recent
  sign-ins, attendance, fee payments, staff approvals, and module changes.
  Technical application logs are written to `web-portal/logs/carol-academy.log`
  and rotated at 10 MB, with up to 14 days retained.
- The sample database and accounts are for local previews. Configure
  production-grade account provisioning, HTTPS, database credentials, backups,
  and deployment settings before using real student information.
- Parent accounts currently share the local seed password. Configure individual
  parent credentials and password-reset/invitation flows before using real
  student information.
- Fee payments are simulations. The page records a local ledger entry and
  adjusts the sample balance; it does not charge a card, mobile wallet, or bank
  account. Select and configure a payment provider before enabling real payments.
- The Swing desktop app remains unchanged and has its own in-memory records.
  The website has a separate persistent database; updates do not synchronize
  between the two applications.
