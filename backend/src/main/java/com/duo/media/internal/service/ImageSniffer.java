package com.duo.media.internal.service;

/**
 * Descobre o tipo <strong>real</strong> de uma imagem olhando os primeiros bytes (a "assinatura" do formato),
 * em vez de confiar no nome do arquivo ou no tipo que o cliente declarou (RN-23): quem envia um {@code .exe}
 * renomeado para {@code .png} não passa. Só JPEG, PNG e WebP são aceitos.
 */
final class ImageSniffer {

	private static final byte[] PNG_SIGNATURE = { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A };

	private ImageSniffer() {
	}

	/** O tipo MIME detectado, ou {@code null} se não for um formato aceito. */
	static String detect(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
			return "image/jpeg";
		}
		if (startsWith(bytes, PNG_SIGNATURE)) {
			return "image/png";
		}
		if (bytes.length >= 12 && startsWith(bytes, new byte[] { 'R', 'I', 'F', 'F' }) && bytes[8] == 'W'
				&& bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
			return "image/webp";
		}
		return null;
	}

	static String extensionOf(String contentType) {
		return switch (contentType) {
			case "image/jpeg" -> "jpg";
			case "image/png" -> "png";
			case "image/webp" -> "webp";
			default -> throw new IllegalArgumentException("Tipo não suportado: " + contentType);
		};
	}

	private static boolean startsWith(byte[] bytes, byte[] prefix) {
		if (bytes.length < prefix.length) {
			return false;
		}
		for (int i = 0; i < prefix.length; i++) {
			if (bytes[i] != prefix[i]) {
				return false;
			}
		}
		return true;
	}
}
