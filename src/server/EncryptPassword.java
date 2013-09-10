package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class EncryptPassword {
	private Key key;
	/**
	 * Constructs the EncryptPassword class and will either load a key from the disk
	 * or create a new one if it doesn't exist.
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public EncryptPassword() throws NoSuchAlgorithmException, IOException{
		File file = new File("/home/students/thadams/key.txt");
		System.out.println(file.getAbsolutePath());
		if(file.exists()) restoreKey(file);
		else genKey();
	}

	
	/**
	 * Restores a key from a file.
	 * @param file File that stores the base64 encoded information of the key.
	 * @throws FileNotFoundException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	private void restoreKey(File file) throws FileNotFoundException,
			NoSuchAlgorithmException, IOException {
		FileInputStream fis = new FileInputStream(file);
		Scanner scanner = new Scanner(fis);
		StringBuilder keyText = new StringBuilder();
		String keyString;
		byte[] b;
		
		while (scanner.hasNext()) keyText.append(scanner.nextLine());
		keyString = keyText.toString();
		if(keyString == ""){
			genKey();
		} else {
			b = DatatypeConverter.parseBase64Binary(keyString);
			key = new SecretKeySpec(b, "DES");
		}
	}
	
	/**
	 * Generates a key, and then saves it to disk as a base64 encoded string.
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	private void genKey() throws NoSuchAlgorithmException, IOException {
		FileWriter fw = new FileWriter("key.txt");
		BufferedWriter bw = new BufferedWriter(fw);
		KeyGenerator gen = KeyGenerator.getInstance("DES");
		byte[] b;
		String keyString;
		
		gen.init(new SecureRandom());
		key = gen.generateKey();
		b = key.getEncoded();
		keyString = DatatypeConverter.printBase64Binary(b);
		bw.write(keyString);
		bw.close();
	}
	
	/**
	 * Encrypts a string and returns it.
	 * @param password String to encrypt.
	 * @return Encrypted String.
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws UnsupportedEncodingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public String encryptPassword(String password) throws NoSuchAlgorithmException, 
	NoSuchPaddingException, InvalidKeyException, 
	UnsupportedEncodingException, IllegalBlockSizeException, 
	BadPaddingException
	{
		Cipher c = Cipher.getInstance("DES/ECB/PKCS5Padding");
		c.init(Cipher.ENCRYPT_MODE, key);
		
		byte[] s = password.getBytes("UTF8");
		byte[] r = c.doFinal(s);
		return DatatypeConverter.printBase64Binary(r);
	}
}
