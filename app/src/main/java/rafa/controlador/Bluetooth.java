package rafa.controlador;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class Bluetooth {
	private BluetoothDevice dispositivo;
	private BluetoothSocket socket;
	private Thread hiloConector;
	private OutputStream outputStream;
	private InputStream inputStream;

	private Runnable runnableConector = new Runnable() {
		@Override
		public void run() {
			if (dispositivo!=null){
				Log.w("Info", "Hay dispositivo emparejado.");
				try {
					socket = dispositivo.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
					//El método connect() bloquea el programa, es necesario ponerlo en un hilo.
					socket.connect();
					Log.d("Info","Conectado");
					inputStream = socket.getInputStream();
					outputStream = socket.getOutputStream();

				} catch (IOException e) {
					Log.e("Info","Error al conectar:"+e.getMessage());
				}
			}
		}
	};

	public Bluetooth(BluetoothDevice dispositivo, int tamano) {
		this.dispositivo = dispositivo;
	}

	public void conectar() {
		if (hiloConector == null) {
			hiloConector = new Thread(runnableConector);
			hiloConector.start();
		}
	}

	public void parar() {
		try {
			// Cierra out
			if (outputStream != null) {
				// Espera a que se envíen todos los datos
				outputStream.flush();
				outputStream.close();
				outputStream = null;
			}
			
			// Cierra in
			if (inputStream != null) {
				inputStream.close();
			}
			
			// Cierra el socket
			if (socket!=null){
				socket.close();
				socket = null;
				Log.w("Info","Socket cerrado.");
			}

			// Espera al hilo conector
			hiloConector.join();
			hiloConector = null;
			Log.w("Info", "Fin del hilo conector");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addDatos(Paquete paquete) {
		if (socket != null && outputStream!=null) {
			try {
				outputStream.write(paquete.getDatos());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
