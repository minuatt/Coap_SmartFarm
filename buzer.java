package week11;
import java.util.List;


import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.rest.BasicCoapResource;
import org.ws4d.coap.core.rest.CoapData;
import org.ws4d.coap.core.tools.Encoder;
import com.pi4j.io.gpio.*;

public class buzer extends BasicCoapResource{
	private String bu = "off";
	GpioController gpio;
	GpioPinDigitalOutput on_bu;
	private buzer(String path, String value, CoapMediaType mediaType) {
		super(path, value, mediaType);
		
	}

	public buzer() {
		this("/buzer", "off", CoapMediaType.text_plain);
		gpio = GpioFactory.getInstance();
		on_bu = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, PinState.LOW);
	}

	@Override
	public synchronized CoapData get(List<String> query, List<CoapMediaType> mediaTypesAccepted) {
		return get(mediaTypesAccepted);
	}
	
	@Override
	public synchronized CoapData get(List<CoapMediaType> mediaTypesAccepted) {
		return new CoapData(Encoder.StringToByte(this.bu), CoapMediaType.text_plain);
	}

	@Override
	public synchronized boolean setValue(byte[] value) {
		this.bu = Encoder.ByteToString(value);
		if(this.bu.equals("on")) {
			on_bu.high();
		}else if(this.bu.equals("off")) {
			on_bu.low();
		}
		return true;
	}
	
	@Override
	public synchronized boolean post(byte[] data, CoapMediaType type) {
		return this.setValue(data);
	}

	@Override
	public synchronized boolean put(byte[] data, CoapMediaType type) {
		return this.setValue(data);
	}

	@Override
	public synchronized String getResourceType() {
		return "Raspberry pi 4 LED";
	}

}