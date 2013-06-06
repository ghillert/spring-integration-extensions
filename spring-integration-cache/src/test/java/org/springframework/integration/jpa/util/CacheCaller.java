package org.springframework.integration.jpa.util;



public interface CacheCaller {
		public Object putData(Object data);
		public Object getData(Object data);
		public Object printData(Object data);

	}
