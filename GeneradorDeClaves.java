import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.util.Scanner;
import java.util.ArrayList;
import javax.crypto.KeyGenerator;

public class GeneradorDeClaves{
	private static ArrayList<String> nombres;
	private static KeyGenerator generador;
	private static Key clave;

	public static boolean buscaNombres( ){
		BufferedReader entrada;
		String linea;

		try{
			entrada = new BufferedReader(new FileReader(new File("passwords")));
			while((linea = entrada.readLine( )) != null){
				String[ ] parte = linea.split(":");
				nombres.add(parte[0]);
			}
			entrada.close( );
			return true;
		}catch(IOException e){
			return false;
		}
	}

	public static boolean generaClave( ){
		try{
			generador = KeyGenerator.getInstance("DES");
			generador.init(56);
			clave = generador.generateKey( );
			return true;
		}catch(Exception e){
			return false;
		}
	}

	public static boolean escribeArchivo(String identificador){
		ObjectOutputStream salida;
		try{
			salida = new ObjectOutputStream(new FileOutputStream(identificador + ".ser"));
			salida.writeObject(clave);
			salida.close( );
			return true;
		}catch(IOException e){
			return false;
		}
	}

	public static void main(String[ ] args){
		nombres = new ArrayList< >( );

		if(buscaNombres( ))
			for(int i = 0; i < nombres.size( ); i++){
				String usuario;

				usuario = nombres.get(i);
				if(generaClave( ))
					if(escribeArchivo(usuario))
						System.out.println("Se generó el archivo " + usuario + ".ser");
					else
						System.out.println("Error al crear el archivo " + usuario + ".ser");
				else
					System.out.println("Error al generar la clave para " + usuario);
			}
		else
			System.out.println("Error al abrir el archivo passwords");
	}
}