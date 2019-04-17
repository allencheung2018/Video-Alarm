package com.hri.ess.util;

public interface IEventNotify {
   public void Notify(Object sender,EnumSubjectEvents event,Object eArgs, byte cmdId);
}
