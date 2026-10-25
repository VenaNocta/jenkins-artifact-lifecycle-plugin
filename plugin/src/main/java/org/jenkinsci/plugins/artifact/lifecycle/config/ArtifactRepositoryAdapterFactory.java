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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import hudson.ExtensionList;
import hudson.ExtensionPoint;

import static org.jenkinsci.plugins.artifact.lifecycle.config.InheritanceUtil.distance;

public interface ArtifactRepositoryAdapterFactory<T extends ArtifactRepository> extends ExtensionPoint {

	public Class<T> targetType();

	public default Class<?> precursorType() {
		return ArtifactRepository.class;
	}

	public T adapt(ArtifactRepository repository);

	@SuppressWarnings("rawtypes")
	public static ExtensionList<ArtifactRepositoryAdapterFactory> all() {
		return ExtensionList.lookup(ArtifactRepositoryAdapterFactory.class);
	}

	@SuppressWarnings("rawtypes")
	public static Stream<ArtifactRepositoryAdapterFactory> all(final Class<? extends ArtifactRepository> targetType) {
		return ExtensionList.lookup(ArtifactRepositoryAdapterFactory.class)
				.stream()
				.filter(Objects::nonNull)
				.filter(f -> {
					final Class<?> target = f.targetType();
					return target != null && targetType.isAssignableFrom(target);
				});
	}

	// TODO: consider a cache like jenkins.model.TransientActionFactory#Cache
	@SuppressWarnings("rawtypes")
	public static Stream<ArtifactRepositoryAdapterFactory> all(final Class<? extends ArtifactRepository> targetType,
			final Class<? extends ArtifactRepository> sourceType) {
		Objects.requireNonNull(targetType);
		Objects.requireNonNull(sourceType);
		// check the amount of valid matches
		final Collection<ArtifactRepositoryAdapterFactory> col = all(targetType) //
				.filter(f -> {
					final Class<?> precursor = f.precursorType();
					return precursor != null && precursor.isAssignableFrom(sourceType);
				})
				.collect(Collectors.toList());
		// 1 - return them
		if (col.size() < 2)
			return col.stream();
		// 2 - order them
		final SortedMap<Integer, Set<ArtifactRepositoryAdapterFactory>> map = new TreeMap<>();
		col.forEach(f -> map.computeIfAbsent(distance(f.precursorType(), sourceType), k -> new LinkedHashSet<>())
				.add(f));
		map.remove(-1);
		return map.values()
				.stream()
				.flatMap(Set::stream);
	}

	public static Set<Class<? extends ArtifactRepository>> allPrecursorsOf(
			final Class<? extends ArtifactRepository> targetType) {
		Objects.requireNonNull(targetType);
		final Set<Class<? extends ArtifactRepository>> result = new HashSet<>();
		all(targetType).map(ArtifactRepositoryAdapterFactory::precursorType)
				.filter(Objects::nonNull)
				.forEach(result::add);
		return Collections.unmodifiableSet(result);
	}
}
