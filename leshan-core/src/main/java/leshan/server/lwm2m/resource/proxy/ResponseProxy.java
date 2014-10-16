package leshan.server.lwm2m.resource.proxy;

import leshan.server.lwm2m.request.CoapResponseCode.ResponseCode;

public abstract class ResponseProxy {
	private final ResponseCode code;

	public ResponseProxy(final ResponseCode code) {
		this.code = code;
	}

	public final ResponseCode getCode(){
		return code;
	}
	
	public boolean isSuccess() {
		return true;
	}
	
	public String getErrorMessage() {
		throw new UnsupportedOperationException("No Error Messages in default ResponseProxies");
	}

	public static ResponseProxy failure(final String localizedMessage, final ResponseCode code) {
		return new FailureResponseProxy(localizedMessage, code);
	}

	private static final class FailureResponseProxy extends ResponseProxy{
		
		private final String errorMessage;

		public FailureResponseProxy(final String localizedMessage, final ResponseCode code) {
			super(code);
			this.errorMessage = localizedMessage;
		}

		@Override
		public boolean isSuccess(){
			return false;
		}
		
		@Override
		public String getErrorMessage(){
			return errorMessage;
		}
	}

}
