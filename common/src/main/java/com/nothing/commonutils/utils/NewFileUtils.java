package com.nothing.commonutils.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class NewFileUtils {

    public static final long ONE_KB = 1024;

    public static final BigInteger ONE_KB_BI = BigInteger.valueOf(ONE_KB);


    public static final long ONE_MB = ONE_KB * ONE_KB;

    public static final BigInteger ONE_MB_BI = ONE_KB_BI.multiply(ONE_KB_BI);

    public static final long ONE_GB = ONE_KB * ONE_MB;

    public static final BigInteger ONE_GB_BI = ONE_KB_BI.multiply(ONE_MB_BI);

    public static final long ONE_TB = ONE_KB * ONE_GB;

    public static final BigInteger ONE_TB_BI = ONE_KB_BI.multiply(ONE_GB_BI);

    public static final long ONE_PB = ONE_KB * ONE_TB;

    public static final BigInteger ONE_PB_BI = ONE_KB_BI.multiply(ONE_TB_BI);

    public static final long ONE_EB = ONE_KB * ONE_PB;

    public static final BigInteger ONE_EB_BI = ONE_KB_BI.multiply(ONE_PB_BI);


    public static final BigInteger ONE_ZB = BigInteger.valueOf(ONE_KB)
                                                      .multiply(BigInteger.valueOf(ONE_EB));


    public static final BigInteger ONE_YB = ONE_KB_BI.multiply(ONE_ZB);


    public static final File[] EMPTY_FILE_ARRAY = {};

    private static CopyOption[] addCopyAttributes(final CopyOption... copyOptions) {
        // Make a copy first since we don't want to sort the call site's version.
        final CopyOption[] actual = Arrays.copyOf(copyOptions, copyOptions.length + 1);
        Arrays.sort(actual, 0, copyOptions.length);
        if (Arrays.binarySearch(copyOptions, 0, copyOptions.length,
                                StandardCopyOption.COPY_ATTRIBUTES) >= 0) {
            return copyOptions;
        }
        actual[actual.length - 1] = StandardCopyOption.COPY_ATTRIBUTES;
        return actual;
    }


    public static String byteCountToDisplaySize(final BigInteger size) {
        Objects.requireNonNull(size, "size");
        final String displaySize;

        if (size.divide(ONE_EB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_EB_BI) + " EB";
        } else if (size.divide(ONE_PB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_PB_BI) + " PB";
        } else if (size.divide(ONE_TB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_TB_BI) + " TB";
        } else if (size.divide(ONE_GB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_GB_BI) + " GB";
        } else if (size.divide(ONE_MB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_MB_BI) + " MB";
        } else if (size.divide(ONE_KB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_KB_BI) + " KB";
        } else {
            displaySize = size + " bytes";
        }
        return displaySize;
    }


    public static String byteCountToDisplaySize(final long size) {
        return byteCountToDisplaySize(BigInteger.valueOf(size));
    }


    public static boolean cleanDirectory(final File directory) throws IOException {
        final File[] files = listFiles(directory, null);

        final List<Exception> causeList = new ArrayList<>();
        for (final File file : files) {
            try {
                forceDelete(file);
            } catch (final IOException ioe) {
                causeList.add(ioe);
            }
        }

        if (!causeList.isEmpty()) {
            return false;
        }
        return true;
    }

    private static boolean cleanDirectoryOnExit(final File directory) throws IOException {
        final File[] files = listFiles(directory, null);

        final List<Exception> causeList = new ArrayList<>();
        for (final File file : files) {
            try {
                forceDeleteOnExit(file);
            } catch (final IOException ioe) {
                causeList.add(ioe);
            }
        }

        if (!causeList.isEmpty()) {
            return false;
        }
        return true;
    }

    public static final int DEFAULT_BUFFER_SIZE = 8192;
    public static final int EOF = -1;

    private static final ThreadLocal<byte[]> SKIP_BYTE_BUFFER = ThreadLocal.withInitial(
            () -> byteArray());

    public static byte[] byteArray() {
        return byteArray(DEFAULT_BUFFER_SIZE);
    }

    public static byte[] byteArray(final int size) {
        return new byte[size];
    }

    static byte[] getByteArray() {
        return SKIP_BYTE_BUFFER.get();
    }

    public static boolean contentEquals(
            final InputStream input1, final InputStream input2
    ) throws IOException {
        // Before making any changes, please test with
        // org.apache.commons.io.jmh.IOUtilsContentEqualsInputStreamsBenchmark
        if (input1 == input2) {
            return true;
        }
        if (input1 == null || input2 == null) {
            return false;
        }

        // reuse one
        final byte[] array1 = getByteArray();
        // allocate another
        final byte[] array2 = byteArray();
        int pos1;
        int pos2;
        int count1;
        int count2;
        while (true) {
            pos1 = 0;
            pos2 = 0;
            for (int index = 0; index < DEFAULT_BUFFER_SIZE; index++) {
                if (pos1 == index) {
                    do {
                        count1 = input1.read(array1, pos1, DEFAULT_BUFFER_SIZE - pos1);
                    } while (count1 == 0);
                    if (count1 == EOF) {
                        return pos2 == index && input2.read() == EOF;
                    }
                    pos1 += count1;
                }
                if (pos2 == index) {
                    do {
                        count2 = input2.read(array2, pos2, DEFAULT_BUFFER_SIZE - pos2);
                    } while (count2 == 0);
                    if (count2 == EOF) {
                        return pos1 == index && input1.read() == EOF;
                    }
                    pos2 += count2;
                }
                if (array1[index] != array2[index]) {
                    return false;
                }
            }
        }
    }

    public static boolean contentEquals(final File file1, final File file2) throws IOException {
        if (file1 == null && file2 == null) {
            return true;
        }
        if (file1 == null || file2 == null) {
            return false;
        }
        final boolean file1Exists = file1.exists();
        if (file1Exists != file2.exists()) {
            return false;
        }

        if (!file1Exists) {
            // two not existing files are equal
            return true;
        }

        requireFile(file1, "file1");
        requireFile(file2, "file2");

        if (file1.length() != file2.length()) {
            // lengths differ, cannot be equal
            return false;
        }

        if (file1.getCanonicalFile().equals(file2.getCanonicalFile())) {
            // same file
            return true;
        }

        try (InputStream input1 = Files.newInputStream(
                file1.toPath()); InputStream input2 = Files.newInputStream(file2.toPath())) {
            return contentEquals(input1, input2);
        }
    }


    public static boolean contentEqualsIgnoreEOL(
            final File file1, final File file2, final String charsetName
    )
            throws IOException {
        if (file1 == null && file2 == null) {
            return true;
        }
        if (file1 == null || file2 == null) {
            return false;
        }
        final boolean file1Exists = file1.exists();
        if (file1Exists != file2.exists()) {
            return false;
        }

        if (!file1Exists) {
            // two not existing files are equal
            return true;
        }

        requireFile(file1, "file1");
        requireFile(file2, "file2");

        if (file1.getCanonicalFile().equals(file2.getCanonicalFile())) {
            // same file
            return true;
        }

        final Charset charset = toCharset(charsetName);
        try (Reader input1 = new InputStreamReader(Files.newInputStream(file1.toPath()), charset);
             Reader input2 = new InputStreamReader(Files.newInputStream(file2.toPath()), charset)) {
            return contentEqualsIgnoreEOL(input1, input2);
        }
    }

    public static Charset toCharset(final String charsetName) throws UnsupportedCharsetException {
        return charsetName == null ? Charset.defaultCharset() : Charset.forName(charsetName);
    }

    public static boolean contentEqualsIgnoreEOL(final Reader reader1, final Reader reader2)
            throws IOException {
        if (reader1 == reader2) {
            return true;
        }
        if (reader1 == null ^ reader2 == null) {
            return false;
        }
        final BufferedReader br1 = toBufferedReader(reader1);
        final BufferedReader br2 = toBufferedReader(reader2);

        String line1 = br1.readLine();
        String line2 = br2.readLine();
        while (line1 != null && line1.equals(line2)) {
            line1 = br1.readLine();
            line2 = br2.readLine();
        }
        return Objects.equals(line1, line2);
    }

    public static BufferedReader toBufferedReader(final Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(
                reader);
    }

    public static File[] convertFileCollectionToFileArray(final Collection<File> files) {
        return files.toArray(EMPTY_FILE_ARRAY);
    }


    public static void copyDirectory(final File srcDir, final File destDir) throws IOException {
        copyDirectory(srcDir, destDir, true);
    }

    public static void copyDirectory(
            final File srcDir, final File destDir, final boolean preserveFileDate
    )
            throws IOException {
        copyDirectory(srcDir, destDir, null, preserveFileDate);
    }


    public static void copyDirectory(final File srcDir, final File destDir, final FileFilter filter)
            throws IOException {
        copyDirectory(srcDir, destDir, filter, true);
    }


    public static void copyDirectory(
            final File srcDir, final File destDir, final FileFilter filter,
            final boolean preserveFileDate
    ) throws IOException {
        copyDirectory(srcDir, destDir, filter, preserveFileDate,
                      StandardCopyOption.REPLACE_EXISTING);
    }


    public static void copyDirectory(
            final File srcDir, final File destDir, final FileFilter fileFilter,
            final boolean preserveFileDate, final CopyOption... copyOptions
    ) throws IOException {
        requireFileCopy(srcDir, destDir);
        requireDirectory(srcDir, "srcDir");
        requireCanonicalPathsNotEquals(srcDir, destDir);

        // Cater for destination being directory within the source directory (see IO-141)
        List<String> exclusionList = null;
        final String srcDirCanonicalPath = srcDir.getCanonicalPath();
        final String destDirCanonicalPath = destDir.getCanonicalPath();
        if (destDirCanonicalPath.startsWith(srcDirCanonicalPath)) {
            final File[] srcFiles = listFiles(srcDir, fileFilter);
            if (srcFiles.length > 0) {
                exclusionList = new ArrayList<>(srcFiles.length);
                for (final File srcFile : srcFiles) {
                    final File copiedFile = new File(destDir, srcFile.getName());
                    exclusionList.add(copiedFile.getCanonicalPath());
                }
            }
        }
        doCopyDirectory(srcDir, destDir, fileFilter, exclusionList,
                        preserveFileDate,
                        preserveFileDate ? addCopyAttributes(copyOptions) : copyOptions);
    }


    public static void copyDirectoryToDirectory(
            final File sourceDir, final File destinationDir
    ) throws IOException {
        requireDirectoryIfExists(sourceDir, "sourceDir");
        requireDirectoryIfExists(destinationDir, "destinationDir");
        copyDirectory(sourceDir, new File(destinationDir, sourceDir.getName()), true);
    }

    public static void copyFile(final File srcFile, final File destFile) throws IOException {
        copyFile(srcFile, destFile, StandardCopyOption.COPY_ATTRIBUTES,
                 StandardCopyOption.REPLACE_EXISTING);
    }


    public static void copyFile(
            final File srcFile, final File destFile, final boolean preserveFileDate
    )
            throws IOException {
        copyFile(srcFile, destFile,
                 preserveFileDate
                         ? new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING}
                         : new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
    }

    public static void copyFile(
            final File srcFile, final File destFile, final boolean preserveFileDate,
            final CopyOption... copyOptions
    )
            throws IOException {
        copyFile(srcFile, destFile,
                 preserveFileDate ? addCopyAttributes(copyOptions) : copyOptions);
    }


    public static void copyFile(
            final File srcFile, final File destFile, final CopyOption... copyOptions
    )
            throws IOException {
        requireFileCopy(srcFile, destFile);
        requireFile(srcFile, "srcFile");
        requireCanonicalPathsNotEquals(srcFile, destFile);
        createParentDirectories(destFile);
        requireFileIfExists(destFile, "destFile");
        if (destFile.exists()) {
            requireCanWrite(destFile, "destFile");
        }

        // On Windows, the last modified time is copied by default.
        Files.copy(srcFile.toPath(), destFile.toPath(), copyOptions);

        // TODO IO-386: Do we still need this check?
        requireEqualSizes(srcFile, destFile, srcFile.length(), destFile.length());
    }


    public static void copyFileToDirectory(
            final File srcFile, final File destDir
    ) throws IOException {
        copyFileToDirectory(srcFile, destDir, true);
    }

    public static void copyFileToDirectory(
            final File sourceFile, final File destinationDir, final boolean preserveFileDate
    )
            throws IOException {
        Objects.requireNonNull(sourceFile, "sourceFile");
        requireDirectoryIfExists(destinationDir, "destinationDir");
        copyFile(sourceFile, new File(destinationDir, sourceFile.getName()), preserveFileDate);
    }

    public static void copyInputStreamToFile(
            final InputStream source, final File destination
    ) throws IOException {
        try (InputStream inputStream = source) {
            copyToFile(inputStream, destination);
        }
    }


    public static void copyToDirectory(
            final File sourceFile, final File destinationDir
    ) throws IOException {
        Objects.requireNonNull(sourceFile, "sourceFile");
        if (sourceFile.isFile()) {
            copyFileToDirectory(sourceFile, destinationDir);
        } else if (sourceFile.isDirectory()) {
            copyDirectoryToDirectory(sourceFile, destinationDir);
        } else {
            throw new FileNotFoundException("The source " + sourceFile + " does not exist");
        }
    }


    public static void copyToDirectory(
            final Iterable<File> sourceIterable, final File destinationDir
    ) throws IOException {
        Objects.requireNonNull(sourceIterable, "sourceIterable");
        for (final File src : sourceIterable) {
            copyFileToDirectory(src, destinationDir);
        }
    }

    public static long copy(
            final InputStream inputStream, final OutputStream outputStream, final int bufferSize
    )
            throws IOException {
        return copyLarge(inputStream, outputStream, byteArray(bufferSize));
    }

    public static long copyLarge(
            final InputStream inputStream, final OutputStream outputStream, final byte[] buffer
    )
            throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");
        Objects.requireNonNull(outputStream, "outputStream");
        long count = 0;
        int n;
        while (EOF != (n = inputStream.read(buffer))) {
            outputStream.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static long copyLarge(final InputStream inputStream, final OutputStream outputStream)
            throws IOException {
        return copy(inputStream, outputStream, DEFAULT_BUFFER_SIZE);
    }


    public static int copy(
            final InputStream inputStream, final OutputStream outputStream
    ) throws IOException {
        final long count = copyLarge(inputStream, outputStream);
        if (count > Integer.MAX_VALUE) {
            return EOF;
        }
        return (int) count;
    }

    public static void copyToFile(
            final InputStream inputStream, final File file
    ) throws IOException {
        try (OutputStream out = openOutputStream(file)) {
            copy(inputStream, out);
        }
    }

    public static void copyURLToFile(final URL source, final File destination) throws IOException {
        try (final InputStream stream = source.openStream()) {
            copyInputStreamToFile(stream, destination);
        }
    }


    public static void copyURLToFile(
            final URL source, final File destination,
            final int connectionTimeoutMillis, final int readTimeoutMillis
    ) throws IOException {
        final URLConnection connection = source.openConnection();
        connection.setConnectTimeout(connectionTimeoutMillis);
        connection.setReadTimeout(readTimeoutMillis);
        try (final InputStream stream = connection.getInputStream()) {
            copyInputStreamToFile(stream, destination);
        }
    }

    public static File createParentDirectories(final File file) throws IOException {
        return mkdirs(getParentFile(file));
    }


    static String decodeUrl(final String url) {
        String decoded = url;
        if (url != null && url.indexOf('%') >= 0) {
            final int n = url.length();
            final StringBuilder buffer = new StringBuilder();
            final ByteBuffer bytes = ByteBuffer.allocate(n);
            for (int i = 0; i < n; ) {
                if (url.charAt(i) == '%') {
                    try {
                        do {
                            final byte octet = (byte) Integer.parseInt(url.substring(i + 1, i + 3),
                                                                       16);
                            bytes.put(octet);
                            i += 3;
                        } while (i < n && url.charAt(i) == '%');
                        continue;
                    } catch (final RuntimeException e) {
                        // malformed percent-encoded octet, fall through and
                        // append characters literally
                    } finally {
                        if (bytes.position() > 0) {
                            bytes.flip();
                            buffer.append(StandardCharsets.UTF_8.decode(bytes).toString());
                            bytes.clear();
                        }
                    }
                }
                buffer.append(url.charAt(i++));
            }
            decoded = buffer.toString();
        }
        return decoded;
    }


    public static File delete(final File file) throws IOException {
        Objects.requireNonNull(file, "file");
        Files.delete(file.toPath());
        return file;
    }


    public static void deleteDirectory(final File directory) throws IOException {
        Objects.requireNonNull(directory, "directory");
        if (!directory.exists()) {
            return;
        }
        if (!isSymlink(directory)) {
            cleanDirectory(directory);
        }
        delete(directory);
    }


    private static void deleteDirectoryOnExit(final File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }
        directory.deleteOnExit();
        if (!isSymlink(directory)) {
            cleanDirectoryOnExit(directory);
        }
    }


    public static boolean deleteQuietly(final File file) {
        if (file == null) {
            return false;
        }
        try {
            if (file.isDirectory()) {
                cleanDirectory(file);
            }
        } catch (final Exception ignored) {
            // ignore
        }

        try {
            return file.delete();
        } catch (final Exception ignored) {
            return false;
        }
    }


    private static void doCopyDirectory(
            final File srcDir, final File destDir, final FileFilter fileFilter,
            final List<String> exclusionList, final boolean preserveDirDate,
            final CopyOption... copyOptions
    ) throws IOException {
        // recurse dirs, copy files.
        final File[] srcFiles = listFiles(srcDir, fileFilter);
        requireDirectoryIfExists(destDir, "destDir");
        mkdirs(destDir);
        requireCanWrite(destDir, "destDir");
        for (final File srcFile : srcFiles) {
            final File dstFile = new File(destDir, srcFile.getName());
            if (exclusionList == null || !exclusionList.contains(srcFile.getCanonicalPath())) {
                if (srcFile.isDirectory()) {
                    doCopyDirectory(srcFile, dstFile, fileFilter, exclusionList, preserveDirDate,
                                    copyOptions);
                } else {
                    copyFile(srcFile, dstFile, copyOptions);
                }
            }
        }
        // Do this last, as the above has probably affected directory metadata
        if (preserveDirDate) {
            setLastModified(srcDir, destDir);
        }
    }

    public static void forceDelete(final File file) throws IOException {
        try {
            file.deleteOnExit();
        } catch (Throwable e) {
            throw e;
        }
    }

    public static void forceDeleteOnExit(final File file) throws IOException {
        Objects.requireNonNull(file, "file");
        if (file.isDirectory()) {
            deleteDirectoryOnExit(file);
        } else {
            file.deleteOnExit();
        }
    }

    public static void forceMkdir(final File directory) throws IOException {
        mkdirs(directory);
    }


    public static void forceMkdirParent(final File file) throws IOException {
        Objects.requireNonNull(file, "file");
        final File parent = getParentFile(file);
        if (parent == null) {
            return;
        }
        forceMkdir(parent);
    }


    public static File getFile(final File directory, final String... names) {
        Objects.requireNonNull(directory, "directory");
        Objects.requireNonNull(names, "names");
        File file = directory;
        for (final String name : names) {
            file = new File(file, name);
        }
        return file;
    }



    public static File getFile(final String... names) {
        Objects.requireNonNull(names, "names");
        File file = null;
        for (final String name : names) {
            if (file == null) {
                file = new File(name);
            } else {
                file = new File(file, name);
            }
        }
        return file;
    }



    private static File getParentFile(final File file) {
        return file == null ? null : file.getParentFile();
    }



    public static File getTempDirectory() {
        return new File(getTempDirectoryPath());
    }



    public static String getTempDirectoryPath() {
        return System.getProperty("java.io.tmpdir");
    }



    public static File getUserDirectory() {
        return new File(getUserDirectoryPath());
    }



    public static String getUserDirectoryPath() {
        return System.getProperty("user.home");
    }


    public static boolean isDirectory(final File file, final LinkOption... options) {
        return file != null && Files.isDirectory(file.toPath(), options);
    }



    public static boolean isEmptyDirectory(final File directory) throws IOException {
        return directory.isDirectory() &&directory.list() != null && Objects.requireNonNull(
                directory.list()).length == 0;
    }


    public static boolean isFileNewer(final File file, final ChronoLocalDate chronoLocalDate) {
        return isFileNewer(file, chronoLocalDate, LocalTime.now());
    }


    public static boolean isFileNewer(
            final File file, final ChronoLocalDate chronoLocalDate, final LocalTime localTime
    ) {
        Objects.requireNonNull(chronoLocalDate, "chronoLocalDate");
        Objects.requireNonNull(localTime, "localTime");
        return isFileNewer(file, chronoLocalDate.atTime(localTime));
    }

    public static boolean isFileNewer(
            final File file, final ChronoLocalDateTime<?> chronoLocalDateTime
    ) {
        return isFileNewer(file, chronoLocalDateTime, ZoneId.systemDefault());
    }


    public static boolean isFileNewer(
            final File file, final ChronoLocalDateTime<?> chronoLocalDateTime, final ZoneId zoneId
    ) {
        Objects.requireNonNull(chronoLocalDateTime, "chronoLocalDateTime");
        Objects.requireNonNull(zoneId, "zoneId");
        return isFileNewer(file, chronoLocalDateTime.atZone(zoneId));
    }


    public static boolean isFileNewer(
            final File file, final ChronoZonedDateTime<?> chronoZonedDateTime
    ) {
        Objects.requireNonNull(chronoZonedDateTime, "chronoZonedDateTime");
        return isFileNewer(file, chronoZonedDateTime.toInstant());
    }

    public static boolean isFileNewer(final File file, final Date date) {
        Objects.requireNonNull(date, "date");
        return isFileNewer(file, date.getTime());
    }


    public static boolean isFileNewer(final File file, final File reference) {
        requireExists(reference, "reference");
        return isFileNewer(file, lastModifiedUnchecked(reference));
    }


    public static boolean isFileNewer(final File file, final Instant instant) {
        Objects.requireNonNull(instant, "instant");
        return isFileNewer(file, instant.toEpochMilli());
    }

    public static boolean isFileNewer(final File file, final long timeMillis) {
        Objects.requireNonNull(file, "file");
        return file.exists() && lastModifiedUnchecked(file) > timeMillis;
    }


    public static boolean isFileOlder(final File file, final ChronoLocalDate chronoLocalDate) {
        return isFileOlder(file, chronoLocalDate, LocalTime.now());
    }


    public static boolean isFileOlder(
            final File file, final ChronoLocalDate chronoLocalDate, final LocalTime localTime
    ) {
        Objects.requireNonNull(chronoLocalDate, "chronoLocalDate");
        Objects.requireNonNull(localTime, "localTime");
        return isFileOlder(file, chronoLocalDate.atTime(localTime));
    }


    public static boolean isFileOlder(
            final File file, final ChronoLocalDateTime<?> chronoLocalDateTime
    ) {
        return isFileOlder(file, chronoLocalDateTime, ZoneId.systemDefault());
    }

    public static boolean isFileOlder(
            final File file, final ChronoLocalDateTime<?> chronoLocalDateTime, final ZoneId zoneId
    ) {
        Objects.requireNonNull(chronoLocalDateTime, "chronoLocalDateTime");
        Objects.requireNonNull(zoneId, "zoneId");
        return isFileOlder(file, chronoLocalDateTime.atZone(zoneId));
    }


    public static boolean isFileOlder(
            final File file, final ChronoZonedDateTime<?> chronoZonedDateTime
    ) {
        Objects.requireNonNull(chronoZonedDateTime, "chronoZonedDateTime");
        return isFileOlder(file, chronoZonedDateTime.toInstant());
    }


    public static boolean isFileOlder(final File file, final Date date) {
        Objects.requireNonNull(date, "date");
        return isFileOlder(file, date.getTime());
    }

    public static boolean isFileOlder(final File file, final File reference) {
        requireExists(reference, "reference");
        return isFileOlder(file, lastModifiedUnchecked(reference));
    }


    public static boolean isFileOlder(final File file, final Instant instant) {
        Objects.requireNonNull(instant, "instant");
        return isFileOlder(file, instant.toEpochMilli());
    }


    public static boolean isFileOlder(final File file, final long timeMillis) {
        Objects.requireNonNull(file, "file");
        return file.exists() && lastModifiedUnchecked(file) < timeMillis;
    }


    public static boolean isRegularFile(final File file, final LinkOption... options) {
        return file != null && Files.isRegularFile(file.toPath(), options);
    }


    public static boolean isSymlink(final File file) {
        return file != null && Files.isSymbolicLink(file.toPath());
    }




    public static long lastModified(final File file) throws IOException {
        // https://bugs.openjdk.java.net/browse/JDK-8177809
        // File.lastModified() is losing milliseconds (always ends in 000)
        // This bug is in OpenJDK 8 and 9, and fixed in 10.
        return Files.getLastModifiedTime(Objects.requireNonNull(file.toPath(), "file")).toMillis();
    }


    public static long lastModifiedUnchecked(final File file) {
        // https://bugs.openjdk.java.net/browse/JDK-8177809
        // File.lastModified() is losing milliseconds (always ends in 000)
        // This bug is in OpenJDK 8 and 9, and fixed in 10.
        try {
            return lastModified(file);
        } catch (final IOException e) {
            throw new UncheckedIOException(file.toString(), e);
        }
    }



    private static File[] listFiles(
            final File directory, final FileFilter fileFilter
    ) throws IOException {
        requireDirectoryExists(directory, "directory");
        final File[] files =
                fileFilter == null ? directory.listFiles() : directory.listFiles(fileFilter);
        if (files == null) {
            // null if the directory does not denote a directory, or if an I/O error occurs.
            throw new IOException("Unknown I/O error listing contents of directory: " + directory);
        }
        return files;
    }



    private static File mkdirs(final File directory) throws IOException {
        if ((directory != null) && (!directory.mkdirs() && !directory.isDirectory())) {
            throw new IOException("Cannot create directory '" + directory + "'.");
        }
        return directory;
    }

    public static void moveDirectory(final File srcDir, final File destDir) throws IOException {
        validateMoveParameters(srcDir, destDir);
        requireDirectory(srcDir, "srcDir");
        requireAbsent(destDir, "destDir");
        if (!srcDir.renameTo(destDir)) {
            if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath() + File.separator)) {
                throw new IOException(
                        "Cannot move directory: " + srcDir + " to a subdirectory of itself: " +
                        destDir);
            }
            copyDirectory(srcDir, destDir);
            deleteDirectory(srcDir);
            if (srcDir.exists()) {
                throw new IOException("Failed to delete original directory '" + srcDir +
                                      "' after copy to '" + destDir + "'");
            }
        }
    }


    public static void moveDirectoryToDirectory(
            final File src, final File destDir, final boolean createDestDir
    )
            throws IOException {
        validateMoveParameters(src, destDir);
        if (!destDir.isDirectory()) {
            if (destDir.exists()) {
                throw new IOException("Destination '" + destDir + "' is not a directory");
            }
            if (!createDestDir) {
                throw new FileNotFoundException("Destination directory '" + destDir +
                                                "' does not exist [createDestDir=" + false + "]");
            }
            mkdirs(destDir);
        }
        moveDirectory(src, new File(destDir, src.getName()));
    }


    public static void moveFile(final File srcFile, final File destFile) throws IOException {
        moveFile(srcFile, destFile, StandardCopyOption.COPY_ATTRIBUTES);
    }


    public static void moveFile(
            final File srcFile, final File destFile, final CopyOption... copyOptions
    )
            throws IOException {
        validateMoveParameters(srcFile, destFile);
        requireFile(srcFile, "srcFile");
        requireAbsent(destFile, null);
        final boolean rename = srcFile.renameTo(destFile);
        if (!rename) {
            copyFile(srcFile, destFile, copyOptions);
            if (!srcFile.delete()) {
                delete(srcFile);
                throw new IOException("Failed to delete original file '" + srcFile +
                                      "' after copy to '" + destFile + "'");
            }
        }
    }

    public static void moveFileToDirectory(
            final File srcFile, final File destDir, final boolean createDestDir
    )
            throws IOException {
        validateMoveParameters(srcFile, destDir);
        if (!destDir.exists() && createDestDir) {
            mkdirs(destDir);
        }
        requireExistsChecked(destDir, "destDir");
        requireDirectory(destDir, "destDir");
        moveFile(srcFile, new File(destDir, srcFile.getName()));
    }


    public static void moveToDirectory(
            final File src, final File destDir, final boolean createDestDir
    )
            throws IOException {
        validateMoveParameters(src, destDir);
        if (src.isDirectory()) {
            moveDirectoryToDirectory(src, destDir, createDestDir);
        } else {
            moveFileToDirectory(src, destDir, createDestDir);
        }
    }

    public static FileInputStream openInputStream(final File file) throws IOException {
        Objects.requireNonNull(file, "file");
        return new FileInputStream(file);
    }

    public static FileOutputStream openOutputStream(final File file) throws IOException {
        return openOutputStream(file, false);
    }


    public static FileOutputStream openOutputStream(
            final File file, final boolean append
    ) throws IOException {
        Objects.requireNonNull(file, "file");
        if (file.exists()) {
            requireFile(file, "file");
            requireCanWrite(file, "file");
        } else {
            createParentDirectories(file);
        }
        return new FileOutputStream(file, append);
    }


    private static void requireAbsent(
            final File file, final String name
    ) throws IllegalStateException {
        if (file.exists()) {
            throw new IllegalStateException(
                    String.format("File element in parameter '%s' already exists: '%s'", name,
                                  file));
        }
    }



    private static void requireCanonicalPathsNotEquals(
            final File file1, final File file2
    ) throws IOException {
        final String canonicalPath = file1.getCanonicalPath();
        if (canonicalPath.equals(file2.getCanonicalPath())) {
            throw new IllegalArgumentException(String
                                                       .format("File canonical paths are equal: '%s' (file1='%s', file2='%s')",
                                                               canonicalPath, file1, file2));
        }
    }

    private static void requireCanWrite(final File file, final String name) {
        Objects.requireNonNull(file, "file");
        if (!file.canWrite()) {
            throw new IllegalArgumentException(
                    "File parameter '" + name + " is not writable: '" + file + "'");
        }
    }


    private static File requireDirectory(final File directory, final String name) {
        Objects.requireNonNull(directory, name);
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(
                    "Parameter '" + name + "' is not a directory: '" + directory + "'");
        }
        return directory;
    }

    private static File requireDirectoryExists(final File directory, final String name) {
        requireExists(directory, name);
        requireDirectory(directory, name);
        return directory;
    }

    private static File requireDirectoryIfExists(final File directory, final String name) {
        Objects.requireNonNull(directory, name);
        if (directory.exists()) {
            requireDirectory(directory, name);
        }
        return directory;
    }


    private static void requireEqualSizes(
            final File srcFile, final File destFile, final long srcLen, final long dstLen
    )
            throws IOException {
        if (srcLen != dstLen) {
            throw new IOException(
                    "Failed to copy full contents from '" + srcFile + "' to '" + destFile
                    + "' Expected length: " + srcLen + " Actual: " + dstLen);
        }
    }


    private static File requireExists(final File file, final String fileParamName) {
        Objects.requireNonNull(file, fileParamName);
        if (!file.exists()) {
            throw new IllegalArgumentException(
                    "File system element for parameter '" + fileParamName + "' does not exist: '" +
                    file + "'");
        }
        return file;
    }


    private static File requireExistsChecked(
            final File file, final String fileParamName
    ) throws FileNotFoundException {
        Objects.requireNonNull(file, fileParamName);
        if (!file.exists()) {
            throw new FileNotFoundException(
                    "File system element for parameter '" + fileParamName + "' does not exist: '" +
                    file + "'");
        }
        return file;
    }


    private static File requireFile(final File file, final String name) {
        Objects.requireNonNull(file, name);
        if (!file.isFile()) {
            throw new IllegalArgumentException("Parameter '" + name + "' is not a file: " + file);
        }
        return file;
    }


    private static void requireFileCopy(
            final File source, final File destination
    ) throws FileNotFoundException {
        requireExistsChecked(source, "source");
        Objects.requireNonNull(destination, "destination");
    }

    private static File requireFileIfExists(final File file, final String name) {
        Objects.requireNonNull(file, name);
        return file.exists() ? requireFile(file, name) : file;
    }


    private static void setLastModified(
            final File sourceFile, final File targetFile
    ) throws IOException {
        Objects.requireNonNull(sourceFile, "sourceFile");
        setLastModified(targetFile, lastModified(sourceFile));
    }


    private static void setLastModified(final File file, final long timeMillis) throws IOException {
        Objects.requireNonNull(file, "file");
        if (!file.setLastModified(timeMillis)) {
            throw new IOException(
                    String.format("Failed setLastModified(%s) on '%s'", timeMillis, file));
        }
    }


    public static long sizeOf(final File file) {
        requireExists(file, "file");
        return file.isDirectory() ? sizeOfDirectory0(file) : file.length();
    }


    private static long sizeOf0(final File file) {
        Objects.requireNonNull(file, "file");
        if (file.isDirectory()) {
            return sizeOfDirectory0(file);
        }
        return file.length(); // will be 0 if file does not exist
    }


    public static BigInteger sizeOfAsBigInteger(final File file) {
        requireExists(file, "file");
        return file.isDirectory() ? sizeOfDirectoryBig0(file) : BigInteger.valueOf(file.length());
    }

    private static BigInteger sizeOfBig0(final File file) {
        Objects.requireNonNull(file, "fileOrDir");
        return file.isDirectory() ? sizeOfDirectoryBig0(file) : BigInteger.valueOf(file.length());
    }


    public static long sizeOfDirectory(final File directory) {
        return sizeOfDirectory0(requireDirectoryExists(directory, "directory"));
    }


    private static long sizeOfDirectory0(final File directory) {
        Objects.requireNonNull(directory, "directory");
        final File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            return 0L;
        }
        long size = 0;

        for (final File file : files) {
            if (!isSymlink(file)) {
                size += sizeOf0(file);
                if (size < 0) {
                    break;
                }
            }
        }

        return size;
    }

    public static BigInteger sizeOfDirectoryAsBigInteger(final File directory) {
        return sizeOfDirectoryBig0(requireDirectoryExists(directory, "directory"));
    }


    private static BigInteger sizeOfDirectoryBig0(final File directory) {
        Objects.requireNonNull(directory, "directory");
        final File[] files = directory.listFiles();
        if (files == null) {
            // null if security restricted
            return BigInteger.ZERO;
        }
        BigInteger size = BigInteger.ZERO;

        for (final File file : files) {
            if (!isSymlink(file)) {
                size = size.add(sizeOfBig0(file));
            }
        }

        return size;
    }



    public static File toFile(final URL url) {
        if (url == null || !"file".equalsIgnoreCase(url.getProtocol())) {
            return null;
        }
        final String filename = url.getFile().replace('/', File.separatorChar);
        return new File(decodeUrl(filename));
    }


    public static File[] toFiles(final URL... urls) {
        if (urls.length == 0) {
            return EMPTY_FILE_ARRAY;
        }
        final File[] files = new File[urls.length];
        for (int i = 0; i < urls.length; i++) {
            final URL url = urls[i];
            if (url != null) {
                if (!"file".equalsIgnoreCase(url.getProtocol())) {
                    throw new IllegalArgumentException(
                            "Can only convert file URL to a File: " + url);
                }
                files[i] = toFile(url);
            }
        }
        return files;
    }

    private static List<File> toList(final Stream<File> stream) {
        return stream.collect(Collectors.toList());
    }


    private static int toMaxDepth(final boolean recursive) {
        return recursive ? Integer.MAX_VALUE : 1;
    }


    private static String[] toSuffixes(final String... extensions) {
        Objects.requireNonNull(extensions, "extensions");
        final String[] suffixes = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            suffixes[i] = "." + extensions[i];
        }
        return suffixes;
    }


    public static void touch(final File file) throws IOException {
        Objects.requireNonNull(file, "file");
        if (!file.exists()) {
            openOutputStream(file).close();
        }
        setLastModified(file, System.currentTimeMillis());
    }

    public static URL[] toURLs(final File... files) throws IOException {
        Objects.requireNonNull(files, "files");
        final URL[] urls = new URL[files.length];
        for (int i = 0; i < urls.length; i++) {
            urls[i] = files[i].toURI().toURL();
        }
        return urls;
    }


    private static void validateMoveParameters(
            final File source, final File destination
    ) throws FileNotFoundException {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(destination, "destination");
        if (!source.exists()) {
            throw new FileNotFoundException("Source '" + source + "' does not exist");
        }
    }

    public static boolean waitFor(final File file, final int seconds) {
        Objects.requireNonNull(file, "file");
        final long finishAtMillis = System.currentTimeMillis() + (seconds * 1000L);
        boolean wasInterrupted = false;
        try {
            while (!file.exists()) {
                final long remainingMillis = finishAtMillis - System.currentTimeMillis();
                if (remainingMillis < 0) {
                    return false;
                }
                try {
                    Thread.sleep(Math.min(100, remainingMillis));
                } catch (final InterruptedException ignore) {
                    wasInterrupted = true;
                } catch (final Exception ex) {
                    break;
                }
            }
        } finally {
            if (wasInterrupted) {
                Thread.currentThread().interrupt();
            }
        }
        return true;
    }

    public static void writeByteArrayToFile(final File file, final byte[] data) throws IOException {
        writeByteArrayToFile(file, data, false);
    }


    public static void writeByteArrayToFile(
            final File file, final byte[] data, final boolean append
    )
            throws IOException {
        writeByteArrayToFile(file, data, 0, data.length, append);
    }


    public static void writeByteArrayToFile(
            final File file, final byte[] data, final int off, final int len
    )
            throws IOException {
        writeByteArrayToFile(file, data, off, len, false);
    }

    public static void writeByteArrayToFile(
            final File file, final byte[] data, final int off, final int len,
            final boolean append
    ) throws IOException {
        try (OutputStream out = openOutputStream(file, append)) {
            out.write(data, off, len);
        }
    }

}
