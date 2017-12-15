import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.security.Key;
import javax.crypto.Cipher;

public class Conexion extends Thread{
	private Socket socket;
	private String nombreCliente;
	private Cipher cifrar;
	private Cipher descifrar;
	private DataInputStream dis;
	private DataOutputStream dos;
	private Key clave;

	public Conexion(Socket socket, String nombreCliente){
		super(nombreCliente);

		this.socket = socket;
		this.nombreCliente = nombreCliente;
	}

	public boolean obtenClave(String identificador){
		ObjectInputStream entrada;

		try{
			entrada = new ObjectInputStream(new FileInputStream(identificador + ".ser"));
			clave = (Key) entrada.readObject( );
			entrada.close( );
			return true;
		}catch(ClassNotFoundException e){
			System.out.println("No se pudo obtener la clase Key");
			return false;
		}catch(IOException e){
			System.out.println("Error al abrir el archivo " + identificador + ".ser");
			return false;
		}
	}

	public boolean instanciaCipher( ){
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

	public byte[ ] cifraCadena(String cadena){
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

	public String descifraCadena(byte[ ] cadenaCifrada){
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

	public void enviaCadena(String cadena){
		try{
			dos.writeUTF(cadena);
		}catch(IOException e){
			System.out.println("Error al enviar la cadena " + e);
		}
	}

	public void enviaCadenaCifrada(byte[ ] cadenaCifrada){
		try{
			dos.write(cadenaCifrada);
		}catch(IOException e){
			System.out.println("Error al enviar la cadena cifrada " + e);
		}
	}

	public String recibeCadena( ){
		String respuesta;

		try{
			respuesta = dis.readUTF( );
			return respuesta;
		}catch(IOException e){
			System.out.println("Error al recibir la cadena " + e);

			return null;
		}
	}

	public byte[ ] recibeCadenaCifrada( ){
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

	public TarjetaHabiente creaTarjetaHabiente( ){
		BufferedReader entrada;
		String nombre;
		String numeroCuenta;
		float saldo;
		TarjetaHabiente tarjetaHabiente;

		try{
			entrada = new BufferedReader(new FileReader(new File("tarjetahabiente")));

			nombre = entrada.readLine( );
			numeroCuenta = entrada.readLine( );
			saldo = Float.parseFloat(entrada.readLine( ));

			tarjetaHabiente = new TarjetaHabiente(nombre, numeroCuenta, saldo);

			entrada.close( );

			return tarjetaHabiente;
		}catch(IOException e){
			System.out.println("Error al abrir el archivo tarjetahabiente");

			return null;
		}
	}

	public void guardaTarjetaHabiente(TarjetaHabiente tarjetaHabiente){
		BufferedWriter salida;

		try{
			salida = new BufferedWriter(new FileWriter(new File("tarjetahabiente")));

			salida.write(tarjetaHabiente.getNombre( ) + "\n");
			salida.write(tarjetaHabiente.getNumeroCuenta( ) + "\n");
			salida.write(tarjetaHabiente.getSaldo( ) + "\n");

			salida.close( );
		}catch(IOException e){
			System.out.println("Error al guardar los cambios");
		}
	}

	public boolean verificaUsuario(String user, String password){
		BufferedReader br;
		String linea;

		try{
			br = new BufferedReader(new FileReader(new File("passwords")));
			while((linea = br.readLine( )) != null){
				String[ ] parte = linea.split(":");
				if(user.equals(parte[0]))
					if(password.equals(parte[1])){
						br.close( );
						return true;
					}else{
						br.close( );
						return false;
					}
			}
		}catch(FileNotFoundException e){
			System.out.println("Error al abrir el archivo passwords");
		}catch(IOException e){
			System.out.println("Error al leer el archivo passwords");
		}
		return false;
	}

	public void run( ){
		String numeroCuenta;
		String nip;
		float monto;
		float saldo;
		byte[ ] buffer;
		String linea;
		String respuesta;
		int nBytes;
		boolean bandera;
		String peticion;
		TarjetaHabiente tarjetaHabiente;

		respuesta = "Datos incorrectos";

		try{
			System.out.println("Se conectó un cliente desde " + nombreCliente);
			dis = new DataInputStream(socket.getInputStream( ));
			dos = new DataOutputStream(socket.getOutputStream( ));

			tarjetaHabiente = creaTarjetaHabiente( );
			if(tarjetaHabiente != null)
				if(obtenClave(tarjetaHabiente.getNumeroCuenta( )) && instanciaCipher( )){
					numeroCuenta = descifraCadena(recibeCadenaCifrada( ));
					nip = descifraCadena(recibeCadenaCifrada( ));
					bandera = verificaUsuario(numeroCuenta, nip);
					if(bandera){
						enviaCadena(tarjetaHabiente.getNombre( ));
						while(true){
							peticion = recibeCadena( );
							if(peticion.equals("CONSULTA")){
								respuesta = Float.toString(tarjetaHabiente.getSaldo( ));
								enviaCadenaCifrada(cifraCadena(respuesta));
							}else if(peticion.equals("DEPOSITAR")){
								saldo = tarjetaHabiente.getSaldo( );
								monto = Float.parseFloat(descifraCadena(recibeCadenaCifrada( )));
								tarjetaHabiente.deposita(monto);
								if(saldo == tarjetaHabiente.getSaldo( ))
									respuesta = "INCORRECTO";
								else{
									respuesta = "HECHO";
									guardaTarjetaHabiente(tarjetaHabiente);
								}
								enviaCadena(respuesta);
							}else if(peticion.equals("RETIRAR")){
								saldo = tarjetaHabiente.getSaldo( );
								monto = Float.parseFloat(descifraCadena(recibeCadenaCifrada( )));
								tarjetaHabiente.retira(monto);
								if(saldo == tarjetaHabiente.getSaldo( ))
									respuesta = "INCORRECTO";
								else{
									respuesta = "HECHO";
									guardaTarjetaHabiente(tarjetaHabiente);
								}
								enviaCadena(respuesta);
							}else if(peticion.equals("SALIR"))
								break;
						}
					}else
						enviaCadena("Datos incorrectos");
				}
			dos.close( );
			dis.close( );
			socket.close( );
		}catch(IOException e){
			System.out.println("Error al establecer conexión con el cliente" + nombreCliente + " :(");
		}
	}
}