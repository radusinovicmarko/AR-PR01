package org.unibl.etf.ar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class ISASimulator {
	
	//Index for the interpretation of the code
	private static int i = 0;
	
	public static Scanner scanner = new Scanner(System.in);

	//Registers
	private static final HashMap<String, Long> registers = new HashMap<>();
	//Memory addresses in address space
	private static final HashMap<Long, Byte> addresses = new HashMap<>();
	
	//Set of keywords
	private static final HashSet<String> keywords = new HashSet<>();
	//Keywords for debugging mode
	public static final String breakpoint = "BREAKPOINT";
	public static final String next = "NEXT";
	public static final String cont = "CONTINUE";
	
	//Debugging mode execution
	private static boolean debuggingMode = false;
	
	//Map of labels for jumps
	private static final HashMap<String, Integer> labels = new HashMap<>();
	
	//Code to interpret
	private static List<String> code;
	//Validity of the code
	private static boolean isValid = true;
	//List of errors
	private static final ArrayList<String> errorList = new ArrayList<>();
	
	//Instruction for switching to machine code execution
	public static final String switchToMachineCodeExec = "SWITCH";
	
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
		Operators.getUnaryOperators().put("NOT", Operators::not);
		Operators.getUnaryOperators().put("JMP", Operators::jmp);
		Operators.getUnaryOperators().put("JE", Operators::je);
		Operators.getUnaryOperators().put("JNE", Operators::jne);
		Operators.getUnaryOperators().put("JGE", Operators::jge);
		Operators.getUnaryOperators().put("JL", Operators::jl);
		Operators.getUnaryOperators().put("PRINT", Operators::print); 
		Operators.getUnaryOperators().put("SCAN", Operators::scan);
		Operators.getBinaryOperators().put("ADD", Operators::add);
		Operators.getBinaryOperators().put("SUB", Operators::sub);
		Operators.getBinaryOperators().put("AND", Operators::and);
		Operators.getBinaryOperators().put("OR", Operators::or);
		Operators.getBinaryOperators().put("MOV", Operators::mov);
		Operators.getBinaryOperators().put("CMP", Operators::cmp); 
		keywords.addAll(Operators.getUnaryOperators().keySet());
		keywords.addAll(Operators.getBinaryOperators().keySet());
		keywords.add(breakpoint);
		keywords.add(switchToMachineCodeExec);
	}
	
	public static void setOpCodes() {
		byte i = 0;
		for (String keyword : keywords)
			MachineCodeSimulator.getOpCodes().put(i++, keyword);
	}
	
	public static int getIndex() {
		return i;
	}
	
	public static void setIndex(int index) {
		i = index;
	}
	
	public static HashMap<String, Long> getRegisters() {
		return registers;
	}
	
	public static HashMap<Long, Byte> getAddresses() {
		return addresses;
	}
	
	public static HashMap<String, Integer> getLabels() {
		return labels;
	}
	
	public static HashSet<String> getKeywords() {
		return keywords;
	}
	
	public static List<String> getCode() {
		return code;
	}
	
	public static boolean getDebuggingMode() {
		return debuggingMode;
	}
	
	public static void setDebuggingMode(boolean mode) {
		debuggingMode = mode;
	}
	
	public static boolean isValid() {
		return isValid;
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
		//Check if a number is the first operand
		code.stream().forEach(s -> {
			s = s.trim();
			int index = s.indexOf(' ');
			if (index == -1)
				return;
			String keyword = s.split(" ")[0].toUpperCase();
			String[] operands = s.substring(index + 1).replaceAll(" ", "").split(",");
			if (Operators.getUnaryOperators().containsKey(keyword) && operands.length != 1) {
				isValid = false;
				errorList.add(s);
			}
			else if (Operators.getBinaryOperators().containsKey(keyword) && operands.length != 2) {
				isValid = false;
				errorList.add(s);
			}
			
			if (Operators.isNumber(operands[0]) && !"PRINT".equals(keyword) && !"CMP".equals(keyword)) {
				isValid = false;
				errorList.add(operands[0]);
			}
			
			Arrays.asList(operands).stream().forEach(o -> {
				String oprnd = o.toUpperCase();
				if (!oprnd.startsWith("[") && !registers.containsKey(oprnd) && !Operators.isNumber(oprnd) && !labels.containsKey(o))
				{
					isValid = false;
					errorList.add(o);
				}
				else if (Operators.isNumber(oprnd))
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
					if (registers.containsKey(address))
						return;
					if (!Operators.isNumber(address))
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
	
	public static void interpretCode() {
		for (i = 0; i < code.size(); i++) {
			String line = code.get(i);
			line = line.trim();
			int index = line.indexOf(' ');
			if (index == -1) {
				if (breakpoint.equals(line.toUpperCase())) {
					debuggingMode = true;
					debug();
					continue;
				}
				else if (switchToMachineCodeExec.equals(line.toUpperCase())) {
					i++;
					registers.put("RIP", MachineCodeSimulator.getRipValues().get(i));
					MachineCodeSimulator.machineCodeExec();
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
			if (Operators.getUnaryOperators().containsKey(operator))
				Operators.getUnaryOperators().get(operator).accept(args1);
			else if (Operators.getBinaryOperators().containsKey(operator)) {
				String arg1 = args.split(",")[0], arg2 = args.split(",")[1];
				Operators.getBinaryOperators().get(operator).accept(arg1, arg2);
			}
			if (debuggingMode)
				debug();
		}
	}
	
	public static void debug() {
		System.out.println();
		registers.keySet().stream().forEach(reg -> System.out.println(reg + " " + registers.get(reg)));
		System.out.println("Enter memory address for examination or NEXT or CONTINUE:");
		String input = "";
		do {
			input = scanner.nextLine();
			if (Operators.isNumber(input)) {
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
	
	public static void execute(List<String> codeToRun) {
		setRegisters();
		setKeywordsAndOperators();
		setOpCodes();
		
		code = codeToRun;
		
		codeValidation();
		if (!isValid)
		{
			System.err.println("Code not valid.");
			System.err.println("Errors:");
			errorList.stream().forEach(s -> System.err.println(s));
			return;
		}
		
		MachineCodeSimulator.translateToMachineCode(0x100);
		interpretCode();
		scanner.close();
	}
	
	public static void main(String[] args) {
		if (args.length == 0)
		{
			System.err.println("Missing an argument.");
			return;
		}
		try {
			execute(Files.readAllLines(Path.of(args[0])));
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return;
		}
	}
}