package com.github.pfichtner.ardulink.core;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pfichtner.ardulink.core.events.AnalogPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.DigitalPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.EventListener;
import com.github.pfichtner.ardulink.core.events.FilteredEventListenerAdapter;

public abstract class AbstractListenerLink implements Link {

	private static final Logger logger = LoggerFactory
			.getLogger(AbstractListenerLink.class);

	private final List<EventListener> eventListeners = new CopyOnWriteArrayList<EventListener>();

	public Link addListener(EventListener listener) throws IOException {
		if (listener instanceof FilteredEventListenerAdapter) {
			Pin pin = ((FilteredEventListenerAdapter) listener).getPin();
			// old impl did start "startListening" on each addListener, so
			// we do too for the moment
			// TODO should/can we change that behavior?
			// if (!hasListenerForPin(pin)) {
			startListening(pin);
			// }
		}
		this.eventListeners.add(listener);
		return this;
	}

	public Link removeListener(EventListener listener) throws IOException {
		this.eventListeners.remove(listener);
		if (listener instanceof FilteredEventListenerAdapter) {
			Pin pin = ((FilteredEventListenerAdapter) listener).getPin();
			if (!hasListenerForPin(pin)) {
				stopListening(pin);
			}
		}
		return this;
	}

	public void fireStateChanged(AnalogPinValueChangedEvent event) {
		for (EventListener eventListener : this.eventListeners) {
			try {
				eventListener.stateChanged(event);
			} catch (Exception e) {
				logger.error("EventListener {} failure", eventListener, e);
			}
		}
	}

	public void fireStateChanged(DigitalPinValueChangedEvent event) {
		for (EventListener eventListener : this.eventListeners) {
			try {
				eventListener.stateChanged(event);
			} catch (Exception e) {
				logger.error("EventListener {} failure", eventListener, e);
			}
		}
	}

	private boolean hasListenerForPin(Pin pin) {
		for (EventListener listener : this.eventListeners) {
			if (listener instanceof FilteredEventListenerAdapter
					&& pin.equals(((FilteredEventListenerAdapter) listener)
							.getPin())) {
				return true;
			}
		}
		return false;
	}

}