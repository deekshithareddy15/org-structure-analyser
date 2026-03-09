package com.bigcompany.util;

import com.bigcompany.model.Employee;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvEmployeeReader {

    private static final int EXPECTED_COLUMN_COUNT = 5;

    public List<Employee> read(String filePath) throws IOException {
        List<Employee> employees = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine(); // skip header
            if (line == null) {
                return employees;
            }

            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",", -1); // -1 keeps trailing empty fields
                if (parts.length != EXPECTED_COLUMN_COUNT) {
                    throw new IllegalArgumentException(
                            "Line " + lineNumber + " has " + parts.length +
                            " columns, expected " + EXPECTED_COLUMN_COUNT + ": [" + line + "]");
                }

                int id            = parseId(parts[0].trim(), lineNumber);
                String firstName  = parts[1].trim();
                String lastName   = parts[2].trim();
                double salary     = parseSalary(parts[3].trim(), lineNumber);
                Integer managerId = parseOptionalId(parts[4].trim(), lineNumber);

                employees.add(new Employee(id, firstName, lastName, salary, managerId));
            }
        }

        return employees;
    }

    private int parseId(String value, int lineNumber) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ID '" + value + "' on line " + lineNumber);
        }
    }

    private double parseSalary(String value, int lineNumber) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid salary '" + value + "' on line " + lineNumber);
        }
    }

    private Integer parseOptionalId(String value, int lineNumber) {
        if (value.isEmpty()) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid managerId '" + value + "' on line " + lineNumber);
        }
    }
}
