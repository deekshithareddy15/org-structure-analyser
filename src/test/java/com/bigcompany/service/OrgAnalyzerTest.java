package com.bigcompany.service;

import com.bigcompany.model.Employee;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrgAnalyzerTest {

    private List<Employee> sampleEmployees() {
        return Arrays.asList(
                new Employee(123, "Joe",    "Doe",      60000, null),
                new Employee(124, "Martin", "Chekov",   45000, 123),
                new Employee(125, "Bob",    "Ronstad",  47000, 123),
                new Employee(300, "Alice",  "Hasacat",  50000, 124),
                new Employee(305, "Brett",  "Hardleaf", 34000, 300)
        );
    }


    @Test
    void sampleData_onlyMartinIsUnderpaid() {
        List<OrgAnalyzer.SalaryIssue> issues = new OrgAnalyzer(sampleEmployees()).findUnderpaidManagers();

        assertEquals(1, issues.size());
        assertEquals(124, issues.get(0).manager().getId());
        assertEquals(15000.0, issues.get(0).difference(), 0.01);
        assertTrue(issues.get(0).earningTooLittle());
    }

    @Test
    void managerExactlyAtMinimumIsNotFlagged() {
        // avg sub = 50000 → minimum = 60000; manager earns exactly 60000
        List<Employee> employees = Arrays.asList(
                new Employee(1, "CEO", "X", 100000, null),
                new Employee(2, "Mgr", "X",  60000, 1),
                new Employee(3, "Sub", "X",  50000, 2)
        );
        assertTrue(new OrgAnalyzer(employees).findUnderpaidManagers().isEmpty());
    }

    @Test
    void managerOneUnitBelowMinimumIsFlagged() {
        List<Employee> employees = Arrays.asList(
                new Employee(1, "CEO", "X", 100000, null),
                new Employee(2, "Mgr", "X",  59999, 1), // min = 60000
                new Employee(3, "Sub", "X",  50000, 2)
        );
        List<OrgAnalyzer.SalaryIssue> issues = new OrgAnalyzer(employees).findUnderpaidManagers();
        assertEquals(1, issues.size());
        assertEquals(1.0, issues.get(0).difference(), 0.01);
    }

    @Test
    void averageSalaryUsedAcrossMultipleSubordinates() {
        // subs earn 30000 and 50000 → avg = 40000, min = 48000
        List<Employee> employees = Arrays.asList(
                new Employee(1, "CEO",  "X", 200000, null),
                new Employee(2, "Mgr",  "X",  48000, 1),
                new Employee(3, "SubA", "X",  30000, 2),
                new Employee(4, "SubB", "X",  50000, 2)
        );
        assertTrue(new OrgAnalyzer(employees).findUnderpaidManagers().isEmpty());
    }

    @Test
    void sampleData_noOverpaidManagers() {
        assertTrue(new OrgAnalyzer(sampleEmployees()).findOverpaidManagers().isEmpty());
    }

    @Test
    void overpaidManagerIsDetected() {
        // avg sub = 50000 → maximum = 75000; manager earns 80000 → excess = 5000
        List<Employee> employees = Arrays.asList(
                new Employee(1, "CEO", "X", 200000, null),
                new Employee(2, "Mgr", "X",  80000, 1),
                new Employee(3, "Sub", "X",  50000, 2)
        );
        List<OrgAnalyzer.SalaryIssue> issues = new OrgAnalyzer(employees).findOverpaidManagers();
        assertEquals(1, issues.size());
        assertEquals(2, issues.get(0).manager().getId());
        assertEquals(5000.0, issues.get(0).difference(), 0.01);
        assertFalse(issues.get(0).earningTooLittle());
    }

    @Test
    void managerExactlyAtMaximumIsNotFlagged() {
        // avg sub = 50000 → maximum = 75000; manager earns exactly 75000
        List<Employee> employees = Arrays.asList(
                new Employee(1, "CEO", "X", 200000, null),
                new Employee(2, "Mgr", "X",  75000, 1),
                new Employee(3, "Sub", "X",  50000, 2)
        );
        assertTrue(new OrgAnalyzer(employees).findOverpaidManagers().isEmpty());
    }


    @Test
    void sampleData_noLongReportingLines() {
        // deepest is Brett at depth 3 (CEO → Martin → Alice → Brett)
        assertTrue(new OrgAnalyzer(sampleEmployees()).findLongReportingLines().isEmpty());
    }

    @Test
    void employeeAtDepthFiveIsFlagged() {
        // chain 1→2→3→4→5→6: employee 6 has 5 managers above, excess = 1
        List<Employee> employees = Arrays.asList(
                new Employee(1, "A", "X", 100000, null),
                new Employee(2, "B", "X",  80000, 1),
                new Employee(3, "C", "X",  60000, 2),
                new Employee(4, "D", "X",  50000, 3),
                new Employee(5, "E", "X",  40000, 4),
                new Employee(6, "F", "X",  30000, 5)
        );
        List<OrgAnalyzer.ReportingLineIssue> issues = new OrgAnalyzer(employees).findLongReportingLines();
        assertEquals(1, issues.size());
        assertEquals(6, issues.get(0).employee().getId());
        assertEquals(5, issues.get(0).managersAbove());
        assertEquals(1, issues.get(0).excess());
    }

    @Test
    void employeeAtDepthFourIsNotFlagged() {
        List<Employee> employees = Arrays.asList(
                new Employee(1, "A", "X", 100000, null),
                new Employee(2, "B", "X",  80000, 1),
                new Employee(3, "C", "X",  60000, 2),
                new Employee(4, "D", "X",  50000, 3),
                new Employee(5, "E", "X",  40000, 4)
        );
        assertTrue(new OrgAnalyzer(employees).findLongReportingLines().isEmpty());
    }

    @Test
    void multipleEmployeesBeyondLimitAreAllFlagged() {
        // CEO→M1→M2→M3→M4→E5a, E5b (both at depth 5)
        List<Employee> employees = Arrays.asList(
                new Employee(1, "CEO", "X", 200000, null),
                new Employee(2, "M1",  "X",  90000, 1),
                new Employee(3, "M2",  "X",  70000, 2),
                new Employee(4, "M3",  "X",  55000, 3),
                new Employee(5, "M4",  "X",  45000, 4),
                new Employee(6, "E5a", "X",  35000, 5),
                new Employee(7, "E5b", "X",  35000, 5)
        );
        assertEquals(2, new OrgAnalyzer(employees).findLongReportingLines().size());
    }


    @Test
    void ceoAloneProducesNoIssues() {
        List<Employee> employees = List.of(
                new Employee(1, "Solo", "CEO", 100000, null)
        );
        OrgAnalyzer analyzer = new OrgAnalyzer(employees);
        assertTrue(analyzer.findUnderpaidManagers().isEmpty());
        assertTrue(analyzer.findOverpaidManagers().isEmpty());
        assertTrue(analyzer.findLongReportingLines().isEmpty());
    }
}
