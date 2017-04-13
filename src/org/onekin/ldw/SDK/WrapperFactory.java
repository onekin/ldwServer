package org.onekin.ldw.SDK;

public interface WrapperFactory {
	   //it creates a new wrapper
   public Wrapper newWrapper(String wrapperFile);
   public String getType ();
   
   
}
