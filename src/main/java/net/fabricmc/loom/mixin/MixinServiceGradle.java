/*
 * This file is part of fabric-loom, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016, 2017, 2018 FabricMC
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

package net.fabricmc.loom.mixin;

import com.google.common.io.ByteStreams;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.mojang.MixinServiceLaunchWrapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class MixinServiceGradle extends MixinServiceLaunchWrapper implements IClassBytecodeProvider {

	private static List<JarFile> jars = new ArrayList<>();

	@Override
	public String getName() {
		return "FabricGradle";
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		for(JarFile file : jars){
			ZipEntry entry = file.getEntry(name);
			if(entry != null){
				try {
					InputStream stream = file.getInputStream(entry);
					return stream;
				} catch (IOException e) {
					throw new RuntimeException("Failed to read mod file", e);
				}
			}
		}
		return super.getResourceAsStream(name);
	}

	public static void setupModFiles(Set<File> mods, File minecraft) throws IOException {
		jars.clear();
		for(File mod : mods){
			JarFile jarFile = new JarFile(mod);
			jars.add(jarFile);
		}
		jars.add(new JarFile(minecraft));
	}

	@Override
	public IClassBytecodeProvider getBytecodeProvider() {
		return this;
	}

	public byte[] getClassBytes(String name, String transformedName) throws IOException {
		InputStream inputStream = getResourceAsStream(name.replace(".", "/") + ".class");
		byte[] classBytes = ByteStreams.toByteArray(inputStream);
		inputStream.close();
		if(classBytes == null){
			return super.getClassBytes(name, transformedName);
		}
		return classBytes;
	}
}
