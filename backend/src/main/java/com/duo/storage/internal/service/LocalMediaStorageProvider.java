package com.duo.storage.internal.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

import com.duo.storage.MediaStorageProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Guarda os arquivos numa pasta do computador ({@code duo.media.dir}, fora do repositório). É a implementação
 * de desenvolvimento (DEC-021). A chave vem do nosso código, nunca direto do usuário, mas mesmo assim ela é
 * conferida: uma chave com {@code ..} poderia escapar da pasta e ler ou apagar qualquer arquivo do computador.
 */
@Component
class LocalMediaStorageProvider implements MediaStorageProvider {

	private static final Pattern SAFE_KEY = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9/_.-]{0,199}$");

	private final Path baseDir;

	LocalMediaStorageProvider(@Value("${duo.media.dir}") Path baseDir) {
		this.baseDir = baseDir.toAbsolutePath().normalize();
	}

	@Override
	public void store(String key, byte[] content) {
		Path target = resolve(key);
		try {
			Files.createDirectories(target.getParent());
			Files.write(target, content);
		}
		catch (IOException e) {
			throw new UncheckedIOException("Não foi possível gravar o arquivo", e);
		}
	}

	@Override
	public Optional<byte[]> read(String key) {
		try {
			return Optional.of(Files.readAllBytes(resolve(key)));
		}
		catch (NoSuchFileException missing) {
			return Optional.empty();
		}
		catch (IOException e) {
			throw new UncheckedIOException("Não foi possível ler o arquivo", e);
		}
	}

	@Override
	public void delete(String key) {
		try {
			Files.deleteIfExists(resolve(key));
		}
		catch (IOException e) {
			throw new UncheckedIOException("Não foi possível apagar o arquivo", e);
		}
	}

	private Path resolve(String key) {
		if (key == null || !SAFE_KEY.matcher(key).matches() || key.contains("..")) {
			throw new IllegalArgumentException("Chave de arquivo inválida");
		}
		Path resolved = baseDir.resolve(key).normalize();
		if (!resolved.startsWith(baseDir)) {
			throw new IllegalArgumentException("Chave de arquivo inválida");
		}
		return resolved;
	}
}
