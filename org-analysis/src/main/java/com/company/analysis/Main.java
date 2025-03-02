package com.company.analysis;

import java.io.IOException;
import java.util.List;

public class Main {
	public static void main(String[] args) {
		try {
			List<Employee> employees = EmployeeParser.parseCSV("src/main/resources/employees.csv");
			OrgAnalyzer analyzer = new OrgAnalyzer(employees);
			analyzer.analyze();
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}
	}
}
