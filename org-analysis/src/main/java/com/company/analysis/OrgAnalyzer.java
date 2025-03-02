package com.company.analysis;

import java.util.*;
import java.util.stream.Collectors;

public class OrgAnalyzer {
	private final Map<Integer, Employee> employeeMap;
	private final Map<Integer, List<Employee>> managerToEmployees = new HashMap<>();

	public OrgAnalyzer(List<Employee> employees) {
		this.employeeMap = employees.stream().collect(Collectors.toMap(Employee::getId, e -> e));
		for (Employee emp : employees) {
			emp.getManagerId().ifPresent(
					managerId -> managerToEmployees.computeIfAbsent(managerId, k -> new ArrayList<>()).add(emp));
		}
	}

	public void analyze() {
		checkManagerSalaries();
		checkReportingDepth();
	}

	private void checkManagerSalaries() {
		for (Map.Entry<Integer, List<Employee>> entry : managerToEmployees.entrySet()) {
			int managerId = entry.getKey();
			Employee manager = employeeMap.get(managerId);
			List<Employee> subordinates = entry.getValue();

			double avgSubordinateSalary = subordinates.stream().mapToDouble(Employee::getSalary).average().orElse(0);

			double minSalary = avgSubordinateSalary * 1.2;
			double maxSalary = avgSubordinateSalary * 1.5;

			if (manager.getSalary() < minSalary) {
				System.out.printf("Manager %s earns %.2f less than minimum allowed salary.\n", manager.getName(),
						minSalary - manager.getSalary());
			}
			if (manager.getSalary() > maxSalary) {
				System.out.printf("Manager %s earns %.2f more than maximum allowed salary.\n", manager.getName(),
						manager.getSalary() - maxSalary);
			}
		}
	}

	private void checkReportingDepth() {
		for (Employee employee : employeeMap.values()) {
			int depth = getDepth(employee);
			if (depth > 4) {
				System.out.printf("Employee %s has a reporting depth of %d (exceeds max by %d levels).\n",
						employee.getName(), depth, depth - 4);
			}
		}
	}

	protected int getDepth(Employee employee) {
		int depth = 0;
		Optional<Integer> managerId = employee.getManagerId();
		while (managerId.isPresent()) {
			depth++;
			managerId = employeeMap.get(managerId.get()).getManagerId();
		}
		return depth;
	}
}
