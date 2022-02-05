package org.unibl.etf.ar.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.unibl.etf.ar.ISASimulator;

public class DebugTest {

	@Test
	public void test() {
		try {
			ISASimulator.execute(Files.readAllLines(Paths.get("DebugTest.txt")));
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		assertEquals((long)-3, (long)ISASimulator.getRegisters().get("RAX"));
		assertEquals((long)2, (long)ISASimulator.getRegisters().get("RBX"));
	}

}
