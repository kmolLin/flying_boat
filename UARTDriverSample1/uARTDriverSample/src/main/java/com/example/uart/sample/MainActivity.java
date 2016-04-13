package com.example.uart.sample;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import coolerd.uart.driver.activity.AbstractSingleUARTActivity;
import coolerd.uart.driver.device.UARTInputDevice;
import coolerd.uart.driver.device.UARTOutputDevice;

public class MainActivity extends AbstractSingleUARTActivity {

	Button a1,a2,a3,a4;
	int t1000=0,t100=0,t10=0,t1=0;
	Context asdadContext;
	TextView dd;
	byte[] sendData = new byte[8];
	int xxxx=0;
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		asdadContext = this;
		a4 = (Button) findViewById(R.id.buttonC);
		a3 = (Button) findViewById(R.id.buttonCis);
		a2 = (Button) findViewById(R.id.buttonD);
		a1 = (Button) findViewById(R.id.buttonDis);
		dd = (TextView) findViewById(R.id.textView3);
		a4.setEnabled(false);
		a3.setEnabled(false);
		a2.setEnabled(false);
		a1.setOnClickListener(new inputDataEvent());
		a2.setOnClickListener(new inputDataEvent());
		a3.setOnClickListener(new inputDataEvent());
		a4.setOnClickListener(new inputDataEvent());
	}
	
	class inputDataEvent implements OnClickListener{

		@Override
		public void onClick(View v) {
			UARTOutputDevice UARTOutputDevice = getUARTOutputDevice();
			
			if(a1.getId() == v.getId())
			{
				for(int i=0;i<8;i++)
					sendData[i] = (byte)(48+i+xxxx);
					UARTOutputDevice.sendUARTMessage(sendData);
					a1.setBackgroundResource(R.drawable.redbuttonimage);
			}
			xxxx++;
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
	} 

	public void onDeviceAttached(final UsbDevice usbDevice) {
		Toast.makeText(this, "UART Adapter is connect...", Toast.LENGTH_SHORT).show();
	}

	public void onDeviceDetached(final UsbDevice usbDevice) {
		Toast.makeText(this, "UART Adapter is unconnect...", Toast.LENGTH_SHORT).show();
	}

	public void ReceivedData(coolerd.uart.driver.device.UARTInputDevice sender,
			String dataByte[]) {
		// TODO Auto-generated method stub
		Toast.makeText(asdadContext, dataByte[0]+":"+dataByte[1]+":"+dataByte[2]+":"+dataByte[3]+":"+dataByte[4]+":"+dataByte[5]+":"+dataByte[6]+":"+dataByte[7], Toast.LENGTH_SHORT).show();
		
	}


}
