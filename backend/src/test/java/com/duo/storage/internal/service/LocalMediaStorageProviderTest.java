package com.duo.storage.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** O armazenamento local de desenvolvimento (DEC-021) e a defesa contra chaves que escapam da pasta. */
class LocalMediaStorageProviderTest {

	@TempDir
	Path dir;

	@Test
	void storesReadsAndDeletes() {
		LocalMediaStorageProvider storage = new LocalMediaStorageProvider(dir);

		storage.store("users/7/avatar-1.png", new byte[] { 1, 2, 3 });

		assertThat(storage.read("users/7/avatar-1.png")).contains(new byte[] { 1, 2, 3 });
		storage.delete("users/7/avatar-1.png");
		assertThat(storage.read("users/7/avatar-1.png")).isEmpty();
	}

	@Test
	void readingAndDeletingSomethingThatDoesNotExistIsHarmless() {
		LocalMediaStorageProvider storage = new LocalMediaStorageProvider(dir);

		assertThat(storage.read("users/1/nada.png")).isEmpty();
		storage.delete("users/1/nada.png");
	}

	@Test
	void storingAgainOnTheSameKeyReplacesTheContent() {
		LocalMediaStorageProvider storage = new LocalMediaStorageProvider(dir);
		storage.store("users/1/a.png", new byte[] { 1 });

		storage.store("users/1/a.png", new byte[] { 9, 9 });

		assertThat(storage.read("users/1/a.png")).contains(new byte[] { 9, 9 });
	}

	@Test
	void keysThatTryToEscapeTheFolderAreRefused() throws Exception {
		LocalMediaStorageProvider storage = new LocalMediaStorageProvider(dir.resolve("media"));
		Path outside = dir.resolve("segredo.txt");
		Files.writeString(outside, "nao pode vazar");

		assertThatThrownBy(() -> storage.read("../segredo.txt")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> storage.read("users/../../segredo.txt")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> storage.store("/etc/passwd", new byte[] { 1 }))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> storage.delete("..\\segredo.txt")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> storage.read(null)).isInstanceOf(IllegalArgumentException.class);
		assertThat(Files.exists(outside)).isTrue();
	}
}
