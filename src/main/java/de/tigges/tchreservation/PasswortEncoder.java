package de.tigges.tchreservation;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to crypt a given password using {@link BCryptPasswordEncoder}
 */
public class PasswortEncoder {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("usage: PasswortEncoder <password>\n crypt password with bcrypt");
			System.exit(1);
		}
		var encoder = new BCryptPasswordEncoder();
		System.out.printf("encrypt password %s = %s%n", args[0], encoder.encode(args[0]));
	}
}
