/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.undertow.async.util;

import sun.misc.Unsafe;

/**
 * 
 * @author hank.whu@gmail.com
 *
 */
public final class UnsafeUtils {
	final static private Unsafe _unsafe;

	static {
		Unsafe tmpUnsafe = null;

		try {
			java.lang.reflect.Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			tmpUnsafe = (sun.misc.Unsafe) field.get(null);
		} catch (java.lang.Exception e) {
			throw new RuntimeException(e);
		}

		_unsafe = tmpUnsafe;
	}

	public static final Unsafe unsafe() {
		return _unsafe;
	}
}
