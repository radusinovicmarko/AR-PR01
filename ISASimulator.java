package org.unibl.etf.ar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class ISASimulator {

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
	public static final String breakpoint = "BREAKPOINT";
	
	//Code to interpret
	public static List<String> code;
	public static boolean isValid = true;
	//List of errors
	public static final ArrayList<String> errorList = new ArrayList<>();
	
	public static void setRegisters() {
		registers.put("RAX", (long)0);
		registers.put("RBX", (long)0);
		registers.put("RCX", (long)0);
		registers.put("RDX", (long)0);
		registers.put("RSI", (long)0);
		registers.put("RDI", (long)0);
	}
	
	public static void setKeywordsAndOperators() {
		unaryOperators.put("NOT", ISASimulator::not);
		binaryOperators.put("ADD", ISASimulator::add);
		binaryOperators.put("SUB", ISASimulator::sub);
		binaryOperators.put("AND", ISASimulator::and);
		binaryOperators.put("OR", ISASimulator::or);
		binaryOperators.put("MOV", ISASimulator::mov);
		binaryOperators.put("SCAN", ISASimulator::scan);
		binaryOperators.put("PRINT", ISASimulator::print);
		keywords.addAll(unaryOperators.keySet());
		keywords.addAll(binaryOperators.keySet());
		keywords.add(breakpoint);
	}
	
	public static void codeValidation() {
		//Syntax analysis
		code.stream().forEach(s -> {
			s = s.trim();
			int index = s.indexOf(' ');
			if (index == -1 && !keywords.contains(s.toUpperCase()))
			{
				errorList.add(s);
				isValid = false; 
			}
			else 
			{
				String keyword = s.split(" ")[0];
				if (!keywords.contains(keyword.toUpperCase()))
				{
					errorList.add(keyword);
					isValid = false;
				}
			}
		});
		
		if (!isValid)
			return;
		
		//Semantic analysis
		//Check for number of operands
		//TODO - Check for validity of operands
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
				if (!oprnd.startsWith("[") && !registers.containsKey(oprnd) && !isNumber(oprnd)
						|| oprnd.startsWith("[") && !registers.containsKey(oprnd.substring(1, oprnd.length() - 1)))
				{
					isValid = false;
					errorList.add(o);
				}
			});
		});
	}
	
	public static void not(String arg) {
		if (!arg.startsWith("[")) {
			if (registers.containsKey(arg))
				registers.put(arg, ~registers.get(arg));
		}
	}
	
	public static void add(String arg1, String arg2) {
		if (!arg1.startsWith("["))
		{
			long result = 0;
			if (!arg2.startsWith("[")) {
				if (registers.containsKey(arg2))
					result = registers.get(arg1) + registers.get(arg2);
				else
					result = registers.get(arg1) + Long.parseLong(arg2);
			}
			//else
			registers.put(arg1, result);
		}
	}
	
	public static void sub(String arg1, String arg2) {
		if (!arg1.startsWith("["))
		{
			long result = 0;
			if (!arg2.startsWith("[")) {
				if (registers.containsKey(arg2))
					result = registers.get(arg1) - registers.get(arg2);
				else
					result = registers.get(arg1) - Long.parseLong(arg2);
			}
			//else
			registers.put(arg1, result);
		}
	}
	
	public static void and(String arg1, String arg2) {
		if (!arg1.startsWith("["))
		{
			long result = 0;
			if (!arg2.startsWith("[")) {
				if (registers.containsKey(arg2))
					result = registers.get(arg1) & registers.get(arg2);
				else
					result = registers.get(arg1) & Long.parseLong(arg2);
			}
			//else
			registers.put(arg1, result);
		}
	}
	
	public static void or(String arg1, String arg2) {
		if (!arg1.startsWith("["))
		{
			long result = 0;
			if (!arg2.startsWith("[")) {
				if (registers.containsKey(arg2))
					result = registers.get(arg1) | registers.get(arg2);
				else
					result = registers.get(arg1) | Long.parseLong(arg2);
			}
			//else
			registers.put(arg1, result);
		}
	}
	
	public static void mov(String arg1, String arg2) {
		if (!arg1.startsWith("["))
		{
			if (!arg2.startsWith("[")) {
				if (registers.containsKey(arg2))
					registers.put(arg1, registers.get(arg2));
				else
					registers.put(arg1, Long.parseLong(arg2));
			}
			//else
		}
	}
	
	//TODO
	public static void scan(String arg1, String arg2) {
		
	}
	
	//TODO
	public static void print(String arg1, String arg2) {
		
	}
	
	public static void interpretCode() {
		for (String line : code) {
			line = line.trim();
			String operator = line.split(" ")[0];
			operator = operator.toUpperCase();
			String args = line.substring(line.indexOf(' '));
			args = args.replaceAll(" ", "");
			args = args.toUpperCase();
			if (unaryOperators.containsKey(operator)) {
				unaryOperators.get(operator).accept(args);
			}
			else if (binaryOperators.containsKey(operator)) {
				String arg1 = args.split(",")[0], arg2 = args.split(",")[1];
				binaryOperators.get(operator).accept(arg1, arg2);
			}
			registers.keySet().stream().forEach(s -> System.out.println(s + " " + registers.get(s)));
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		setRegisters();
		setKeywordsAndOperators();;
		if (args.length == 0)
		{
			System.err.println("Missing argument.");
			return;
		}
		try {
			code = Files.readAllLines(Path.of(args[0]));
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return;
		}
		codeValidation();
		if (!isValid)
		{
			System.err.println("Code not valid.");
			System.err.println("Errors:");
			errorList.stream().forEach(s -> System.err.println(s));
			return;
		}
		interpretCode();
	}
	
	private static boolean isNumber(String s) {
		try {
			Long.parseLong(s);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}

}
