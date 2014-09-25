package leshan.client.lwm2m.bootstrap;

public class BootstrapMessageDeliverer {

	public enum InterfaceTypes {
		BOOTSTRAP,
		REGISTRATION,
		MANAGEMENT,
		REPORTING;
	}

	public enum OperationTypes {
		CREATE,
		DELETE,
		DEREGISTER,
		DISCOVER,
		EXECUTE,
		NOTIFY,
		OBSERVE,
		READ,
		REQUEST,
		REGISTER,
		UPDATE,
		WRITE,
		WRITE_ATTRIBUTES;
	}

}
