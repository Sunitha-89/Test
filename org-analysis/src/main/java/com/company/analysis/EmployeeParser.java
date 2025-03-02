package com.company.analysis;

import java.io.*;
import java.util.*;

public class EmployeeParser {
	public static List<Employee> parseCSV(String filePath) throws IOException {
		List<Employee> employees = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			br.readLine(); // Skip header
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",");
				int id = Integer.parseInt(parts[0]);
				String name = parts[1];
				double salary = Double.parseDouble(parts[2]);
				Optional<Integer> managerId = parts.length > 3 && !parts[3].isEmpty()
						? Optional.of(Integer.parseInt(parts[3]))
						: Optional.empty();

				employees.add(new Employee(id, name, salary, managerId));
			}
		}
		return employees;
	}
}
