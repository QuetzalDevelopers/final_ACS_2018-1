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
	private static String numeroCuenta;
	private static KeyGenerator generador;
	private static Key clave;

	public static boolean buscaNombre( ){
		BufferedReader entrada;

		try{
			entrada = new BufferedReader(new FileReader(new File("tarjetahabiente")));
			entrada.readLine( );
			numeroCuenta = entrada.readLine( );
			entrada.close( );
			return true;
		}catch(IOException e){
			System.out.println("No se encuentra el archivo tarjetahabiente");
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
			System.out.println("Error al generar la clave");
			return false;
		}
	}

	public static boolean escribeArchivo( ){
		ObjectOutputStream salida;

		try{
			salida = new ObjectOutputStream(new FileOutputStream(numeroCuenta + ".ser"));
			salida.writeObject(clave);
			salida.close( );
			return true;
		}catch(IOException e){
			System.out.println("Error al escribir el archivo " + numeroCuenta +".ser");
			return false;
		}
	}

	public static void main(String[ ] args){
		if(buscaNombre( ) && generaClave( ) && escribeArchivo( ))
			System.out.println("Se generó el archivo " + numeroCuenta + ".ser");
	}
}