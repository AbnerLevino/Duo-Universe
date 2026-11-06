package com.duo.media.dto;

/** O arquivo de uma foto, já autorizado para quem pediu. {@code sha256} serve de ETag para o navegador guardar. */
public record MediaContent(byte[] bytes, String contentType, String sha256) {
}
