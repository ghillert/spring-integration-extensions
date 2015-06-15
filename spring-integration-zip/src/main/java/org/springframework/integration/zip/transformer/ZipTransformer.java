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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.Deflater;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.transformer.Transformer;
import org.springframework.integration.zip.ZipHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.zeroturnaround.zip.ByteSource;
import org.zeroturnaround.zip.FileSource;
import org.zeroturnaround.zip.ZipEntrySource;

/**
 * {@link Transformer} implementation that applies a Zip transformation to the
 * message payload. Keep in mind that Zip entry timestamps are recorded only to
 * two 2 second precision:
 *
 * See also:  http://mindprod.com/jgloss/zip.html
 *
 * If you want to generate Zip files larger than {@code 4GB}, you must use Java 7:
 *
 * See also: https://blogs.oracle.com/xuemingshen/entry/zip64_support_for_4g_zipfile
 *
 * @author Gunnar Hillert
 * @since 1.0
 *
 */
public class ZipTransformer extends AbstractZipTransformer {

	private static final Log logger = LogFactory.getLog(ZipTransformer.class);

	private static final String ZIP_EXTENSION = ".zip";

	private volatile int compressionLevel = Deflater.DEFAULT_COMPRESSION;

	private volatile boolean useFileAttributes = true;

	@Autowired Environment env;

	/**
	 * Sets the compression level. Default is {@link Deflater#DEFAULT_COMPRESSION}.
	 *
	 * @param compressionLevel Must be an integer value from 0-9.
	 */
	public void setCompressionLevel(int compressionLevel) {
		Assert.isTrue(compressionLevel >= 0 && compressionLevel <= 9, "Acceptable levels are 0-9");
		this.compressionLevel = compressionLevel;
	}

	/**
	 * Specifies whether the name of the file shall be used for the
	 * zip entry.
	 *
	 * @param useFileAttributes Defaults to true if not set explicitly
	 */
	public void setUseFileAttributes(boolean useFileAttributes) {
		this.useFileAttributes = useFileAttributes;
	}

	/**
	 * The payload may encompass the following types:
	 *
	 * <ul>
	 *   <li>{@link File}
	 *...<li>{@link String}
	 *...<li>byte[]
	 *...<li>{@link Iterable}
	 * </ul>
	 *
	 * When providing an {@link Iterable}, nested Iterables are not supported. However,
	 * payloads can be of of any of the other supported types.
	 *
	 */
	@Override
	protected Object doZipTransform(Message<?> message) throws Exception {

		try {

			final Object payload = message.getPayload();
			final Object zippedData;
			final String baseFileName = this.fileNameGenerator.generateFileName(message);

			final String zipEntryName;
			final String zipFileName;

			if (message.getHeaders().containsKey(ZipHeaders.ZIP_ENTRY_FILE_NAME)) {
				zipEntryName = (String) message.getHeaders().get(ZipHeaders.ZIP_ENTRY_FILE_NAME);
			}
			else {
				zipEntryName = baseFileName;
			}

			if (message.getHeaders().containsKey(FileHeaders.FILENAME)) {
				zipFileName = baseFileName;
			}
			else {
				zipFileName = baseFileName + ZIP_EXTENSION;
			}

			final Date lastModifiedDate;

			if (message.getHeaders().containsKey(ZipHeaders.ZIP_ENTRY_LAST_MODIFIED_DATE)) {
				lastModifiedDate = (Date) message.getHeaders().get(ZipHeaders.ZIP_ENTRY_LAST_MODIFIED_DATE);
			}
			else {
				lastModifiedDate = new Date();
			}

			java.util.List<ZipEntrySource> entries = new ArrayList<ZipEntrySource>();

			if (payload instanceof Iterable<?>) {
				int counter = 1;

				String baseName = FilenameUtils.getBaseName(zipEntryName);
				String fileExtension = FilenameUtils.getExtension(zipEntryName);

				if (StringUtils.hasText(fileExtension)) {
					fileExtension = FilenameUtils.EXTENSION_SEPARATOR_STR + fileExtension;
				}

				for (Object item : (Iterable<?>) payload) {

					final ZipEntrySource zipEntrySource = createZipEntrySource(item, lastModifiedDate, baseName + "_" + counter + fileExtension, useFileAttributes);
					if (logger.isDebugEnabled()) {
						logger.debug("ZipEntrySource path: '" + zipEntrySource.getPath() + "'");
					}
					entries.add(zipEntrySource);
					counter++;
				}
			}
			else {
				final ZipEntrySource zipEntrySource = createZipEntrySource(payload, lastModifiedDate, zipEntryName, useFileAttributes);
				entries.add(zipEntrySource);
			}

			final byte[] zippedBytes = SpringZipUtils.pack(entries, this.compressionLevel);

			if (ZipResultType.FILE.equals(zipResultType)) {
				final File zippedFile = new File(this.workDirectory, zipFileName);
				FileCopyUtils.copy(zippedBytes, zippedFile);
				zippedData = zippedFile;
			}
			else if (ZipResultType.BYTE_ARRAY.equals(zipResultType)) {
				zippedData = zippedBytes;
			}
			else {
				throw new IllegalStateException("Unsupported zipResultType " + zipResultType);
			}

			return this.getMessageBuilderFactory()
				.withPayload(zippedData).copyHeaders(message.getHeaders()).setHeader(FileHeaders.FILENAME, zipFileName).build();
		}
		catch (Exception e) {
			throw new MessageHandlingException(message, "Failed to apply Zip transformation.", e);
		}
	}

	private ZipEntrySource createZipEntrySource(Object item,
			Date lastModifiedDate, String zipEntryName, boolean useFileAttributes) {

		if (item instanceof File) {
			final File filePayload = (File) item;

			final String fileName = useFileAttributes ? filePayload.getName() : zipEntryName;

			if (((File) item).isDirectory()) {
				throw new UnsupportedOperationException("Zipping of directories is not supported.");
			}

			final FileSource fileSource = new FileSource(fileName, filePayload);

			if (this.deleteFiles) {
				if (!filePayload.delete() && logger.isWarnEnabled()) {
					logger.warn("failed to delete File '" + filePayload + "'");
				}
			}

			return fileSource;

		}
		else if (item instanceof byte[] || item instanceof String) {

			byte[] bytesToCompress = null;

			if (item instanceof String) {
				bytesToCompress = ((String) item).getBytes(this.charset);
			}
			else {
				bytesToCompress = (byte[]) item;
			}

			final ZipEntrySource zipEntrySource = new ByteSource(zipEntryName, bytesToCompress, lastModifiedDate.getTime());

			return zipEntrySource;
		}
		else {
			throw new IllegalArgumentException("Unsupported payload type. The only supported payloads are " +
						"java.io.File, java.lang.String, and byte[]");
		}
	}
}
