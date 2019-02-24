package rafa.controlador;

import rafa.controlador.CuandoAnguloCambie;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class GestorVoz implements OnInitListener, CuandoAnguloCambie{
	private TextToSpeech voz;
	
	public GestorVoz(Context context){
		//TextToSpeech
		voz = new TextToSpeech(context,this); //Args: context y listener
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cuandoAnguloCambie(int tipo, int estado) {
		if (!voz.isSpeaking()){
			
			if (tipo== HORIZONTAL){
				if (estado== MAXIMO){
					//voz.speak("Giro máximo.", TextToSpeech.QUEUE_FLUSH, null);
				}
			}
			else if (tipo== VERTICAL){
				if (estado== MAXIMO){
					//voz.speak("Velocidad máxima.", TextToSpeech.QUEUE_FLUSH, null);
				}
			}
			
		}
	}
	
	public void liberar(){
		voz.shutdown();
	}
}
