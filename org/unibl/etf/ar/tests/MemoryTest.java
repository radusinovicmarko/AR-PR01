package org.unibl.etf.ar.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.unibl.etf.ar.ISASimulator;

public class MemoryTest {

	@Test
	public void test() {
		try {
			ISASimulator.execute(Files.readAllLines(Paths.get("MemoryTest.txt")));
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		assertEquals((long)2, (long)ISASimulator.getRegisters().get("RAX"));
		assertEquals((long)9, (long)ISASimulator.getRegisters().get("RBX"));
	}

}
