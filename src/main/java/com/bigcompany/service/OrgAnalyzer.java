package com.bigcompany.service;

import com.bigcompany.model.Employee;

import java.util.*;

public class OrgAnalyzer {

    private static final double MIN_SALARY_FACTOR = 1.20;
    private static final double MAX_SALARY_FACTOR = 1.50;
    private static final int    MAX_MANAGERS_ABOVE = 4;

    public record SalaryIssue(Employee manager, double difference, boolean earningTooLittle) {

    }

    public record ReportingLineIssue(Employee employee, int managersAbove) {
        public int excess() {
            return managersAbove - MAX_MANAGERS_ABOVE;
        }
    }

    private final Map<Integer, Employee>       employeesById;
    private final Map<Integer, List<Employee>> subordinates; // managerId → direct reports

    public OrgAnalyzer(List<Employee> employees) {
        employeesById = new HashMap<>();
        subordinates  = new HashMap<>();

        for (Employee e : employees) {
            employeesById.put(e.getId(), e);
        }
        for (Employee e : employees) {
            if (e.getManagerId() != null) {
                subordinates
                        .computeIfAbsent(e.getManagerId(), k -> new ArrayList<>())
                        .add(e);
            }
        }
    }

    public List<SalaryIssue> findUnderpaidManagers() {
        List<SalaryIssue> issues = new ArrayList<>();

        for (Map.Entry<Integer, List<Employee>> entry : subordinates.entrySet()) {
            Employee manager    = employeesById.get(entry.getKey());
            if (manager == null) continue;

            double avgSubSalary = averageSalary(entry.getValue());
            double minimum      = avgSubSalary * MIN_SALARY_FACTOR;

            if (manager.getSalary() < minimum) {
                issues.add(new SalaryIssue(manager, minimum - manager.getSalary(), true));
            }
        }

        issues.sort(Comparator.comparingInt(i -> i.manager().getId()));
        return issues;
    }

    public List<SalaryIssue> findOverpaidManagers() {
        List<SalaryIssue> issues = new ArrayList<>();

        for (Map.Entry<Integer, List<Employee>> entry : subordinates.entrySet()) {
            Employee manager    = employeesById.get(entry.getKey());
            if (manager == null) continue;

            double avgSubSalary = averageSalary(entry.getValue());
            double maximum      = avgSubSalary * MAX_SALARY_FACTOR;

            if (manager.getSalary() > maximum) {
                issues.add(new SalaryIssue(manager, manager.getSalary() - maximum, false));
            }
        }

        issues.sort(Comparator.comparingInt(i -> i.manager().getId()));
        return issues;
    }

    public List<ReportingLineIssue> findLongReportingLines() {
        List<ReportingLineIssue> issues = new ArrayList<>();

        Employee ceo = findCeo();
        if (ceo == null) return issues;

        Queue<Employee>       queue = new LinkedList<>();
        Map<Integer, Integer> depth = new HashMap<>();

        queue.add(ceo);
        depth.put(ceo.getId(), 0);

        while (!queue.isEmpty()) {
            Employee current      = queue.poll();
            int      currentDepth = depth.get(current.getId());

            for (Employee report : subordinates.getOrDefault(current.getId(), Collections.emptyList())) {
                int reportDepth = currentDepth + 1;
                depth.put(report.getId(), reportDepth);
                queue.add(report);

                if (reportDepth > MAX_MANAGERS_ABOVE) {
                    issues.add(new ReportingLineIssue(report, reportDepth));
                }
            }
        }

        issues.sort(Comparator.comparingInt(i -> i.employee().getId()));
        return issues;
    }

    private Employee findCeo() {
        return employeesById.values().stream()
                .filter(e -> e.getManagerId() == null)
                .findFirst()
                .orElse(null);
    }

    private double averageSalary(List<Employee> employees) {
        return employees.stream()
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0);
    }
}
