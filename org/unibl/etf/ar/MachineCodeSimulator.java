package org.unibl.etf.ar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MachineCodeSimulator {

	
	//Locations in memory storing machine code
	private static final ArrayList<Long> ripValues = new ArrayList<>();
	
	//OpCodes for instructions
	private static final HashMap<Byte, String> opCodes = new HashMap<>();
	
	public static ArrayList<Long> getRipValues() {
		return ripValues;
	}
	
	public static HashMap<Byte, String> getOpCodes() {
		return opCodes;
	}
	
	public static void translateToMachineCode(long address) {
		//Translation to machine code
		for (String line : ISASimulator.getCode()) {
			line = line.trim();
			int index = line.indexOf(' ');
			if (index == -1 && ISASimulator.getKeywords().contains(line.toUpperCase())) {
				ISASimulator.getAddresses().put(address, getKeyForValue(line.toUpperCase()));
				ripValues.add(address++);
				continue;
			}
			else if (index == -1 && !ISASimulator.getKeywords().contains(line.toUpperCase()))
				continue;
			else if (index != -1)
				ISASimulator.getAddresses().put(address++, getKeyForValue(line.split(" ")[0].toUpperCase()));
			line = line.substring(index + 1);
			byte[] arr = line.replace(" ", "").getBytes();
			ripValues.add(address - 1);
			for (byte b : arr) 
				ISASimulator.getAddresses().put(address++, b);
			ISASimulator.getAddresses().put(address++, (byte)0);
		}
		ISASimulator.getRegisters().put("RIP", ripValues.get(0));
	}
	
	private static Byte getKeyForValue(String value) {
		for (Map.Entry<Byte, String> entry : opCodes.entrySet())
			if (entry.getValue().equals(value))
				return entry.getKey();
		return null;
	}
	
	public static void machineCodeExec() {
		for (; ; ISASimulator.setIndex(ISASimulator.getIndex() + 1)) {
			//1 Fetch
			if (ISASimulator.getAddresses().get(ISASimulator.getRegisters().get("RIP")) == null) 
				return;
			long address = ISASimulator.getRegisters().get("RIP");
			//2 Decode
			String instruction = opCodes.get(ISASimulator.getAddresses().get(address++));
			//3 Fetch operands
			StringBuilder sb = new StringBuilder();
			if (!ISASimulator.breakpoint.equals(instruction))
				while (ISASimulator.getAddresses().get(address) != 0)
					sb.append((char)(int)ISASimulator.getAddresses().get(address++));
			ISASimulator.getRegisters().put("RIP", address + 1);
			String operands = sb.toString();
			//4 Execute
			if (ISASimulator.breakpoint.equals(instruction)) {
				ISASimulator.setDebuggingMode(true);
				ISASimulator.debug();
				ISASimulator.getRegisters().put("RIP", address);
				continue;
			} 
			else if (ISASimulator.switchToMachineCodeExec.equals(instruction))
				return;
			else if (Operators.getUnaryOperators().containsKey(instruction)) 
				Operators.getUnaryOperators().get(instruction).accept(operands);
			else if (Operators.getBinaryOperators().containsKey(instruction))
				Operators.getBinaryOperators().get(instruction).accept(operands.split(",")[0].toUpperCase(), operands.split(",")[1].toUpperCase());
			if (ISASimulator.getDebuggingMode())
				ISASimulator.debug();
		}
	}
}
