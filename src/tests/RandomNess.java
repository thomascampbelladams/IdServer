package tests;
import java.io.IOException;
import java.util.Random;


public class RandomNess {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		NameGenerator ng = new NameGenerator("syl");
		
		int itr = Integer.parseInt(args[1]);
		String conn = args[0];
		
		String format = "java -jar IdClient.jar %s -c  %s \"%s\" -p %s\n";
		
		for ( int i = 0; i < itr; i++ )
		{
			String username= ng.compose(1 +new Random().nextInt(3));
			String realname= username + " " + ng.compose(1 +new Random().nextInt(3));
			System.out.printf(format, conn, username, realname, PassPhrase.getNext() );
		}

	}

}
