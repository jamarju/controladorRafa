package rafa.controlador;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;



public class Controlador extends Activity implements SensorEventListener, OnClickListener, OnCheckedChangeListener,
OnSeekBarChangeListener, OnTouchListener, OnInitListener {

	protected static final DecimalFormat formateador = new DecimalFormat("* ###0.00");
	private static final int RETARDO_ACELEROMETRO = SensorManager.SENSOR_DELAY_GAME;
	private static final int REFRESCO_PANTALLA = 100;	// ms en refrescar los valores numericos en pantalla
	private SensorManager mSensorManager;
	private Sensor acelerometro;
	private float acelerometroX, acelerometroY, acelerometroZ;
	
	private TextView coordenadas, tvVelocidadIzquierda,tvVelocidadDerecha;
	private CheckBox cBloquear;
	private SeekBar sbVelocidadMaxima, sbSensibilidadGiro;
	private Button bParar, bLento, bNormal, bRapido;
	private Velocimetro elVelocimetro;
	
	private boolean parado;
	
	private BluetoothDevice dispositivo;
	private Bluetooth bluetooth;
	
	private ControladorVelocidad elControlador;
	
	private Vibrator vibrador;
	
	//private GestorVoz gestorVoz;
	
	private AudioTrack sonido;
	private long ultimoRefresco;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_controlador);

		sbVelocidadMaxima = (SeekBar)findViewById(R.id.barraVelocidadMaxima);
		sbVelocidadMaxima.setOnSeekBarChangeListener(this);
		
		sbSensibilidadGiro = (SeekBar)findViewById(R.id.barraSensibilidadGiro);
		sbSensibilidadGiro.setOnSeekBarChangeListener(this);
		
		tvVelocidadIzquierda = (TextView)findViewById(R.id.tvVelocidadIzquierda);
		tvVelocidadDerecha = (TextView)findViewById(R.id.tvVelocidadDerecha);
		
		coordenadas = (TextView)this.findViewById(R.id.lCoordenadas);
		cBloquear = (CheckBox)findViewById(R.id.cBloquear);
		cBloquear.setOnCheckedChangeListener(this);
		
		bParar = (Button)findViewById(R.id.bParar);
		bParar.setOnClickListener(this);
		bParar.setOnTouchListener(this);
		
		bLento = (Button)findViewById(R.id.bLento);
		bLento.setOnClickListener(this);
		
		bNormal = (Button)findViewById(R.id.bNormal);
		bNormal.setOnClickListener(this);
		
		bRapido = (Button)findViewById(R.id.bRapido);
		bRapido.setOnClickListener(this);

		elVelocimetro = new Velocimetro(this);
		
		Bundle bundle = this.getIntent().getExtras();
		
		if (bundle!=null){
			dispositivo = bundle.getParcelable("dispositivo");
		}
		
		sonido = preparaSonido();
		
		bluetooth = new Bluetooth(dispositivo, 4); 

		elControlador = new ControladorVelocidadImplementacion1(bluetooth,null);
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		acelerometro = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, acelerometro, RETARDO_ACELEROMETRO);
		
		//El vibrador
		vibrador = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
	    
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		ultimoRefresco = System.currentTimeMillis();
	}


	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
		elVelocimetro.setEnPanico(true);
		elVelocimetro.pause();
		
		Log.d("Info","Pausado");

	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, acelerometro, RETARDO_ACELEROMETRO);
		elVelocimetro.setEnPanico(false);

		elVelocimetro.resume();
		
		parado = true;
}

	@Override
	public void onStart(){
		super.onStart();
		bluetooth.conectar();
	}
	
	@Override
	public void onStop(){
		super.onStop();
		bluetooth.parar();
	}
	
	public void onDestroy(){
		super.onDestroy();
		//gestorVoz.liberar();
	}
	
	
	////LISTENER DEL SENSOR////
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		//Se multiplica por cien, dejando un n�mero de tres cifras y decimales, y se convierte
		//en integer para quitarle los decimales.
		acelerometroX = (event.values[0]);
		acelerometroY = (event.values[1]);
		acelerometroZ = (event.values[2]);
		
		elControlador.actualizarDatosAcelerometro(acelerometroX, acelerometroY, acelerometroZ);
		if (! parado) {
			elControlador.enviarDatos();
		}
		
		if (elControlador.getEstado()==ControladorVelocidadImplementacion1.EstadoGiradoAlMaximo){
			vibrador.vibrate(50);
		}
		
		///Veloc�metro
		float velocidadMedia = (elControlador.getVelocidadIzquierda() + elControlador.getVelocidadDerecha())/2;
		elVelocimetro.setVelocidad(velocidadMedia, elControlador.getVelocidadMaxima());

		///SONIDO MOTOR (entre 22050 y 64000 Hz)
		float rate = ControladorVelocidad.getValor(0, elControlador.getVelocidadMaxima(), 41950, Math.abs(velocidadMedia));
		sonido.setPlaybackRate((int) (22050 + rate));


		final long t = System.currentTimeMillis();
		if (t - ultimoRefresco > REFRESCO_PANTALLA) {
			tvVelocidadIzquierda.setText(formateador.format(elControlador.getVelocidadIzquierda()));
			tvVelocidadDerecha.setText(formateador.format(elControlador.getVelocidadDerecha()));
			coordenadas.setText(elControlador.getInfo());
			ultimoRefresco = t;
		}
	}
	
	
	///LISTENER DEL BOT�N///
	@Override
	public void onClick(View boton) {
		if (boton==bLento){
			sbVelocidadMaxima.setProgress(30);
		}
		else if (boton==bNormal){
			sbVelocidadMaxima.setProgress(40);
			
		}
		else if (boton==bRapido){
			sbVelocidadMaxima.setProgress(70);
		}

	}
	
	///LISTENER DEL CHECKBOX///

	@Override
	public void onCheckedChanged(CompoundButton view, boolean state) {
		if (view.getId()==cBloquear.getId()){
			bloquearInterfaz(state);
		}
		
	}
	
	///OTRAS COSAS
	public void bloquearInterfaz(boolean estado){
		sbVelocidadMaxima.setEnabled(!estado);
		sbSensibilidadGiro.setEnabled(!estado);
		bLento.setEnabled(!estado);
		bNormal.setEnabled(!estado);
		bRapido.setEnabled(!estado);
	}

	/// LISTENER DE LA BARRA DE VELOCIDAD ///
	public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
		if (sb==sbVelocidadMaxima){
			elControlador.setVelocidadMaxima(progress);
		}
		else if (sb==sbSensibilidadGiro){
			elControlador.setSensibilidadGiro(progress+1);
		}
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.controlador, menu);
		return true;
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		if (view==bParar){
			if (event.getAction()==MotionEvent.ACTION_DOWN){
				parado = false;
				sonido.play();
			}
			else if (event.getAction()==MotionEvent.ACTION_UP){
				parado = true;
				elControlador.setVelocidad(0, 0);
				elControlador.enviarDatos();
				sonido.pause();
			}
		}
		
		
		return false;
	}

	@Override
	public void onInit(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		switch(level) {
		case TRIM_MEMORY_BACKGROUND:
			Log.d("trimlevel", "TRIM_MEMORY_BACKGROUND");
			break;
		case TRIM_MEMORY_COMPLETE:
			Log.d("trimlevel", "TRIM_MEMORY_COMPLETE");
			break;
		case TRIM_MEMORY_MODERATE:
			Log.d("trimlevel", "TRIM_MEMORY_MODERATE");
			break;
		case TRIM_MEMORY_RUNNING_CRITICAL:
			Log.d("trimlevel", "TRIM_MEMORY_RUNNING_CRITICAL");
			break;
		case TRIM_MEMORY_RUNNING_LOW:
			Log.d("trimlevel", "TRIM_MEMORY_RUNNING_LOW");
			break;
		case TRIM_MEMORY_RUNNING_MODERATE:
			Log.d("trimlevel", "TRIM_MEMORY_RUNNING_MODERATE");
			break;
		case TRIM_MEMORY_UI_HIDDEN:
			Log.d("trimlevel", "TRIM_MEMORY_UI_HIDDEN");
			break;
		default:
			Log.d("trimlevel", String.format("%d", level));
			break;
		}
	}
	
	private AudioTrack preparaSonido() {
		InputStream in = getResources().openRawResource(R.raw.motor_stereo_22050hz_16bit_little_endian);
		byte[] buffer;
		try {
			buffer = new byte[in.available()];
			int i = 0;
			while (in.available() > 0) {
				i += in.read(buffer, i, buffer.length);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		AudioTrack at = new AudioTrack(
				AudioManager.STREAM_MUSIC, 
				22050, 
				AudioFormat.CHANNEL_OUT_STEREO,
				AudioFormat.ENCODING_PCM_16BIT,
				buffer.length,
				AudioTrack.MODE_STATIC);
		
		at.write(buffer, 0, buffer.length);
		final int bytesPorFrame = 4;	// 2 bytes * 2 canales; en mono = 2
		at.setLoopPoints(0, buffer.length / bytesPorFrame, -1);
		return at;

	}
	
}
