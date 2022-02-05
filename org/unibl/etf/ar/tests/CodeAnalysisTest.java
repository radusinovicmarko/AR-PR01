package org.unibl.etf.ar.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.unibl.etf.ar.*;

public class CodeAnalysisTest {

	@Test
	public void test() {
		try {
			ISASimulator.execute(Files.readAllLines(Paths.get("CodeAnalysisTest.txt")));
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		assertTrue(!ISASimulator.isValid());
	}

}
