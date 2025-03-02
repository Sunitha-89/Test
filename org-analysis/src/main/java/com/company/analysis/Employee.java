package com.company.analysis;

import java.util.Optional;

public class Employee {
	private final int id;
	private final String name;
	private final double salary;
	private final Optional<Integer> managerId;

	public Employee(int id, String name, double salary, Optional<Integer> managerId) {
		this.id = id;
		this.name = name;
		this.salary = salary;
		this.managerId = managerId;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public double getSalary() {
		return salary;
	}

	public Optional<Integer> getManagerId() {
		return managerId;
	}
}
