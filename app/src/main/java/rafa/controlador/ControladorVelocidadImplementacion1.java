package rafa.controlador;

public class ControladorVelocidadImplementacion1 extends ControladorVelocidad {

	private float anguloCabeceo, anguloAlabeo;
	
	private CuandoAnguloCambie anguloHorizonalCambieListener;

	public ControladorVelocidadImplementacion1(Bluetooth bluetooth, GestorVoz gestorVoz) {
		super(bluetooth);
		anguloHorizonalCambieListener = null;
		
		if (gestorVoz!=null){
			this.setCuandoAnguloHorizontalCambieListener(gestorVoz);
		}
	}
	

	public void actualizarDatosAcelerometro(float x, float y, float z){
		super.actualizarDatosAcelerometro(x, y, z);
		
		setEstado(0); //Se resetea el estado.
		
		float moduloG = (float) Math.sqrt(acelerometroX * acelerometroX +
										  acelerometroY * acelerometroY +
										  acelerometroZ * acelerometroZ);
		anguloCabeceo = (float)(Math.atan2((double)acelerometroZ,(double)acelerometroX)/Math.PI*180);
		anguloAlabeo = (float) ((Math.asin(acelerometroY / moduloG)) * 180.0 / Math.PI);
		
		// 50-90 grados: marcha adelante
		if (anguloCabeceo>=50){
			if (anguloCabeceo>=90){
				//Si está muy inclinado, se establece la velocidad al máximo.
				this.setVelocidad(velocidadMaxima,velocidadMaxima);
				
				if (anguloHorizonalCambieListener!=null){
					anguloHorizonalCambieListener.cuandoAnguloCambie(CuandoAnguloCambie.VERTICAL, CuandoAnguloCambie.MAXIMO);
				}
			}
			else{
				//Si no, se establece una velocidad proporcional a la inclinaci�n.
				float vel = getValor(50,90,velocidadMaxima,anguloCabeceo);
				this.setVelocidad(vel,vel);
			}
		}
		// 40-50 grados: freno
		else if (anguloCabeceo>=40){
			this.setVelocidad(0,0);
		}
		// 0-40 grados: marcha atrás
		else{
			if (anguloCabeceo < 0){
				//Si est� muy inclinado, se establece la velocidad al m�ximo.
				this.setVelocidad(-velocidadMaxima, -velocidadMaxima);
				
				if (anguloHorizonalCambieListener!=null){
					anguloHorizonalCambieListener.cuandoAnguloCambie(CuandoAnguloCambie.VERTICAL, CuandoAnguloCambie.MAXIMO);
				}
			}
			else{
				//Si no, velocidad proporcional.
				float vel = getValor(40,0,velocidadMaxima,anguloCabeceo);
				this.setVelocidad(-vel,-vel);
			}
		}
		
		//////////////////////////////////////////////
		/// GIRO /////////////////////////////////////
		//////////////////////////////////////////////
		
		if (anguloAlabeo<-5){
			//Hacia la izquierda.
			float incrIzquierda = 0, incrDerecha = 0;
			
			if (anguloAlabeo>-45){
				incrDerecha = getValor(-5,-45,this.getVelocidadDerecha()/getSensibilidadGiro(),anguloAlabeo);
				incrIzquierda = incrDerecha;
			}
			else{
				//Al m�ximo.
				incrDerecha = this.getVelocidadDerecha()/getSensibilidadGiro();
				incrIzquierda = incrDerecha;
				setEstado(EstadoGiradoAlMaximo);
				
				if (anguloHorizonalCambieListener!=null){
					anguloHorizonalCambieListener.cuandoAnguloCambie(CuandoAnguloCambie.HORIZONTAL, CuandoAnguloCambie.MAXIMO);
				}
			}
			
			this.setVelocidad(this.getVelocidadIzquierda()-incrIzquierda, this.getVelocidadDerecha()+incrDerecha);
		}
		else if (anguloAlabeo>5){
			//Hacia la derecha.
			float incrIzquierda = 0, incrDerecha = 0;
			
			if (anguloAlabeo<45){
				incrIzquierda = getValor(5,45,this.getVelocidadIzquierda()/getSensibilidadGiro(),anguloAlabeo);
				incrDerecha = incrIzquierda;
			}
			else{
				incrIzquierda = this.getVelocidadDerecha()/getSensibilidadGiro();
				incrDerecha = incrIzquierda;
				setEstado(EstadoGiradoAlMaximo);
				
				if (anguloHorizonalCambieListener!=null){
					anguloHorizonalCambieListener.cuandoAnguloCambie(CuandoAnguloCambie.HORIZONTAL, CuandoAnguloCambie.MAXIMO);
				}
			}
			this.setVelocidad(this.getVelocidadIzquierda()+incrIzquierda, this.getVelocidadDerecha()-incrDerecha);
		}
	}
	
	
	public String getInfo(){
		return super.getInfo() + 
				"\nC:" + formateador.format(anguloCabeceo) +
				"  A:" + formateador.format(anguloAlabeo);
	}
	
	public void setCuandoAnguloHorizontalCambieListener(CuandoAnguloCambie c){
		this.anguloHorizonalCambieListener = c;
	}


}
