/*
 * Copyright © 2026 VenaNocta (venanocta@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.artifact.lifecycle.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class InheritanceUtil {

	public static int distance(final Class<?> superType, final Class<?> sourceType) {
		if (superType == null || sourceType == null || !superType.isAssignableFrom(sourceType))
			return -1;
		if (superType == sourceType)
			return 0;

		final Set<Class<?>> set = new LinkedHashSet<>();
		set.add(sourceType);
		int depth = 1;

		while (!set.isEmpty()) {
			final ArrayList<Class<?>> col = new ArrayList<>(set);
			set.clear();
			for (Class<?> type : col) {
				// search class
				final Class<?> clazz = type.getSuperclass();
				if (clazz != null) {
					if (superType == clazz)
						return depth;
					if (superType.isAssignableFrom(clazz))
						set.add(clazz);
				}
				// search interfaces
				for (Class<?> iface : type.getInterfaces()) {
					if (superType == iface)
						return depth;
					if (superType.isAssignableFrom(iface))
						set.add(iface);
				}
			}
			depth = depth + 1;
		}
		// fallback = theoretically unreachable
		return -1;
	}
}
