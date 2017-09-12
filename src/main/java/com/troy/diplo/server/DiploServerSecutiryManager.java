package com.troy.diplo.server;

import java.security.Permission;

public class DiploServerSecutiryManager extends SecurityManager {

	@Override
	public void checkExit(int status) {
		StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
		String callerName = "";
		for (int i = 1; i < stElements.length; i++) {
			StackTraceElement ste = stElements[i];
			if (!ste.getClassName().equals(DiploServerSecutiryManager.class.getName()) && 
					!ste.getClassName().equals(java.lang.Runtime.class.getName()) && !ste.getClassName().equals(java.lang.System.class.getName()) && ste.getClassName().indexOf("java.lang.Thread") != 0) {
				callerName = ste.getClassName();
				break;
			}
		}
		if (!callerName.equals(DiploServer.class.getName()))
			throw new SecurityException("Can only by calling DiploServer.forceShutdown() or DiploServer.shutdown() called by " + callerName);
	}

	@Override
	public void checkPermission(Permission perm) {
		//String permName = perm == null ? "missing" : (perm.getName() != null ? perm.getName() : "missing");
		//Grant all permissions
	}

}
