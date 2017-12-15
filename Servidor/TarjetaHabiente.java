public class TarjetaHabiente{
	private String nombre;
	private String numeroCuenta;
	private float saldo;

	public TarjetaHabiente(String nombre, String numeroCuenta, float saldo){
		this.nombre = nombre;
		this.numeroCuenta = numeroCuenta;
		this.saldo = saldo;
	}

	public String getNombre( ){
		return this.nombre;
	}

	public String getNumeroCuenta( ){
		return this.numeroCuenta;
	}

	public float getSaldo( ){
		return this.saldo;
	}

	public void deposita(float monto){
		if(monto > 0)
			saldo = saldo + monto;
	}

	public void retira(float monto){
		if(monto > 0 && monto < saldo)
			saldo = saldo - monto;
	}
}