package rafa.controlador;

import java.util.Set;

import android.R.layout;
import android.os.Bundle;
import android.os.PowerManager;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class SelectorDispositivo extends Activity implements OnItemClickListener, OnClickListener {
	
	private BluetoothDevice dispositivo;
	ListView listaDispositivos;
	Button bCreditos;
	
	BluetoothAdapter mBluetoothAdapter;
	
	Set<BluetoothDevice> pairedDevices;
	String nombreDispositivos[];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selector_dispositivo);
		try {
			this.setTitle("Ciclotrón "+this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		bCreditos = (Button)this.findViewById(R.id.bCreditos);
		bCreditos.setOnClickListener(this);
		
		//Se obtiene el adaptador del Bluetooth.
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
		    Log.d("Info", "No hay adaptador de Bluetooth.");
		}
		
		//Se comprueba si est� activado.
		if (!mBluetoothAdapter.isEnabled()) {
			//Si no est� activado se inicia una activity que pida al usuario su activaci�n.
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, 1);
		}
		else{
			//Y si est� activo, cargamos la lista de dispositivos emparejados para que el usuario elija.
			cargarListaDispositivosEmparejados();
		}
		
		dispositivo = null;

	}
	
	public void onPause(){
		super.onPause();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
		
		Intent intent = new Intent(this,Controlador.class);
		
		//Se busca el dispositivo con el nombre especificado.
		for (BluetoothDevice device : pairedDevices){
			if (device.getName().compareTo(nombreDispositivos[position])==0){
				dispositivo = device;
				break;
			}
		}
		
		if (dispositivo!=null){
			Log.d("Info","El dispositivo que env�o tiene de nombre: "+dispositivo.getName());
			
			intent.putExtra("dispositivo", dispositivo);
			this.startActivity(intent);
		}
		else{
			Log.d("Info","No hay tal dispositivo.");
		}
		
		
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		//1 es el n�mero que le pasamos como par�metro a startActivityForResult()
		if (requestCode==1){
			if (resultCode==Activity.RESULT_OK){
				
				//Ahora que estamos seguros de que el Bluetooth est� activo, cargamos la lista.
				
				cargarListaDispositivosEmparejados();
			}
		}
	}
	
	public void cargarListaDispositivosEmparejados(){
		pairedDevices = mBluetoothAdapter.getBondedDevices();
		//Se crea un array de strings para almacenar el nombre de los dispositivos emparejados.
		nombreDispositivos = new String[pairedDevices.size()];
		//Iteramos sobre los dispositivos emparejados y vamos a�adiendo sus nombres al array.
		int i = 0;
		for (BluetoothDevice device : pairedDevices){
			nombreDispositivos[i] = device.getName();
			i++;
		}
		
		listaDispositivos = (ListView)this.findViewById(R.id.selectorDispositivo);
		
		ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,nombreDispositivos);
		
		listaDispositivos.setAdapter(adaptador);
		listaDispositivos.setOnItemClickListener(this);
	}

	@Override
	public void onClick(View view) {
		
		if (view==bCreditos){
			Intent intent = new Intent(this,Creditos.class);
			this.startActivity(intent);
		}
		
	}

}
