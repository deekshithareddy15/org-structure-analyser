package com.bigcompany.util;

import com.bigcompany.model.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvEmployeeReaderTest {

    @TempDir
    Path tempDir;

    private Path writeCsv(String content) throws IOException {
        Path file = tempDir.resolve("employees.csv");
        Files.writeString(file, content);
        return file;
    }

    @Test
    void parsesValidFileCorrectly() throws IOException {
        Path csv = writeCsv(
                "Id,firstName,lastName,salary,managerId\n" +
                "123,Joe,Doe,60000,\n" +
                "124,Martin,Chekov,45000,123\n"
        );

        List<Employee> employees = new CsvEmployeeReader().read(csv.toString());
        assertEquals(2, employees.size());

        Employee ceo = employees.get(0);
        assertEquals(123, ceo.getId());
        assertEquals("Joe", ceo.getFirstName());
        assertEquals("Doe", ceo.getLastName());
        assertEquals(60000, ceo.getSalary(), 0.01);
        assertNull(ceo.getManagerId());

        Employee martin = employees.get(1);
        assertEquals(124, martin.getId());
        assertEquals(123, martin.getManagerId());
    }

    @Test
    void ignoresBlankLines() throws IOException {
        Path csv = writeCsv(
                "Id,firstName,lastName,salary,managerId\n" +
                "1,Joe,Doe,60000,\n" +
                "\n" +
                "2,Jane,Smith,40000,1\n"
        );
        assertEquals(2, new CsvEmployeeReader().read(csv.toString()).size());
    }

    @Test
    void emptyFileReturnsEmptyList() throws IOException {
        Path csv = writeCsv("Id,firstName,lastName,salary,managerId\n");
        assertTrue(new CsvEmployeeReader().read(csv.toString()).isEmpty());
    }

    @Test
    void throwsOnWrongColumnCount() throws IOException {
        Path csv = writeCsv(
                "Id,firstName,lastName,salary,managerId\n" +
                "1,Joe,Doe,60000\n" // missing managerId column
        );
        assertThrows(IllegalArgumentException.class,
                () -> new CsvEmployeeReader().read(csv.toString()));
    }

    @Test
    void throwsOnInvalidSalary() throws IOException {
        Path csv = writeCsv(
                "Id,firstName,lastName,salary,managerId\n" +
                "1,Joe,Doe,notANumber,\n"
        );
        assertThrows(IllegalArgumentException.class,
                () -> new CsvEmployeeReader().read(csv.toString()));
    }

    @Test
    void throwsOnInvalidId() throws IOException {
        Path csv = writeCsv(
                "Id,firstName,lastName,salary,managerId\n" +
                "abc,Joe,Doe,60000,\n"
        );
        assertThrows(IllegalArgumentException.class,
                () -> new CsvEmployeeReader().read(csv.toString()));
    }

    @Test
    void throwsOnFileNotFound() {
        assertThrows(IOException.class,
                () -> new CsvEmployeeReader().read("/nonexistent/path/employees.csv"));
    }
}
