package leshan.client.lwm2m.object;

public class Operations {
	
	private static final byte READ    = 0x1;
	private static final byte WRITE   = 0x1 << 1;
	private static final byte EXECUTE = 0x1 << 2;
	
	public static final boolean isReadable(byte operationAllowed)  {
		return (READ & operationAllowed) == READ;
	}
	
	public static final boolean isWritable(byte operationAllowed)  {
		return (WRITE & operationAllowed) == WRITE;
	}
	
	public static final boolean isExecutable(byte operationAllowed)  {
		return (EXECUTE & operationAllowed) == EXECUTE;
	}
}
