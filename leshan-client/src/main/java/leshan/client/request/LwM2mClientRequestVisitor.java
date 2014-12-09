package leshan.client.request;

public interface LwM2mClientRequestVisitor {
	void visit(RegisterRequest request);
	
	void visit(DeregisterRequest request);

	void visit(UpdateRequest updateRequest);

	void visit(BootstrapRequest bootstrapRequest);
	
}
