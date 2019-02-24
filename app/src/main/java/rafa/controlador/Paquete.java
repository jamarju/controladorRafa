package rafa.controlador;

public class Paquete {
	private byte[] datos = null;

	public Paquete(float velocidadIzquierda, float velocidadDerecha) {
		datos = empaquetaVelocidades(velocidadIzquierda, velocidadDerecha);
	}

	public byte[] getDatos() {
		return datos;
	}

	private byte [] empaquetaVelocidades(float velocidadIzquierda, float velocidadDerecha) {
		float velIzq = 0, velDer = 0;
		byte direcciones = 0;

		final byte izquierdaPositivo = 1; //Máscara
		final byte derechaPositivo = 2; //Máscara

		if (velocidadIzquierda>0){
			direcciones = (byte) (direcciones|izquierdaPositivo);
			velIzq = velocidadIzquierda;
		}
		else{
			//Se convierte en positivo.
			velIzq = -velocidadIzquierda;
		}

		if (velocidadDerecha>0){
			direcciones = (byte) (direcciones|derechaPositivo);
			velDer = velocidadDerecha;
		}
		else{
			velDer = -velocidadDerecha;
		}
		
		return new byte[]{(byte) 255,(byte)velIzq,(byte)velDer,direcciones};
	}

	@Override
	public String toString() {
		String s = "" + ((int)datos[0] & 0xff);
		for (int i = 1; i < 4; i++) {
			s += "," + ((int)datos[i] & 0xff);
		}
		return s;
	}
		
}
