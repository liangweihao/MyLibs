package com.nothing.commonutils.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import androidx.core.content.ContextCompat
import java.io.*
import java.util.*


@Deprecated(" use NewFileUtils")
object FileUtils {

    /**
     * @author laijian
     * @version 2017/9/11
     * @Copyright (C)下午12:02 , www.hotapk.cn
     * 文件操作工具类
     */
    /**
     * 获取根目录
     */
    val rootDir: String
        get() = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            Environment.getExternalStorageDirectory().absolutePath
        } else {
            ""
        }

    /**
     * 获取扩展内存的路径
     *
     * @return
     */
    fun getStoragePath(context: Context): String? {
        val mStorageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        var storageVolumeClazz: Class<*>? = null
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
            val getVolumeList = mStorageManager.javaClass.getMethod("getVolumeList")
            val getPath = storageVolumeClazz.getMethod("getPath")
            val isRemovable = storageVolumeClazz.getMethod("isRemovable")
            val result = getVolumeList.invoke(mStorageManager)
            val length = java.lang.reflect.Array.getLength(result)
            for (i in 0 until length) {
                val storageVolumeElement = java.lang.reflect.Array.get(result, i)
                val path = getPath.invoke(storageVolumeElement) as String
                val removable = isRemovable.invoke(storageVolumeElement) as Boolean
                if (removable) {
                    return path
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 获取存储空间总容量
     *
     * @param filePath
     * @return
     */
    fun storageToal(filePath: File): Long {
        val stat = StatFs(filePath.path) // 创建StatFs对象
        val blockSize = stat.blockSizeLong // 获取block的size
        val totalBlocks = stat.blockCountLong // 获取block的总数
        return blockSize * totalBlocks
    }

    /**
     * 获取存储空间使用量
     *
     * @param filePath
     * @return
     */
    fun storageUse(filePath: File): Long {
        val stat = StatFs(filePath.path) // 创建StatFs对象
        val availableBlocks = stat.availableBlocksLong // 获取可用块大小
        val blockSize = stat.blockSizeLong // 获取block的size
        val totalBlocks = stat.blockCountLong // 获取block的总数
        return (totalBlocks - availableBlocks) * blockSize
    }

    /**
     * 可创建多个文件夹
     * dirPath 文件路径
     */
    fun mkDir(dirPath: String): Boolean {
        val dirArray = dirPath.split("/").toTypedArray()
        var pathTemp = ""
        var mkdir = false
        for (i in dirArray.indices) {
            pathTemp = pathTemp + "/" + dirArray[i]
            val newF = File(dirArray[0] + pathTemp)
            if (!newF.exists()) {
                mkdir = newF.mkdir()
            }
        }
        return mkdir
    }

    /**
     * 创建文件
     *
     *
     * dirpath 文件目录
     * fileName 文件名称
     */
    fun creatFile(dirPath: String?, fileName: String?): Boolean {
        val file = File(dirPath, fileName)
        var newFile = false
        if (!file.exists()) {
            newFile = try {
                file.createNewFile()
            } catch (e: IOException) {
                false
            }
        }
        return newFile
    }

    /**
     * 创建文件
     * filePath 文件路径
     */
    fun creatFile(filePath: String?): Boolean {
        val file = File(filePath)
        var newFile = false
        if (!file.exists()) {
            newFile = try {
                file.createNewFile()
            } catch (e: IOException) {
                false
            }
        }
        return newFile
    }

    /**
     * 创建文件
     * file 文件
     */
    fun creatFile(file: File): Boolean {
        var newFile = false
        if (!file.exists()) {
            newFile = try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
        return newFile
    }

    /**
     * 删除文件
     * dirpath 文件目录
     * fileName 文件名称
     */
    fun delFile(dirpath: String?, fileName: String?): Boolean {
        val file = File(dirpath, fileName)
        var delete = false
        delete = if (!file.exists() || file.isDirectory) {
            false
        } else {
            file.delete()
        }
        return delete
    }

    /**
     * 删除文件
     * filepath 文件路径
     */
    fun delFile(filepath: String?): Boolean {
        val file = File(filepath ?: "")
        var delete = false
        delete = if (!file.exists() || file.isDirectory) {
            false
        } else {
            file.delete()
        }
        return delete
    }

    /**
     * 删除文件
     * filepath 文件路径
     */
    fun delFile(filepath: File?): Boolean {
        var delete = false
        delete = if (filepath == null || !filepath.exists() || filepath.isDirectory) {
            false
        } else {
            filepath.delete()
        }
        return delete
    }

    /**
     * 删除文件夹
     * dirPath 文件路径
     */
    fun delDir(dirpath: String?): Boolean {
        val dir = File(dirpath)
        return deleteDirWihtFile(dir)
    }

    fun deleteDirWihtFile(dir: File?): Boolean {
        var delete = false
        if (dir == null || !dir.exists() || !dir.isDirectory) {
            delete = false
        } else {
            val files = dir.listFiles()
            for (i in files.indices) {
                if (files[i].isFile) {
                    files[i].delete()
                } else if (files[i].isDirectory) {
                    deleteDirWihtFile(files[i])
                }
                delete = dir.delete() // 删除目录本身
            }
        }
        return delete
    }

    /**
     * 修改SD卡上的文件或目录名
     * oldFilePath 旧文件或文件夹路径
     * newFilePath 新文件或文件夹路径
     */
    fun renameFile(oldFilePath: String?, newFilePath: String?): Boolean {
        val oldFile = File(oldFilePath)
        val newFile = File(newFilePath)
        return oldFile.renameTo(newFile)
    }

    fun copyFileTo(srcFile: String?, destFile: String?): Boolean {
        return copyFileTo(File(srcFile), File(destFile))
    }

    /**
     * 拷贝一个文件
     * srcFile源文件
     * destFile目标文件
     */
    fun copyFileTo(srcFile: File, destFile: File): Boolean {
        var copyFile = false
        if (!srcFile.exists() || srcFile.isDirectory || destFile.isDirectory) {
            copyFile = false
        } else {
            var `is`: FileInputStream? = null
            var os: FileOutputStream? = null
            try {
                `is` = FileInputStream(srcFile)
                os = FileOutputStream(destFile)
                val buffer = ByteArray(1024)
                var length: Int
                while (`is`.read(buffer).also { length = it } > 0) {
                    os.write(buffer, 0, length)
                }
                copyFile = true
            } catch (e: Exception) {
                copyFile = false
            } finally {
                if (`is` != null) {
                    try {
                        `is`.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                if (os != null) {
                    try {
                        os.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return copyFile
    }

    /**
     * 拷贝目录下的所有文件到指定目录
     * srcDir 原目录
     * destDir 目标目录
     */
    fun copyFilesTo(srcDir: File, destDir: File): Boolean {
        if (!srcDir.exists() || !srcDir.isDirectory || !destDir.isDirectory) {
            return false
        }
        val srcFiles = srcDir.listFiles()
        for (i in srcFiles.indices) {
            if (srcFiles[i].isFile) {
                val destFile = File(destDir.absolutePath, srcFiles[i].name)
                copyFileTo(srcFiles[i], destFile)
            } else {
                val theDestDir = File(destDir.absolutePath, srcFiles[i].name)
                copyFilesTo(srcFiles[i], theDestDir)
            }
        }
        return true
    }

    /**
     * 移动一个文件
     * srcFile源文件
     * destFile目标文件
     */
    fun moveFileTo(srcFile: File, destFile: File): Boolean {
        if (!srcFile.exists() || srcFile.isDirectory || destFile.isDirectory) {
            return false
        }
        val iscopy = copyFileTo(srcFile, destFile)
        return if (!iscopy) {
            false
        } else {
            delFile(srcFile)
            true
        }
    }

    /**
     * 移动目录下的所有文件到指定目录
     * srcDir 原路径
     * destDir 目标路径
     */
    fun moveFilesTo(srcDir: File, destDir: File): Boolean {
        if (!srcDir.exists() || !srcDir.isDirectory || !destDir.isDirectory) {
            return false
        }
        val srcDirFiles = srcDir.listFiles()
        for (i in srcDirFiles.indices) {
            if (srcDirFiles[i].isFile) {
                val oneDestFile = File(destDir.absolutePath, srcDirFiles[i].name)
                moveFileTo(srcDirFiles[i], oneDestFile)
            } else {
                val oneDestFile = File(destDir.absolutePath, srcDirFiles[i].name)
                moveFilesTo(srcDirFiles[i], oneDestFile)
            }
        }
        return true
    }

    /**
     * 文件转byte数组
     * file 文件路径
     */
    @Throws(IOException::class)
    fun file2byte(file: File?): ByteArray? {
        var bytes: ByteArray? = null
        if (file != null) {
            val `is`: InputStream = FileInputStream(file)
            val length = file.length().toInt()
            if (length > Int.MAX_VALUE) { // 当文件的长度超过了int的最大值
                println("this file is max ")
                `is`.close()
                return null
            }
            bytes = ByteArray(length)
            var offset = 0
            var numRead = 0
            while (offset < bytes.size && `is`.read(bytes, offset, bytes.size - offset).also {
                    numRead = it
                } >= 0) {
                offset += numRead
            }
            `is`.close() // 如果得到的字节长度和file实际的长度不一致就可能出错了
            if (offset < bytes.size) {
                println("file length is error")
                return null
            }
        }
        return bytes
    }

    /**
     * 文件读取
     * filePath 文件路径
     */
    fun readFile(filePath: File): String? {
        var bufferedReader: BufferedReader? = null
        var fileStr: String? = ""
        if (!filePath.exists() || filePath.isDirectory) {
            return null
        }
        try {
            bufferedReader = BufferedReader(FileReader(filePath))
            var tempFileStr: String? = ""
            while (bufferedReader.readLine().also { tempFileStr = it } != null) {
                fileStr += tempFileStr
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return fileStr
    }

    /**
     * 文件读取
     * strPath 文件路径
     */
    fun readFile(strPath: String?): String? {
        return readFile(File(strPath))
    }

    /**
     * InputStream 转字符串
     */
    fun readInp(inputStream: InputStream): String {
        val outputStream = ByteArrayOutputStream()
        val buf = ByteArray(1024)
        try {
            var len1: Int
            while (inputStream.read(buf).also { len1 = it } != -1) {
                outputStream.write(buf, 0, len1)
            }
            inputStream.close()
            outputStream.close()
        } catch (var5: IOException) {
        }
        return outputStream.toString()
    }

    /**
     * InputStream转byte数组
     *
     * @param inputStream
     * @return
     */
    fun inputStreamToByteArray(inputStream: InputStream): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val buf = ByteArray(1024)
        try {
            var len1: Int
            while (inputStream.read(buf).also { len1 = it } != -1) {
                outputStream.write(buf, 0, len1)
            }
            outputStream.close()
            inputStream.close()
        } catch (var5: IOException) {
        }
        return outputStream.toByteArray()
    }

    /**
     * BufferedReader 转字符串
     */
    fun readBuff(bufferedReader: BufferedReader?): String? {
        var readerstr: String? = ""
        return try {
            var tempstr: String? = ""
            while (bufferedReader!!.readLine().also { tempstr = it } != null) {
                readerstr += tempstr
            }
            readerstr
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * InputStream转文件
     *
     * @param inputStream
     * @param absPath
     */
    fun inputStreamToFile(inputStream: InputStream, absPath: String?): Boolean {
        var fos: FileOutputStream? = null
        return try {
            fos = FileOutputStream(absPath, false)
            fos.write(inputStreamToByteArray(inputStream))
            true
        } catch (var7: IOException) {
            var7.printStackTrace()
            false
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 文件转InputStream
     *
     * @param absPath
     * @return
     */
    fun file2Inp(absPath: String?): InputStream? {
        val file = File(absPath)
        if (!file.exists()) {
            return null
        }
        var `is`: InputStream? = null
        return try {
            `is` = BufferedInputStream(FileInputStream(file))
            `is`
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 写入数据到文件
     *
     * @param filePath
     * @param content
     * @return
     */
    fun writeText(filePath: File, content: String?): Boolean {
        creatFile(filePath)
        var bufferedWriter: BufferedWriter? = null
        try {
            bufferedWriter = BufferedWriter(FileWriter(filePath))
            bufferedWriter.write(content)
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return true
    }

    fun writeText(filePath: String?, content: String?): Boolean {
        return writeText(filePath, content)
    }

    /**
     * byte数组转文件
     *
     * @param content
     * @param file_name
     */
    fun writeByteArrayToFile(content: ByteArray?, file_name: String?): Boolean {
        try {
            val file = File(file_name)
            val fileW = FileOutputStream(file.canonicalPath)
            fileW.write(content)
            fileW.close()
        } catch (var4: Exception) {
            return false
        }
        return true
    }

    /**
     * 追加数据
     *
     * @param filePath
     * @param content
     * @return
     */
    fun appendText(filePath: File, content: String?): Boolean {
        creatFile(filePath)
        var writer: FileWriter? = null
        try { // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = FileWriter(filePath, true)
            writer.write(content)
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            if (writer != null) {
                try {
                    writer.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return true
    }

    /**
     * 追加数据
     *
     * @param filePath
     * @param content
     * @param header   是否在头部追加数据
     */
    fun appendText(filePath: String?, content: String, header: Boolean) {
        var raf: RandomAccessFile? = null
        var tmpOut: FileOutputStream? = null
        var tmpIn: FileInputStream? = null
        try {
            val tmp = File.createTempFile("tmp", null)
            tmp.deleteOnExit() //在JVM退出时删除
            raf = RandomAccessFile(filePath, "rw") //创建一个临时文件夹来保存插入点后的数据
            tmpOut = FileOutputStream(tmp)
            tmpIn = FileInputStream(tmp)
            var fileLength: Long = 0
            if (!header) {
                fileLength = raf.length()
            }
            raf.seek(fileLength)
            /**将插入点后的内容读入临时文件夹 */
            val buff = ByteArray(1024) //用于保存临时读取的字节数
            var hasRead = 0 //循环读取插入点后的内容
            while (raf.read(buff).also { hasRead = it } > 0) { // 将读取的数据写入临时文件中
                tmpOut.write(buff, 0, hasRead)
            } //插入需要指定添加的数据
            raf.seek(fileLength) //返回原来的插入处
            //追加需要追加的内容
            raf.write(content.toByteArray()) //最后追加临时文件中的内容
            while (tmpIn.read(buff).also { hasRead = it } > 0) {
                raf.write(buff, 0, hasRead)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (tmpOut != null) {
                try {
                    tmpOut.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (tmpIn != null) {
                try {
                    tmpIn.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (raf != null) {
                try {
                    raf.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 获取文件大小
     *
     * @param filePath
     * @return
     */
    fun getLength(filePath: File): Long {
        return if (!filePath.exists()) {
            -1
        } else {
            filePath.length()
        }
    }

    /**
     * 获取文件大小
     *
     * @param filePath
     * @return
     */
    fun getLength(filePath: String?): Long {
        return getLength(File(filePath))
    }

    /**
     * 获取文件名
     *
     * @param filePath
     * @return
     */
    fun getFileName(filePath: String?): String? {
        val file = File(filePath)
        return if (!file.exists()) {
            null
        } else file.name
    }

    /**
     * 判断文件是否存在
     *
     * @param filePath
     * @return
     */
    fun exists(filePath: String?): Boolean {
        return if (File(filePath).exists()) {
            true
        } else false
    }

    /**
     * 按文件时间排序
     *
     * @param fliePath
     * @param desc
     * @return
     */
    fun orderByDate(fliePath: File, desc: Boolean): Array<File?> {
        val fs = fliePath.listFiles()
        Arrays.sort(fs, object : Comparator<File> {
            override fun compare(f1: File, f2: File): Int {
                val diff = f1.lastModified() - f2.lastModified()
                return if (diff > 0) 1 else if (diff == 0L) 0 else -1
            }

            override fun equals(obj: Any?): Boolean {
                return true
            }
        })
        return if (desc) {
            val nfs = arrayOfNulls<File>(fs.size)
            for (i in fs.size - 1 downTo -1 + 1) {
                nfs[fs.size - 1 - i] = fs[i]
            }
            nfs
        } else {
            fs
        }
    }

    /**
     * 按照文件名称排序
     *
     * @param fliePath
     * @param desc
     * @return
     */
    fun orderByName(fliePath: File, desc: Boolean): Array<File?> {
        val files = fliePath.listFiles()
        Arrays.sort(files, object : Comparator<File> {
            override fun compare(o1: File, o2: File): Int {
                if (o1.isDirectory && o2.isFile) return -1
                return if (o1.isFile && o2.isDirectory) 1 else o1.name.compareTo(o2.name)
            }
        })
        return if (desc) {
            val nfs = arrayOfNulls<File>(files.size)
            for (i in files.size - 1 downTo -1 + 1) {
                nfs[files.size - 1 - i] = files[i]
            }
            nfs
        } else {
            files
        }
    }

    /**
     * 按照文件大小排序
     *
     * @param fliePath
     */
    fun orderByLength(fliePath: File, desc: Boolean): Array<File?> {
        val files = fliePath.listFiles()
        Arrays.sort(files, object : Comparator<File> {
            override fun compare(f1: File, f2: File): Int {
                val diff = f1.length() - f2.length()
                return if (diff > 0) 1 else if (diff == 0L) 0 else -1
            }

            override fun equals(obj: Any?): Boolean {
                return true
            }
        })
        return if (desc) {
            val nfs = arrayOfNulls<File>(files.size)
            for (i in files.size - 1 downTo -1 + 1) {
                nfs[files.size - 1 - i] = files[i]
            }
            nfs
        } else {
            files
        }
    }

    /**
     * 文件筛选
     *
     * @param files
     * @param filter
     * @return
     */
    fun filter(files: Array<File>?, filter: String?): List<File> {
        val filels: MutableList<File> = ArrayList()
        if (files != null) {
            for (i in files.indices) {
                if (files[i].name.contains(filter!!)) {
                    filels.add(files[i])
                }
            }
        }
        return filels
    }

    /**
     * 文件筛选
     *
     * @param file
     * @param filterName
     * @return
     */
    fun fileNameFilter(file: File, filterName: String?): Array<File>? {
        return if (!file.isDirectory) {
            null
        } else file.listFiles { pathname ->
            pathname.name.contains(filterName!!)
        }
    }

    /**
     * 获取文件列表
     *
     * @param fileDir
     */
    fun getFiles(fileDir: String?): Array<File>? {
        return getFiles(File(fileDir))
    }

    /**
     * 获取文件列表
     *
     * @param fileDir
     */
    fun getFiles(fileDir: File): Array<File>? {
        return if (!fileDir.isDirectory) {
            null
        } else fileDir.listFiles()
    }

    fun getPreferencesDir(context: Context): File {
        var mPreferencesDir = File(ContextCompat.getDataDir(context), "shared_prefs")
        return ensurePrivateDirExists(mPreferencesDir, 505, -1, null)
    }


    fun ensurePrivateDirExists(file: File, mode: Int, gid: Int, xattr: String?): File {
        if (!file.exists()) {
            val path = file.absolutePath
            try {
                Os.mkdir(path, mode)
                Os.chmod(path, mode)
                if (gid != -1) {
                    Os.chown(path, -1, gid)
                }
            } catch (e: ErrnoException) {
                if (e.errno == OsConstants.EEXIST) { // We must have raced with someone; that's okay
                } else {
                }
            }
            if (xattr != null) {
                try { //                    val stat = Os.stat(file.absolutePath)
                    val value =
                        ByteArray(8) //                    Memory.pokeLong(value, 0, stat.st_ino, ByteOrder.nativeOrder())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Os.setxattr(file.parentFile!!.absolutePath, xattr, value, 0)
                    }
                } catch (e: ErrnoException) {
                    e.printStackTrace()
                }
            }
        }
        return file
    }

    /**
     * 输出 B KB MB GB  TB
     * */
    fun formatFileSize(fileSizeInBytes: Long): String {
        val unit = 1024
        if (fileSizeInBytes < unit) {
            return "$fileSizeInBytes B"
        }
        val exp = (Math.log(fileSizeInBytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1].toString() + ""
        return String.format(
            "%.1f %sB", fileSizeInBytes / Math.pow(unit.toDouble(), exp.toDouble()), pre
        )
    }

}