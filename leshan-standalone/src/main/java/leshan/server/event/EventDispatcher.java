/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leshan.server.event;

import java.util.Set;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.RegistryListener;
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.ResourceSpec;
import leshan.server.lwm2m.observation.ResourceObserver;
import org.eclipse.jetty.util.ConcurrentHashSet;

/**
 *
 * @author root
 */
public class EventDispatcher implements RegistryListener, ResourceObserver {

  private static EventDispatcher instance;

  // Note: alternatively we could keep the separate interfaces and maintain two sets
  private final Set<EventListener> eventListeners = new ConcurrentHashSet<>();

  private EventDispatcher() {
  }

  public static EventDispatcher getInstance() {
    if (null == instance) {
      instance = new EventDispatcher();
    }
    return instance;
  }

  public void addEventListener(EventListener eventListener) {
    eventListeners.add(eventListener);
  }

  public void removeEventListener(EventListener eventListener) {
    eventListeners.remove(eventListener);
  }

  /**
   * forward the received event to all eventListeners
   * {@inheritDoc }
   */
  @Override
  public void registered(Client client) {
    for ( EventListener eventListener : eventListeners) {
      eventListener.registered( client);
    }
  }

  /**
   * forward the received event to all eventListeners
   * {@inheritDoc }
   */
  @Override
  public void updated(Client clientUpdated) {
    for ( EventListener eventListener : eventListeners) {
      eventListener.updated( clientUpdated);
    }
  }

  /**
   * forward the received event to all eventListeners
   * {@inheritDoc }
   */
  @Override
  public void unregistered(Client client) {
    for ( EventListener eventListener : eventListeners) {
      eventListener.unregistered( client);
    }
  }

  /**
   * forward the received event to all eventListeners
   * {@inheritDoc }
   */
  @Override
  public void notify(byte[] content, ContentFormat contentFormat, ResourceSpec target) {
    for ( EventListener eventListener : eventListeners) {
      eventListener.notify(content, contentFormat, target);
    }
  }

}
