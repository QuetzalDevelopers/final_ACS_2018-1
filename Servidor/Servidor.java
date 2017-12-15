import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor{
	private static ServerSocket serverSocket;
	private static Socket socket;

	public static boolean iniciaServidor( ){
		try{
			serverSocket = new ServerSocket(8000);
			return true;
		}catch(IOException e){
			System.out.println("Error al inciar el servidor :(");
			return false;
		}
	}

	public static boolean aceptaConexion( ){
		try{
			socket = serverSocket.accept( );
			return true;
		}catch(IOException e){
			System.out.println("Error al establecer la conexión con el cliente :(");
			return false;
		}
	}

	public static void cierraServidor( ){
		try{
			serverSocket.close( );
			System.out.println("El servidor se cerro de manera correcta");
		}catch(IOException e){
			System.out.println("Error al cerrar el servidor");
		}
	}

	public static void main(String args[ ]){
		Conexion conexion;

		System.out.println("Escuchando por el puerto 8000");
		if(iniciaServidor( )){
			System.out.println("Esperando a que los clientes se conecten...");
			while(true){
				if(aceptaConexion( )){
					conexion = new Conexion(socket, socket.getInetAddress( ).getHostName( ));
					conexion.start( );
				}
			}
		}
	}
}