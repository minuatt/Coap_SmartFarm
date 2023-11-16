# Coap_SmartFarm
한림대학교 2021학년도 1학기 임베디드시스템 미니프로젝트(시연영상 : https://www.youtube.com/watch?v=rAMpOdtfIlM)
## 개요
&nbsp;한림대학교 2021학년도 1학기 임베디드시스템 미니프로젝트로 Coap기반 IOT서비스 아이디어로 스마트팜에 적용시킬 수 있는 기술들을 만들어보았다. 센서를 통해 자동적으로 값을 받아온 후 일정 온도와 습도가 변화하게되면 설치한 부저에서 벨이 울리도록 작동시켰다. 이는 자동적으로 농장을 운영할 수 있는 온,습도 관리체게에 활용될 수 있을것이다.

1. 생육환경 유지관리 소프트웨어에 적용시킬수있다.
2. 환경정보 모니터링이 가능하다(온,습도 일사량, Co2, 생육환경 등 자동으로 수집된다
3. 자동/원격 환경관리가 가능하다.
## 실행 요약
&nbsp;프로젝트는 자바와 Coap간의 통신으로 이루어진다. </br>
- [Temp_Sensor]
```java
public class Temp_sensor extends BasicCoapResource{

	private String value = "0";
	DHT11 dht = new DHT11();
	
	private Temp_sensor(String path, String value, CoapMediaType mediaType) {
		super(path, value, mediaType);
	}
	public Temp_sensor() {
		this("/temperature", "0", CoapMediaType.text_plain);
	}
 }
```
- 먼저 온도센서를 제어하기 위한 Temp_sensor 클래스를 만들어주고, DHT11클래스의 객체를 생성한다. Temp_Sensor의 생성자로 path는 /temperature, 초기값은 0으로 설정하였다.
```java
@Override
	public synchronized CoapData get(List<CoapMediaType> mediaTypesAccepted) {
		float[] sensing_data = dht.getData(15);
		this.value = Float.toString(sensing_data[1]);
		return new CoapData(Encoder.StringToByte(this.value), CoapMediaType.text_plain);
	}
	
	public synchronized void optional_changed() {
		float[] sensing_data = dht.getData(15);
		String temp = Float.toString(sensing_data[1]);
		if(temp.equals("-99.0")) {
			System.out.println("There was an error while sensing temp.");
		}
		else {
			this.changed(temp);
			this.value = temp;
		}
		
	}
```
- Coap통신을 위한 optional_change메소드에 온도를 받을수 있는 sensing_data의 1번인덱스의 값을 String값으로 변화하여 받고, -99도가 나오면 잘읽어오지 못한것으로 print문이 나오도록 설정하였다. </br>
</br>

- [LED]
```java  
 public class LED extends BasicCoapResource{
	private String state = "off";
	GpioController gpio;
	GpioPinDigitalOutput r_led;
	GpioPinDigitalOutput g_led;
	GpioPinDigitalOutput b_led;
	
	private LED(String path, String value, CoapMediaType mediaType) {
		super(path, value, mediaType);
		
	}

	public LED() {
		this("/led", "off", CoapMediaType.text_plain);
		gpio = GpioFactory.getInstance();
		r_led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_08, PinState.LOW);
		g_led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_09, PinState.LOW);
		b_led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, PinState.LOW);
	}
```
- 처음 state상태는“off” 로 설정하고, path는 /led로 설정한다.</br>
<빨간색>r_led는 Gpio_08(WPI)로 받아주고, 상태는 LOW</br>
<파란색>b_led는 Gpio_07(WPI)로 받아주고, 상태는 LOW</br>
<초록색>g_led 는 Gpio_09(WPI)로 받아주고, 상태는 LOW</br>
```java
public synchronized boolean setValue(byte[] value) {
		this.state = Encoder.ByteToString(value);
		
		if(this.state.equals("red")) {
			r_led.high();
			g_led.low();
			b_led.low();
		}
		else if(this.state.equals("green")) {
			r_led.low();
			g_led.high();
			b_led.low();
			
		}
		else if(this.state.equals("blue")) {
			r_led.low();
			g_led.low();
			b_led.high();
			
		}
		else if(this.state.equals("off")) {
			r_led.low();
			g_led.low();
			b_led.low();
		}
		return true;
	}
```
- State가 “red”와 같으면 r_led를 high로 올려 빨간색을킨다.</br>
“green” 와 같으면 g_led를 high로 올려 초록색을 킨다.</br>
“blue” 와 같으면 b_led를 high로 올려 파란색을 킨다.</br>
“off”와 같으면 led를 끈다.</br>
</br>

- [buzer]
```java
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
```
- Buzer를 컨트롤 하기위한 buzer클래스를 만들고, path는 /buzer, 상태는 “off”로 설정한다. 
```java
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
```
- Buzer의 전압을 받을 수 있는  핀은 GPIO_02번으로 설정하고, 핀의 상태는 LOW로 설정한다.</br>
Buzer클래스의 변수bu가 “on”과 같으면 high를줘 전압을 준뒤 부저가 울리게 한다.</br>
변수 bu가“off”와 같으면 low를 줘서 부저를 끈다.</br>
</br>

- [Coap_server]
```java
 LED led = new LED();
		Temp_sensor temp_sensor = new Temp_sensor();
		buzer bu = new buzer();
		temp_sensor.registerServerListener(resourceServer);
		
		// add resource to server
		this.resourceServer.createResource(temp_sensor);
		this.resourceServer.createResource(led);
		this.resourceServer.createResource(bu);
```
- Led, Temp_Sensor,buzzer 클래스를 객체화 한다
```java
	// run the server
		try {
			this.resourceServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		while(true) {
			try {
				Thread.sleep(4000);
				temp_sensor.optional_changed();
			}catch(Exception e) {
				
			}
		}
```
- Temp_sensor의 온도값을 4초에 한번씩 계속 읽어오는 것으로 설정하였다.</br>
</br>
  
- [GuI_Client]
 ```java
 btn_get.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String path = path_text.getText();
				String payload = payload_text.getText();				
				CoapRequest request = clientChannel.createRequest(CoapRequestCode.GET, path, true);
				displayRequest(request);
				clientChannel.sendMessage(request);
			}
		});
```
- 온도 측정을 시작하면 요청하는 값에 obTocken으로 Token값을 추가하였다.
```java
if(Encoder.ByteToString(response.getToken()).equals("obTocken")) {
			float temp = Float.parseFloat(Encoder.ByteToString(response.getPayload()));
			//temp값에 따라서 led resource에 대한 PUT 요청메세지 전송
			this.control_led(temp);
			this.control_buzer(temp);
		}
```
- 요청한값에 obTocken이 있으면 led의 buzer의 (temp)값에 따른 put요청 메시지를 전송한다.
```java
  public void control_led(float temp) {
		
		if(temp >= 25) {
			//red
			CoapRequest request = clientChannel.createRequest(CoapRequestCode.PUT, "/led", true);
			request.setPayload(new CoapData("red", CoapMediaType.text_plain));
			displayRequest(request);
			clientChannel.sendMessage(request);
		}else if(temp == -99.0){
			//blue
			CoapRequest request = clientChannel.createRequest(CoapRequestCode.PUT, "/led", true);
			request.setPayload(new CoapData("blue", CoapMediaType.text_plain));
			displayRequest(request);
			clientChannel.sendMessage(request);
		}else {
			//green
			CoapRequest request = clientChannel.createRequest(CoapRequestCode.PUT, "/led", true);
			request.setPayload(new CoapData("green", CoapMediaType.text_plain));
			displayRequest(request);
			clientChannel.sendMessage(request);
		}
	}
 ```
- Temp값을 매개변수로하는 led control 메소드이다.</br>

Temp가 25보다 크면 “red”를 led 객체에 전달(빨간불 뜸)</br>
Temp가 -99보다 크면 “blue”를 led객체에 전달(파란색뜸)</br>
그외의 상황에선 “green”을 led객체에 전달(초록색뜸)</br>

```java
public void control_buzer(float temp) {
		
		if(temp >=25) {
			//buzer on
			CoapRequest request = clientChannel.createRequest(CoapRequestCode.PUT, "/buzer", true);
			request.setPayload(new CoapData("on", CoapMediaType.text_plain));
			displayRequest(request);
			clientChannel.sendMessage(request);
		}else if(temp == -99.0) {
			//buzer off
			CoapRequest request = clientChannel.createRequest(CoapRequestCode.PUT, "/buzer", true);
			request.setPayload(new CoapData("off", CoapMediaType.text_plain));
			displayRequest(request);
			clientChannel.sendMessage(request);
		}else {
			//buzer off
			CoapRequest request = clientChannel.createRequest(CoapRequestCode.PUT, "/buzer", true);
			request.setPayload(new CoapData("off", CoapMediaType.text_plain));
			displayRequest(request);
			clientChannel.sendMessage(request);
		}
		
	}
```
- Temp값을 매개변수로하는 buzer control 메소드이다.</br>

Temp가 25보다 크면 “on”을 buzer객체에 전달(부저 울림)</br>
Temp가 -99보다 크면 “off”을 buzer객체에 전달(부저 꺼짐)</br>
그외의 상황에선 “off”을 buzer객체에 전달(부저 꺼짐)</br>

```java
private void displayRequest(CoapRequest request){
		String temp = Encoder.ByteToString(request.getPayload());
		if(request.getPayload() != null){
			if(temp.equals("red")) {
			display_text.append("농장의 온도가 25도이상입니다 온도를 조절해주세요");
			display_text.setCaretPosition(display_text.getDocument().getLength());  
		} 
		else if(temp.equals("green")){
			display_text.append("농장의 온도가 정상입니다" );
			display_text.setCaretPosition(display_text.getDocument().getLength());  
		}
		else if(temp.equals("blue")){
			display_text.append("농장의 온도가 제대로 측정되지 않았습니다." );
			display_text.setCaretPosition(display_text.getDocument().getLength()); 
		}
	}
		display_text.append(System.lineSeparator());
		display_text.append("*");
		display_text.append(System.lineSeparator());
	}
```
- Client에 뜨는 정보를 조절하는 메소드이다.</br>
Temp가 “red”와 같으면 “농장의 온도가 25도이상입니다＂출력</br>
Temp가 “green”과 같으면 “농장의 온도가 정상입니다” 출력</br>
Temp가 “blue”와 같으면 “농장의 온도가 제대로 측정되지 않았습니다.” 출력


 

