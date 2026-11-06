package com.duo.media.internal.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/** RN-23: o tipo é o dos bytes, nunca o do nome do arquivo. */
class ImageSnifferTest {

	static byte[] jpeg() {
		return new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16, 'J', 'F', 'I', 'F' };
	}

	static byte[] png() {
		return new byte[] { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0 };
	}

	static byte[] webp() {
		return new byte[] { 'R', 'I', 'F', 'F', 1, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P', '8', ' ' };
	}

	@Test
	void detectsTheThreeAcceptedFormatsByTheirSignature() {
		assertThat(ImageSniffer.detect(jpeg())).isEqualTo("image/jpeg");
		assertThat(ImageSniffer.detect(png())).isEqualTo("image/png");
		assertThat(ImageSniffer.detect(webp())).isEqualTo("image/webp");
	}

	@Test
	void rejectsEverythingElse() {
		assertThat(ImageSniffer.detect("MZ executavel".getBytes(StandardCharsets.UTF_8))).isNull();
		assertThat(ImageSniffer.detect("<svg onload=alert(1)>".getBytes(StandardCharsets.UTF_8))).isNull();
		assertThat(ImageSniffer.detect("GIF89a".getBytes(StandardCharsets.UTF_8))).isNull();
		assertThat(ImageSniffer.detect(new byte[0])).isNull();
		assertThat(ImageSniffer.detect(null)).isNull();
	}

	@Test
	void aRiffThatIsNotWebpIsRejected() {
		byte[] wav = { 'R', 'I', 'F', 'F', 1, 0, 0, 0, 'W', 'A', 'V', 'E' };

		assertThat(ImageSniffer.detect(wav)).isNull();
	}

	@Test
	void tooShortFilesAreRejectedWithoutErrors() {
		assertThat(ImageSniffer.detect(new byte[] { (byte) 0xFF, (byte) 0xD8 })).isNull();
		assertThat(ImageSniffer.detect(new byte[] { (byte) 0x89, 'P', 'N' })).isNull();
	}
}
