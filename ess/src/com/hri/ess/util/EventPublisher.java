package com.hri.ess.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class EventPublisher {
	private HashMap<EnumSubjectEvents, List<IEventNotify>> hashNotify;

	public EventPublisher() {
		this.hashNotify = new HashMap<EnumSubjectEvents, List<IEventNotify>>();
	}
	
	private static EventPublisher publisher;
	public static EventPublisher getIntance(){
		if(publisher == null){
			publisher = new EventPublisher();
		}		
		return publisher;
	}

	public void subject(EnumSubjectEvents event, IEventNotify iEventNotify) {
		if (!this.hashNotify.containsKey(event)) {
			this.hashNotify.put(event, new ArrayList<IEventNotify>());
		}
		List<IEventNotify> lst = this.hashNotify.get(event);
		lst.add(iEventNotify);
	}

	public void unSubject(EnumSubjectEvents event, IEventNotify iEventNotify) {
		if (!this.hashNotify.containsKey(event)) {
			return;
		}
		List<IEventNotify> lst = this.hashNotify.get(event);
		if (lst.contains(iEventNotify)) {
			lst.remove(iEventNotify);
		}
	}

	public void publish(Object sender, EnumSubjectEvents event, Object eArgs, byte cmdId) {
		if (!this.hashNotify.containsKey(event)) {
			return;
		}
		List<IEventNotify> lst = this.hashNotify.get(event);
		for (int i = 0; i < lst.size(); i++) {
			IEventNotify iEventNotify = lst.get(i);
			iEventNotify.Notify(sender, event, eArgs,cmdId);
		}
	}
}
