package rafa.controlador;

import java.io.File;
import java.io.IOException;

import rafa.controlador.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Velocimetro {

	private static final Point centroVelocimetro = new Point(402, 392);
	private static final Point centroAguja = new Point(295, 16);
	private static final float kFiltro = 0.5f;

	SurfaceHolder holder;
	SurfaceView superficie;

	Paint pincel;

	String velocidad; //String que representa la velocidad media de los dos motores.
	int colorVelocidad; //Color que se utilizar� al dibujar el texto. Useless!

	Thread hilo;
	boolean renderizando;

	Bitmap imagenVelocimetro;
	Bitmap imagenAguja;
	float agujaX, agujaY;

	float angulo;
	private boolean enPanico;

	private Runnable runnable = new Runnable() { 
		@Override
		public void run() {
			while (renderizando){
				if (!holder.getSurface().isValid()){
					continue;
				}

				Canvas c = holder.lockCanvas();

				if (enPanico)
					c.drawARGB(255, 255, 0, 0);
				else
					c.drawARGB(0, 0, 0, 0);

				if (superficie.getHeight()!=0){
					final float factorEscala = (float)superficie.getHeight() / (float)imagenVelocimetro.getHeight() ;
					final int estadoCanvas = c.save();

					// Escala el canvas para que la referencia sea el tamaño del velocimetro
					c.scale(factorEscala, factorEscala);

					// Fondo
					final Point origenVelocimetro = new Point((int) ((c.getWidth()/factorEscala)/2 - imagenVelocimetro.getWidth()/2), 0);
					c.drawBitmap(imagenVelocimetro, origenVelocimetro.x, origenVelocimetro.y, null);

					// Texto
					pincel.setTextSize((int)(imagenVelocimetro.getHeight()*0.2));
					c.drawText(velocidad, origenVelocimetro.x + imagenVelocimetro.getWidth()/2, origenVelocimetro.y + imagenVelocimetro.getHeight()/2, pincel);

					// Aguja
					Matrix m = new Matrix();
					m.setTranslate(-centroAguja.x, -centroAguja.y);	// centra el eje de la aguja en (0,0)
					m.postRotate(angulo);
					m.postTranslate(origenVelocimetro.x + centroVelocimetro.x, origenVelocimetro.y + centroVelocimetro.y);
					c.drawBitmap(imagenAguja, m, null);

					// Expande/contrae todo lo que haya dibujado en el canvas para que ocupe la superficie
					c.restoreToCount(estadoCanvas);
				}
				holder.unlockCanvasAndPost(c);

				try {
					Thread.sleep(60);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			Log.d("Info", "Fin del hilo de renderizado, Velocimetro");
		}
	};


	public Velocimetro(Activity activity){
		superficie = (SurfaceView)activity.findViewById(R.id.superficie);

		holder = superficie.getHolder();

		pincel = new Paint();
		pincel.setTypeface(Typeface.createFromAsset(activity.getAssets(), "digital-7.ttf"));
		pincel.setTextAlign(Align.CENTER);
		pincel.setColor(Color.rgb(0, 255, 0));

		try {
			imagenVelocimetro = BitmapFactory.decodeStream(activity.getAssets().open("velocimetro.png"));
			imagenAguja = BitmapFactory.decodeStream(activity.getAssets().open("aguja.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		angulo = 0;
		velocidad = "";
	}


	public void pause(){
		renderizando = false;

		while (true){
			try {
				Log.d("Info", "Se inicia la espera...");
				hilo.join();
				hilo = null;
				Log.d("Info", "Yast�...");
				break;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void resume(){
		renderizando = true;
		hilo = new Thread(runnable);
		hilo.start();
	}

	private void setAngulo(float angulo){
		this.angulo = angulo * kFiltro + this.angulo * (1 - kFiltro);

		if (this.angulo<0){
			this.angulo = 0;
		}
		if (this.angulo>=180f){
			this.angulo = 179f;
		}
	}

	public void setVelocidad(float velocidadMedia, float velocidadMaxima){
		// Colorea
		if (velocidadMedia == 0) {
			setColorVelocidad(Color.rgb(0, 255, 0));
		} else if (velocidadMedia >= velocidadMaxima || velocidadMedia <= -velocidadMaxima){
			setColorVelocidad(Color.rgb(255, 0, 0));
		} else {
			setColorVelocidad(Color.rgb(0, 255, 255));
		}

		this.velocidad = String.format("%+6.1f", velocidadMedia);
		setAngulo(ControladorVelocidad.getValor(0, velocidadMaxima, 180, Math.abs(velocidadMedia)));

	}

	private void setColorVelocidad(int color){
		this.colorVelocidad = color;
		pincel.setColor(color);
	}

	public void setEnPanico(boolean enPanico) {
		this.enPanico = enPanico;
	}


}
