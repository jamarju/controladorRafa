package rafa.controlador;

import java.text.DecimalFormat;

import android.util.Log;

public class ControladorVelocidad {

	protected static final DecimalFormat formateador = new DecimalFormat("* ###0.00");
	public static final float kFilteringFactor = 0.8f; //ramp-speed - play with this value until satisfied
	public static final int EstadoGiradoAlMaximo = 1;
	float accel[] = new float[3]; 	//last result storage - keep definition outside of this function, eg. in wrapping object 

	protected float acelerometroX, acelerometroY, acelerometroZ;
	protected Bluetooth bluetooth;
	protected float velocidadMaxima;
	
	protected float velocidadIzquierda;
	protected float velocidadDerecha;
	
	private int estado;
	
	
	protected float sensibilidadGiro;
	
	public ControladorVelocidad(Bluetooth bluetooth){
		this.bluetooth = bluetooth;
		velocidadMaxima = 20;
		
		velocidadIzquierda = 0;
		velocidadDerecha = 0;
		
		sensibilidadGiro = 2;
		
		estado = 0;
		
	}

	public void filtra(float x, float y, float z) {
		// filtro paso bajo para reducir ruido / movimientos del telefono
		accel[0] = x * kFilteringFactor + accel[0] * (1.0f - kFilteringFactor);
		accel[1] = y * kFilteringFactor + accel[1] * (1.0f - kFilteringFactor);
		accel[2] = z * kFilteringFactor + accel[2] * (1.0f - kFilteringFactor);
		acelerometroX = accel[0];
		acelerometroY = accel[1];
		acelerometroZ = accel[2];
	}

	public void setVelocidadMaxima(float velocidad){
		velocidadMaxima = velocidad;
	}
	
	public void actualizarDatosAcelerometro(float x, float y, float z){
		filtra(x, y, z);
	}
	
	public void setVelocidad(float izquierda, float derecha){
		setVelocidadIzquierda(izquierda);
		setVelocidadDerecha(derecha);
		
	}
	
	public void setVelocidadIzquierda(float velocidad){
		velocidadIzquierda = velocidad;
	}
	
	public void setVelocidadDerecha(float velocidad){
		velocidadDerecha = velocidad;
	}
	
	public void setSensibilidadGiro(float sensibilidad){
		this.sensibilidadGiro = sensibilidad;
	}
	
	public void enviarDatos(){
		bluetooth.addDatos(new Paquete(velocidadIzquierda, velocidadDerecha));
	}
	
	public float getVelocidadIzquierda(){
		return velocidadIzquierda;
	}
	
	public float getVelocidadDerecha(){
		return velocidadDerecha;
	}
	
	public float getVelocidadMaxima(){
		return velocidadMaxima;
	}
	
	public float getSensibilidadGiro(){
		return sensibilidadGiro;
	}
	
	
	
	public static float getValor(float origen, float max, float rMax, float val){
		//El valor val, que est� entre origen-max, ser� devuelto en un rango que va desde 0 hasta rMax.
		float ancho = max-origen;
		float progreso = val-origen;
		
		return ((progreso/ancho)*rMax);
	}
	
	
	public void setEstado(int nuevoEstado){
		estado = nuevoEstado;
	}
	
	public int getEstado(){
		return estado;
	}
	
	public String getInfo(){

		return "X:" + formateador.format(acelerometroX) + 
				"  Y:" + formateador.format(acelerometroY) + 
				"  Z:" + formateador.format(acelerometroZ);
	}

	
}
