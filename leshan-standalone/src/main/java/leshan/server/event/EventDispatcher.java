/*
 * Copyright (c) 2014, IT-Designers GmbH
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of {{ project }} nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
 * Singleton class to receive LWM2M events from the underlying core implementation
 * and dispatch them at the application level
 */
public enum EventDispatcher implements RegistryListener, ResourceObserver {

  INSTANCE;

  // Note: alternatively we could keep the separate interfaces and maintain two sets
  private final Set<EventListener> eventListeners = new ConcurrentHashSet<>();

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
