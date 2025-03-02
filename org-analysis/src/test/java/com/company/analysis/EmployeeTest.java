package com.company.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

public class EmployeeTest {

	@Test
	public void testEmployeeCreation() {
		Employee employee = new Employee(1, "John Doe", 50000.0, Optional.of(2));
		assertEquals(1, employee.getId());
		assertEquals("John Doe", employee.getName());
		assertEquals(50000.0, employee.getSalary(), 0.001);
		assertTrue(employee.getManagerId().isPresent());
		assertEquals(Integer.valueOf(2), employee.getManagerId().get());
	}

	@Test
	public void testEmployeeWithoutManager() {
		Employee employee = new Employee(2, "Jane Doe", 60000.0, Optional.empty());
		assertEquals(2, employee.getId());
		assertEquals("Jane Doe", employee.getName());
		assertEquals(60000.0, employee.getSalary(), 0.001);
		assertFalse(employee.getManagerId().isPresent());
	}
}
