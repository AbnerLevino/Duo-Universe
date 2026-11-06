package com.duo.storage;

import java.util.Optional;

/**
 * Onde os arquivos de verdade moram (DEC-019, DEC-021). O resto do Duo só conhece esta interface: hoje a
 * implementação grava numa pasta do computador; quando houver o R2 (ou outro armazenamento S3), troca-se a
 * implementação e nada mais muda. A {@code key} é um caminho lógico como {@code users/7/avatar-ab12.png}.
 */
public interface MediaStorageProvider {

	/** Grava (ou substitui) o conteúdo na chave dada. */
	void store(String key, byte[] content);

	/** O conteúdo, ou vazio se não existe nenhum arquivo nessa chave. */
	Optional<byte[]> read(String key);

	/** Apaga o arquivo; sem erro se ele já não existia. */
	void delete(String key);
}
