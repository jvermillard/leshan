/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package leshan.server.event;

import leshan.server.lwm2m.client.RegistryListener;
import leshan.server.lwm2m.observation.ResourceObserver;

/**
 * bundle the existing interfaces into one
 */
public interface EventListener extends RegistryListener, ResourceObserver {
  
}
