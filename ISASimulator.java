package org.unibl.etf.ar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class ISASimulator {
	
	//Index for the interpretation of the code
	public static int i = 0;
	//Index for starting execution
	public static int startExec = 0;
	
	public static Scanner scanner = new Scanner(System.in);

	//Registers
	public static final HashMap<String, Long> registers = new HashMap<>();
	//Memory addresses in address space
	public static final HashMap<Long, Byte> addresses = new HashMap<>();
	
	//Set of keywords
	public static final HashSet<String> keywords = new HashSet<>();
	//Map of unary operators
	public static final HashMap<String, Consumer<String>> unaryOperators = new HashMap<>();
	//Map of binary operators
	public static final HashMap<String, BiConsumer<String, String>> binaryOperators = new HashMap<>();
	
	//Keywords for debugging mode
	public static final String breakpoint = "BREAKPOINT";
	public static final String next = "NEXT";
	public static final String cont = "CONTINUE";
	
	//Debugging mode execution
	public static boolean debuggingMode = false;
	
	//Map of labels for jumps
	public static final HashMap<String, Integer> labels = new HashMap<>();
	
	//Variables for the result of CMP instruction
	public static boolean equalResult, lessResult;
	
	//Code to interpret
	public static List<String> code;
	//Validity of the code
	public static boolean isValid = true;
	//List of errors
	public static final ArrayList<String> errorList = new ArrayList<>();
	
	//Locations in memory storing machine code
	public static final ArrayList<Long> ripValues = new ArrayList<>();
	//OpCodes for instructions
	public static final HashMap<Byte, String> opCodes = new HashMap<>();
	
	//Instruction for switching to machine code execution
	public static final String switchToMachineCodeExec = "SWITCH";
	
	//Self-modifying code
	public static boolean selfModifyingCode = false;
	//Address to modify
	private static long modifyingAddress = 0;
	
	public static void setRegisters() {
		registers.put("RAX", (long)0);
		registers.put("RBX", (long)0);
		registers.put("RCX", (long)0);
		registers.put("RDX", (long)0);
		registers.put("RSI", (long)0);
		registers.put("RDI", (long)0);
		registers.put("RIP", (long)0);
	}
	
	public static void setKeywordsAndOperators() {
		unaryOperators.put("NOT", ISASimulator::not);
		unaryOperators.put("JMP", ISASimulator::jmp);
		unaryOperators.put("JE", ISASimulator::je);
		unaryOperators.put("JNE", ISASimulator::jne);
		unaryOperators.put("JGE", ISASimulator::jge);
		unaryOperators.put("JL", ISASimulator::jl);
		unaryOperators.put("PRINT", ISASimulator::print);
		unaryOperators.put("SCAN", ISASimulator::scan);
		binaryOperators.put("ADD", ISASimulator::add);
		binaryOperators.put("SUB", ISASimulator::sub);
		binaryOperators.put("AND", ISASimulator::and);
		binaryOperators.put("OR", ISASimulator::or);
		binaryOperators.put("MOV", ISASimulator::mov);
		binaryOperators.put("CMP", ISASimulator::cmp);
		keywords.addAll(unaryOperators.keySet());
		keywords.addAll(binaryOperators.keySet());
		keywords.add(breakpoint);
		keywords.add(switchToMachineCodeExec);
	}
	
	public static void setOpCodes() {
		byte i = 0;
		for (String keyword : keywords)
			opCodes.put(i++, keyword);
	}
	
	public static void codeValidation() {
		//Syntax analysis
		for (int i = 0; i < code.size(); i++) {
			String s = code.get(i);
			s = s.trim();
			int index = s.indexOf(' ');
			if (index == -1 && breakpoint.equals(s.toUpperCase()))
				continue;
			if (index == -1 && !keywords.contains(s.toUpperCase()) && !s.endsWith(":"))
			{
				errorList.add(s);
				isValid = false; 
			}
			else if (index == -1 && s.endsWith(":"))
				labels.put(s.substring(0, s.length() - 1), i);
			else 
			{
				String keyword = s.split(" ")[0];
				if (!keywords.contains(keyword.toUpperCase()))
				{
					errorList.add(keyword);
					isValid = false;
				}
			}
		}
		
		if (!isValid)
			return;
		
		//Semantic analysis
		//Check for number of operands
		//Check for validity of operands
		//TODO - Check if a number is the first operand
		code.stream().forEach(s -> {
			s = s.trim();
			int index = s.indexOf(' ');
			if (index == -1)
				return;
			String keyword = s.split(" ")[0].toUpperCase();
			String[] operands = s.substring(index + 1).replaceAll(" ", "").split(",");
			if (unaryOperators.containsKey(keyword) && operands.length != 1)
			{
				isValid = false;
				errorList.add(s);
			}
			else if (binaryOperators.containsKey(keyword) && operands.length != 2)
			{
				isValid = false;
				errorList.add(s);
			}
			Arrays.asList(operands).stream().forEach(o -> {
				String oprnd = o.toUpperCase();
				if (!oprnd.startsWith("[") && !registers.containsKey(oprnd) && !isNumber(oprnd) && !labels.containsKey(o))
				{
					isValid = false;
					errorList.add(o);
				}
				else if (isNumber(oprnd))
				{
					if (!oprnd.startsWith("0X"))
						addresses.put(Long.parseLong(oprnd), (byte)0);
					else
						addresses.put(Long.parseLong(oprnd.substring(2), 16), (byte)0);
				}
				else if (oprnd.startsWith("["))
				{
					if (!oprnd.endsWith("]"))
					{
						isValid = false;
						errorList.add(o);
						return;
					}
					String address = oprnd.substring(1, oprnd.length() - 1);
					/*if (!registers.containsKey(address) && !memories.containsKey(address) &&
							(!address.startsWith("0x") || address.startsWith("0x") && !addresses.containsKey(Long.parseLong(address, 16))))
					{
						isValid = false;
						errorList.add(o);
					}*/
					if (registers.containsKey(address))
						return;
					if (!isNumber(address))
					{
						isValid = false;
						errorList.add(address);
					}
					else if (!address.startsWith("0X"))
						addresses.put(Long.parseLong(address), (byte)0);
					else
						addresses.put(Long.parseLong(address.substring(2), 16), (byte)0);
				}
			});
		});
	}
	
	public static void jmp(String arg) {
		i = labels.get(arg);
		registers.put("RIP", ripValues.get(i));
	}
	
	public static void je(String arg) {
		if (equalResult) {
			i = labels.get(arg);
			registers.put("RIP", ripValues.get(i));
		}
	}
	
	public static void jne(String arg) {
		if (!equalResult) {
			i = labels.get(arg);
			registers.put("RIP", ripValues.get(i));
		}
	}
	
	public static void jge(String arg) {
		if (!lessResult) {
			i = labels.get(arg);
			registers.put("RIP", ripValues.get(i));
		}
	}
	
	public static void jl(String arg) {
		if (lessResult) {
			i = labels.get(arg);
			registers.put("RIP", ripValues.get(i));
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
	
	//TODO
	//Check if input is a number, if true store it
	//if false, it is a string, store it as string
	public static void scan(String arg) {
		arg = arg.toUpperCase();
		String input = scanner.nextLine();
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
	
	public static void interpretCode() {
		for (i = 0; i < code.size(); i++) {
			String line = code.get(i);
			line = line.trim();
			int index = line.indexOf(' ');
			if (index == -1) {
				//if (!breakpoint.equals(line.toUpperCase()))
					//continue;
				if (breakpoint.equals(line.toUpperCase())) {
					debuggingMode = true;
					debug();
					continue;
				}
				else if (switchToMachineCodeExec.equals(line.toUpperCase())) {
					i++;
					registers.put("RIP", ripValues.get(i));
					machineCodeExec();
					continue;
				}
				else
					continue;
			}
			String operator = line.split(" ")[0];
			operator = operator.toUpperCase();
			String args1 = line.substring(line.indexOf(' '));
			args1 = args1.replaceAll(" ", "");
			String args = args1.toUpperCase();
			if (unaryOperators.containsKey(operator))
				unaryOperators.get(operator).accept(args1);
			else if (binaryOperators.containsKey(operator)) {
				String arg1 = args.split(",")[0], arg2 = args.split(",")[1];
				binaryOperators.get(operator).accept(arg1, arg2);
			}
			if (debuggingMode)
				debug();
			//registers.keySet().stream().forEach(s -> System.out.println(s + " " + registers.get(s)));
			//System.out.println();
		}
	}
	
	public static void debug() {
		System.out.println();
		registers.keySet().stream().forEach(reg -> System.out.println(reg + " " + registers.get(reg)));
		System.out.println("Enter memory address for examination or NEXT or CONTINUE:");
		String input = "";
		do {
			input = scanner.nextLine();
			if (isNumber(input)) {
				if (input.startsWith("0x") || input.startsWith("0X")) {
					long address = Long.parseLong(input.substring(2), 16);
					System.out.println(input + ": " + (addresses.containsKey(address) ? addresses.get(address) : 0));
				}
				else {
					long address = Long.parseLong(input);
					System.out.println(input + ": " + (addresses.containsKey(address) ? addresses.get(address) : 0));
				}
			}
			else if (next.equals(input.toUpperCase()))
				return;
			else if (cont.equals(input.toUpperCase())) {
				debuggingMode = false;
				return;
			}
			else 
				System.err.println("Invalid command or memory address!");
		} while (true);
	}
	
	public static void translateToMachineCode(long address) {
		//Translation to machine code
		for (String line : code) {
			line = line.trim();
			int index = line.indexOf(' ');
			if (index == -1 && keywords.contains(line.toUpperCase())) {
				addresses.put(address, getKeyForValue(line.toUpperCase()));
				ripValues.add(address++);
				continue;
			}
			else if (index == -1 && !keywords.contains(line.toUpperCase()))
				continue;
			else if (index != -1) {
				addresses.put(address++, getKeyForValue(line.split(" ")[0].toUpperCase()));
				if ("ADD".equals(line.split(" ")[0].toUpperCase()))
					modifyingAddress = address - 1;
			}
			line = line.substring(index + 1);
			byte[] arr = line.replace(" ", "").getBytes();
			ripValues.add(address - 1);
			for (byte b : arr) 
				addresses.put(address++, b);
			addresses.put(address++, (byte)0);
		}
		registers.put("RIP", ripValues.get(0));
	}
	
	private static Byte getKeyForValue(String value) {
		for (Map.Entry<Byte, String> entry : opCodes.entrySet())
			if (entry.getValue().equals(value))
				return entry.getKey();
		return null;
	}
	
	public static void machineCodeExec() {
		for (; ; i++) {
			//1 Fetch
			if (addresses.get(registers.get("RIP")) == null) {
				if (!selfModifyingCode)
					return;
				addresses.put(modifyingAddress, getKeyForValue("SUB"));
				registers.put("RIP", modifyingAddress);
				selfModifyingCode = false;
			}
			long address = registers.get("RIP");
			//2 Decode
			String instruction = opCodes.get(addresses.get(address++));
			//3 Fetch operands
			StringBuilder sb = new StringBuilder();
			while (addresses.get(address) != 0)
				sb.append((char)(int)addresses.get(address++));
			registers.put("RIP", address + 1);
			String operands = sb.toString();
			//4 Execute
			if (breakpoint.equals(instruction)) {
				debuggingMode = true;
				debug();
				continue;
			} 
			else if (switchToMachineCodeExec.equals(instruction))
				return;
			else if (unaryOperators.containsKey(instruction)) 
				unaryOperators.get(instruction).accept(operands);
			else if (binaryOperators.containsKey(instruction))
				binaryOperators.get(instruction).accept(operands.split(",")[0].toUpperCase(), operands.split(",")[1].toUpperCase());
			if (debuggingMode)
				debug();
		}
	}
	
	public static void main(String[] args) {
		setRegisters();
		setKeywordsAndOperators();
		setOpCodes();
		if (args.length == 0)
		{
			System.err.println("Missing an argument.");
			return;
		}
		try {
			code = Files.readAllLines(Path.of(args[0]));
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return;
		}
		if (args.length == 2 && "true".equals(args[1])) {
			selfModifyingCode = true;
		}
		
		codeValidation();
		if (!isValid)
		{
			System.err.println("Code not valid.");
			System.err.println("Errors:");
			errorList.stream().forEach(s -> System.err.println(s));
			return;
		}
		
		translateToMachineCode(0x100);
		interpretCode();
		scanner.close();
	}
	
	private static long getValue(String arg) 
	{
		long result = 0;
		if (!arg.startsWith("[")) {
			if (registers.containsKey(arg))
				result = registers.get(arg);
			else if (arg.startsWith("0X"))
				result = Long.parseLong(arg.substring(2), 16);
			else
				result = Long.parseLong(arg);
		}
		else {
			arg = arg.substring(1, arg.length() - 1);
			if (registers.containsKey(arg))
				result = addresses.get(registers.get(arg));
			else if (arg.startsWith("0X")) {
				long address = Long.parseLong(arg.substring(2), 16);
				result = addresses.get(address);
			}
			else 
				result = addresses.get(Long.parseLong(arg));
		}
		return result;
	}
	
	private static void putValue(String arg, long result)
	{
		if (!arg.startsWith("["))
			registers.put(arg, result);
		else {
			arg = arg.substring(1, arg.length() - 1);
			if (registers.containsKey(arg))
				addresses.put(registers.get(arg), (byte)result);
			else if (arg.startsWith("0X")) {
				long address = Long.parseLong(arg.substring(2), 16);
				addresses.put(address, (byte)result);
			}
			else
				addresses.put(Long.parseLong(arg), (byte)result);
		}
	}
	
	private static boolean isNumber(String s) {
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