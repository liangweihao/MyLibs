package com.nothing.commonutils.utils

import android.util.ArrayMap
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.*
import java.nio.charset.StandardCharsets

/**
 *--------------------
 *<p>Author：
 *         liangweihao
 *<p>Created Time:
 *          2022/5/22
 *<p>Intro:
 *
 *<p>Thinking:
 *
 *<p>Problem:
 *
 *<p>Attention:
 *--------------------
 */

fun readXmlFromFile(xmlFile:File):HashMap<String,Any> {
    val newPullParser = XmlPullParserFactory.newInstance().newPullParser()
    newPullParser.setInput(FileInputStream(xmlFile), StandardCharsets.UTF_8.name())
    return readValueXml(newPullParser, arrayOf("")) as HashMap<String, Any>
}



@Throws(XmlPullParserException::class, IOException::class)
fun readValueXml(parser:XmlPullParser, name:Array<String>):Any {
    var eventType:Int = parser.eventType
    do {
        if (eventType == XmlPullParser.START_TAG) {
            return  readThisValueXml(parser, name, null, false)
        } else if (eventType == XmlPullParser.END_TAG) {
            throw XmlPullParserException("Unexpected end tag at: " + parser.getName())
        } else if (eventType == XmlPullParser.TEXT) {
            throw XmlPullParserException("Unexpected text: " + parser.getText())
        }
        eventType = parser.next()
    } while (eventType != XmlPullParser.END_DOCUMENT)
    throw XmlPullParserException("Unexpected end of document")
}

interface ReadMapCallback {
    fun readThisUnknownObjectXml(`in`:XmlPullParser?, tag:String):Any
}


@Throws(XmlPullParserException::class, IOException::class) private fun readThisValueXml(
    parser:XmlPullParser, name:Array<String>,
    callback:ReadMapCallback?, arrayMap:Boolean,
):Any {
    val valueName:String = parser.getAttributeValue("", "name")?:""
    val tagName:String = parser.name?:""

    //System.out.println("Reading this value tag: " + tagName + ", name=" + valueName);
    var res:Any
    if (tagName == "null") {
        res = ""
    } else if (tagName == "string") {
        val value = StringBuilder()
        var eventType:Int
        while (parser.next().also { eventType = it } != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("string")) {
                    name[0] =
                        valueName //System.out.println("Returning value for " + valueName + ": " + value);
                    return value.toString()
                }
                throw XmlPullParserException("Unexpected end tag in <string>: " + parser.getName())
            } else if (eventType == XmlPullParser.TEXT) {
                value.append(parser.getText())
            } else if (eventType == XmlPullParser.START_TAG) {
                throw XmlPullParserException("Unexpected start tag in <string>: " + parser.getName())
            }
        }
        throw XmlPullParserException("Unexpected end of document in <string>")
    } else if (readThisPrimitiveValueXml(parser, tagName).also {
            res = it ?: ""
        } != null) { // all work already done by readThisPrimitiveValueXml
    } else if (tagName == "byte-array") {
        res = readThisByteArrayXml(parser, "byte-array")
        name[0] = valueName //System.out.println("Returning value for " + valueName + ": " + res);
        return res
    } else if (tagName == "int-array") {
        res = readThisIntArrayXml(parser, "int-array", name)
        name[0] = valueName //System.out.println("Returning value for " + valueName + ": " + res);
        return res
    } else if (tagName == "long-array") {
        res = readThisLongArrayXml(parser, "long-array", name)
        name[0] = valueName //System.out.println("Returning value for " + valueName + ": " + res);
        return res
    } else if (tagName == "double-array") {
        res = readThisDoubleArrayXml(parser, "double-array", name)
        name[0] = valueName //System.out.println("Returning value for " + valueName + ": " + res);
        return res
    } else if (tagName == "string-array") {
        res = readThisStringArrayXml(parser, "string-array", name)
        name[0] = valueName //System.out.println("Returning value for " + valueName + ": " + res);
        return res
    } else if (tagName == "boolean-array") {
        res = readThisBooleanArrayXml(parser, "boolean-array", name)
        name[0] = valueName //System.out.println("Returning value for " + valueName + ": " + res);
        return res
    } else if (tagName == "map") {
        parser.next()
        res =
            if (arrayMap) readThisArrayMapXml(parser, "map", name, callback) else readThisMapXml(parser, "map", name, callback)
        name[0] = valueName //System.out.println("Returning value for " + valueName + ": " + res);
        return res
    } else if (tagName == "list") {
        parser.next()
        res = readThisListXml(parser, "list", name, callback, arrayMap)
        name[0] = valueName //System.out.println("Returning value for " + valueName + ": " + res);
        return res
    } else if (tagName == "set") {
        parser.next()
        res = readThisSetXml(parser, "set", name, callback, arrayMap)
        name[0] = valueName //System.out.println("Returning value for " + valueName + ": " + res);
        return res
    } else if (callback != null) {
        res = callback.readThisUnknownObjectXml(parser, tagName)
        name[0] = valueName
        return res
    } else {
        throw XmlPullParserException("Unknown tag: $tagName")
    }

    // Skip through to end tag.
    var eventType:Int
    while (parser.next().also { eventType = it } != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.END_TAG) {
            if (parser.getName().equals(tagName)) {
                name[0] =
                    valueName //System.out.println("Returning value for " + valueName + ": " + res);
                return res
            }
            throw XmlPullParserException("Unexpected end tag in <" + tagName + ">: " + parser.getName())
        } else if (eventType == XmlPullParser.TEXT) {
            throw XmlPullParserException("Unexpected text in <" + tagName + ">: " + parser.getName())
        } else if (eventType == XmlPullParser.START_TAG) {
            throw XmlPullParserException("Unexpected start tag in <" + tagName + ">: " + parser.getName())
        }
    }
    throw XmlPullParserException("Unexpected end of document in <$tagName>")
}


@Throws(XmlPullParserException::class, IOException::class)
private fun readThisPrimitiveValueXml(parser:XmlPullParser, tagName:String):Any? {
    return if (tagName == "int") {
        parser.getAttributeValue("", "value")?:"0".toInt()
    } else if (tagName == "long") {
        parser.getAttributeValue("", "value")?:"0".toLong()
    } else if (tagName == "float") {
        parser.getAttributeValue("", "value")?:"0".toFloat()
    } else if (tagName == "double") {
        parser.getAttributeValue("", "value")?:"0".toDouble()
    } else if (tagName == "boolean") {
        parser.getAttributeValue("", "value")?:"false".toBoolean()
    } else {
        null
    }
}

fun String?.toSafeString(default:String):String {
    if (this.isNullOrEmpty()){
        return default
    }
    return this
}

@Throws(XmlPullParserException::class, IOException::class) fun readThisIntArrayXml(
    parser:XmlPullParser,
    endTag:String, name:Array<String>?,
):IntArray {
    val num:Int = parser.getAttributeValue("", "num").toInt()
    parser.next()
    val array = IntArray(num)
    var i = 0
    var eventType:Int = parser.getEventType()
    do {
        if (eventType == XmlPullParser.START_TAG) {
            if (parser.getName().equals("item")) {
                array[i] = parser.getAttributeValue("", "value").toInt()
            } else {
                throw XmlPullParserException("Expected item tag at: " + parser.getName())
            }
        } else if (eventType == XmlPullParser.END_TAG) {
            if (parser.getName().equals(endTag)) {
                return array
            } else if (parser.getName().equals("item")) {
                i++
            } else {
                throw XmlPullParserException("Expected " + endTag + " end tag at: " + parser.getName())
            }
        }
        eventType = parser.next()
    } while (eventType != XmlPullParser.END_DOCUMENT)
    throw XmlPullParserException("Document ended before $endTag end tag")
}

@Throws(XmlPullParserException::class, IOException::class) fun readThisByteArrayXml(
    parser:XmlPullParser,
    endTag:String,
):ByteArray {
    val num:Int = parser.getAttributeValue("", "num").toInt()

    // 0 len byte array does not have a text in the XML tag. So, initialize to 0 len array.
    // For all other array lens, HexEncoding.decode() below overrides the array.
    var array = ByteArray(0)
    var eventType:Int = parser.eventType
    do {
        if (eventType == XmlPullParser.TEXT) {
            if (num > 0) {
                val values:String = parser.text
                if (values.length != num) {
                    throw XmlPullParserException("Invalid value found in byte-array: $values")
                }
                array = HexEncoding.decode(values)
            }
        } else if (eventType == XmlPullParser.END_TAG) {
            return if (parser.name.equals(endTag)) {
                array
            } else {
                throw XmlPullParserException("Expected " + endTag + " end tag at: " + parser.name)
            }
        }
        eventType = parser.next()
    } while (eventType != XmlPullParser.END_DOCUMENT)
    throw XmlPullParserException("Document ended before $endTag end tag")
}


@Throws(XmlPullParserException::class, IOException::class) fun readThisLongArrayXml(
    parser:XmlPullParser,
    endTag:String, name:Array<String>?,
):LongArray {
    val num:Int = parser.getAttributeValue("", "num").toInt()
    parser.next()
    val array = LongArray(num)
    var i = 0
    var eventType:Int = parser.getEventType()
    do {
        if (eventType == XmlPullParser.START_TAG) {
            if (parser.getName().equals("item")) {
                array[i] = parser.getAttributeValue("", "value").toLong()
            } else {
                throw XmlPullParserException("Expected item tag at: " + parser.getName())
            }
        } else if (eventType == XmlPullParser.END_TAG) {
            if (parser.getName().equals(endTag)) {
                return array
            } else if (parser.getName().equals("item")) {
                i++
            } else {
                throw XmlPullParserException("Expected " + endTag + " end tag at: " + parser.getName())
            }
        }
        eventType = parser.next()
    } while (eventType != XmlPullParser.END_DOCUMENT)
    throw XmlPullParserException("Document ended before $endTag end tag")
}


@Throws(XmlPullParserException::class, IOException::class) fun readThisDoubleArrayXml(
    parser:XmlPullParser, endTag:String,
    name:Array<String>?,
):DoubleArray {
    val num:Int = parser.getAttributeValue("", "num").toInt()
    parser.next()
    val array = DoubleArray(num)
    var i = 0
    var eventType:Int = parser.getEventType()
    do {
        if (eventType == XmlPullParser.START_TAG) {
            if (parser.getName().equals("item")) {
                array[i] = parser.getAttributeValue("", "value").toDouble()
            } else {
                throw XmlPullParserException("Expected item tag at: " + parser.getName())
            }
        } else if (eventType == XmlPullParser.END_TAG) {
            if (parser.getName().equals(endTag)) {
                return array
            } else if (parser.getName().equals("item")) {
                i++
            } else {
                throw XmlPullParserException("Expected " + endTag + " end tag at: " + parser.getName())
            }
        }
        eventType = parser.next()
    } while (eventType != XmlPullParser.END_DOCUMENT)
    throw XmlPullParserException("Document ended before $endTag end tag")
}


@Throws(XmlPullParserException::class, IOException::class) fun readThisStringArrayXml(
    parser:XmlPullParser, endTag:String,
    name:Array<String>?,
):Array<String?> {
    val num:Int = parser.getAttributeValue("", "num").toInt()
    parser.next()
    val array = arrayOfNulls<String>(num)
    var i = 0
    var eventType:Int = parser.getEventType()
    do {
        if (eventType == XmlPullParser.START_TAG) {
            if (parser.getName().equals("item")) {
                array[i] = parser.getAttributeValue("", "value").toString()
            } else {
                throw XmlPullParserException("Expected item tag at: " + parser.getName())
            }
        } else if (eventType == XmlPullParser.END_TAG) {
            if (parser.getName().equals(endTag)) {
                return array
            } else if (parser.getName().equals("item")) {
                i++
            } else {
                throw XmlPullParserException("Expected " + endTag + " end tag at: " + parser.getName())
            }
        }
        eventType = parser.next()
    } while (eventType != XmlPullParser.END_DOCUMENT)
    throw XmlPullParserException("Document ended before $endTag end tag")
}


@Throws(XmlPullParserException::class, IOException::class) fun readThisBooleanArrayXml(
    parser:XmlPullParser, endTag:String,
    name:Array<String>?,
):BooleanArray {
    val num:Int = parser.getAttributeValue("", "num").toInt()
    parser.next()
    val array = BooleanArray(num)
    var i = 0
    var eventType:Int = parser.getEventType()
    do {
        if (eventType == XmlPullParser.START_TAG) {
            if (parser.getName().equals("item")) {
                array[i] = parser.getAttributeValue("", "value").toBoolean()
            } else {
                throw XmlPullParserException("Expected item tag at: " + parser.getName())
            }
        } else if (eventType == XmlPullParser.END_TAG) {
            if (parser.getName().equals(endTag)) {
                return array
            } else if (parser.getName().equals("item")) {
                i++
            } else {
                throw XmlPullParserException("Expected " + endTag + " end tag at: " + parser.getName())
            }
        }
        eventType = parser.next()
    } while (eventType != XmlPullParser.END_DOCUMENT)
    throw XmlPullParserException("Document ended before $endTag end tag")
}


@Throws(XmlPullParserException::class, IOException::class) fun readThisArrayMapXml(
    parser:XmlPullParser,
    endTag:String, name:Array<String>, callback:ReadMapCallback?,
):ArrayMap<String, *> {
    val map = ArrayMap<String, Any>()
    var eventType:Int = parser.getEventType()
    do {
        if (eventType == XmlPullParser.START_TAG) {
            val `val`:Any = readThisValueXml(parser, name, callback, true)
            map[name[0]] = `val`
        } else if (eventType == XmlPullParser.END_TAG) {
            if (parser.getName().equals(endTag)) {
                return map
            }
            throw XmlPullParserException("Expected " + endTag + " end tag at: " + parser.getName())
        }
        eventType = parser.next()
    } while (eventType != XmlPullParser.END_DOCUMENT)
    throw XmlPullParserException("Document ended before $endTag end tag")
}


@Throws(XmlPullParserException::class, IOException::class) fun readThisMapXml(
    parser:XmlPullParser, endTag:String,
    name:Array<String>, callback:ReadMapCallback?,
):HashMap<String, *> {
    val map = HashMap<String, Any>()
    var eventType:Int = parser.getEventType()
    do {
        if (eventType == XmlPullParser.START_TAG) {
            val `val`:Any = readThisValueXml(parser, name, callback, false)
            map[name[0]] = `val`
        } else if (eventType == XmlPullParser.END_TAG) {
            if (parser.getName().equals(endTag)) {
                return map
            }
            throw XmlPullParserException("Expected " + endTag + " end tag at: " + parser.getName())
        }
        eventType = parser.next()
    } while (eventType != XmlPullParser.END_DOCUMENT)
    throw XmlPullParserException("Document ended before $endTag end tag")
}


@Throws(XmlPullParserException::class, IOException::class) private fun readThisListXml(
    parser:XmlPullParser, endTag:String,
    name:Array<String>, callback:ReadMapCallback?, arrayMap:Boolean,
):ArrayList<Any> {
    val list:ArrayList<Any> = ArrayList<Any>()
    var eventType:Int = parser.getEventType()
    do {
        if (eventType == XmlPullParser.START_TAG) {
            val v = readThisValueXml(parser, name, callback, arrayMap)
            list.add(v) //System.out.println("Adding to list: " + val);
        } else if (eventType == XmlPullParser.END_TAG) {
            if (parser.getName().equals(endTag)) {
                return list
            }
            throw XmlPullParserException("Expected " + endTag + " end tag at: " + parser.getName())
        }
        eventType = parser.next()
    } while (eventType != XmlPullParser.END_DOCUMENT)
    throw XmlPullParserException("Document ended before $endTag end tag")
}

@Throws(XmlPullParserException::class, IOException::class) private fun readThisSetXml(
    parser:XmlPullParser, endTag:String,
    name:Array<String>, callback:ReadMapCallback?, arrayMap:Boolean,
):HashSet<*> {
    val set:HashSet<Any> = HashSet<Any>()
    var eventType:Int = parser.getEventType()
    do {
        if (eventType == XmlPullParser.START_TAG) {
            val v:Any = readThisValueXml(parser, name, callback, arrayMap)
            set.add(v) //System.out.println("Adding to set: " + val);
        } else if (eventType == XmlPullParser.END_TAG) {
            if (parser.getName().equals(endTag)) {
                return set
            }
            throw XmlPullParserException("Expected " + endTag + " end tag at: " + parser.getName())
        }
        eventType = parser.next()
    } while (eventType != XmlPullParser.END_DOCUMENT)
    throw XmlPullParserException("Document ended before $endTag end tag")
}



