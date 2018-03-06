package hep.crest.server.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternExample {

	private static String QRY_PATTERN = "([a-zA-Z0-9_\\-\\.]+?)(:|<|>)([a-zA-Z0-9_\\-\\/\\.:]+?)(,|;)";

	public PatternExample() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String args[]) {
		System.out.println("Testing pattern in java "+PatternExample.QRY_PATTERN);
		
		String by = "name:THIS_IS_A_TAG:V1,description:AND_ADESCRIPTION;p>10";
		Pattern pattern = Pattern.compile(QRY_PATTERN);
		Matcher matcher = pattern.matcher(by + ",");
		System.out.println("Pattern is " + pattern);
		System.out.println("Matcher is " + matcher);
		while (matcher.find()) {
			System.out.println("groups are: " + matcher.group(1) +" "+ matcher.group(2)+" "+ matcher.group(3)+ " " + matcher.group(4));
		}

	}
}
