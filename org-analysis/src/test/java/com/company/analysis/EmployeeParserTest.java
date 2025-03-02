package com.company.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmployeeParserTest {

	private static final String TEST_FILE = "test_employees.csv";

	@BeforeEach
	void setUp() throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_FILE))) {
			writer.write("id,name,salary,managerId\n"); // Header
			writer.write("1,Alice,50000,\n"); // No manager
			writer.write("2,Bob,60000,1\n"); // Alice is manager
			writer.write("3,Charlie,55000,1\n");
		}
	}

	@AfterEach
	void tearDown() {
		new File(TEST_FILE).delete();
	}

	@Test
	void testParseCSV_ValidFile() throws IOException {
		List<Employee> employees = EmployeeParser.parseCSV(TEST_FILE);
		assertEquals(3, employees.size());

		assertEquals(1, employees.get(0).getId());
		assertEquals("Alice", employees.get(0).getName());
		assertEquals(50000, employees.get(0).getSalary());
		assertFalse(employees.get(0).getManagerId().isPresent());

		assertEquals(2, employees.get(1).getId());
		assertEquals("Bob", employees.get(1).getName());
		assertEquals(60000, employees.get(1).getSalary());
		assertTrue(employees.get(1).getManagerId().isPresent());
		assertEquals(1, employees.get(1).getManagerId().get());
	}

	@Test
	void testParseCSV_MalformedData() throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_FILE))) {
			writer.write("id,name,salary,managerId\n");
			writer.write("4,Dave,not_a_number,2\n");
		}

		assertThrows(NumberFormatException.class, () -> EmployeeParser.parseCSV(TEST_FILE));
	}
}
