package com.duo.media.dto;

/** Um arquivo recebido, ainda não confiável: o serviço confere o tipo real, o tamanho e a cota. */
public record UploadedFile(byte[] bytes) {
}
