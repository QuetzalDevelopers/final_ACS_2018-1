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

public class Cliente{
	private static Socket socket;
	private static DataOutputStream dos;
	private static DataInputStream dis;
	private static Key clave;
	private static Cipher cifrar;
	private static Cipher descifrar;
	
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

	public static boolean obtenClave(String identificador){
		ObjectInputStream entrada;

		try{
			entrada = new ObjectInputStream(new FileInputStream(identificador + ".ser"));
			clave = (Key) entrada.readObject( );
			entrada.close( );
			return true;
		}catch(ClassNotFoundException e){
			return false;
		}catch(IOException e){
			return false;
		}
	}

	public static boolean instanciaCipher( ){
		try{
			cifrar = Cipher.getInstance("DES");
			cifrar.init(Cipher.ENCRYPT_MODE, clave);
			descifrar = Cipher.getInstance("DES");
			descifrar.init(Cipher.DECRYPT_MODE, clave);
			return true;
		}catch(Exception e){
			System.out.println("Error al instanciar el Cipher");
			return false;
		}
	}

	public static byte[ ] cifraCadena(String cadena){
		byte[ ] textoPlano;
		byte[ ] cadenaCifrada;

		try{
			textoPlano = cadena.getBytes("UTF8");
			cadenaCifrada = cifrar.doFinal(textoPlano);
			return cadenaCifrada;
		}catch(Exception e){
			System.out.println("Error al cifrar la cadena");
			return null;
		}
	}

	public static String descifraCadena(byte[ ] cadenaCifrada){
		byte[ ] textoPlano, buffer;
		String descifrado;

		try{
			textoPlano = descifrar.doFinal(cadenaCifrada);
			descifrado = new String(textoPlano, "UTF8");

			return descifrado;
		}catch(Exception e){
			System.out.println("Error al descifrar la cadena");
			return null;
		}
	}

	public static void enviaCadena(String cadena){
		try{
			dos.writeUTF(cadena);
		}catch(IOException e){
			System.out.println("Error al enviar la cadena " + e);
		}
	}

	public static void enviaCadenaCifrada(byte[ ] cadenaCifrada){
		try{
			dos.write(cadenaCifrada);
		}catch(IOException e){
			System.out.println("Error al enviar la cadena cifrada " + e);
		}
	}

	public static String recibeCadena( ){
		String respuesta;

		try{
			respuesta = dis.readUTF( );
			return respuesta;
		}catch(IOException e){
			System.out.println("Error al recibir la cadena " + e);

			return null;
		}
	}

	public static byte[ ] recibeCadenaCifrada( ){
		int n;
		byte[ ] buffer = new byte[254];

		try{
			n = dis.read(buffer);

			byte[ ] cadenaCifrada = new byte[n];

			for(int i = 0; i < n; i++){
				cadenaCifrada[i] = buffer[i];
			}

			return cadenaCifrada;
		}catch(IOException e){
			System.out.println("Error al recibir la cadena cifrada " + e);	

			return null;
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
		String respuesta;
		String numeroCuenta;
		String nip;
		String peticion;

		socket = null;
		console = System.console( );
		teclado = new Scanner(System.in);
		
		System.out.println("Me conecto al puerto 8000 del servidor");
		if(conectar(a[0], 8000)){
			System.out.print("Numero de cuenta: ");
			numeroCuenta = teclado.nextLine( );
			if(obtenClave(numeroCuenta) && instanciaCipher( )){
				nip = new String(console.readPassword("Nip: "));
				enviaCadenaCifrada(cifraCadena(numeroCuenta));
				enviaCadenaCifrada(cifraCadena(nip));
				respuesta = recibeCadena( );
				if(!respuesta.equals("Datos incorrectos")){
					System.out.println("Bienvenido " + respuesta);
					while(true){
						System.out.print("> ");
						peticion = teclado.nextLine( );
						String[ ] parte = peticion.split(" ");
						if(parte[0].equals("CONSULTA")){
							enviaCadena(peticion);
							respuesta = descifraCadena(recibeCadenaCifrada( ));
							System.out.println("Tu saldo actual es: " + respuesta);
						}else if(parte[0].equals("DEPOSITAR")){
							enviaCadena(parte[0]);
							enviaCadenaCifrada(cifraCadena(parte[1]));
							respuesta = recibeCadena( );
							System.out.println(respuesta);
						}else if(parte[0].equals("RETIRAR")){
							enviaCadena(parte[0]);
							enviaCadenaCifrada(cifraCadena(parte[1]));
							respuesta = recibeCadena( );
							System.out.println(respuesta);
						}else if(parte[0].equals("SALIR")){
							enviaCadena(peticion);
							System.out.println("Gracias, vuelva pronto :)");
							break;
						}else
							System.out.println("No se reconoce la peticion");
					}
				}
				else
					System.out.println(respuesta);
			}else
				System.out.println("Hay un problema con la clave");
			cierraConexion( );
		}else
			System.out.println("El cajero esta teniendo problemas, intenta de nuevo");
	}
}