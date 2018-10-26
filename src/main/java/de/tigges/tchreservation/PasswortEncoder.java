package de.tigges.tchreservation;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
/**
 * Utility class to crypt a given password using {@link BCryptPasswordEncoder}
 */
public class PasswortEncoder {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println(String.format("usage: %s <password>\n crypt password with bcrypt", 
					PasswortEncoder.class.getSimpleName()));
			System.exit(1);
		}
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		System.out.println(String.format("encrypt password %s = %s", 
				args[0], encoder.encode(args[0])));
	}
}
