package rafa.controlador;

public interface CuandoAnguloCambie {
	static final int MAXIMO = 1;
	
	static final int HORIZONTAL = 1;
	static final int VERTICAL = 2;
	
	public void cuandoAnguloCambie(int tipo, int estado);
	
}
