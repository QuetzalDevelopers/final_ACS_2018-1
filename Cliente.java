import java.io.Console;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.security.Key;
import java.util.Scanner;
import javax.crypto.Cipher;

public class ClienteV2{
	private static Socket socket;
	private static DataOutputStream dos;
	private static DataInputStream dis;
	private static Key clave;
	private static String usuario;
	private static String password;
	private static byte[ ] passwordCifrada;
	private static String respuesta;
	
	public static boolean conectar(String direccion, int puerto){
		try{
			socket = new Socket(direccion, puerto);
			dos = new DataOutputStream(socket.getOutputStream( ));
			dis = new DataInputStream(socket.getInputStream( ));
			return true;
		}catch(IOException e){
			System.out.println("Error al establecer conexión con el servidor");
			return false;
		}
	}

	public static boolean obtenClave( ){
		ObjectInputStream entrada;

		try{
			entrada = new ObjectInputStream(new FileInputStream(usuario + ".ser"));
			clave = (Key) entrada.readObject( );
			entrada.close( );
			return true;
		}catch(ClassNotFoundException e){
			return false;
		}catch(IOException e){
			return false;
		}
	}

	public static boolean cifraPassword( ){
		Cipher cifrar;
		byte[ ] textoPlano;

		try{
			textoPlano = password.getBytes("UTF8");
			cifrar = Cipher.getInstance("DES");
			cifrar.init(Cipher.ENCRYPT_MODE, clave);
			passwordCifrada = cifrar.doFinal(textoPlano);
			return true;
		}catch(Exception e){
			return false;
		}
	}

	public static boolean enviaDatos( ){
		try{
			dos.writeUTF(usuario);
			dos.write(passwordCifrada, 0, passwordCifrada.length);
			return true;
		}catch(IOException e){
			System.out.println("Error al enviar los datos del usuario");
			return false;
		}
	}

	public static boolean recibeDatos( ){
		try{
			respuesta = dis.readUTF( );
			return true;
		}catch(IOException e){
			System.out.println("Error al recibir la respuesta del servidor " + e);
			return false;
		}
	}

	public static void cierraConexion( ){
		try{
			dos.close( );
			dis.close( );
			socket.close( );
		}catch(IOException e){
			System.out.println("Error al cerrar la conexion");
		}
	}

	public static void main(String a[ ]){
		Console console;
		Scanner teclado;

		socket = null;
		console = System.console( );
		teclado = new Scanner(System.in);
		
		System.out.println("Me conecto al puerto 8000 del servidor");
		if(conectar(a[0], 8000)){
			System.out.print("Nombre de usuario: ");
			usuario = teclado.nextLine( );
			password = new String(console.readPassword("Password: "));
			if(obtenClave( )){
				if(cifraPassword( ))
					if(enviaDatos( ))
						if(recibeDatos( ))
							System.out.println("El mensaje que me envio el servidor es: " + respuesta);
						else
							System.out.println("Error al recibir la respuesta");
					else
						System.out.println("Error al enviar los datos del usuario");
				else
					System.out.println("Error al cifrar la contraseña");
			}
			else
				System.out.println("No se encuentra la clave del usuario " + usuario);
			cierraConexion( );
		}
	}
}