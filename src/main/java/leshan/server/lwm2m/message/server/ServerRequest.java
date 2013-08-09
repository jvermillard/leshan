package leshan.server.lwm2m.message.server;

import org.apache.mina.filter.query.Request;

/**
 * A request expecting a response from the LW-M2M client.
 */
public interface ServerRequest extends ServerMessage, Request {

}
