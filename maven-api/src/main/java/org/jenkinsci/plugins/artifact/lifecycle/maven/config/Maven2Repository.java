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
package org.jenkinsci.plugins.artifact.lifecycle.maven.config;

import java.net.URI;
import java.util.Set;

import org.jenkinsci.plugins.artifact.lifecycle.config.ArtifactRepository;
import org.jenkinsci.plugins.artifact.lifecycle.config.ArtifactRepositoryAdapterFactory;
import org.jenkinsci.plugins.artifact.lifecycle.config.ArtifactRepositoryTypeProvider;
import org.jenkinsci.plugins.artifact.lifecycle.config.WithDownloadSupport;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Extension;

public interface Maven2Repository extends ArtifactRepository, WithDownloadSupport {

	@CheckForNull
	public URI getMaven2Uri();

	// some implementations support multiple types!
	public default boolean hasMaven2Support() {
		return getMaven2Uri() != null;
	}

	public static boolean isSupported(final ArtifactRepository repository) {
		if (repository == null)
			return false;
		if (repository instanceof Maven2Repository repo)
			return repo.hasMaven2Support();
		return false;
	}

	public static boolean isSupportedWithAdapter(final ArtifactRepository repository) {
		if (isSupported(repository))
			return true;
		return ArtifactRepositoryAdapterFactory.all(Maven2Repository.class, repository.getClass())
				.map(f -> f.adapt(repository))
				.anyMatch(Maven2Repository::isSupported);
	}

	@Extension
	public static class TypeProvider implements ArtifactRepositoryTypeProvider {

		@Override
		public Set<String> getProviderTypes(final ArtifactRepository repository) {
			if (repository == null)
				return Set.of();
			if (Maven2Repository.isSupportedWithAdapter(repository))
				return Set.of("Maven2");
			return Set.of();
		}
	}
}
