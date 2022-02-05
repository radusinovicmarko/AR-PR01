package org.unibl.etf.ar;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Operators {
	
	//Map of unary operators
	private static final HashMap<String, Consumer<String>> unaryOperators = new HashMap<>();
	//Map of binary operators
	private static final HashMap<String, BiConsumer<String, String>> binaryOperators = new HashMap<>();
	
	//Variables for the result of CMP instruction
	private static boolean equalResult, lessResult;
	
	public static HashMap<String, Consumer<String>> getUnaryOperators() {
		return unaryOperators;
	}
	
	public static HashMap<String, BiConsumer<String, String>> getBinaryOperators() {
		return binaryOperators;
	}
	
	public static void jmp(String arg) {
		ISASimulator.setIndex(ISASimulator.getLabels().get(arg));
		if (ISASimulator.getIndex() < MachineCodeSimulator.getRipValues().size())
			ISASimulator.getRegisters().put("RIP", MachineCodeSimulator.getRipValues().get(ISASimulator.getIndex()));
		else
			ISASimulator.getRegisters().put("RIP", null);
	}
	
	public static void je(String arg) {
		if (equalResult) {
			ISASimulator.setIndex(ISASimulator.getLabels().get(arg));
			if (ISASimulator.getIndex() < MachineCodeSimulator.getRipValues().size())
				ISASimulator.getRegisters().put("RIP", MachineCodeSimulator.getRipValues().get(ISASimulator.getIndex()));
			else
				ISASimulator.getRegisters().put("RIP", null);
		}
	}
	
	public static void jne(String arg) {
		if (!equalResult) {
			ISASimulator.setIndex(ISASimulator.getLabels().get(arg));
			if (ISASimulator.getIndex() < MachineCodeSimulator.getRipValues().size())
				ISASimulator.getRegisters().put("RIP", MachineCodeSimulator.getRipValues().get(ISASimulator.getIndex()));
			else
				ISASimulator.getRegisters().put("RIP", null);
		}
	}
	
	public static void jge(String arg) {
		if (!lessResult) {
			ISASimulator.setIndex(ISASimulator.getLabels().get(arg));
			if (ISASimulator.getIndex() < MachineCodeSimulator.getRipValues().size())
				ISASimulator.getRegisters().put("RIP", MachineCodeSimulator.getRipValues().get(ISASimulator.getIndex()));
			else
				ISASimulator.getRegisters().put("RIP", null);
		}
	}
	
	public static void jl(String arg) {
		if (lessResult) {
			ISASimulator.setIndex(ISASimulator.getLabels().get(arg));
			if (ISASimulator.getIndex() < MachineCodeSimulator.getRipValues().size())
				ISASimulator.getRegisters().put("RIP", MachineCodeSimulator.getRipValues().get(ISASimulator.getIndex()));
			else
				ISASimulator.getRegisters().put("RIP", null);
		}
	}
	
	public static void not(String arg) {
		arg = arg.toUpperCase();
		putValue(arg, ~getValue(arg));
	}
	
	public static void add(String arg1, String arg2) {
		long result = getValue(arg1) + getValue(arg2);
		putValue(arg1, result);
	}
	
	public static void sub(String arg1, String arg2) {
		long result = getValue(arg1) - getValue(arg2);
		putValue(arg1, result);
	}
	
	public static void and(String arg1, String arg2) {
		long result = getValue(arg1) & getValue(arg2);
		putValue(arg1, result);
	}
	
	public static void or(String arg1, String arg2) {
		long result = getValue(arg1) | getValue(arg2);
		putValue(arg1, result);
	}
	
	public static void mov(String arg1, String arg2) {
		putValue(arg1, getValue(arg2));
	}
	
	//Check if input is a number, if true store it
	//if false, it is a string, store it as string
	public static void scan(String arg) {
		arg = arg.toUpperCase();
		String input = ISASimulator.scanner.nextLine();
		if (isNumber(input)) {
			long result = input.startsWith("0x") || input.startsWith("0X") ? Long.parseLong(input.substring(2), 16) : Long.parseLong(input);
			putValue(arg, result);
		} 
		else {
			if (!arg.startsWith("[")) {
				long result = 0;
				for (int i = 0; i < 8 && i < input.length(); i++)
					result = result * 100 + (int)input.charAt(i);
				putValue(arg, result);
			}
			else {
				putValue(arg, (int)input.charAt(0));
			}
		}
	}
	
	public static void print(String arg) {
		arg = arg.toUpperCase();
		System.out.println(getValue(arg));
	}
	
	public static void cmp(String arg1, String arg2) {
		long num1 = getValue(arg1), num2 = getValue(arg2);
		equalResult = num1 == num2;
		lessResult = num1 < num2;
	}
	
	private static long getValue(String arg) 
	{
		long result = 0;
		if (!arg.startsWith("[")) {
			if (ISASimulator.getRegisters().containsKey(arg))
				result = ISASimulator.getRegisters().get(arg);
			else if (arg.startsWith("0X"))
				result = Long.parseLong(arg.substring(2), 16);
			else
				result = Long.parseLong(arg);
		}
		else {
			arg = arg.substring(1, arg.length() - 1);
			if (ISASimulator.getRegisters().containsKey(arg))
				result = ISASimulator.getAddresses().get(ISASimulator.getRegisters().get(arg));
			else if (arg.startsWith("0X")) {
				long address = Long.parseLong(arg.substring(2), 16);
				result = ISASimulator.getAddresses().get(address);
			}
			else 
				result = ISASimulator.getAddresses().get(Long.parseLong(arg));
		}
		return result;
	}
	
	private static void putValue(String arg, long result)
	{
		if (!arg.startsWith("["))
			ISASimulator.getRegisters().put(arg, result);
		else {
			arg = arg.substring(1, arg.length() - 1);
			if (ISASimulator.getRegisters().containsKey(arg))
				ISASimulator.getAddresses().put(ISASimulator.getRegisters().get(arg), (byte)result);
			else if (arg.startsWith("0X")) {
				long address = Long.parseLong(arg.substring(2), 16);
				ISASimulator.getAddresses().put(address, (byte)result);
			}
			else
				ISASimulator.getAddresses().put(Long.parseLong(arg), (byte)result);
		}
	}
	
	public static boolean isNumber(String s) {
		try {
			Long.parseLong(s);
			return true;
		} catch(NumberFormatException e) {
			try {
				s = s.substring(2);
				Long.parseLong(s, 16);
				return true;
			} catch (NumberFormatException e1) {
				return false;
			}
		}
	}
}
