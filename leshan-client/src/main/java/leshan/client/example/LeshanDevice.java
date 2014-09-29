package leshan.client.example;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.operation.ExecuteResponse;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.resource.LwM2mObjectDefinition;
import leshan.client.lwm2m.resource.SingleResourceDefinition;
import leshan.client.lwm2m.resource.StringLwM2mExchange;
import leshan.client.lwm2m.resource.StringLwM2mResource;
import leshan.client.lwm2m.response.OperationResponse;

/*
 * To build: 
 * mvn assembly:assembly -DdescriptorId=jar-with-dependencies
 * To use:
 * java -jar target/leshan-client-*-SNAPSHOT-jar-with-dependencies.jar 127.0.0.1 5683 9000
 */
public class LeshanDevice {
	private static final int TIMEOUT_MS = 2000;
	private static String deviceLocation;
	private static RegisterUplink registerUplink;

	public static void main(final String[] args) {
		if(args.length < 4){
			System.out.println("Usage:\njava -jar target/leshan-client-*-SNAPSHOT-jar-with-dependencies.jar [Client IP] [Client port] [Server IP] [Server Port]");
		}
		else{
			new LeshanDevice(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]));
		}
	}


	public LeshanDevice(final String localHostName, final int localPort, final String serverHostName, final int serverPort){
		final LwM2mObjectDefinition objectDevice = createObjectDefinition();
		final LwM2mClient client = new LwM2mClient(objectDevice);
		
		//Connect to the server provided
		final InetSocketAddress clientAddress = new InetSocketAddress(localHostName, localPort);
		final InetSocketAddress serverAddress = new InetSocketAddress(serverHostName, serverPort);
		registerUplink = client.startRegistration(clientAddress, serverAddress);
		final OperationResponse operationResponse = registerUplink.register(UUID.randomUUID().toString(), new HashMap<String, String>(), TIMEOUT_MS);
		
		//Report registration response.
		System.out.println("Device Registration (Success? " + operationResponse.isSuccess() + ")");
		if(operationResponse.isSuccess()){
			System.out.println("\tDevice: Registered Client Location '" + operationResponse.getLocation() + "'");
			deviceLocation = operationResponse.getLocation();
		}
		else{
			System.err.println("\tDevice: " + operationResponse.getErrorMessage());
			System.err.println("If you're having issues connecting to the LWM2M endpoint, try using the DTLS port instead");
		}
		
		//Deregister on shutdown.
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				if(deviceLocation != null){
					System.out.println("\tDevice: Deregistering Client '" + deviceLocation + "'");
					registerUplink.deregister(deviceLocation, TIMEOUT_MS);
				}
			}
		});
	}

	private LwM2mObjectDefinition createObjectDefinition() {
		final TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");
		//Create an object model
		final StringValueResource manufacturerResource = new StringValueResource("Leshan Example Device", 0);
		final StringValueResource modelResource = new StringValueResource("Model 500", 1);
		final StringValueResource serialNumberResource = new StringValueResource("LT-500-000-0001", 2);
		final StringValueResource firmwareResource = new StringValueResource("1.0.0", 3);
		final ExecutableResource rebootResource = new ExecutableResource(4);
		final ExecutableResource factoryResetResource = new ExecutableResource(5);
		final StringValueResource powerSourceVoltageResource = new StringValueResource("5.02V", 7);
		final StringValueResource batteryLevelResource = new StringValueResource("92%", 9);
		final MemoryFreeResource memoryFreeResource = new MemoryFreeResource();
		final StringValueResource errorCodeResource = new StringValueResource("0", 11);
		final TimeResource currentTimeResource = new TimeResource();
		final StringValueResource utcOffsetResource = new StringValueResource(Integer.toString(timeZone.getOffset(System.currentTimeMillis())), 14);
		final StringValueResource timezoneResource = new StringValueResource(timeZone.getDisplayName(), 15);
		final StringValueResource bindingsResource = new StringValueResource("U", 16);
		
		final LwM2mObjectDefinition objectDevice = new LwM2mObjectDefinition(3, true, true,
				new SingleResourceDefinition(0, manufacturerResource, true),
				new SingleResourceDefinition(1, modelResource, true),
				new SingleResourceDefinition(2, serialNumberResource, true),
				new SingleResourceDefinition(3, firmwareResource, true),
				new SingleResourceDefinition(4, rebootResource, true),
				new SingleResourceDefinition(5, factoryResetResource, true),
				new SingleResourceDefinition(7, powerSourceVoltageResource, true),
				new SingleResourceDefinition(9, batteryLevelResource, true),
				new SingleResourceDefinition(10, memoryFreeResource, true),
				new SingleResourceDefinition(11, errorCodeResource, true),
				new SingleResourceDefinition(12, new ExecutableResource(12), true),
				new SingleResourceDefinition(13, currentTimeResource, true),
				new SingleResourceDefinition(14, utcOffsetResource, true),
				new SingleResourceDefinition(15, timezoneResource, true),
				new SingleResourceDefinition(16, bindingsResource, true));
		return objectDevice;
	}
	
	public class TimeResource extends StringLwM2mResource {
		public void setValue(final String newValue) {
			notifyResourceUpdated();
		}

		public String getValue() {
			return Double.toString(System.currentTimeMillis());
		}

		@Override
		public void handleWrite(final StringLwM2mExchange exchange) {
			setValue(exchange.getRequestPayload());

			exchange.respondSuccess();
		}

		@Override
		public void handleRead(final StringLwM2mExchange exchange) {
			System.out.println("\tDevice: Reading Current Device Time.");
			exchange.respondContent(getValue());
		}
	}
	
	public class MemoryFreeResource extends StringLwM2mResource {
		public void setValue(final String newValue) {
			notifyResourceUpdated();
		}

		public String getValue() {
			final Random rand = new Random();
			return Integer.toString(114 + rand.nextInt(50)) + "KB";
		}

		@Override
		public void handleWrite(final StringLwM2mExchange exchange) {
			setValue(exchange.getRequestPayload());

			exchange.respondSuccess();
		}

		@Override
		public void handleRead(final StringLwM2mExchange exchange) {
			System.out.println("\tDevice: Reading Memory Free Resource");
			exchange.respondContent(getValue());
		}
	}

	public class StringValueResource extends StringLwM2mResource {

		private String value;
		private final int resourceId;

		public StringValueResource(final String initialValue, final int resourceId) {
			value = initialValue;
			this.resourceId = resourceId;
		}

		public void setValue(final String newValue) {
			value = newValue;
			notifyResourceUpdated();
		}

		public String getValue() {
			return value;
		}

		@Override
		public void handleWrite(final StringLwM2mExchange exchange) {
			System.out.println("\tDevice: Writing on Resource " + resourceId);
			setValue(exchange.getRequestPayload());

			exchange.respondSuccess();
		}

		@Override
		public void handleRead(final StringLwM2mExchange exchange) {
			System.out.println("\tDevice: Reading on Resource " + resourceId);
			exchange.respondContent(value);
		}
		
	}
	
	public class ExecutableResource extends StringLwM2mResource {

		private final int resourceId;

		public ExecutableResource(final int resourceId) {
			this.resourceId = resourceId;
		}

		@Override
		public void handleExecute(final LwM2mExchange exchange) {
			System.out.println("Executing on Resource " + resourceId);
			
			exchange.respond(ExecuteResponse.success());
		}
		
		@Override
		protected void handleWrite(final StringLwM2mExchange exchange) {
			exchange.respondSuccess();
		}

	}

}

