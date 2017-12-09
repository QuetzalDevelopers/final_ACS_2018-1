import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.security.Key;
import javax.crypto.Cipher;

public class Conexion extends Thread{
	private Socket socket;
	private String nombreCliente;

	public Conexion(Socket socket, String nombreCliente){
		super(nombreCliente);

		this.socket = socket;
		this.nombreCliente = nombreCliente;
	}

	public Key obtenClave(String usuario){
		ObjectInputStream entrada;
		Key clave;

		try{
			entrada = new ObjectInputStream(new FileInputStream(usuario + ".ser"));
			clave = (Key) entrada.readObject( );
			entrada.close( );
			return clave;
		}catch(ClassNotFoundException e){
			return null;
		}catch(IOException e){
			return null;
		}
	}

	public String descifraPassword(Key clave, byte[ ] passwordCifrada, int n){
		Cipher descifrar;
		byte[ ] textoPlano, buffer;
		String descifrado;

		buffer = new byte[n];

		for(int i = 0; i < n; i++){
			buffer[i] = passwordCifrada[i];
		}

		try{
			descifrar = Cipher.getInstance("DES");
			descifrar.init(Cipher.DECRYPT_MODE, clave);
			textoPlano = descifrar.doFinal(buffer);
			descifrado = new String(textoPlano, "UTF8");
			return descifrado;
		}catch(Exception e){
			return null;
		}
	}

	public void run( ){
		DataInputStream dis;
		DataOutputStream dos;
		Key clave;
		String nombreUsuario;
		byte[ ] passwordCifrada;
		String password;
		File registro;
		FileReader fr;
		BufferedReader br;
		String linea;
		String respuesta;
		int nBytes;

		respuesta = "Datos incorrectos";
		passwordCifrada = new byte[64];
		nBytes = 0;

		try{
			System.out.println("Se conectó un cliente desde " + nombreCliente);
			dis = new DataInputStream(socket.getInputStream( ));
			dos = new DataOutputStream(socket.getOutputStream( ));
			try{
				registro = new File("passwords");
				fr = new FileReader(registro);
				br = new BufferedReader(fr);

				nombreUsuario = dis.readUTF( );
				nBytes = dis.read(passwordCifrada);
				clave = obtenClave(nombreUsuario);
				if(clave != null){
					password = descifraPassword(clave, passwordCifrada, nBytes);
					if(password != null)
						while((linea = br.readLine( ))!= null){
							String[ ] parte = linea.split(":");
							if(nombreUsuario.equals(parte[0]))
								if(password.equals(parte[1])){
									respuesta = "Datos correctos";
									break;
								}
						}
					else
						System.out.println("Error al descifrar la contraseña");
					if(respuesta.equals("Datos correctos"))
						System.out.println("Acceso autorizado a " + nombreUsuario);
					else
						System.out.println("Acceso denegado a " + nombreUsuario);
				}else{
					respuesta = "Perdí tu clave :(";
				}
				dos.writeUTF(respuesta);
				br.close( );
				fr.close( );
			}catch(FileNotFoundException e){
				System.out.println("No se encuentra el archivo de registros :(");
			}
			dos.close( );
			dis.close( );
			socket.close( );
		}catch(IOException e){
			System.out.println("Error al establecer conexión con el cliente" + nombreCliente + " :(");
		}
	}
}