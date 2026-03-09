package com.bigcompany;

import com.bigcompany.model.Employee;
import com.bigcompany.service.OrgAnalyzer;
import com.bigcompany.service.OrgAnalyzer.ReportingLineIssue;
import com.bigcompany.service.OrgAnalyzer.SalaryIssue;
import com.bigcompany.util.CsvEmployeeReader;

import java.util.List;

public class Main {

    private static final String DEFAULT_CSV_PATH = "src/main/resources/employees.csv";

    public static void main(String[] args) {
        String csvPath = (args.length > 0) ? args[0] : DEFAULT_CSV_PATH;

        System.out.println("Reading employees from: " + csvPath);

        List<Employee> employees;
        try {
            employees = new CsvEmployeeReader().read(csvPath);
        } catch (Exception e) {
            System.err.println("ERROR: Could not read file – " + e.getMessage());
            System.exit(1);
            return;
        }

        if (employees.isEmpty()) {
            System.out.println("No employees found in file.");
            return;
        }

        OrgAnalyzer analyzer = new OrgAnalyzer(employees);

        printUnderpaidManagers(analyzer.findUnderpaidManagers());
        printOverpaidManagers(analyzer.findOverpaidManagers());
        printLongReportingLines(analyzer.findLongReportingLines());
    }

    private static void printUnderpaidManagers(List<SalaryIssue> issues) {
        System.out.println("\n=== Managers earning less than they should ===");
        if (issues.isEmpty()) {
            System.out.println("  None.");
        } else {
            for (SalaryIssue issue : issues) {
                System.out.printf("  %s (ID %d) earns %.2f less than the required minimum%n",
                        issue.manager().getFullName(),
                        issue.manager().getId(),
                        issue.difference());
            }
        }
    }

    private static void printOverpaidManagers(List<SalaryIssue> issues) {
        System.out.println("\n=== Managers earning more than they should ===");
        if (issues.isEmpty()) {
            System.out.println("  None.");
        } else {
            for (SalaryIssue issue : issues) {
                System.out.printf("  %s (ID %d) earns %.2f more than the allowed maximum%n",
                        issue.manager().getFullName(),
                        issue.manager().getId(),
                        issue.difference());
            }
        }
    }

    private static void printLongReportingLines(List<ReportingLineIssue> issues) {
        System.out.println("\n=== Employees with a reporting line that is too long ===");
        if (issues.isEmpty()) {
            System.out.println("  None.");
        } else {
            for (ReportingLineIssue issue : issues) {
                System.out.printf("  %s (ID %d) has %d managers above them (%d too many)%n",
                        issue.employee().getFullName(),
                        issue.employee().getId(),
                        issue.managersAbove(),
                        issue.excess());
            }
        }
        System.out.println();
    }
}
