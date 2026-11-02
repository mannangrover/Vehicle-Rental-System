# RoadReady: Vehicle Rental Management System

A web application for renting cars, bikes and SUVs by the hour or by the day. Customers search for vehicles, book them and pay. Bookings are confirmed automatically. Staff hand vehicles over and process returns.

This is a course project for Advanced Java (CSE3143). Payments are simulated: no real money moves and no payment gateway is involved.

## Features

Customers can:
- Register and log in
- Search vehicles by pick-up and return time, type and maximum daily rate
- Book a vehicle and see the price before booking
- Pay by card, UPI or cash (simulated)
- See their bookings, invoices and payments
- Cancel a pending or confirmed booking before the pick-up time, with a full refund

Admins can:
- Mark a vehicle as picked up, and process its return with late and damage fees
- Add and edit vehicles, and change their status
- See a dashboard with revenue and fleet figures
- Read the audit log and download bookings as a CSV file

Pricing rules:

- Each vehicle has a daily rate and an hourly rate, set by the admin. The hourly rate can't be more than the daily rate.
- A rental runs in whole hours, from 1 hour to 30 days. It can start at the moment another rental ends.
- The price is full days at the daily rate, plus any extra hours at the hourly rate. Extra hours never cost more than one day.
- Discounts for 7 days or more, deposits and late charges are in the `Car`, `Bike` and `Suv` classes:

| Type | Discount for 7 days or more | Security deposit | Late charge |
|------|-----------------------------|------------------|-----------------|
| Car  | 10% | 5,000 | 1.5 x normal price |
| Bike | 15% | 1,000 | 1.25 x normal price |
| SUV  | 5%  | 10,000 | 2 x normal price |

Late time is charged per hour, and part of an hour counts as a full hour.

Booking limits are in `rental-policy.xml`: at most 30 days long, at most 90 days in advance, and pickup from 1 hour before the start time.

## Tech stack

- Java 21 language level, Spring Boot 3.5
- JSP and JSTL for pages, Bootstrap 5 for layout
- Barlow fonts (SIL Open Font License), stored in `static/fonts` so the app works offline
- Spring JDBC (`JdbcTemplate`) with plain SQL
- H2 database saved to a file, with an optional MySQL setup
- JUnit 5, AssertJ and MockMvc for tests
- Maven, through the included wrapper

## How the syllabus maps to the code

Paths start at `src/main/java/com/vehiclerental/` unless they say otherwise.

| Topic | Where to look |
|-------|---------------|
| OOP | `model/Vehicle.java` is abstract. `Car`, `Bike` and `Suv` override its discount, deposit and late fee. `VehicleFactory` picks the subclass. |
| Exceptions | `exception/`: custom unchecked exceptions, all extending `RentalException`. |
| Collections | `List` results from the services. `Map` values in `model/DashboardStats.java`. |
| Multithreading and concurrency | `createBooking` in `service/impl/BookingServiceImpl.java` takes a row lock with `SELECT ... FOR UPDATE` (`dao/VehicleDao.java`) before it checks for overlaps. This stops two customers booking the same vehicle at once. `service/impl/DashboardServiceImpl.java` runs its queries on a thread pool. `src/test/.../service/DoubleBookingTest.java` tests it with 8 threads, an `ExecutorService` and a `CountDownLatch`. |
| JDBC | `dao/`: `JdbcTemplate` with `?` parameters, for example `dao/BookingDao.java`. Tables are in `src/main/resources/schema.sql`. |
| Transactions | `@Transactional` in `service/impl/BookingServiceImpl.java`: for example `processReturn` saves the booking and any refund together. |
| Dependency injection | Constructor injection: see `web/VehicleController.java`, which receives the `VehicleService` interface and the `Clock` bean. |
| Bean life cycle and XML config | `src/main/resources/rental-policy.xml` defines the `RentalPolicy` bean with `init-method="validate"`. `VehicleRentalApplication` loads it with `@ImportResource`. |
| Events | `createBooking` publishes `event/BookingConfirmedEvent.java`, and `event/BookingEventListener.java` creates the invoice. |
| AOP | `aspect/AuditAspect.java` runs after methods marked `@Audited` and writes to the `audit_logs` table. See it at `/admin/audit`. |
| JSP | `src/main/webapp/WEB-INF/jsp/`. Shared parts are in `common/`. |
| Servlets | `web/admin/BookingsCsvServlet.java` is a plain `HttpServlet` for `/admin/reports/bookings.csv`, registered in `web/admin/AdminServletConfig.java`. Login and role checks are in the servlet filter `web/filter/AuthFilter.java`, registered in `config/WebConfig.java`. |
| Session management | `model/SessionUser.java` is stored in the `HttpSession` at login. Cookie settings and the timeout are in `application.properties`. |
| Swing | Out of scope: this is a web application, so the interface is JSP. |
| Spring Cloud | Out of scope: there is one application and no separate services to connect. |

## Booking lifecycle

```
CONFIRMED --pay, then pickup--> ACTIVE --return--> COMPLETED
   |
   | cancel
   v
CANCELLED
```

1. The customer books. Overlapping times are rejected. Sharing even one hour counts as an overlap.
2. The booking is confirmed automatically and an invoice is created.
3. The customer pays the rental plus the deposit.
4. From 1 hour before the start time, the admin marks the pickup. Status is ACTIVE. This needs full payment.
5. The admin records the return. Status is COMPLETED.
6. At the return, total charges are rental + late fee + damage fee. If the customer has paid more than that, the extra is refunded automatically. If less, the customer pays the balance from the booking page.

## Project structure

```
pom.xml                     Maven build
src/main/java/com/vehiclerental/
    VehicleRentalApplication.java   starts the app
    aspect/                 audit logging with AOP
    config/                 configuration, RentalPolicy and demo data
    dao/                    SQL through JdbcTemplate
    dto/                    form and search objects, with validation
    event/                  booking confirmed event and its listener
    exception/              custom exceptions
    model/                  entities, enums and the Vehicle classes
    scheduler/              hourly job that marks overdue rentals
    service/                service interfaces
    service/impl/           service logic
    web/                    controllers, error handling and the login filter
src/main/resources/
    application.properties  main settings (H2 saved to a file)
    application-h2.properties   in-memory H2, used by the tests
    application-mysql.properties    optional MySQL settings
    schema.sql              creates the tables
    rental-policy.xml       booking limits, as an XML bean
    static/css/app.css      styles
src/main/webapp/WEB-INF/jsp/    the pages
src/test/java/com/vehiclerental/
    model/VehiclePricingTest.java   prices, deposits and late fees
    service/BookingFlowTest.java    booking lifecycle and its rejections
    service/DoubleBookingTest.java  overlaps, search and the concurrency test
    web/PageSecurityTest.java       login and role checks on pages
```

## Run it in IntelliJ

1. Open the project folder as a Maven project.
2. Set the Project SDK to JDK 21 or newer.
3. Choose **Run app** from the run menu at the top right, and press Run.
4. Open http://localhost:8080.

The app saves its data in an H2 database in the `data` folder. It needs no install or password. To start again with fresh demo data, stop the app and delete the `data` folder.

### Optional: MySQL

The project can also run on MySQL, but this hasn't been tested yet. MySQL must be running on `localhost:3306`. Create `application-local.properties` in the project folder with your login, then choose **Run with MySQL**.

```properties
spring.datasource.username=your_mysql_user
spring.datasource.password=your_mysql_password
```

## Run it from the terminal

`JAVA_HOME` must point to the JDK.

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test
```

The tests use a separate in-memory H2 database, so they don't change your data.

## Demo accounts

For local demo only. These are added when the tables are empty. The login page has a one-click button for each. Set `app.seed-demo-data=false` in `application.properties` to remove the buttons and stop adding demo data.

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@roadready.test | Admin@123 |
| Customer | customer@roadready.test | Customer@123 |

The demo data has 4 customers, 13 vehicles and 16 bookings with payments, invoices and audit entries. The demo customer has a booking in every state: completed, balance due, late, overdue, paid, unpaid and cancelled. Dates are set relative to the day the data is added.

## Demo script for the viva

Delete the `data` folder first, so the data is clean.

1. Open `/vehicles` without logging in. Search from the next hour to 3 hours later. Show the hourly price and deposit on a vehicle page.
2. Open `/admin` while logged out. It redirects to the login page. Log in as the customer, then open `/admin` again. Access is refused.
3. As the customer, book that vehicle for those 3 hours. Status is CONFIRMED and an invoice now exists.
4. Try to book the same vehicle for the same times. It is rejected as not available. Search again to show the vehicle is now hidden for those times.
5. Log in as the admin and open the booking. Try to mark the pickup: it is refused, because nothing has been paid.
6. As the customer, pay the booking. Explain that the payment is simulated and covers rental plus deposit.
7. As the admin, mark the pickup. Then process the return with the current time and a small damage fee. Status is COMPLETED. As the customer, show the invoice and the automatic refund of the rest of the deposit.
8. As the admin, show the dashboard, the audit log and the CSV download. Then run `DoubleBookingTest` in IntelliJ and explain the 8-thread test: one booking succeeds and seven get `VehicleNotAvailableException`.

## Troubleshooting

- **"Port 8080 was already in use":** another program, or an earlier run of this app, is using the port. Stop it, or add `--server.port=8081` to the Run Configuration's program arguments and open http://localhost:8081.
- **A JSP change doesn't show:** restart the app.
- **The page is a 404 for every URL:** check that the Run Configuration's working directory is the project folder.
- **Database already in use:** another copy of the app is running. Stop it first.

## Limits

- Payments are simulated.
- There is no email, no vehicle photo upload and no password reset.
- The demo accounts and their passwords are public in this file. Don't use them outside local development.
