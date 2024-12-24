package com.nothing.commonutils.utils;

import java.util.HashMap;
import java.util.Map;

public class MimeTypeUtils {

    // 用于存储文件扩展名到MIME类型的映射
    private static final Map<String, String> EXTENSION_TO_MIME_TYPE_MAP = new HashMap<>();

    // 用于存储MIME类型到文件扩展名的映射
    private static final Map<String, String> MIME_TYPE_TO_EXTENSION_MAP = new HashMap<>();

    static {
        // 初始化文件扩展名到MIME类型的映射
        EXTENSION_TO_MIME_TYPE_MAP.put("doc", "application/msword");
        EXTENSION_TO_MIME_TYPE_MAP.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        EXTENSION_TO_MIME_TYPE_MAP.put("xls", "application/vnd.ms-excel");
        EXTENSION_TO_MIME_TYPE_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        EXTENSION_TO_MIME_TYPE_MAP.put("ppt", "application/vnd.ms-powerpoint");
        EXTENSION_TO_MIME_TYPE_MAP.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        EXTENSION_TO_MIME_TYPE_MAP.put("gz", "application/x-gzip");
        EXTENSION_TO_MIME_TYPE_MAP.put("zip", "application/zip");
        EXTENSION_TO_MIME_TYPE_MAP.put("7zip", "application/zip");
        EXTENSION_TO_MIME_TYPE_MAP.put("rar", "application/rar");
        EXTENSION_TO_MIME_TYPE_MAP.put("tar", "application/x-tar");
        EXTENSION_TO_MIME_TYPE_MAP.put("tgz", "application/x-compressed");
        EXTENSION_TO_MIME_TYPE_MAP.put("pdf", "application/pdf");
        EXTENSION_TO_MIME_TYPE_MAP.put("rtf", "application/rtf");
        EXTENSION_TO_MIME_TYPE_MAP.put("gif", "image/gif");
        EXTENSION_TO_MIME_TYPE_MAP.put("jpg", "image/jpeg");
        EXTENSION_TO_MIME_TYPE_MAP.put("jpeg", "image/jpeg");
        EXTENSION_TO_MIME_TYPE_MAP.put("jp2", "image/jp2");
        EXTENSION_TO_MIME_TYPE_MAP.put("png", "image/png");
        EXTENSION_TO_MIME_TYPE_MAP.put("bmp", "image/bmp");
        EXTENSION_TO_MIME_TYPE_MAP.put("svg", "image/svg+xml");
        EXTENSION_TO_MIME_TYPE_MAP.put("webp", "image/webp");
        EXTENSION_TO_MIME_TYPE_MAP.put("ico", "image/x-icon");
        EXTENSION_TO_MIME_TYPE_MAP.put("wps", "application/kswps");
        EXTENSION_TO_MIME_TYPE_MAP.put("et", "application/kset");
        EXTENSION_TO_MIME_TYPE_MAP.put("dps", "application/ksdps");
        EXTENSION_TO_MIME_TYPE_MAP.put("psd", "application/x-photoshop");
        EXTENSION_TO_MIME_TYPE_MAP.put("cdr", "application/x-coreldraw");
        EXTENSION_TO_MIME_TYPE_MAP.put("swf", "application/x-shockwave-flash");
        EXTENSION_TO_MIME_TYPE_MAP.put("txt", "text/plain");
        EXTENSION_TO_MIME_TYPE_MAP.put("js", "application/x-javascript");
        EXTENSION_TO_MIME_TYPE_MAP.put("css", "text/css");
        EXTENSION_TO_MIME_TYPE_MAP.put("htm", "text/html");
        EXTENSION_TO_MIME_TYPE_MAP.put("html", "text/html");
        EXTENSION_TO_MIME_TYPE_MAP.put("shtml", "text/html");
        EXTENSION_TO_MIME_TYPE_MAP.put("xht", "application/xhtml+xml");
        EXTENSION_TO_MIME_TYPE_MAP.put("xhtml", "application/xhtml+xml");
        EXTENSION_TO_MIME_TYPE_MAP.put("xml", "text/xml");
        EXTENSION_TO_MIME_TYPE_MAP.put("vcf", "text/x-vcard");
        EXTENSION_TO_MIME_TYPE_MAP.put("php", "application/x-httpd-php");
        EXTENSION_TO_MIME_TYPE_MAP.put("jar", "application/java-archive");
        EXTENSION_TO_MIME_TYPE_MAP.put("apk", "application/vnd.android.package-archive");
        EXTENSION_TO_MIME_TYPE_MAP.put("exe", "application/octet-stream");
        EXTENSION_TO_MIME_TYPE_MAP.put("crt", "application/x-x509-user-cert");
        EXTENSION_TO_MIME_TYPE_MAP.put("pem", "application/x-x509-user-cert");
        EXTENSION_TO_MIME_TYPE_MAP.put("mp3", "audio/mpeg");
        EXTENSION_TO_MIME_TYPE_MAP.put("mid", "audio/midi");
        EXTENSION_TO_MIME_TYPE_MAP.put("midi", "audio/midi");
        EXTENSION_TO_MIME_TYPE_MAP.put("wav", "audio/x-wav");
        EXTENSION_TO_MIME_TYPE_MAP.put("m3u", "audio/x-mpegurl");
        EXTENSION_TO_MIME_TYPE_MAP.put("m4a", "audio/x-m4a");
        EXTENSION_TO_MIME_TYPE_MAP.put("ogg", "audio/ogg");
        EXTENSION_TO_MIME_TYPE_MAP.put("ra", "audio/x-realaudio");
        EXTENSION_TO_MIME_TYPE_MAP.put("mp4", "video/mp4");
        EXTENSION_TO_MIME_TYPE_MAP.put("mpeg", "video/mpeg");
        EXTENSION_TO_MIME_TYPE_MAP.put("mpg", "video/mpeg");
        EXTENSION_TO_MIME_TYPE_MAP.put("mpe", "video/mpeg");
        EXTENSION_TO_MIME_TYPE_MAP.put("mov", "video/quicktime");
        EXTENSION_TO_MIME_TYPE_MAP.put("qt", "video/quicktime");
        EXTENSION_TO_MIME_TYPE_MAP.put("m4v", "video/x-m4v");
        EXTENSION_TO_MIME_TYPE_MAP.put("wmv", "video/x-ms-wmv");
        EXTENSION_TO_MIME_TYPE_MAP.put("avi", "video/x-msvideo");
        EXTENSION_TO_MIME_TYPE_MAP.put("webm", "video/webm");
        EXTENSION_TO_MIME_TYPE_MAP.put("flv", "video/x-flv");

        EXTENSION_TO_MIME_TYPE_MAP.put("evy", "application/envoy");
        EXTENSION_TO_MIME_TYPE_MAP.put("fif", "application/fractals");
        EXTENSION_TO_MIME_TYPE_MAP.put("spl", "application/futuresplash");
        EXTENSION_TO_MIME_TYPE_MAP.put("hta", "application/hta");
        EXTENSION_TO_MIME_TYPE_MAP.put("acx", "application/internet-property-stream");
        EXTENSION_TO_MIME_TYPE_MAP.put("hqx", "application/mac-binhex40");
        EXTENSION_TO_MIME_TYPE_MAP.put("dot", "application/msword");
        EXTENSION_TO_MIME_TYPE_MAP.put("bin", "application/octet-stream");
        EXTENSION_TO_MIME_TYPE_MAP.put("class", "application/octet-stream");
        EXTENSION_TO_MIME_TYPE_MAP.put("dms", "application/octet-stream");
        EXTENSION_TO_MIME_TYPE_MAP.put("lha", "application/octet-stream");
        EXTENSION_TO_MIME_TYPE_MAP.put("lzh", "application/octet-stream");
        EXTENSION_TO_MIME_TYPE_MAP.put("oda", "application/oda");
        EXTENSION_TO_MIME_TYPE_MAP.put("axs", "application/olescript");
        EXTENSION_TO_MIME_TYPE_MAP.put("prf", "application/pics-rules");
        EXTENSION_TO_MIME_TYPE_MAP.put("p10", "application/pkcs10");
        EXTENSION_TO_MIME_TYPE_MAP.put("crl", "application/pkix-crl");
        EXTENSION_TO_MIME_TYPE_MAP.put("ai", "application/postscript");
        EXTENSION_TO_MIME_TYPE_MAP.put("eps", "application/postscript");
        EXTENSION_TO_MIME_TYPE_MAP.put("ps", "application/postscript");
        EXTENSION_TO_MIME_TYPE_MAP.put("setpay", "application/set-payment-initiation");
        EXTENSION_TO_MIME_TYPE_MAP.put("setreg", "application/set-registration-initiation");
        EXTENSION_TO_MIME_TYPE_MAP.put("xla", "application/vnd.ms-excel");
        EXTENSION_TO_MIME_TYPE_MAP.put("xlc", "application/vnd.ms-excel");
        EXTENSION_TO_MIME_TYPE_MAP.put("xlm", "application/vnd.ms-excel");
        EXTENSION_TO_MIME_TYPE_MAP.put("xlt", "application/vnd.ms-excel");
        EXTENSION_TO_MIME_TYPE_MAP.put("xlw", "application/vnd.ms-excel");
        EXTENSION_TO_MIME_TYPE_MAP.put("msg", "application/vnd.ms-outlook");
        EXTENSION_TO_MIME_TYPE_MAP.put("sst", "application/vnd.ms-pkicertstore");
        EXTENSION_TO_MIME_TYPE_MAP.put("cat", "application/vnd.ms-pkiseccat");
        EXTENSION_TO_MIME_TYPE_MAP.put("stl", "application/vnd.ms-pkistl");
        EXTENSION_TO_MIME_TYPE_MAP.put("pot", "application/vnd.ms-powerpoint");
        EXTENSION_TO_MIME_TYPE_MAP.put("pps", "application/vnd.ms-powerpoint");
        EXTENSION_TO_MIME_TYPE_MAP.put("mpp", "application/vnd.ms-project");
        EXTENSION_TO_MIME_TYPE_MAP.put("wcm", "application/vnd.ms-works");
        EXTENSION_TO_MIME_TYPE_MAP.put("wdb", "application/vnd.ms-works");
        EXTENSION_TO_MIME_TYPE_MAP.put("wks", "application/vnd.ms-works");
        EXTENSION_TO_MIME_TYPE_MAP.put("hlp", "application/winhlp");
        EXTENSION_TO_MIME_TYPE_MAP.put("bcpio", "application/x-bcpio");
        EXTENSION_TO_MIME_TYPE_MAP.put("cdf", "application/x-cdf");
        EXTENSION_TO_MIME_TYPE_MAP.put("z", "application/x-compress");
        EXTENSION_TO_MIME_TYPE_MAP.put("cpio", "application/x-cpio");
        EXTENSION_TO_MIME_TYPE_MAP.put("csh", "application/x-csh");
        EXTENSION_TO_MIME_TYPE_MAP.put("dcr", "application/x-director");
        EXTENSION_TO_MIME_TYPE_MAP.put("dir", "application/x-director");
        EXTENSION_TO_MIME_TYPE_MAP.put("dxr", "application/x-director");
        EXTENSION_TO_MIME_TYPE_MAP.put("dvi", "application/x-dvi");
        EXTENSION_TO_MIME_TYPE_MAP.put("gtar", "application/x-gtar");
        EXTENSION_TO_MIME_TYPE_MAP.put("hdf", "application/x-hdf");
        EXTENSION_TO_MIME_TYPE_MAP.put("ins", "application/x-internet-signup");
        EXTENSION_TO_MIME_TYPE_MAP.put("isp", "application/x-internet-signup");
        EXTENSION_TO_MIME_TYPE_MAP.put("iii", "application/x-iphone");
        EXTENSION_TO_MIME_TYPE_MAP.put("latex", "application/x-latex");
        EXTENSION_TO_MIME_TYPE_MAP.put("mdb", "application/x-msaccess");
        EXTENSION_TO_MIME_TYPE_MAP.put("crd", "application/x-mscardfile");
        EXTENSION_TO_MIME_TYPE_MAP.put("clp", "application/x-msclip");
        EXTENSION_TO_MIME_TYPE_MAP.put("dll", "application/x-msdownload");
        EXTENSION_TO_MIME_TYPE_MAP.put("m13", "application/x-msmediaview");
        EXTENSION_TO_MIME_TYPE_MAP.put("m14", "application/x-msmediaview");
        EXTENSION_TO_MIME_TYPE_MAP.put("mvb", "application/x-msmediaview");
        EXTENSION_TO_MIME_TYPE_MAP.put("wmf", "application/x-msmetafile");
        EXTENSION_TO_MIME_TYPE_MAP.put("mny", "application/x-msmoney");
        EXTENSION_TO_MIME_TYPE_MAP.put("pub", "application/x-mspublisher");
        EXTENSION_TO_MIME_TYPE_MAP.put("scd", "application/x-msschedule");
        EXTENSION_TO_MIME_TYPE_MAP.put("trm", "application/x-msterminal");
        EXTENSION_TO_MIME_TYPE_MAP.put("wri", "application/x-mswrite");
        EXTENSION_TO_MIME_TYPE_MAP.put("nc", "application/x-netcdf");
        EXTENSION_TO_MIME_TYPE_MAP.put("pma", "application/x-perfmon");
        EXTENSION_TO_MIME_TYPE_MAP.put("pmc", "application/x-perfmon");
        EXTENSION_TO_MIME_TYPE_MAP.put("pml", "application/x-perfmon");
        EXTENSION_TO_MIME_TYPE_MAP.put("pmr", "application/x-perfmon");
        EXTENSION_TO_MIME_TYPE_MAP.put("pmw", "application/x-perfmon");
        EXTENSION_TO_MIME_TYPE_MAP.put("p12", "application/x-pkcs12");
        EXTENSION_TO_MIME_TYPE_MAP.put("pfx", "application/x-pkcs12");
        EXTENSION_TO_MIME_TYPE_MAP.put("p7b", "application/x-pkcs7-certificates");
        EXTENSION_TO_MIME_TYPE_MAP.put("p7c", "application/x-pkcs7-mime");
        EXTENSION_TO_MIME_TYPE_MAP.put("p7m", "application/x-pkcs7-mime");
        EXTENSION_TO_MIME_TYPE_MAP.put("p7r", "application/x-pkcs7-certreqresp");
        EXTENSION_TO_MIME_TYPE_MAP.put("p7s", "application/x-pkcs7-signature");
        EXTENSION_TO_MIME_TYPE_MAP.put("sh", "application/x-sh");
        EXTENSION_TO_MIME_TYPE_MAP.put("shar", "application/x-shar");
        EXTENSION_TO_MIME_TYPE_MAP.put("sit", "application/x-stuffit");
        EXTENSION_TO_MIME_TYPE_MAP.put("sv4cpio", "application/x-sv4cpio");
        EXTENSION_TO_MIME_TYPE_MAP.put("sv4crc", "application/x-sv4crc");
        EXTENSION_TO_MIME_TYPE_MAP.put("tcl", "application/x-tcl");
        EXTENSION_TO_MIME_TYPE_MAP.put("tex", "application/x-tex");
        EXTENSION_TO_MIME_TYPE_MAP.put("texi", "application/x-texinfo");
        EXTENSION_TO_MIME_TYPE_MAP.put("texinfo", "application/x-texinfo");
        EXTENSION_TO_MIME_TYPE_MAP.put("roff", "application/x-troff");
        EXTENSION_TO_MIME_TYPE_MAP.put("t", "application/x-troff");
        EXTENSION_TO_MIME_TYPE_MAP.put("tr", "application/x-troff");
        EXTENSION_TO_MIME_TYPE_MAP.put("man", "application/x-troff-man");
        EXTENSION_TO_MIME_TYPE_MAP.put("me", "application/x-troff-me");
        EXTENSION_TO_MIME_TYPE_MAP.put("ms", "application/x-troff-ms");
        EXTENSION_TO_MIME_TYPE_MAP.put("ustar", "application/x-ustar");
        EXTENSION_TO_MIME_TYPE_MAP.put("src", "application/x-wais-source");
        EXTENSION_TO_MIME_TYPE_MAP.put("cer", "application/x-x509-ca-cert");
        EXTENSION_TO_MIME_TYPE_MAP.put("der", "application/x-x509-ca-cert");
        EXTENSION_TO_MIME_TYPE_MAP.put("pko", "application/ynd.ms-pkipko");
        EXTENSION_TO_MIME_TYPE_MAP.put("au", "audio/basic");
        EXTENSION_TO_MIME_TYPE_MAP.put("snd", "audio/basic");
        EXTENSION_TO_MIME_TYPE_MAP.put("rmi", "audio/mid");
        EXTENSION_TO_MIME_TYPE_MAP.put("aif", "audio/x-aiff");
        EXTENSION_TO_MIME_TYPE_MAP.put("aifc", "audio/x-aiff");
        EXTENSION_TO_MIME_TYPE_MAP.put("aiff", "audio/x-aiff");
        EXTENSION_TO_MIME_TYPE_MAP.put("ram", "audio/x-pn-realaudio");
        EXTENSION_TO_MIME_TYPE_MAP.put("jpe", "image/jpeg");
        EXTENSION_TO_MIME_TYPE_MAP.put("heic", "image/heic");
        EXTENSION_TO_MIME_TYPE_MAP.put("jfif", "image/pipeg");
        EXTENSION_TO_MIME_TYPE_MAP.put("tif", "image/tiff");
        EXTENSION_TO_MIME_TYPE_MAP.put("tiff", "image/tiff");
        EXTENSION_TO_MIME_TYPE_MAP.put("ras", "image/x-cmu-raster");
        EXTENSION_TO_MIME_TYPE_MAP.put("cmx", "image/x-cmx");
        EXTENSION_TO_MIME_TYPE_MAP.put("pnm", "image/x-portable-anymap");
        EXTENSION_TO_MIME_TYPE_MAP.put("pbm", "image/x-portable-bitmap");
        EXTENSION_TO_MIME_TYPE_MAP.put("pgm", "image/x-portable-graymap");
        EXTENSION_TO_MIME_TYPE_MAP.put("ppm", "image/x-portable-pixmap");
        EXTENSION_TO_MIME_TYPE_MAP.put("rgb", "image/x-rgb");
        EXTENSION_TO_MIME_TYPE_MAP.put("xbm", "image/x-xbitmap");
        EXTENSION_TO_MIME_TYPE_MAP.put("xpm", "image/x-xpixmap");
        EXTENSION_TO_MIME_TYPE_MAP.put("xwd", "image/x-xwindowdump");
        EXTENSION_TO_MIME_TYPE_MAP.put("mht", "message/rfc822");
        EXTENSION_TO_MIME_TYPE_MAP.put("mhtml", "message/rfc822");
        EXTENSION_TO_MIME_TYPE_MAP.put("nws", "message/rfc822");
        EXTENSION_TO_MIME_TYPE_MAP.put("323", "text/h323");
        EXTENSION_TO_MIME_TYPE_MAP.put("stm", "text/html");
        EXTENSION_TO_MIME_TYPE_MAP.put("uls", "text/iuls");
        EXTENSION_TO_MIME_TYPE_MAP.put("bas", "text/plain");
        EXTENSION_TO_MIME_TYPE_MAP.put("c", "text/plain");
        EXTENSION_TO_MIME_TYPE_MAP.put("h", "text/plain");
        EXTENSION_TO_MIME_TYPE_MAP.put("rtx", "text/richtext");
        EXTENSION_TO_MIME_TYPE_MAP.put("sct", "text/scriptlet");
        EXTENSION_TO_MIME_TYPE_MAP.put("tsv", "text/tab-separated-values");
        EXTENSION_TO_MIME_TYPE_MAP.put("htt", "text/webviewhtml");
        EXTENSION_TO_MIME_TYPE_MAP.put("htc", "text/x-component");
        EXTENSION_TO_MIME_TYPE_MAP.put("etx", "text/x-setext");
        EXTENSION_TO_MIME_TYPE_MAP.put("mp2", "video/mpeg");
        EXTENSION_TO_MIME_TYPE_MAP.put("mpa", "video/mpeg");
        EXTENSION_TO_MIME_TYPE_MAP.put("mpv2", "video/mpeg");
        EXTENSION_TO_MIME_TYPE_MAP.put("lsf", "video/x-la-asf");
        EXTENSION_TO_MIME_TYPE_MAP.put("lsx", "video/x-la-asf");
        EXTENSION_TO_MIME_TYPE_MAP.put("asf", "video/x-ms-asf");
        EXTENSION_TO_MIME_TYPE_MAP.put("asr", "video/x-ms-asf");
        EXTENSION_TO_MIME_TYPE_MAP.put("asx", "video/x-ms-asf");
        EXTENSION_TO_MIME_TYPE_MAP.put("movie", "video/x-sgi-movie");
        EXTENSION_TO_MIME_TYPE_MAP.put("flr", "x-world/x-vrml");
        EXTENSION_TO_MIME_TYPE_MAP.put("vrml", "x-world/x-vrml");
        EXTENSION_TO_MIME_TYPE_MAP.put("wrl", "x-world/x-vrml");
        EXTENSION_TO_MIME_TYPE_MAP.put("wrz", "x-world/x-vrml");
        EXTENSION_TO_MIME_TYPE_MAP.put("xaf", "x-world/x-vrml");
        EXTENSION_TO_MIME_TYPE_MAP.put("xof", "x-world/x-vrml");

        EXTENSION_TO_MIME_TYPE_MAP.put("", "application/octet-stream");

        // 初始化MIME类型到文件扩展名的映射
        for (Map.Entry<String, String> entry : EXTENSION_TO_MIME_TYPE_MAP.entrySet()) {
            MIME_TYPE_TO_EXTENSION_MAP.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * 根据文件扩展名获取对应的MIME类型
     *
     * @param fileExtension 文件扩展名，如"pdf"、"jpg"等
     * @return 对应的MIME类型
     */
    public static String getMimeTypeByExtension(String fileExtension) {
        return EXTENSION_TO_MIME_TYPE_MAP.getOrDefault(fileExtension,"application/octet-stream");
    }

    /**
     * 根据MIME类型获取可能的文件扩展名
     *
     * @param mimeType MIME类型，如"image/jpeg"、"application/pdf"等
     * @return 对应的文件扩展
     */
    public static String getExtensionByMimeType(String mimeType) {
        return MIME_TYPE_TO_EXTENSION_MAP.getOrDefault(mimeType,"application/octet-stream");
    }

    public static String getFileExtension(String filePath) {
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filePath.length() - 1) {
            return filePath.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

}