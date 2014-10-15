package leshan.server.lwm2m.resource.proxy;


public abstract class ExchangeProxy {

	public abstract RequestProxy getRequest();

	public abstract void respondWithBadRequest();

}
