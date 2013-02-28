/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.zip.transformer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.MessagingException;
import org.zeroturnaround.zip.ZipEntryCallback;
import org.zeroturnaround.zip.ZipUtil;

/**
 * Transformer implementation that applies an UnZip transformation to the message
 * payload.
 *
 * @author Gunnar Hillert
 * @since 1.0
 *
 */
public class UnZipTransformer extends AbstractZipTransformer {

	private static final Log logger = LogFactory.getLog(UnZipTransformer.class);

	private volatile boolean expectSingleResult = false;

	/**
	 *
	 * This parameter indicates that only one result object shall be returned as
	 * a result from the executed Unzip operation. If set to <code>true</code> and
	 * more than 1 element is returned, then that
	 * 1 element is extracted and returned as payload.
	 *
	 * If the result map contains more than 1 element and
	 * {@link #expectSingleResult} is <code>true</code>, then a
	 * {@link MessagingException} is thrown.
	 *
	 * If set to <code>false</code>, the complete result list is returned as the
	 * payload. This is the {@code default}.
	 *
	 * @param expectSingleResult If not set explicitly, will default to false
	 *
	 */
	public void setExpectSingleResult(boolean expectSingleResult) {
		this.expectSingleResult = expectSingleResult;
	}

	@Override
	protected Object doZipTransform(final Message<?> message) throws Exception {

		try {
			final Object payload = message.getPayload();
			final Object unzippedData;

			InputStream inputStream = null;

			try {
				if (payload instanceof File) {
					final File filePayload = (File) payload;

					if (filePayload.isDirectory()) {
						throw new UnsupportedOperationException(String.format("Cannot unzip a directory: '%s'", filePayload.getAbsolutePath()));
					}

					if (!SpringZipUtils.isValid(filePayload)) {
						throw new IllegalStateException(String.format("Not a zip file: '%s'.", filePayload.getAbsolutePath()));
					}

					inputStream = new FileInputStream(filePayload);
				}
				else if (payload instanceof InputStream) {
					inputStream = (InputStream) payload;
				}
				else if (payload instanceof byte[]) {
					inputStream = new ByteArrayInputStream((byte[]) payload);
				}
				else {
					throw new IllegalArgumentException(String.format("Unsupported payload type '%s'. The only supported payload types are " +
							"java.io.File, byte[] and java.io.InputStream", payload.getClass().getSimpleName()));
				}

				final SortedMap<String, Object> uncompressedData = new TreeMap<String, Object>();

				ZipUtil.iterate(inputStream, new ZipEntryCallback() {

					@Override
					public void process(InputStream zipEntryInputStream, ZipEntry zipEntry) throws IOException {

						final String zipEntryName = zipEntry.getName();
						final long zipEntryTime = zipEntry.getTime();
						final long zipEntryCompressedSize = zipEntry.getCompressedSize();
						final String type = zipEntry.isDirectory() ? "directory" : "file";

						if (logger.isWarnEnabled()) {
							logger.warn(String.format("Unpacking Zip Entry - Name: '%s',Time: '%s', Compressed Size: '%s', Type: '%s'",
									zipEntryName, zipEntryTime, zipEntryCompressedSize, type));
						}

						if (ZipResultType.FILE.equals(zipResultType)) {
							final File tempDir = new File(workDirectory, message.getHeaders().getId().toString());
							tempDir.mkdirs();
							final File destinationFile = new File(tempDir, zipEntryName);

							if (zipEntry.isDirectory()) {
								destinationFile.mkdirs();
							}
							else {
								SpringZipUtils.copy(zipEntryInputStream, destinationFile);
								uncompressedData.put(zipEntryName, destinationFile);
							}
						}
						else if (ZipResultType.BYTE_ARRAY.equals(zipResultType)) {
							if (!zipEntry.isDirectory()) {
								byte[] data = IOUtils.toByteArray(zipEntryInputStream);
								uncompressedData.put(zipEntryName, data);
							}
						}
						else {
							throw new IllegalStateException("Unsupported zipResultType " + zipResultType);
						}
					}
				});

				if (uncompressedData.isEmpty()) {
					if (logger.isWarnEnabled()) {
						logger.warn("No data unzipped from payload with message Id " + message.getHeaders().getId());
					}
					unzippedData = null;
				}
				else {

					if (this.expectSingleResult) {
						if (uncompressedData.size() == 1) {
							unzippedData = uncompressedData.values().iterator().next();
						}
						else {
							throw new MessagingException(message,
								String.format("The UnZip operation extracted %s "
							  + "result objects but expectSingleResult was 'true'.", uncompressedData.size()));
						}
					}
					else {
						unzippedData = uncompressedData;
					}

				}

				if (payload instanceof File && this.deleteFiles) {
					final File filePayload = (File) payload;
					if (!filePayload.delete() && logger.isWarnEnabled()) {
						if (logger.isWarnEnabled()) {
							logger.warn("failed to delete File '" + filePayload + "'");
						}
					}
				}
			}
			finally {
				IOUtils.closeQuietly(inputStream);
			}
			return unzippedData;
		}
		catch (Exception e) {
			throw new MessageHandlingException(message, "Failed to apply Zip transformation.", e);
		}
	}
}
