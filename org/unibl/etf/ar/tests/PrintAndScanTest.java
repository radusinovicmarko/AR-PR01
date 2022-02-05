package org.unibl.etf.ar.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.unibl.etf.ar.ISASimulator;

public class PrintAndScanTest {

	@Test
	public void test() {
		try {
			ISASimulator.execute(Files.readAllLines(Paths.get("PrintAndScanTest.txt")));
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		//15 and 10 as input
		assertEquals((long)10, (long)ISASimulator.getRegisters().get("RAX"));
	}

}
