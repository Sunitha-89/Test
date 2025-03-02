package com.company.analysis;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class OrgAnalyzerTest {
	private OrgAnalyzer orgAnalyzer;

	@Before
	public void setUp() {
		List<Employee> employees = Arrays.asList(new Employee(1, "Alice", 100000, Optional.empty()),
				new Employee(2, "Bob", 70000, Optional.of(1)), new Employee(3, "Charlie", 80000, Optional.of(1)),
				new Employee(4, "David", 50000, Optional.of(2)), new Employee(5, "Eve", 60000, Optional.of(2)),
				new Employee(6, "Frank", 40000, Optional.of(4)), new Employee(7, "Grace", 30000, Optional.of(6)));
		orgAnalyzer = new OrgAnalyzer(employees);
	}

	@Test
	public void testReportingDepth() throws Exception {
		Employee employee = new Employee(7, "Grace", 30000, Optional.of(6));
		int depth = orgAnalyzer.getDepth(employee); // Assuming getDepth is made public or accessed via reflection
		assertEquals(4, depth);
	}
}
