package com.vehiclerental.web.admin;

import com.vehiclerental.model.Booking;
import com.vehiclerental.service.BookingService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/** Downloads every booking as a CSV file. Registered in AdminServletConfig; the login filter protects its URL. */
public class BookingsCsvServlet extends HttpServlet {

    private static final String HEADER_ROW =
            "id,customer,email,vehicle,reg number,start,end,status,rental,late fee,damage fee";
    private static final String LINE_END = "\r\n";

    /** A spreadsheet treats a cell starting with one of these as a formula. */
    private static final String FORMULA_START_CHARS = "=+-@\t\r";

    private final BookingService bookingService;

    public BookingsCsvServlet(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Booking> bookings = bookingService.listAll(null);

        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"bookings.csv\"");

        PrintWriter out = response.getWriter();
        out.print(HEADER_ROW + LINE_END);
        for (Booking booking : bookings) {
            out.print(toRow(booking) + LINE_END);
        }
    }

    private String toRow(Booking booking) {
        return String.join(",",
                valueCell(booking.getId()),
                textCell(booking.getCustomerName()),
                textCell(booking.getCustomerEmail()),
                textCell(booking.getVehicleName()),
                textCell(booking.getRegNumber()),
                valueCell(booking.getStartAt()),
                valueCell(booking.getEndAt()),
                valueCell(booking.getStatus()),
                valueCell(booking.getRentalAmount()),
                valueCell(booking.getLateFee()),
                valueCell(booking.getDamageFee()));
    }

    /** For text someone typed in: guards against formula injection, then quotes. */
    static String textCell(String value) {
        if (value == null) {
            return quote("");
        }
        if (!value.isEmpty() && FORMULA_START_CHARS.indexOf(value.charAt(0)) >= 0) {
            return quote("'" + value);
        }
        return quote(value);
    }

    /** For numbers, dates and enums, which the application generates itself. */
    private static String valueCell(Object value) {
        return quote(value == null ? "" : value.toString());
    }

    private static String quote(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
