/*
 * Class NativeUtils is published under the The MIT License:
 *
 * Copyright (c) 2012 Adam Heinrich <adam@adamh.cz>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package capturemanager.utils;

import java.io.*;

/**
 * A simple library class which helps with loading dynamic libraries stored in the
 * JAR archive. These libraries usualy contain implementation of some methods in
 * native code (using JNI - Java Native Interface).
 *
 * @see http://adamheinrich.com/blog/2012/how-to-load-native-jni-library-from-jar
 * @see https://github.com/adamheinrich/native-utils
 *
 */
public class NativeUtils {

    /**
     * Private constructor - this class will never be instanced
     */
    private NativeUtils() {
    }

    public static File loadLibrary(final String path) throws IOException {
        final File file = new File(path);

        System.load(file.getAbsolutePath());

        return file;
    }
}
