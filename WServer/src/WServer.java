import java.net.*;
import java.io.*;
/** 
 * La classe WServer s'encarrega de establir el port
 * que s'utilitzara en la conexio i connectar client 
 * i servidor, quan rebem una peticio creem un 
 * PrimeThread perque s'en encarregui
 */
public class WServer
{
	public static void main (String[] args) {
		ServerSocket serverSocket;
		Socket socket2;

		int port = 8503;
		try
		{
			serverSocket = new ServerSocket(port);
			while(true)
			{
				//obtenim socket per comunicar-nos amb el client
				socket2 = serverSocket.accept();
				new Thread(new PrimeThread(socket2)).start();
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static int len(String[] tokens) {
		// TODO Auto-generated method stub
		return 0;
	}
}