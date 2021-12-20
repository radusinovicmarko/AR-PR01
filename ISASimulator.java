package org.unibl.etf.ar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	//Set of unary operators
	public static final HashSet<String> unaryOperators = new HashSet<>();
	//Set of binary operators
	public static final HashSet<String> binaryOperators = new HashSet<>();
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
		unaryOperators.add("NOT");
		binaryOperators.add("ADD");
		binaryOperators.add("SUB");
		binaryOperators.add("AND");
		binaryOperators.add("OR");
		binaryOperators.add("MOV");
		binaryOperators.add("SCAN");
		binaryOperators.add("PRINT");
		keywords.addAll(unaryOperators);
		keywords.addAll(binaryOperators);
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
			if (unaryOperators.contains(keyword) && operands.length != 1)
			{
				isValid = false;
				errorList.add(s);
			}
			else if (binaryOperators.contains(keyword) && operands.length != 2)
			{
				isValid = false;
				errorList.add(s);
			}
			Arrays.asList(operands).stream().forEach(o -> {
				String oprnd = o.toUpperCase();
				if (!oprnd.startsWith("[") && !registers.containsKey(oprnd)
						|| oprnd.startsWith("[") && !registers.containsKey(oprnd.substring(1, oprnd.length() - 1)))
				{
					isValid = false;
					errorList.add(o);
				}
			});
		});
	}
	
	public static void add(String arg1, String arg2) {
		
	}
	
	public static void interpretCode() {
		
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

}
